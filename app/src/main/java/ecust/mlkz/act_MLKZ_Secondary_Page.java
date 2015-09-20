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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ecust.main.R;
import ecust.mlkz.act_MLKZ_Secondary_Page.forum_Structs_Collection.struct_forumChildSectionNode;
import ecust.mlkz.act_MLKZ_Secondary_Page.forum_Structs_Collection.struct_forumCommitPostURL;
import ecust.mlkz.act_MLKZ_Secondary_Page.forum_Structs_Collection.struct_forumDataRoot;
import ecust.mlkz.act_MLKZ_Secondary_Page.forum_Structs_Collection.struct_forumHeadInfo;
import ecust.mlkz.act_MLKZ_Secondary_Page.forum_Structs_Collection.struct_forumPostNode;
import ecust.mlkz.act_MLKZ_Secondary_Page.forum_Structs_Collection.struct_forumSubjectClassificationNode;
import lib.Global;
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

    //头部bar
    private headbar_Secondary_Page headbar;

    //HeadBar回调接口
    private headbar_Secondary_Page.OnHeadbarClickListener onHeadbarClickListener =
            new headbar_Secondary_Page.OnHeadbarClickListener() {

                //排序按钮点击
                @Override
                public void onSortButtonClick(int sortByTime) {
                    switch (sortByTime) {
                        case headbar_Secondary_Page.SORT_BY_POSTTIME:
                            logUtil.toast("发帖时间");
                            break;
                        case headbar_Secondary_Page.SORT_BY_REPLYTIME:
                            logUtil.toast("回复时间");
                            break;
                    }
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

        headbar = (headbar_Secondary_Page) findViewById(R.id.mlkz_secondary_page_headbar);
        headbar.setOnHeadbarClickListener(onHeadbarClickListener);
    }

    @Override
    public void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String returnHtmlMessage) {
        if (!bSucceed) return;

        //数据解析
        struct_forumDataRoot result = new htmlParser().parseHtmlData(returnHtmlMessage);


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

        /**
         * 解析网络数据
         *
         * @param html 网页数据
         */
        protected struct_forumDataRoot parseHtmlData(String html) {
            //解析后的数据集
            struct_forumDataRoot result = factory.new struct_forumDataRoot();

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
                            //解析 今日新增贴子数量、版块总主题数量、收藏版块
                            struct_forumHeadInfo headInfo = parseHeadPostsCount(parser);
                            result.setHeadInfo(headInfo);

                            if (headInfo != null) {
                                logUtil.d(this, "=========版块头部信息==========");
                                logUtil.d(this, "[今日主题数量]" + headInfo.getItem_Count_Today());
                                logUtil.d(this, "[版块总主题数量]" + headInfo.getItem_Count_Subjects());
                                logUtil.d(this, "[收藏版块] URL=" + headInfo.getFavoriteSectionURL());
                            }
                            break;

                        case "tz pbn":
                            //解析 发帖地址
                            struct_forumCommitPostURL commitPostURL = parseCommitPostURL(parser);
                            result.setCommitPostURL(commitPostURL);

                            if (commitPostURL != null) {
                                logUtil.d(this, "=========发帖地址==========");
                                logUtil.d(this, "[发帖]" + commitPostURL.getPostURL());
                                logUtil.d(this, "[投票]" + commitPostURL.getVoteURL());
                                logUtil.d(this, "[悬赏]" + commitPostURL.getBountyURL());
                            }

                            break;

                        case "bm_c":
                        case "bm_c bt":
                            //解析 一条条的贴子 结构
                            struct_forumPostNode item = parsePostItem(parser);
                            if (item != null) {
                                result.forumPosts.add(item);
                                logUtil.d(this, item.toString());
                            }
                            break;

                        case "box ttp":
                            //解析 主题分类

                            List<struct_forumSubjectClassificationNode> subjectClassificationList =
                                    parseSubjectClassification(parser);

                            if (subjectClassificationList == null) break;

                            headbar.setSubjectClassificationData(subjectClassificationList);

                            result.subjectClassification = subjectClassificationList;
                            logUtil.d(this, "============主题分类=============");
                            for (struct_forumSubjectClassificationNode i : subjectClassificationList)
                                logUtil.d(this, i.toString());

                            break;

                        case "fl":
                            //解析 子版块

                            List<struct_forumChildSectionNode> childSectionNodeList =
                                    parseChildSection(parser);

                            if (childSectionNodeList == null || childSectionNodeList.size() == 0) {
                                logUtil.d(this, "=========子版块为空=======");
                            } else {
                                result.childSections = childSectionNodeList;
                                logUtil.d(this, "=========解析子版块=========");
                                for (struct_forumChildSectionNode i : childSectionNodeList) {
                                    logUtil.d(this, i.toString());
                                }
                            }
                            break;
                    }
                }

                //获取下一条内容
                try {
                    event = parser.next();
                } catch (Exception e) {
                    logUtil.w(this, "[网页标签未成对] Line=" + parser.getLineNumber());
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

        //解析发帖地址
        protected struct_forumCommitPostURL parseCommitPostURL(XmlPullParser parser) {
            int event = XmlPullParser.START_DOCUMENT;

            struct_forumCommitPostURL result = factory.new struct_forumCommitPostURL();

            while (event != XmlPullParser.END_DOCUMENT) {
                try {
                    event = parser.next();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (event == XmlPullParser.START_TAG && "a".equals(parser.getName())) {
                    try {
                        String href = parser.getAttributeValue(null, "href");
                        String name = parser.nextText();

                        if (name.contains("发帖"))
                            result.setPostURL(href);
                        else if (name.contains("投票"))
                            result.setVoteURL(href);
                        else if (name.contains("悬赏"))
                            result.setBountyURL(href);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (event == XmlPullParser.END_TAG && "div".equals(parser.getName())) {
                    return result;
                }
            }

            return null;
        }

        //解析子版块内容
        protected List<struct_forumChildSectionNode> parseChildSection(XmlPullParser parser) {
            //数据集
            List<struct_forumChildSectionNode> result = new ArrayList<>(10);
            struct_forumChildSectionNode node;

            int event = XmlPullParser.START_DOCUMENT;

            while (event != XmlPullParser.END_DOCUMENT) {
                try {
                    event = parser.next();
                } catch (Exception e) {
                    logUtil.w(this, "[网页标签未成对] Line=" + parser.getLineNumber());
                }


                if (event == XmlPullParser.START_TAG && "a".equals(parser.getName())) {
                    //设置数据
                    node = factory.new struct_forumChildSectionNode();

                    try {
                        String href = parser.getAttributeValue(null, "href");
                        node.setUrl(href);

                        String name = parser.nextText();
                        node.setName(name);

                        result.add(node);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (event == XmlPullParser.START_TAG && "div".equals(parser.getName()) &&
                        "ft".equals(parser.getAttributeValue(null, "class"))) {
                    //结束
                    return result;
                }
            }
            return null;
        }

        //解析主题分类（不一定每个版块都有这一项）
        protected List<struct_forumSubjectClassificationNode> parseSubjectClassification(XmlPullParser parser) {
            int event = XmlPullParser.START_DOCUMENT;
            //数据集
            List<struct_forumSubjectClassificationNode> result = new ArrayList<>(20);

            while (event != XmlPullParser.END_DOCUMENT) {
                try {
                    event = parser.next();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("a".equals(parser.getName())) {

                            struct_forumSubjectClassificationNode node =
                                    factory.new struct_forumSubjectClassificationNode();

                            //地址
                            String href = parser.getAttributeValue(null, "href");
                            node.setUrl(href);

                            //主题分类名称
                            try {
                                String name = parser.nextText();
                                node.setName(name);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //添加项目
                            result.add(node);
                        }
                    case XmlPullParser.END_TAG:
                        //返回数据
                        if ("div".equals(parser.getName())) {
                            return result;
                        }
                }
            }
            //异常位置出口（一般不从这里退出）
            return null;
        }

        //解析当前的贴子结构

        protected struct_forumPostNode parsePostItem(XmlPullParser parser) {
            //临时字符串
            String str = null;
            //解析第一行内容（相对的是第二行）
            boolean bParseFirstLine = true;
            //单个贴子信息
            struct_forumPostNode postItem = factory.new struct_forumPostNode();

            int event = XmlPullParser.START_DOCUMENT;

            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        //解析第一行
                        if ("a".equals(parser.getName()) && bParseFirstLine) {
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

                            //2.设置贴子类别
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

                            //解析第二行标记
                            bParseFirstLine = false;
                            break;
                        }

                        //解析第二行
                        if ("span".equals(parser.getName()) && !bParseFirstLine) {

                            //移动指针
                            try {
                                event = parser.next();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //根据标签选择不同处理过程
                            if (event == XmlPullParser.TEXT && parser.getText().contains("匿名")) {
                                //分支1：作者为匿名（少数情况）

                                //4.设置作者空间URL
                                postItem.setAuthorSpaceUrl("");
                                //5.设置作者名称
                                postItem.setAuthor("匿名");

                                //6.设置发帖时间
                                //传入参数类似  "2014-9-7                        回26"
                                try {
                                    str = parser.getText();
                                    str = str.replace("匿名 ", "").trim();
                                    postItem.setFirstReleaseTime(str);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                //7.设置回复数量
                                postItem.setReply(str);
                            } else {
                                //分支2：作者非匿名（多数情况）
                                try {
                                    event = parser.next();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                if (event == XmlPullParser.START_TAG && "a".equals(parser.getName())) {
                                    //4.设置作者空间URL
                                    str = parser.getAttributeValue(null, "href");
                                    postItem.setAuthorSpaceUrl(str);

                                    //5.设置作者名称
                                    try {
                                        str = parser.nextText();
                                        postItem.setAuthor(str);
                                        parser.next();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    //6.设置发帖时间
                                    //传入参数类似  "2014-9-7                        回26"
                                    try {
                                        str = parser.getText();
                                        str = str.replace("匿名 ", "").trim();
                                        postItem.setFirstReleaseTime(str);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    //7.设置回复数量
                                    postItem.setReply(str);
                                }
                            }
                            //正常退出的地方
                            return postItem;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        //解析结束退出
                        if (parser.getName().equals("div")) {
                            return postItem;
                        }
                }

                //拿到下一条内容
                try {
                    event = parser.next();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //不是从这里退出函数的
            return null;
        }


        //设置 今日新增贴子数量、版块总主题数量
        protected struct_forumHeadInfo parseHeadPostsCount(XmlPullParser parser) {
            struct_forumHeadInfo result = factory.new struct_forumHeadInfo();

            int event = XmlPullParser.START_DOCUMENT;
            try {
                event = parser.getEventType();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

            while (true) {
                switch (event) {
                    case XmlPullParser.END_DOCUMENT:
                        return null;
                    case XmlPullParser.START_TAG:
                        if ("a".equals(parser.getName())) {
                            //收藏版块
                            String href = parser.getAttributeValue(null, "href");
                            result.setFavoriteSectionURL(href);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        //设置属性
                        String str = parser.getText();
                        result.setItem_Count_Today(str);
                        result.setItem_Count_Subjects(str);
                        break;
                    case XmlPullParser.END_TAG:
                        //解析结束退出
                        if (parser.getName().equals("div")) return result;
                }

                //拿到下一条内容
                try {
                    event = parser.next();
                } catch (Exception e) {
                    logUtil.w(this, "[未成对标签] Line=" + parser.getLineNumber());
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
            //头部信息
            private struct_forumHeadInfo headInfo = new struct_forumHeadInfo();
            //发帖地址
            private struct_forumCommitPostURL commitPostURL = new struct_forumCommitPostURL();
            //当前已显示的数量
            private int item_Count_Current = 0;
            //主题分类（不一定存在）
            private List<struct_forumSubjectClassificationNode> subjectClassification = new ArrayList<>();
            //子版块（不一定存在）
            private List<struct_forumChildSectionNode> childSections = new ArrayList<>(10);

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

            public struct_forumHeadInfo getHeadInfo() {
                return headInfo;
            }

            public void setHeadInfo(struct_forumHeadInfo headInfo) {
                this.headInfo = headInfo;
            }

            public struct_forumCommitPostURL getCommitPostURL() {
                return commitPostURL;
            }

            public void setCommitPostURL(struct_forumCommitPostURL commitPostURL) {
                this.commitPostURL = commitPostURL;
            }
        }

        /**
         * 头部信息
         */
        protected class struct_forumHeadInfo {
            //今日发帖
            private int item_Count_Today = 0;
            //主题数量
            private int item_Count_Subjects = 0;
            //收藏版块
            private String favoriteSectionURL;


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
                } catch (Exception e) {
                    logUtil.e(this, str);
                    logUtil.printException(this, e);
                }
            }

            public String getFavoriteSectionURL() {
                return favoriteSectionURL;
            }

            public void setFavoriteSectionURL(String favoriteSectionURL) {
                //null
                if (favoriteSectionURL == null || favoriteSectionURL.length() < 10) {
                    this.favoriteSectionURL = "";
                } else {
                    this.favoriteSectionURL = favoriteSectionURL;
                }
            }
        }


        /**
         * 发帖地址（发帖、发投票、发悬赏）
         */
        protected class struct_forumCommitPostURL {
            private String postURL;
            private String voteURL;
            private String bountyURL;

            public String getPostURL() {
                return postURL;
            }

            public void setPostURL(String postURL) {
                this.postURL = postURL;
            }

            public String getVoteURL() {
                return voteURL;
            }

            public void setVoteURL(String voteURL) {
                this.voteURL = voteURL;
            }

            public String getBountyURL() {
                return bountyURL;
            }

            public void setBountyURL(String bountyURL) {
                this.bountyURL = bountyURL;
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

            @Override
            public String toString() {
                return "[" + name + "]" + url;
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

            @Override
            public String toString() {
                return "[标题]" + title + "\r\n" +
                        "[URL]" + url + "\r\n" +
                        "[作者]" + author + "\r\n" +
                        "[作者空间]" + authorSpaceUrl + "\r\n" +
                        "[发表日期]" + firstReleaseTime + "\r\n" +
                        "[回复数量]" + reply + "\r\n" +
                        "[贴子类型]" + forumPostType;
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
                    default:
                        logUtil.w(this, "[未能解析的贴子类型-图片]" + picURL);
                }
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                title = title.replace("\r", "").replace("\n", "").trim();
                this.title = title;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getAuthor() {
                return author;
            }

            public void setAuthor(String author) {
                this.author = author;
            }

            public String getAuthorSpaceUrl() {
                return authorSpaceUrl;
            }

            public void setAuthorSpaceUrl(String authorSpaceUrl) {
                this.authorSpaceUrl = authorSpaceUrl;
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
                final int maxLength = 10;
                this.firstReleaseTime = firstReleaseTime.substring(0,
                        Math.min(maxLength, firstReleaseTime.length())).trim();
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

            @Override
            public String toString() {
                return "[" + name + "]" + url;
            }
        }
    }
}

