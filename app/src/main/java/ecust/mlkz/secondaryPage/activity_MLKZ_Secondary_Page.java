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
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.List;

import ecust.main.R;
import ecust.mlkz.cls_MLKZ_Login;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_ClassificationNode;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_CurrentSection;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_MLKZ_Data;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_PostNode;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_SortList;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_TertiarySectionNode;
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
    //清除旧数据标记
    private boolean mFlagCleanOldData;
    /**
     * 排序选项被点击
     */
    public AdapterView.OnItemClickListener mSortItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            struct_SortList mSortList = mAdapter.getData().mPageInformation.getSortList();
            String url = mSortList.mUrl.get(position);
            openNewPage(url, true);
            //隐藏菜单
            mHeadBar.closeCurrentView();
        }
    };
    /**
     * RecyclerView滚动监听
     */
    public RecyclerView.OnScrollListener onRecyclerViewScrollListener = new RecyclerView.OnScrollListener() {
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

        if (url == null) return null;
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
            openNewPage(nextPageURL, false);
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
        openNewPage(sectionURL, false);
    }

    /**
     * 打开一个新的链接
     *
     * @param url          地址
     * @param cleanOldData 清除原有数据
     */
    private void openNewPage(String url, boolean cleanOldData) {
        mFlagCleanOldData = cleanOldData;
        if (cleanOldData)
            mAdapter.setData(null);

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
        mRecyclerView.addOnScrollListener(onRecyclerViewScrollListener);

        //初始化HeadBar
        mHeadBar = (HeadBar) findViewById(R.id.mlkz_secondary_page_headbar);
        mHeadBar.addTab("版块").addTab("排序");
        mHeadBar.setOnTagClickListener(this);
    }

    /**
     * HeadBar主题分类（筛选）被点击
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final int pos = item.getItemId();
        //获取节点信息-主题分类
        struct_ClassificationNode mClassificationNode =
                mAdapter.getData().mPageInformation.getClassificationNodes().get(pos);

        //跳转新页面
        openNewPage(mClassificationNode.getUrl(), true);

        return super.onContextItemSelected(item);
    }

    /**
     * HeadBar被点击
     */
    @Override
    public void onHeadBarTagClick(int position, HeadBar.NewWindow mWindow) {
        View mPopView = null;
        switch (position) {
            case 0:
                //版块按钮
                mPopView = createLeftCatalogView();
                break;
            case 1:
                //排序按钮
                mPopView = createRightSortListView();
                break;
        }
        mWindow.popupView(mPopView);
    }

    /**
     * 创建版块目录
     */
    private View createLeftCatalogView() {
        if (mAdapter.getData() == null) return null;
        //创建一个版块选择的View
        catalogListView mCatalogListView = new catalogListView(this);
        mCatalogListView.setData(mAdapter.getData().mPageInformation);
        mCatalogListView.setOnChildSectionSelectedListener(new catalogListView.OnChildSectionSelectedListener() {
            //有选中需跳转的节点
            @Override
            public void OnChildSectionSelected(struct_TertiarySectionNode mSection_3rd) {
                //打开新页面
                openNewPage(mSection_3rd.getUrl(), true);
                //关闭PopupWindow
                mHeadBar.closeCurrentView();
            }
        });
        return mCatalogListView;
    }

    /**
     * 创建排序的ListView
     */
    @SuppressWarnings("unchecked")
    private ListView createRightSortListView() {
        //获取数据集
        if (mAdapter.getData() == null) return null;
        struct_SortList mSortData = mAdapter.getData().mPageInformation.getSortList();

        final ListView listView = new ListView(this);
        listView.setBackgroundColor(Color.WHITE);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mSortData.mTitle);
        listView.setAdapter(adapter);
        //点击事件
        listView.setOnItemClickListener(mSortItemClickListener);
        listView.setOnTouchListener(new View.OnTouchListener() {
            int lastDownPosition;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ListView mListView = (ListView) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastDownPosition = mListView.pointToPosition((int) event.getX(), (int) event.getY());
                    case MotionEvent.ACTION_MOVE:
                        return true;
                    case MotionEvent.ACTION_UP:
                        //获取点击的位置
                        final int position = mListView.pointToPosition((int) event.getX(), (int) event.getY());
                        if (position == lastDownPosition && position != -1) {
                            //分发OnItemClick事件，触发不了，找不到被什么抢了焦点
                            mListView.performItemClick(mListView, position, 0);
                            break;
                        }
                    case MotionEvent.ACTION_CANCEL:
                        mHeadBar.closeCurrentView();
                        return true;
                }
                return false;
            }
        });
        return listView;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        logUtil.toast("访问失败 " + error.getMessage());
    }

    @Override
    public void onResponse(struct_MLKZ_Data newData) {
        mHeadBar.closeCurrentView();

        //设置标题
        struct_CurrentSection mCurrentSection = newData.mPageInformation.getCurrentSection();
        if (mCurrentSection != null)
            if (mCurrentSection.getTertiarySectionName() != null && mCurrentSection.getTertiarySectionName().length() > 0) {
                Global.setTitle(this, "梅陇客栈 - " + mCurrentSection.getTertiarySectionName());
            } else {
                Global.setTitle(this, "梅陇客栈 - " + mCurrentSection.getSecondarySectionName());
            }

        //判断是否与旧数据进行合并
        if (!mFlagCleanOldData) {
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
        } else {
            mRecyclerView.scrollToPosition(0);
        }
        //设置数据
        mAdapter.setData(newData);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mQueue.cancelAll(this);
        mQueue.stop();
    }
}

