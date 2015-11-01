package ecust.lecture;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ecust.main.R;
import lib.clsFailureBar;
import lib.clsUtils.httpUtil;
import lib.logUtils.abstract_LogUtil;
import myWidget.BaseAppCompatActivity;
import statistics.clsUmeng;

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
 * Created by 彩笔怪盗基德 on 2015/6/5
 * Copyright (C) 2015 彩笔怪盗基德
 */
public class act_Lecture_Detail extends BaseAppCompatActivity implements clsFailureBar.OnWebRetryListener,
        httpUtil.OnHttpVisitListener {
    private static final int MENU_ACTION_VIEW = Menu.FIRST;
    private clsLectureDetail mLectureDetail;            //学术讲座版块解析类
    private clsFailureBar wFailureBar;            //失败，加载条

    //链接地址
    private String lecture_URL;

    /**
     * 解析数据，保存数据
     */
    @Override
    public void onHttpBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {
        if (bSucceed) {
            //解析数据
            mLectureDetail.clsParse.parseAllData(rtnHtmlMessage);

            //存储到sql
            abstract_LogUtil.i(this, "讲座信息数据存储");
            new DataBase_Lecture(this).detail.SaveData(mLectureDetail.mData);
        }
    }

    /**
     * 讲座文字部分内容消息返回(返回一次就算加载成功)
     */
    @Override
    public void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {
        if (!bSucceed) {
            wFailureBar.setStateFailure();      //加载失败
        } else {
            wFailureBar.setStateSucceed();      //加载成功，隐藏进度条
            mLectureDetail.Show();
        }
    }

    @Override
    public void onPictureLoadCompleted(String url, String cookie, boolean bSucceed, byte[] pic) {
    }

    @Override
    public void onPictureBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {
    }

    /**
     * 来自clsFailureBar
     */
    @Override
    public void onWebRetryCompleted() {
        abstract_LogUtil.i(this, "[重试刷新中]" + mLectureDetail.mData.url);
        wFailureBar.setStateLoading();
        httpUtil.getSingleton().getHttp(mLectureDetail.mData.url, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecture_detail);

        //获取新闻地址URL、所在分类版块
        lecture_URL = getIntent().getStringExtra("URL");

        //标题
        initToolBar();

        mLectureDetail = new clsLectureDetail();
        mLectureDetail.mData.url = lecture_URL;

        wFailureBar = new clsFailureBar(this);
        wFailureBar.setOnWebRetryListener(this);

        //获取数据库缓存数据
        struct_LectureDetail cacheData = new DataBase_Lecture(this).detail.GetData(lecture_URL);

        if (!cacheData.url.equals("")) {
            abstract_LogUtil.i(this, "[讲座详细]缓存数据加载成功" + lecture_URL);
            mLectureDetail.mData = cacheData;
            wFailureBar.setStateSucceed();      //加载成功，隐藏进度条
            mLectureDetail.Show();
        } else {
            abstract_LogUtil.i(this, "[讲座详细]网页加载" + lecture_URL);
            httpUtil.getSingleton().getHttp(lecture_URL, this); //获取讲座文本内容
        }
    }

    private void initToolBar() {
        getSupportToolBar(this).setTitle("学术讲座");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wFailureBar.setOnWebRetryListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        clsUmeng.onEvent(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ACTION_VIEW, 0, "在浏览器中打开");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case MENU_ACTION_VIEW:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(lecture_URL));
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 专门用于解析学术讲座版块
     */
    private class clsLectureDetail {
        public clsParse clsParse = new clsParse();
        private struct_LectureDetail mData = new struct_LectureDetail();                                        //数据集

        //显示学术讲座内容
        public void Show() {
            //报告题目、开始时间、报告地点、报告人、主办单位、备注
            TextView wReportTitle = (TextView) findViewById(R.id.news_detail_report_title);
            TextView wReportStartTime = (TextView) findViewById(R.id.news_detail_report_starttime);
            TextView wReportAddress = (TextView) findViewById(R.id.news_detail_report_address);
            TextView wReportReporter = (TextView) findViewById(R.id.news_detail_report_reporter);
            TextView wReportOrganization = (TextView) findViewById(R.id.news_detail_report_organization);
            TextView wReportRemark = (TextView) findViewById(R.id.news_detail_report_remark);

            //设置内容
            wReportTitle.setText(mData.title);
            wReportStartTime.setText(mData.startTime);
            wReportAddress.setText(mData.address);
            wReportReporter.setText(mData.reporter);
            wReportOrganization.setText(mData.organization);

            //备注栏空
            if (!mData.remark.equals(""))
                wReportRemark.setText(mData.remark);
            else {
                LinearLayout wRreportRemarkTitle = (LinearLayout) findViewById(R.id.news_detail_report_remark_title);
                wRreportRemarkTitle.setVisibility(View.GONE);
                wReportRemark.setVisibility(View.GONE);
            }
        }

        /**
         * 数据解析类
         */
        private class clsParse {

            /**
             * 解析数据
             */
            public void parseAllData(String rtnMsg) {
                try {
                    Document document = Jsoup.parse(rtnMsg.replace("&nbsp;", " "));                 //替换空格
                    Element content = document.getElementsByClass("content").first();

                    //先找到每行文本内容
                    Elements collection_tr = content.getElementsByTag("tr");
                    int len = collection_tr.size();
                    for (int i = 0; i < len; i++) {
                        Element tr = collection_tr.get(i);
                        //将一行中左右部分分割
                        Elements collection_td = tr.getElementsByTag("td");
                        if (collection_td.size() == 2) {
                            String leftPart = Html.fromHtml(collection_td.first().toString()).toString().trim();
                            leftPart = leftPart.replace(" ", "").replace(":", "");
                            String rightPart = Html.fromHtml(collection_td.last().toString()).toString().trim();

                            //解析存数数据
                            parseItem(leftPart, rightPart, mData);
                        }
                    }
                } catch (Exception e) {
                    abstract_LogUtil.i(this, e.toString());
                    e.printStackTrace();
                }
            }

            //按项目存储解析数据
            private void parseItem(String leftPart, String rightPart, struct_LectureDetail data) {
                //报告题目、开始时间、报告地点、报告人、主办单位、备注
                if (leftPart.length() <= 0) return;

                switch (leftPart) {
                    case "报告题目":
                        data.title = rightPart;
                        break;
                    case "开始时间":
                        data.startTime = rightPart;
                        break;
                    case "报告地点":
                        data.address = rightPart;
                        break;
                    case "报告人":
                        data.reporter = rightPart;
                        break;
                    case "主办单位":
                        data.organization = rightPart;
                        break;
                    case "备注":
                        data.remark = rightPart;
                        break;
                }
            }
        }
    }
}
