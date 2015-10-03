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

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_PostNode;
import lib.clsUtils.logUtil;
import lib.myXmlPullParserUtils;


/**
 * Html网页解析类
 * 解析复杂的网页，千万不要用Pull来解析啊！！！！！
 */
public class htmlParser extends myXmlPullParserUtils {
    private static final String tag = htmlParser.class.getCanonicalName();
    private static final myXmlPullParserUtils utils = new myXmlPullParserUtils();

    /**
     * 解析数据
     *
     * @param html html数据
     */
    protected static void parseHtmlData(String html) {
        XmlPullParser parser = Xml.newPullParser();

        //事件消息类型
        int event;

        //转义&，否则Pull会报错
        byte[] bytes = html.replace("&", "&amp;").getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);

        //设置内容
        try {
            parser.setInput(inputStream, "UTF-8");
            event = parser.getEventType();
        } catch (XmlPullParserException e) {
            logUtil.printException(tag, e);
            return;
        }

        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                final String id = parser.getAttributeValue(null, "id");
                final String className = parser.getAttributeValue(null, "class");

                switch (parser.getName()) {
                    case "div":
                        if ("pt".equals(id) && "bm cl".equals(className)) {
                            //解析当前版块目录
                            //如：论坛›特色版块›学联学代会› 志愿服务总队
                            logUtil.d(tag, "========头部信息========");
                        } else if ("forumleftside".equals(id) && "tbn".equals(className)) {
                            //解析版块导航
                            logUtil.d(tag, "========版块导航========");
                        } else if ("vfboxs".equals(className)) {
                            //解析子版块
                            logUtil.d(tag, "========子版块========");
                        } else if ("th".equals(className)) {
                            //解析排序方式、精华帖
                            logUtil.d(tag, "========排序方式、精华帖========");
                        }
                        break;
                    case "ul":
                        if ("thread_types".equals(id) && "ttp bm cl".equals(className)) {
                            //解析贴子分类筛选
                            logUtil.d(tag, "=========分类筛选========");
                        } else if ("newspecial_menu".equals(id) && "p_pop".equals(className)) {
                            //解析发布（发表帖子、投票、悬赏、活动）
                            logUtil.d(tag, "=========发布内容========");
                        }
                        break;
                    case "form":
                        if ("moderate".equals(id)) {
                            //解析贴子（主要内容啊！）
                            logUtil.d(tag, "========帖子集合========");
                            parsePostItems(parser);
                        }
                        break;
                }
            }

            //继续寻找下一个
            event = utils.moveToNext(parser);
        }
    }

    /**
     * 解析帖子集合的结构
     */
    private static void parsePostItems(XmlPullParser parser) {
        int event;
        while ((event = utils.moveToNext(parser)) != XmlPullParser.END_DOCUMENT) {
            //帖子全部解析完成,跳出循环
            if (event == XmlPullParser.END_TAG && "form".equals(parser.getName()))
                break;

            //解析单个帖子
            if (event == XmlPullParser.START_TAG && "tbody".equals(parser.getName())) {
                struct_PostNode postNode = parseSinglePost(parser);
            }
        }
    }

    /**
     * 解析单个帖子结果
     */
    private static struct_PostNode parseSinglePost(XmlPullParser parser) {
        int event;
        struct_PostNode node = new struct_PostNode();

        //1.设置是否帖子为置顶
        String id = parser.getAttributeValue(null, "id");
        if (id.contains("stickthread")) {
            node.getPostAttribute().isTop = true;         //置顶帖
        } else if (id.contains("normalthread")) {
            node.getPostAttribute().isTop = false;        //非置顶帖
        } else {
            return null;                 //分割线,不需要处理这条帖子
        }
        logUtil.d(tag, "=====贴子i=====");
        //logUtil.d(tag, "[1.置顶帖] " + node.getPostAttribute().isTop);

        //2.设置贴子类型
        utils.moveToNextStartTag(parser, "img", null);
        String postAttribute = parser.getAttributeValue(null, "src");
        node.getPostAttribute().setPostAttribute(postAttribute);
        //logUtil.d(tag, "[2.贴子类型] " + node.getPostAttribute().getPostAttribute());

        //3.设置贴子主题分类
        utils.moveToNextStartTag(parser, "th", null);
        utils.moveToNextStartTag(parser);
        if ("em".equals(parser.getName())) {
            String strClassification = utils.moveToNextText(parser, 2);
            node.setClassificationName(strClassification);
            utils.moveToNextStartTag(parser, "a", null);
        } else {
            node.setClassificationName("无");
        }
        logUtil.d(tag, "[2.主题分类] " + node.getClassificationName());

        //4.设置贴子超链接及标题
        node.setPostUrl(parser.getAttributeValue(null, "href"));
        node.setTitle(utils.moveToNextText(parser, 0));
        logUtil.d(tag, "[3.贴子标题]" + node.getTitle());
        logUtil.d(tag, "[4.URL]" + node.getPostUrl());

        //5.设置贴子含附件、精品、含图、热门、被点赞标签
        do {
            event = utils.moveToNextStartTag(parser);
            //没有图片对应的属性，退出循环
            if (!"img".equals(parser.getName()))
                break;

            //找到标签
            String title = parser.getAttributeValue(null, "title");
            if (title.contains("图片附件")) {
                node.getPostAttribute().hasPictures = true;
            } else if (title.contains("热门")) {
                node.getPostAttribute().isHot = true;
            } else if (title.contains("帖子被加分")) {
                node.getPostAttribute().hasPraise = true;
            } else if (title.contains("精华")) {
                node.getPostAttribute().isExcellent = true;
            } else {
                logUtil.w(tag, "[设置附件、精品、热门、点赞信息等] 这是什么标签 " + title);
            }
        } while (event != XmlPullParser.END_DOCUMENT);
        logUtil.d(tag, "[5.贴子类型] " + node.getPostAttribute().getPostAttribute());

        //6.设置回帖奖励
        if ("span".equals(parser.getName()) && "xi1".equals(parser.getAttributeValue(null,"class"))){
            utils.moveToNextText(parser,2);
           String str= utils.moveToNextText(parser,2);
           node.setRewardSum(Integer.valueOf(str.trim()));
        }
        logUtil.d(tag,"[6.回帖奖励] "+node.getRewardSum());

        //7.作者

        while (XmlPullParser.END_DOCUMENT != (event = utils.moveToNext(parser))) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    break;
                case XmlPullParser.END_TAG:
                    if ("tbody".equals(parser.getName())) {
                        //返回结果值

                        return node;
                    }
                    break;
                case XmlPullParser.TEXT:
                    break;
            }
        }

        return null;
    }
}
