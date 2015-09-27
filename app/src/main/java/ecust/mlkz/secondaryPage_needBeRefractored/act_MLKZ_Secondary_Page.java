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
 * Created by 彩笔怪盗基德 on 2015/9/11
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */
package ecust.mlkz.secondaryPage_needBeRefractored;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import ecust.main.R;
import ecust.mlkz.cls_MLKZ_Login;
import ecust.mlkz.secondaryPage_needBeRefractored.struct_Forum_Items.struct_forumDataRoot;
import ecust.mlkz.secondaryPage_needBeRefractored.struct_Forum_Items.struct_forumPostNode;
import lib.Global;
import lib.clsUtils.httpUtil;
import lib.clsUtils.logUtil;

/**
 * 梅陇客栈-版块发帖目录
 */
public class act_MLKZ_Secondary_Page extends Activity implements httpUtil.OnHttpVisitListener {

    //数据集
    private struct_forumDataRoot forumData = new struct_forumDataRoot();

    //Cookie
    private String cookie;

    //头部bar
    private widget_HeadBar headbar;

    //HeadBar回调接口
    private widget_HeadBar.OnHeadbarClickListener onHeadbarClickListener =
            new widget_HeadBar.OnHeadbarClickListener() {
                //跳转到新版块
                @Override
                public void jumpToNewSection(String name, String href) {
                    logUtil.toast(name + " " + href);
                }

                //子版块被选中
                @Override
                public void onChildSectionSelected(String str) {
                    logUtil.toast("子版块 " + str);
                }

                //筛选按钮
                @Override
                public void onClassificationSelected(String str) {
                    logUtil.toast("筛选 = " + str);
                }

                //排序按钮点击
                @Override
                public void onSortSelected(int sortByTimeType) {
                    switch (sortByTimeType) {
                        case widget_HeadBar.SORT_BY_POSTTIME:
                            logUtil.toast("发帖时间");
                            break;
                        case widget_HeadBar.SORT_BY_REPLYTIME:
                            logUtil.toast("回复时间");
                            break;
                    }
                }
            };

    //RecyclerView回调
    private recyclerViewAdapter.OnClickListener onRecyclerViewClickListener = new recyclerViewAdapter.OnClickListener() {
        @Override
        public void OnAuthorSelected(struct_forumPostNode node) {
            logUtil.toast(node.getAuthorName());
        }

        @Override
        public void OnPostItemSelected(struct_forumPostNode node) {
            logUtil.toast(node.getTitle() + "\r\n" + node.getPostUrl());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mlkz_secondary_page);

        //设置当前版块标题、URL
        forumData.setSectionTitle(getIntent().getStringExtra("title"));
        forumData.setSectionURL(getIntent().getStringExtra("url"));

        //当前cookie
        cookie = new cls_MLKZ_Login(this).getPreference().getCookie();

        //获取内容
        String url = forumData.getSectionURL();
        httpUtil.getSingleton().getHttp(url, cookie, this);

        Global.setTitle(this, "梅陇客栈");

        initHeadbar();
        initRecyclerView();
    }

    /**
     * 初始化headbar
     */
    private void initHeadbar() {
        headbar = (widget_HeadBar) findViewById(R.id.mlkz_secondary_page_headbar);
        headbar.setOnHeadbarClickListener(onHeadbarClickListener);
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mlkz_secondary_page_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter adapter = new recyclerViewAdapter(this, forumData);
        recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(onRecyclerViewClickListener);
        //添加分割线
//        recyclerView.addItemDecoration(new recyclerViewItemDecoration(this));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                TextView fab = (TextView) findViewById(R.id.mlkz_secondary_page_fab);
                if (dy > 0) {
                    fab.setVisibility(View.GONE);
                } else {
                    AnimationSet animationSet = new AnimationSet(act_MLKZ_Secondary_Page.this, null);
                    Animation animation1 = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0,
                            Animation.RELATIVE_TO_SELF, 0,
                            Animation.RELATIVE_TO_PARENT, -0.5f,
                            Animation.ABSOLUTE, 0);

                    animationSet.addAnimation(animation1);

                    animationSet.setDuration(500);
                    animationSet.setInterpolator(new LinearInterpolator());

                    if (fab.getVisibility() == View.GONE) {
                        fab.setVisibility(View.VISIBLE);
                        logUtil.e(this, fab.getY() + " " + fab.getTranslationY());
                        fab.startAnimation(animationSet);
                    }
                }

            }
        });
    }

    @Override
    public void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String returnHtmlMessage) {
        if (!bSucceed) return;

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mlkz_secondary_page_recyclerview);
        recyclerViewAdapter adapter = (recyclerViewAdapter) recyclerView.getAdapter();
        adapter.setData(forumData);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onHttpBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, String returnHtmlMessage) {
        if (!bSucceed) return;

        //数据解析
        struct_forumDataRoot result = new htmlParser().parseHtmlData(returnHtmlMessage);
        headbar.setData(result);

        //合并结果
        forumData = result;
    }

    @Override
    public void onPictureLoadCompleted(String url, String cookie, boolean bSucceed, byte[] returnPicBytes) {

    }

    @Override
    public void onPictureBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, byte[] returnPicBytes) {

    }


}

