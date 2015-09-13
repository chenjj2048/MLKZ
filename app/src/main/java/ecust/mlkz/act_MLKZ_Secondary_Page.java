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
package ecust.mlkz;

import android.app.Activity;
import android.os.Bundle;
import android.util.Xml;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ecust.main.R;
import ecust.mlkz.act_MLKZ_Secondary_Page.forum_Structs_Collection.struct_forumDataRoot;
import ecust.mlkz.act_MLKZ_Secondary_Page.forum_Structs_Collection.struct_forumPostNode;
import lib.clsUtils.httpUtil;
import lib.clsUtils.logUtil;

/**
 * 梅陇客栈-版块发帖目录
 */
public class act_MLKZ_Secondary_Page extends Activity implements httpUtil.OnHttpVisitListener {

    //数据集
    private final forum_Structs_Collection factory = new forum_Structs_Collection();
    private struct_forumDataRoot forumData = factory.new struct_forumDataRoot();

    //Cookie
    private String cookie;

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
    }

    @Override
    public void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String returnHtmlMessage) {
        if (!bSucceed) return;

        ((TextView) findViewById(R.id.mlkz_secondary_page_text)).setText(returnHtmlMessage);

        //数据解析
        new htmlParser().parseHtmlData(returnHtmlMessage);

    }

    @Override
    public void onHttpBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, String returnHtmlMessage) {

    }

    @Override
    public void onPictureLoadCompleted(String url, String cookie, boolean bSucceed, byte[] returnPicBytes) {

    }

    @Override
    public void onPictureBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, byte[] returnPicBytes) {

    }

    /**
     * 用于HTML页面的解析
     */
    protected class htmlParser {
        private static final int IDLE = 0;
        private static final int PARSERING_HEAD_START = 1;
        private static final int PARSERING_ITEM_START = 2;
        //解析后的数据集
        protected struct_forumDataRoot result = factory.new struct_forumDataRoot();
        //贴子主题Item
        protected struct_forumPostNode forumPostNode;
        //当前状态
        private int currentStatus = IDLE;

        /**
         * 解析网络数据
         *
         * @param html 网页数据
         */
        protected struct_forumDataRoot parseHtmlData(String html) {
            XmlPullParser parser = Xml.newPullParser();

            //事件
            int event;

            //转义&，否则Pull会报错
            byte[] bytes = html.replace("&", "&amp;").getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);

            //设置内容
            try {
                parser.setInput(inputStream, "UTF-8");
                event = parser.getEventType();
            } catch (XmlPullParserException e) {
                logUtil.printException(this, e);
                return null;
            }

            //开始解析
            while (event != XmlPullParser.END_DOCUMENT) {
                //处理内容
                if (event == XmlPullParser.START_TAG && parser.getName().equals("div")) {
                    switch (parser.getAttributeValue(null, "class")) {
                        case "box flif":
                            //解析 今日新增贴子数量、版块总主题数量
                            parseHeadPostItemCount(event, parser);
                            break;
                        case "bm_c":
                        case "bm_c bt":
                            //解析 一条条的贴子 结构
                            parsePostItem(event, parser);
                            break;
                    }
                }

                //获取下一条内容
                try {
                    event = parser.next();
                } catch (Exception e) {
//                    logUtil.e(this, e.toString());
                }
            }

            //关闭连接
            try {
                inputStream.close();
            } catch (IOException e) {
                logUtil.printException(this, e);
            }

            return result;
        }

        //解析当前的贴子结构
        private void parsePostItem(int currentEvent, XmlPullParser parser) {

            String str;
            //单个贴子信息
            struct_forumPostNode postItem = factory.new struct_forumPostNode();

            int event = currentEvent;
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        //先后顺序不能乱
                        if ("span".equals(parser.getName())) {

                            //移动指针
                            try {
                                event = parser.next();
                                event = parser.next();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //根据标签选择不同处理过程
                            if (event == XmlPullParser.START_TAG && "a".equals(parser.getName())) {
                                //分支1：作者非匿名（多数情况）

                                //4.设置作者空间URL
                                str = parser.getAttributeValue(null, "href");
                                postItem.setAuthorSpaceUrl(str);
//
//                                //5.设置作者名称
//                                try {
//                                    str = parser.nextText();
//                                    postItem.setAuthor(str);
//                                } catch (Exception e) { //
//                                }
//
//                                //移动指针
//                                try {
//                                    parser.nextText();
//                                } catch (Exception e) { //
//                                }
//                            } else {
//                                //分支2：作者为匿名
//
//                                //4.设置作者空间URL
//                                postItem.setAuthorSpaceUrl("");
//                                //5.设置作者名称
//                                postItem.setAuthor("匿名");
                            }
//
//                            //6.设置发帖时间
//                            // 如2014-9-7                        回26
//                            str = parser.getText().replace("匿名 ", "").trim();
//                            postItem.setFirstReleaseTime(str);
//
//                            //7.设置回复数量
//                            postItem.setReply(str);

                            return;
                        }
                        if ("a".equals(parser.getName())) {
                            //1.设置贴子URL
                            str = parser.getAttributeValue(null, "href");
                            postItem.setUrl(str);

                            //移动指针
                            try {
                                event = parser.next();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //判断类型(遇到了图片还是文字)，文字有可能空白，但长度为1
                            if (parser.getText() == null || parser.getText().length() < 2) {
                                //再移动一次指针，两个TAG间的文字有空白
                                try {
                                    event = parser.next();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            //2.设置贴子类型
                            if (event == XmlPullParser.START_TAG && "img".equals(parser.getName())) {
                                str = parser.getAttributeValue(null, "src");
                                postItem.setForumPostType(str);
                                try {
                                    event = parser.next();    //移动到</img>结束部分
                                    event = parser.next();    //移动到文字部分
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            //3.设置贴子标题
                            if (event == XmlPullParser.TEXT) {
                                postItem.setTitle(parser.getText());
                            }
                        }
                        break;
                    case XmlPullParser.TEXT:

                        break;
                    case XmlPullParser.END_TAG:
                        //解析结束退出,添加项目
                        if (parser.getName().equals("div")) {
                            result.forumPosts.add(postItem);
                            return;
                        }
                }

                //拿到下一条内容
                try {
                    event = parser.next();
                } catch (Exception e) {     //
                }
            }
        }


        //设置 今日新增贴子数量、版块总主题数量
        private void parseHeadPostItemCount(int currentEvent, XmlPullParser parser) {
            int event = currentEvent;
            while (true) {
                switch (event) {
                    case XmlPullParser.END_DOCUMENT:
                        return;
                    case XmlPullParser.TEXT:
                        //设置属性
                        String str = parser.getText();
                        result.setItem_Count_Today(str);
                        result.setItem_Count_Subjects(str);
                        break;
                    case XmlPullParser.END_TAG:
                        //解析结束退出
                        if (parser.getName().equals("div")) return;
                }

                //拿到下一条内容
                try {
                    event = parser.next();
                } catch (Exception e) {     //
                }
            }
        }

    }

    /**
     * 数据集合
     */
    protected class forum_Structs_Collection {
        /**
         * 论坛贴子类型
         */
        protected static final int POST_STATUS_NORMAL = 0;
        protected static final int POST_STATUS_TOP = 1;
        protected static final int POST_STATUS_LOCK = 2;
        protected static final int POST_STATUS_VOTE = 3;
        protected static final int POST_STATUS_BOUNTY = 4;

        /**
         * 论坛数据解析结果-根节点
         */
        protected class struct_forumDataRoot {
            //贴子汇总
            protected List<struct_forumPostNode> forumPosts = new ArrayList<>(100);
            //当前版块名称
            private String sectionTitle;
            //当前版块URL
            private String sectionURL;
            //今日发帖
            private int item_Count_Today = 0;
            //主题数量
            private int item_Count_Subjects = 0;
            //当前已显示的数量
            private int item_Count_Current = 0;
            //主题分类（不一定存在）
            private List<struct_forumSubjectClassificationNode> subjectClassification = new ArrayList<>(10);
            //子版块（不一定存在）
            private List<struct_forumChildSectionNode> childSections = new ArrayList<>(10);

            public int getItem_Count_Today() {
                return item_Count_Today;
            }

            /**
             * 设置今日新增主题数量
             *
             * @param str 如"今日0"
             */
            public void setItem_Count_Today(String str) {
                if (str == null || !str.contains("今日")) return;

                try {
                    this.item_Count_Today = Integer.parseInt(str.replace("今日", "").trim());
                    logUtil.d(this, "[今日主题数量]" + getItem_Count_Today());
                } catch (Exception e) {
                    logUtil.e(this, str);
                    logUtil.printException(this, e);
                }
            }

            public int getItem_Count_Subjects() {
                return item_Count_Subjects;
            }

            /**
             * 设置版块 主题数量
             *
             * @param str 如"主题963"
             */
            public void setItem_Count_Subjects(String str) {
                if (str == null || !str.contains("主题")) return;

                try {
                    this.item_Count_Subjects = Integer.parseInt(str.replace("主题", "").trim());
                    logUtil.d(this, "[版块总主题数量]" + getItem_Count_Subjects());
                } catch (Exception e) {
                    logUtil.e(this, str);
                    logUtil.printException(this, e);
                }
            }

            public String getSectionTitle() {
                return sectionTitle;
            }

            public void setSectionTitle(String sectionTitle) {
                this.sectionTitle = sectionTitle;
            }

            public String getSectionURL() {
                return sectionURL;
            }

            public void setSectionURL(String sectionURL) {
                this.sectionURL = sectionURL;
            }
        }

        /**
         * 子版块-节点
         */
        protected class struct_forumChildSectionNode {
            //子版块标题
            private String name;
            //子版块URL
            private String url;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }

        /**
         * 论坛贴子-节点
         */
        protected class struct_forumPostNode {
            //贴子题目
            private String title;
            //贴子URL
            private String url;
            //作者
            private String author;
            //作者空间URL
            private String authorSpaceUrl;
            //发帖时间
            private String firstReleaseTime;
            //回复数量
            private int reply = 0;
            //贴子类型
            private int forumPostType = POST_STATUS_NORMAL;

            public struct_forumPostNode() {
                logUtil.d(this, "=======新建贴子节点=========");
            }

            public int getForumPostType() {
                return forumPostType;
            }

            public void setForumPostType(String picURL) {
                //设置相应的贴子类型
                switch (picURL) {
                    case "template/eis_012/img/pin_1.gif":
                        this.forumPostType = POST_STATUS_TOP;
                        break;
                    case "template/eis_012/img/folder_lock.gif":
                        this.forumPostType = POST_STATUS_LOCK;
                        break;
                    case "template/eis_012/img/pollsmall.gif":
                        this.forumPostType = POST_STATUS_VOTE;
                        break;
                    case "template/eis_012/img/rewardsmall.gif":
                        this.forumPostType = POST_STATUS_BOUNTY;
                        break;
                }
                logUtil.d(this, "[贴子类型]" + this.forumPostType);
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                title = title.replace("\r", "").replace("\n", "").trim();
                this.title = title;
                logUtil.d(this, "[标题]" + this.title);
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
                logUtil.d(this, "[贴子URL]" + this.url);
            }

            public String getAuthor() {
                return author;
            }

            public void setAuthor(String author) {
                this.author = author;
                logUtil.d(this, "[作者]" + author);
            }

            public String getAuthorSpaceUrl() {
                return authorSpaceUrl;
            }

            public void setAuthorSpaceUrl(String authorSpaceUrl) {
                this.authorSpaceUrl = authorSpaceUrl;
                logUtil.d(this, "[作者空间]" + this.authorSpaceUrl);
            }

            public String getFirstReleaseTime() {
                return firstReleaseTime;
            }

            /**
             * 设置发帖时间
             *
             * @param firstReleaseTime 传入参数可能为
             *                         "2014-9-7                        回26" 或  "2014-9-7"
             */
            public void setFirstReleaseTime(String firstReleaseTime) {

                this.firstReleaseTime = firstReleaseTime;
                logUtil.d(this, "[发帖时间]" + this.firstReleaseTime);
            }

            public int getReply() {
                return reply;
            }

            /**
             * 设置会附属
             *
             * @param str 传入参数可能为
             *            "2014-9-7                        回26" 或  "2014-9-7"
             */
            public void setReply(String str) {
                if (str != null && str.contains("回")) {
                    str = str.substring(str.lastIndexOf("回"));
                    str = str.replace("回", "").trim();
                    this.reply = Integer.parseInt(str);
                } else {
                    this.reply = 0;
                }
                logUtil.d(this, "[回帖数量]" + this.reply);
            }
        }

        /**
         * 主题分类
         */
        protected class struct_forumSubjectClassificationNode {
            private String name;
            private String url;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }
    }
}

