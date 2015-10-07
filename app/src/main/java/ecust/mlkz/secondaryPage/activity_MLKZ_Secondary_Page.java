/**
 * =============================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 * .
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * .
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * =============================================================================
 * .
 * Created by 彩笔怪盗基德 on 2015/9/29
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.mlkz.secondaryPage;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.List;

import ecust.main.R;
import ecust.mlkz.cls_MLKZ_Login;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_MLKZ_Data;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_PostNode;
import lib.Global;
import lib.logUtils.logUtil;


/**
 * 显示帖子目录及各版块信息
 */
public class activity_MLKZ_Secondary_Page extends Activity implements Listener<struct_MLKZ_Data>, ErrorListener,
        HeadBar.OnTagClickListener {
    //Volley队列
    private RequestQueue mQueue;
    //Adapter
    private recyclerViewAdapter mAdapter;
    //RecyclerView
    private RecyclerView mRecyclerView;
    //顶部栏
    private HeadBar mHeadBar;
    /**
     * RecyclerView滚动监听
     */
    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        private LinearLayoutManager layoutManager;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (layoutManager == null)
                layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            final int lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
            final int totalItemCount = layoutManager.getItemCount();

            if (newState == RecyclerView.SCROLL_STATE_IDLE && lastItemPosition >= totalItemCount - 1
                    && totalItemCount != 0) {
                //到达底部
                onRecyclerViewBottom();
            }
        }
    };

    /**
     * 地址格式转换，能获取不同的网页结果（PC版还是WAP版页面）
     * From: http://bbs.ecust.edu.cn/forum.php?mod=forumdisplay&fid=91&mobile=yes
     * To:   http://bbs.ecust.edu.cn/forum.php?mod=forumdisplay&fid=91&mobile=no
     *
     * @param url          地址
     * @param convertToWAP true转为WAP格式，false转为PC格式
     * @return URL
     */
    protected static String addressConvertToWAP(String url, boolean convertToWAP) {
        final String style_WAP = "&mobile=yes";
        final String style_PC = "&mobile=no";

        if (!url.contains("&mobile="))
            url += style_PC;
        if (convertToWAP) {
            //WAP登陆
            return url.replace(style_PC, style_WAP);
        } else {
            //PC登陆
            return url.replace(style_WAP, style_PC);
        }
    }

    private void onRecyclerViewBottom() {
        new logUtil(this).d("RecyclerView到达底部");
        //加载下一页
        String nextPageURL = mAdapter.getData().mPageInformation.getNextPageURL();
        if (nextPageURL != null)
            openNewPage(nextPageURL);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mlkz_secondary_page);

        Global.setTitle(this, "梅陇客栈");

        //设置当前版块标题、URL
        String sectionURL = getIntent().getStringExtra("url");

        //初始化
        mQueue = Volley.newRequestQueue(this);
        initViews();

        //加载新页面
        openNewPage(sectionURL);
    }

    /**
     * 打开一个新的链接
     *
     * @param url 地址
     */
    private void openNewPage(String url) {
        //当前cookie
        String cookie = new cls_MLKZ_Login(this).getPreference().getCookie();
        //访问
        url = addressConvertToWAP(url, false);
        mQueue.add(new mlkzRequest(Request.Method.GET, url, cookie, this, this));
    }

    private void initViews() {
        //初始化RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.mlkz_secondary_page_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new recyclerViewAdapter(this, mQueue);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(onScrollListener);

        //初始化HeadBar
        mHeadBar = (HeadBar) findViewById(R.id.mlkz_secondary_page_headbar);
        mHeadBar.addTab("版块", this);
        mHeadBar.addTab("筛选", this);
        mHeadBar.addTab("还有没", this);

    }

    @Override
    public void onHeadBarTagClick(String name) {

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        logUtil.toast("访问失败 " + error.getMessage());
    }

    @Override
    public void onResponse(struct_MLKZ_Data newData) {

        if (mAdapter.getData() != null) {
            //取得旧数据，并在其后面添加新数据
            List<struct_PostNode> oldData = mAdapter.getData().mPostsList;
            for (struct_PostNode i : newData.mPostsList) {
                //判断是否重复
                if (!oldData.contains(i))
                    oldData.add(i);
            }
            //替换数据
            newData.mPostsList = oldData;
        }
        mAdapter.setData(newData);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mQueue.cancelAll(this);
        mQueue.stop();
    }
}
