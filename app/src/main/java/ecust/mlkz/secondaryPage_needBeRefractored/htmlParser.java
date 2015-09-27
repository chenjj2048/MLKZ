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
 * Created by 彩笔怪盗基德 on 2015/9/27
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.mlkz.secondaryPage_needBeRefractored;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ecust.mlkz.secondaryPage_needBeRefractored.struct_Forum_Items.struct_forumChildSectionNode;
import ecust.mlkz.secondaryPage_needBeRefractored.struct_Forum_Items.struct_forumCommitPostURL;
import ecust.mlkz.secondaryPage_needBeRefractored.struct_Forum_Items.struct_forumDataRoot;
import ecust.mlkz.secondaryPage_needBeRefractored.struct_Forum_Items.struct_forumHeadInfo;
import ecust.mlkz.secondaryPage_needBeRefractored.struct_Forum_Items.struct_forumPosition;
import ecust.mlkz.secondaryPage_needBeRefractored.struct_Forum_Items.struct_forumPostNode;
import ecust.mlkz.secondaryPage_needBeRefractored.struct_Forum_Items.struct_forumSubjectClassificationNode;
import lib.clsUtils.logUtil;

/**
 * 用于HTML页面的解析
 */
public class htmlParser {

    /**
     * 解析网络数据
     *
     * @param html 网页数据
     */
    public struct_forumDataRoot parseHtmlData(String html) {
        //解析后的数据集
        struct_forumDataRoot result = new struct_forumDataRoot();
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
                    case "box":
                        //解析 版块所处位置 论坛->一级版块->二级版块
                        struct_forumPosition forumPosition = parseCurrentSectionPosition(parser);
                        result.setForumPosition(forumPosition);

                        if (forumPosition != null) {
                            logUtil.d(this, "========版块位置=========");
                            logUtil.d(this, "[" + forumPosition.getHomePageName() + "]" + forumPosition.getHomePageURL());
                            logUtil.d(this, "[" + forumPosition.getSecondaryPageName() + "]" + forumPosition.getSecondaryPageURL());
                            logUtil.d(this, "[" + forumPosition.getThirdPageName() + "] null URL");
                        }

                        break;
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

                        result.setSubjectClassification(subjectClassificationList);
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

    /**
     * 解析版块名字及URL
     * 论坛 > 学院平台 > 资源与环境工程学院
     */
    protected struct_forumPosition parseCurrentSectionPosition(XmlPullParser parser) {
        int event = XmlPullParser.START_DOCUMENT;
        int href_position = 0;

        struct_forumPosition result = new struct_forumPosition();

        while (event != XmlPullParser.END_DOCUMENT) {
            try {
                event = parser.next();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (event == XmlPullParser.START_TAG && "a".equals(parser.getName())) {
                //URL
                String href = parser.getAttributeValue(null, "href");
                if (++href_position == 1) {
                    //第一级<a href=xxxx>
                    try {
                        result.setHomePageName(parser.nextText());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    result.setHomePageURL(href);
                } else if (href_position == 2) {
                    //第二级<a href=xxxx>
                    try {
                        result.setSecondaryPageName(parser.nextText());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    result.setSecondaryPageURL(href);
                }
            } else if (event == XmlPullParser.TEXT && parser.getText().trim().length() > 1 && !parser.getText().contains("&gt;")) {
                //第三级名称，无URL
                result.setThirdPageName(parser.getText().trim());
            } else if (event == XmlPullParser.END_TAG && "div".equals(parser.getName())) {
                //返回结果
                return result;
            }
        }

        return null;
    }

    //解析发帖地址
    protected struct_forumCommitPostURL parseCommitPostURL(XmlPullParser parser) {
        int event = XmlPullParser.START_DOCUMENT;

        struct_forumCommitPostURL result = new struct_forumCommitPostURL();

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
                node = new struct_forumChildSectionNode();

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
                                new struct_forumSubjectClassificationNode();

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
        struct_forumPostNode postItem = new struct_forumPostNode();

        int event = XmlPullParser.START_DOCUMENT;

        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    //解析第一行
                    if ("a".equals(parser.getName()) && bParseFirstLine) {
                        //1.设置贴子URL
                        str = parser.getAttributeValue(null, "href");
                        postItem.setPostUrl(str);

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
                            postItem.setAuthorName("匿名");

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
                                    postItem.setAuthorName(str);
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
        struct_forumHeadInfo result = new struct_forumHeadInfo();

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
