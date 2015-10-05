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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_ClassificationNode;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_CurrentSection;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_ForumPageAllData;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_MLKZ_Data;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_PostNode;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_PrimarySectionNode;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_SecondarySectionNode;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_SortList;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_SortList.SortStyle;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_SubmitURL;
import lib.logUtils.abstract_LogUtil;
import lib.logUtils.logUtil;
import lib.logUtils.logUtil.LogOFF;
import lib.logUtils.logUtil.LogStatus;
import lib.myXmlPullParserUtils;


/**
 * Html网页解析类
 * 解析复杂的网页，千万不要用Pull来解析啊！！！！！
 * 工作量巨大啊，有木有
 * 代码还显得特别凌乱，不方便调试
 * 请照着类似如下地址调试，否则看不懂的的
 * http://bbs.ecust.edu.cn/forum.php?mod=forumdisplay&fid=4
 */
public class htmlParser {
    //事件消息类型
    int event;
    //XmlPull指针，辅助工具
    private myXmlPullParserUtils pointer;
    //当前的解析器
    private XmlPullParser parser;

    /**
     * 初始化XmlPullParser系列工作
     */
    private void initXmlPullParser() {
        //新建解析器
        parser = Xml.newPullParser();
        //关联指针
        pointer = new myXmlPullParserUtils();
        pointer.setXmlPullParser(parser);
    }

    /**
     * XmlPullParser加载数据
     */
    private boolean loadData(@NonNull String htmlString) {
        //转义&，否则Pull会报错
        byte[] bytes = htmlString.replace("&", "&amp;").getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);

        //设置内容
        try {
            parser.setInput(inputStream, "UTF-8");
            event = parser.getEventType();
            return true;
        } catch (XmlPullParserException e) {
            abstract_LogUtil.printException(this, e);
            return false;
        }
    }

    /**
     * 解析数据
     *
     * @param html html数据
     */
    @Nullable
    @LogStatus(aliasName = "主循环", logShowClassName = true, logShowCurrentFunction = true)
    protected struct_MLKZ_Data parseAllData(String html) {
        //初始化
        initXmlPullParser();
        //日志
        logUtil log = new logUtil(this);

        //加载数据
        if (!loadData(html)) return null;

        //初始化页面信息集合
        struct_MLKZ_Data allData = new struct_MLKZ_Data();
        allData.mPageInformation = new struct_ForumPageAllData();

        //主循环框架
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                final String id = parser.getAttributeValue(null, "id");
                final String className = parser.getAttributeValue(null, "class");

                pointer.printLog("主循环");

                switch (parser.getName()) {
                    case "div":
                        if ("z".equals(className)) {
                            //解析当前版块目录
                            //如：论坛›特色版块›学联学代会› 志愿服务总队
                            struct_CurrentSection currentSection = parseCurrentSection();
                            if (currentSection != null) {
                                log.d("========当前版块========");
                                allData.mPageInformation.setCurrentSection(currentSection);
                                log.d("[当前版块] " + currentSection.toString());
                            }
                        } else if ("forumleftside".equals(id) && "tbn".equals(className)) {
                            //解析版块导航
                            log.d("========版块导航========");
                            List<struct_PrimarySectionNode> mPrimaryNodes = parsePrimarySection();
                            allData.mPageInformation.setPrimarySectionNodes(mPrimaryNodes);
                        } else if ("vfboxs".equals(className)) {
                            //解析子版块
                            log.d("========子版块========");
                        } else if ("th".equals(className)) {
                            //解析精华帖
                            log.d("========精华帖========");
                            String excelentPostsURL = parseExcelentPostsURL();
                            allData.mPageInformation.setExcelentPostsURL(excelentPostsURL);
                            log.d("[精华帖URL] " + allData.mPageInformation.getExcelentPostsURL());
                        } else if ("filter_orderby_menu".equals(id)) {
                            //解析排序方式
                            log.d("========排序方式========");
                            struct_SortList mSortList = parseSortList();
                            allData.mPageInformation.setSortList(mSortList);
                        }
                        break;
                    case "ul":
                        if ("thread_types".equals(id) && "ttp bm cl".equals(className)) {
                            //解析贴子分类筛选
                            log.d("=========分类筛选========");
                            List<struct_ClassificationNode> mClassification = parseClassification();
                            allData.mPageInformation.setClassificationNodes(mClassification);
                        } else if ("newspecial_menu".equals(id) && "p_pop".equals(className)) {
                            //解析发布（发表帖子、投票、悬赏、活动）
                            log.d("=========发布帖子========");
                            struct_SubmitURL mCommitPostURL = parseCommitPostURL();
                            allData.mPageInformation.setSubmitURL(mCommitPostURL);
                            log.d("[发帖]" + mCommitPostURL.getPostURL());
                            log.d("[投票]" + mCommitPostURL.getVoteURL());
                            log.d("[悬赏]" + mCommitPostURL.getBountyURL());
                        }
                        break;
                    case "form":
                        if ("moderate".equals(id)) {
                            //解析贴子（主要内容啊！）
                            log.d("========帖子集合========");
                            allData.mPostsList = parsePostItems();
                            pointer.printLog("帖子集合解析完毕");
                        }
                        break;
                }
            }

            //继续寻找下一个
            event = pointer.moveToNext();
        }
        return allData;
    }

    /**
     * 获取一级版块数组
     */
    @NonNull
    @LogStatus(aliasName = "一级版块数组")
    private List<struct_PrimarySectionNode> parsePrimarySection() {
        List<struct_PrimarySectionNode> result = new ArrayList<>();
        logUtil log = new logUtil(this);
        while (pointer.moveToNextStartTag("dl", "div") != pointer.TAG_NOT_FOUND) {
            String id = parser.getAttributeValue(null, "id");
            if ("lf_fav".equals(id)) {
                //收藏的板块
                log.d("这里是收藏的板块");
            } else {
                //一级版块
                String primarySection = pointer.moveToNextText(2);
                log.d(primarySection);
                //设置数据
                struct_PrimarySectionNode primaryNode = new struct_PrimarySectionNode();
                primaryNode.setName(primarySection);
                //解析数据
                parseSecondarySection(primaryNode);
                result.add(primaryNode);
            }
        }
        return result;
    }

    /**
     * 一级版块
     */
    @LogStatus(aliasName = "一级版块")
    private void parseSecondarySection(struct_PrimarySectionNode fatherNode) {
        String href;
        String title;
        logUtil log = new logUtil(this);
        List<struct_SecondarySectionNode> mNodeList = new ArrayList<>();
        while (pointer.moveToNextStartTag("a", "dl") != pointer.TAG_NOT_FOUND) {
            //获取标题、地址
            href = parser.getAttributeValue(null, "href");
            title = parser.getAttributeValue(null, "title");
            //设置数据
            struct_SecondarySectionNode node = new struct_SecondarySectionNode();
            node.setName(title).setUrl(href);
            mNodeList.add(node);
            log.d(node.toString());
        }
        fatherNode.setSecondaryNodes(mNodeList);
    }

    /**
     * 获取分类筛选数组(如 全部、求助、讨论、科普知识、学术知识、其他) 或 "全部"
     */
    @NonNull
    @LogOFF
    @LogStatus(aliasName = "主题分类")
    private List<struct_ClassificationNode> parseClassification() {
        List<struct_ClassificationNode> result = new ArrayList<>();
        logUtil log = new logUtil(this);
        while (pointer.moveToNextStartTag("a", "ul") != pointer.TAG_NOT_FOUND) {
            String href = parser.getAttributeValue(null, "href");
            String name = pointer.moveToNextText(2);
            //设置数据
            struct_ClassificationNode node = new struct_ClassificationNode();
            node.setName(name).setUrl(href);
            log.d(node.toString());
            result.add(node);
        }
        return result;
    }

    /**
     * 返回发帖URL地址
     *
     * @return 发帖、投票、悬赏的地址
     */
    @NonNull
    @LogOFF
    private struct_SubmitURL parseCommitPostURL() {
        String url;
        int i = 0;
        struct_SubmitURL result = new struct_SubmitURL();

        label_OK:

        while (pointer.moveToNextStartTag("a", "table") != pointer.TAG_NOT_FOUND) {
            url = parser.getAttributeValue(null, "href");
            switch (i++) {
                case 0:
                    //发表贴子
                    result.setPostURL(url);
                    break;
                case 1:
                    //发表投票
                    result.setVoteURL(url);
                    break;
                case 2:
                    //发表悬赏
                    result.setBountyURL(url);
                    break;
                default:
                    break label_OK;
            }
        }

        return result;
    }

    /**
     * 解析排序地址
     */
    @NonNull
    @LogOFF
    @LogStatus(aliasName = "排序方式")
    private struct_SortList parseSortList() {
        String url;
        SortStyle sortStyle;
        struct_SortList result = new struct_SortList();
        while (pointer.moveToNextStartTag("a", "div") != pointer.TAG_NOT_FOUND) {
            url = parser.getAttributeValue(null, "href");
            //设置对应属性
            if (url.contains("dateline"))
                sortStyle = SortStyle.DATELINE;
            else if (url.contains("replies"))
                sortStyle = SortStyle.REPLYS;
            else if (url.contains("views"))
                sortStyle = SortStyle.VIEWS;
            else if (url.contains("lastpost"))
                sortStyle = SortStyle.LAST_POST_TIME;
            else if (url.contains("heats"))
                sortStyle = SortStyle.HEATS;
            else
                sortStyle = SortStyle.DEFAULT;

            //赋值
            result.setSortURL(sortStyle, url);
            new logUtil(this).d(sortStyle.name() + " " + url);

            //抵达了最后一项，就退出
            if (sortStyle == SortStyle.HEATS)
                break;
        }
        return result;
    }

    /**
     * @return 精品贴URL地址
     */
    @Nullable
    private String parseExcelentPostsURL() {
        String url;
        String name;
        while (pointer.moveToNextStartTag("a", "div") != pointer.TAG_NOT_FOUND) {
            url = parser.getAttributeValue(null, "href");
            name = pointer.moveToNextText(2);
            if ("精华".equals(name))
                return url;
        }
        return null;
    }

    /**
     * 解析头部信息
     * 类似
     * 谈天说地›梅陇画廊
     * 或
     * 励志书院›多学科俱乐部› 教学资源分享区
     */
    @Nullable
    private struct_CurrentSection parseCurrentSection() {
        String temp;
        struct_CurrentSection result = null;
        int i = 0;
        while (pointer.moveToNextEndTag("em", "div") != pointer.TAG_NOT_FOUND) {
            if (result == null)
                result = new struct_CurrentSection();

            switch (i++) {
                case 1:
                    //一级版块名称
                    temp = pointer.moveToNextText(2);
                    result.primarySectionName = temp;
                    break;
                case 2:
                    //二级版块名称
                    temp = pointer.moveToNextText(2);
                    result.secondarySectionName = temp;
                    break;
                case 3:
                    //三级版块名称
                    temp = pointer.moveToNextText(2);
                    result.tertiarySectionName = temp;
                    break;
            }
        }
        return result;
    }


    /**
     * 解析帖子集合的结构
     */
    @NonNull
    private List<struct_PostNode> parsePostItems() {
        List<struct_PostNode> postNodeList = new ArrayList<>(30);
        while ((event = pointer.moveToNext()) != XmlPullParser.END_DOCUMENT) {
            //帖子全部解析完成,跳出循环
            if (event == XmlPullParser.END_TAG && "form".equals(parser.getName()))
                break;
            else if (postNodeList.size() >= 30) {
                //一页最多解析出30个贴子
                break;
            }

            //解析单个帖子
            if (event == XmlPullParser.START_TAG && "tbody".equals(parser.getName())) {
                struct_PostNode postNode = parseSinglePost();
                if (postNode != null)
                    postNodeList.add(postNode);
            }
        }
        return postNodeList;
    }

    /**
     * 解析单个帖子结果
     * 这个函数解析得简直作死啊，各种有的木有的标签特性,结构不统一，各种分支情况
     * 感觉复杂网页DOM、SAX肯定会比Pull方便很多，虽然效率差点
     */
    @Nullable
    @LogOFF
    @LogStatus(aliasName = "单一贴子")
    private struct_PostNode parseSinglePost() {
        //日志
        logUtil log = new logUtil(this);

        struct_PostNode node = new struct_PostNode();
        String id = parser.getAttributeValue(null, "id");
        //分割线一行不处理
        if (id.contains("separatorline")) {
            log.v("空行");
            return null;
        }
        log.d("=====贴子i=====");
        //1.设置是否帖子为置顶
        if (id.contains("stickthread")) {
            node.getPostAttribute().isTop = true;         //置顶帖
        } else if (id.contains("normalthread")) {
            node.getPostAttribute().isTop = false;        //非置顶帖
        }

        //2.设置贴子一部分类型
        pointer.moveToNextStartTag("img", null);
        String postAttribute = parser.getAttributeValue(null, "src");
        node.getPostAttribute().setPostAttribute(postAttribute);

        //3.设置贴子主题分类
        pointer.moveToNextStartTag("th", null);
        pointer.moveToNextStartTag();
        if ("em".equals(parser.getName())) {
            String strClassification = pointer.moveToNextText(2);
            node.setClassificationName(strClassification);
            pointer.moveToNextStartTag("a", null);
        } else {
            node.setClassificationName("无");
        }
        log.d("[2.主题分类] " + node.getClassificationName());

        //4.设置贴子超链接及标题
        node.setPostUrl(parser.getAttributeValue(null, "href"));
        node.setTitle(pointer.moveToNextText(0));
        log.d("[3.贴子标题]" + node.getTitle());
        log.d("[4.URL]" + node.getPostUrl());

        //5.设置贴子含附件、精品、含图、热门、被点赞标签
        parseSinglePost_Attribute(node);
        log.d("[5.贴子类型] " + node.getPostAttribute().getPostAttribute());

        //6.设置回帖奖励
        if ("span".equals(parser.getName()) && "xi1".equals(parser.getAttributeValue(null, "class"))) {
            pointer.moveToNextText(2);
            String str = pointer.moveToNextText(2);
            node.setRewardSum(Integer.valueOf(str.trim()));
        }
        log.d("[6.回帖奖励] " + node.getRewardSum());

        //7.设置作者
        if (pointer.getEventType() != XmlPullParser.START_TAG ||
                !parser.getAttributeValue(null, "class").equals("by")) {
            pointer.moveToNextStartTag("td", "tbody");
        }
        parseSinglePost_Author(node);
        log.d("[7.作者名称] " + node.getAuthor().getName());
        log.d("[7.作者空间] " + node.getAuthor().getSpaceUrl());
        log.d("[7.作者头像] " + node.getAuthor().getImageUrl());

        //8.设置发帖时间
        String firstReleaseTime = pointer.moveToNextText(2);
        node.setFirstReleaseTime(firstReleaseTime);
        log.d("[8.发帖时间] " + node.getFirstReleaseTime());

        //9.设置回复、查看数量
        //<td class="num">
        pointer.moveToNextStartTag("td", null);
        parseSinglePost_ReplyAndVisitCount(node);
        log.d("[9.数量] 回复 = " + node.getReplyCount() + "  查看 = " + node.getVisitCount());

        //10.最后回复时间
        pointer.moveToNextText(2);
        String lastReplyTime = pointer.moveToNextText(2);
        node.setLastReplyTime(lastReplyTime);
        log.d("[10.最后回复] " + node.getLastReplyTime());

        return node;
    }

    /**
     * 解析回复、查看数量
     */
    private void parseSinglePost_ReplyAndVisitCount(struct_PostNode node) {
        try {
            String replyCount = pointer.moveToNextText(1);
            String visitCounte = pointer.moveToNextText(1);
            node.setReplyCount(Integer.valueOf(replyCount));
            node.setVisitCount(Integer.valueOf(visitCounte));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置贴子特性
     * 含附件、精品、含图、热门、被点赞标签
     */
    private void parseSinglePost_Attribute(struct_PostNode node) {
        do {
            event = pointer.moveToNextStartTag();
            //没有图片对应的属性，退出循环
            if (!"img".equals(parser.getName()))
                break;

            //找到标签
            String title = parser.getAttributeValue(null, "title");
            if (title.contains("图片附件")) {
                node.getPostAttribute().hasPictures = true;
            } else if (title.contains("附件")) {
                //上面是图片附件，这里才是真附件！暂时用不到不写
            } else if (title.contains("热门")) {
                node.getPostAttribute().isHot = true;
            } else if (title.contains("帖子被加分")) {
                node.getPostAttribute().hasPraise = true;
            } else if (title.contains("精华")) {
                node.getPostAttribute().isExcellent = true;
            } else {
                new logUtil(this).w("[设置附件、精品、热门、点赞信息等] 这是什么标签 " + title);
            }
        } while (event != XmlPullParser.END_DOCUMENT);
    }

    /**
     * 解析作者信息
     * 作者空间、作者头像图片、作者名称
     * 如果作者匿名的话，啥都不干！
     */
    private void parseSinglePost_Author(struct_PostNode node) {
        if (pointer.moveToNextStartTag("a", "cite") != pointer.TAG_NOT_FOUND) {
            //作者空间地址
            String spaceURL = parser.getAttributeValue(null, "href");
            node.getAuthor().setSpaceUrl(spaceURL);
            //移动至下一个
            pointer.moveToNextStartTag();
            //头像图片地址
            String imageURL = parser.getAttributeValue(null, "src");
            node.getAuthor().setImageUrl(imageURL);
            //作者名称
            String authorName = pointer.moveToNextText(2);
            node.getAuthor().setName(authorName);
        } else {
            //作者匿名
            node.getAuthor().setName("匿名");
        }
    }
}
