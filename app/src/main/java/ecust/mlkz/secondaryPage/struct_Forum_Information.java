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

import java.util.ArrayList;
import java.util.List;

import lib.logUtils.abstract_LogUtil;

/**
 * 二级页面
 * 论坛结构信息
 */
public class struct_Forum_Information {
    /**
     * 处理网页URL
     * 添加头部http://bbs.ecust.edu.cn/
     * 替换&amp;为&
     */
    @Nullable
    private static String decorateURL(String url) {
        if (url == null) return null;
        final String BBS = "http://bbs.ecust.edu.cn";

        url = url.replace("&amp;", "&");
        if (!url.startsWith("http://"))
            if (url.startsWith("/"))
                url = BBS + url;
            else
                url = BBS + "/" + url;
        return url;
    }

    /**
     * 返回的所有数据
     */
    protected static class struct_MLKZ_Data {
        //1.页面结果
        public struct_ForumPageAllData mPageInformation;
        //2.帖子结果
        public List<struct_PostNode> mPostsList;
    }

    /**
     * 排序方式对应的地址
     */
    protected static class struct_SortList {
        public List<String> mTitle = new ArrayList<>();
        public List<String> mUrl = new ArrayList<>();

        /**
         * 存储地址
         *
         * @param key 排序方式名称
         * @param Url 对应地址
         */
        public void putSortUrl(String key, @Nullable String Url) {
            Url = decorateURL(Url);
            if (key != null && Url != null)
                if ("默认排序".equals(key)) {
                    mTitle.add(0, key);
                    mUrl.add(0, Url);
                } else {
                    mTitle.add(key);
                    mUrl.add(Url);
                }
        }
    }

    /**
     * 帖子结构
     */
    protected static class struct_PostNode {
        //===============================
        //帖子标题
        private String title = "";
        //帖子URL
        private String postUrl = "";

        //===============================
        //作者信息
        private struct_Person author = new struct_Person();

        //===============================
        //发帖时间
        private String firstReleaseTime = "";
        //最后回复时间
        private String lastReplyTime = "";
        //回复数量
        private int replyCount = 0;
        //查看数量
        private int visitCount = 0;

        //=============================
        //贴子所在分类主题
        private String classificationName = "";
        //贴子特性
        private struct_PostAttribute postAttribute = new struct_PostAttribute();
        //回帖奖励
        private int rewardSum = 0;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            struct_PostNode postNode = (struct_PostNode) o;

            return !(postUrl != null ? !postUrl.equals(postNode.postUrl) : postNode.postUrl != null);

        }

        @Override
        public int hashCode() {
            return postUrl != null ? postUrl.hashCode() : 0;
        }

        public int getRewardSum() {
            return rewardSum;
        }

        public void setRewardSum(int rewardSum) {
            this.rewardSum = rewardSum;
        }

        public struct_PostAttribute getPostAttribute() {
            return postAttribute;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title.replace("&amp;", "&");
        }

        public String getPostUrl() {
            return postUrl;
        }

        public void setPostUrl(String postUrl) {
            this.postUrl = decorateURL(postUrl);
        }

        public struct_Person getAuthor() {
            return author;
        }

        public String getFirstReleaseTime() {
            return firstReleaseTime;
        }

        public void setFirstReleaseTime(String firstReleaseTime) {
            this.firstReleaseTime = firstReleaseTime;
        }

        public String getLastReplyTime() {
            return lastReplyTime;
        }

        public void setLastReplyTime(String lastReplyTime) {
            lastReplyTime = lastReplyTime.replace("&nbsp;", " ");
            this.lastReplyTime = lastReplyTime;
        }

        public int getReplyCount() {
            return replyCount;
        }

        public void setReplyCount(int replyCount) {
            this.replyCount = replyCount;
        }

        public int getVisitCount() {
            return visitCount;
        }

        public void setVisitCount(int visitCount) {
            this.visitCount = visitCount;
        }

        public String getClassificationName() {
            return classificationName;
        }

        public void setClassificationName(String classificationName) {
            this.classificationName = classificationName;
        }
    }

    /**
     * 贴子特性
     */
    protected static class struct_PostAttribute {
        //============多选项===============
        //帖子-热门
        public boolean isHot = false;
        //帖子-精品
        public boolean isExcellent = false;
        //贴子-含图
        public boolean hasPictures = false;
        //贴子-被评分
        public boolean hasPraise = false;
        //贴子-含附件(这项暂时就不用了)
        //public boolean hasAttachment = false;

        //============复合项===============
        //置顶帖
        public boolean isTop = false;

        //============单选项===============
        //贴子被锁定
        public boolean isLock = false;
        //投票贴
        public boolean isVote = false;
        //悬赏贴
        public boolean isBounty = false;

        /**
         * 获取帖子类型
         */
        protected String getPostAttribute() {
            StringBuffer sb = new StringBuffer();
            if (isTop) sb.append("置顶贴 ");

            if (isLock) sb.append("锁贴 ");
            if (isVote) sb.append("投票贴 ");
            if (isBounty) sb.append("悬赏贴 ");

            if (isHot) sb.append("热门 ");
            if (isExcellent) sb.append("精品 ");
            if (hasPictures) sb.append("含图 ");
            if (hasPraise) sb.append("贴子被评分 ");

            if (sb.length() <= 0)
                sb.append("无特性");
            return new String(sb);
        }

        /**
         * 根据图片URL地址来确定贴子类型（锁定、投票、悬赏）
         *
         * @param postAttribute 象形图片的URL地址
         */
        protected void setPostAttribute(String postAttribute) {
            switch (postAttribute) {
                case "template/eis_012/img/pollsmall.gif":
                    this.isVote = true;
                    break;
                case "template/eis_012/img/folder_lock.gif":
                    this.isLock = true;
                    break;
                case "template/eis_012/img/folder_new.gif":
                    //正常贴，什么都没有
                    break;
                case "template/eis_012/img/pin_1.gif":
                    //置顶帖（前面已经处理过了，这里就不处理了）
                    break;
                default:
                    abstract_LogUtil.w(this, "[未知贴子类型]" + postAttribute);
            }
        }
    }

    /**
     * 会员结构
     */
    protected static class struct_Person {
        //名称
        private String name = "";
        //空间URL
        private String spaceUrl = "";
        //头像地址
        private String imageUrl = "";

        @NonNull
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @NonNull
        public String getSpaceUrl() {
            return spaceUrl;
        }

        public void setSpaceUrl(String spaceUrl) {
            this.spaceUrl = decorateURL(spaceUrl);
        }

        @NonNull
        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = decorateURL(imageUrl);
        }
    }

    /**
     * 论坛数据集合
     * 不包含帖子内容
     */
    protected static class struct_ForumPageAllData {
        //当前位置
        private struct_CurrentSection currentSection;
        //版块集合
        private List<struct_PrimarySectionNode> primarySectionNodes;
        //发帖地址集合
        private struct_SubmitURL submitURL;
        //精品帖地址
        private String excelentPostsURL;
        //主题分类
        private List<struct_ClassificationNode> classificationNodes;
        //排序方式
        private struct_SortList mSortList;
        //下一页的URL，为空代表已经到达最后了
        private String nextPageURL;

        public String getNextPageURL() {
            return nextPageURL;
        }

        //转成PC版
        public void setNextPageURL(String nextPageURL) {
            this.nextPageURL = activity_MLKZ_Secondary_Page.addressConvertToWAP(decorateURL(nextPageURL), false);
        }

        public struct_SortList getSortList() {
            return mSortList;
        }

        public void setSortList(struct_SortList mSortList) {
            this.mSortList = mSortList;
        }

        public String getExcelentPostsURL() {
            return excelentPostsURL;
        }

        public void setExcelentPostsURL(String excelentPostsURL) {
            this.excelentPostsURL = decorateURL(excelentPostsURL);
        }

        @Nullable
        public struct_CurrentSection getCurrentSection() {
            return currentSection;
        }

        public void setCurrentSection(struct_CurrentSection currentSection) {
            this.currentSection = currentSection;
        }

        public List<struct_PrimarySectionNode> getPrimarySectionNodes() {
            return primarySectionNodes;
        }

        public void setPrimarySectionNodes(List<struct_PrimarySectionNode> primarySectionNodes) {
            this.primarySectionNodes = primarySectionNodes;
        }

        public struct_SubmitURL getSubmitURL() {
            return submitURL;
        }

        public void setSubmitURL(struct_SubmitURL submitURL) {
            this.submitURL = submitURL;
        }

        public List<struct_ClassificationNode> getClassificationNodes() {
            return classificationNodes;
        }

        public void setClassificationNodes(List<struct_ClassificationNode> classificationNodes) {
            this.classificationNodes = classificationNodes;
        }
    }

    /**
     * 主题分类-节点（筛选操作）
     */
    protected static class struct_ClassificationNode {
        private String name;
        private String url;

        @Override
        public String toString() {
            return "[" + name + "] " + url;
        }

        public String getName() {
            return name;
        }

        public struct_ClassificationNode setName(String name) {
            this.name = name;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public struct_ClassificationNode setUrl(String url) {
            this.url = decorateURL(url);
            return this;
        }
    }

    /**
     * 发帖地址（发帖、发投票、发悬赏，暂不包含发活动）
     */
    protected static class struct_SubmitURL {
        private String postURL;
        private String voteURL;
        private String bountyURL;

        public String getPostURL() {
            return postURL;
        }

        public void setPostURL(String postURL) {
            this.postURL = decorateURL(postURL);
        }

        public String getVoteURL() {
            return voteURL;
        }

        public void setVoteURL(String voteURL) {
            this.voteURL = decorateURL(voteURL);
        }

        public String getBountyURL() {
            return bountyURL;
        }

        public void setBountyURL(String bountyURL) {
            this.bountyURL = decorateURL(bountyURL);
        }
    }

    /**
     * 一级标题节点
     */
    protected static class struct_PrimarySectionNode {
        //一级标题名称
        private String name;
        //二级节点
        private List<struct_SecondarySectionNode> secondaryNodes;

        public String getName() {
            return name;
        }

        public struct_PrimarySectionNode setName(String name) {
            this.name = name;
            return this;
        }

        public List<struct_SecondarySectionNode> getSecondaryNodes() {
            return secondaryNodes;
        }

        public void setSecondaryNodes(List<struct_SecondarySectionNode> secondaryNodes) {
            this.secondaryNodes = secondaryNodes;
        }
    }

    /**
     * 二级标题节点
     */
    protected static class struct_SecondarySectionNode {
        //二级标题名称
        private String name;
        //二级URL
        private String url;
        //三级节点
        private List<struct_TertiarySectionNode> tertiaryNodes;

        @Override
        public String toString() {
            return "[" + name + "] " + url;
        }

        public String getName() {
            return name;
        }

        public struct_SecondarySectionNode setName(String name) {
            //原来标题太长，替换掉
            if (name != null && name.contains("新生报到"))
                name = "新生报到";
            this.name = name;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public struct_SecondarySectionNode setUrl(String url) {
            this.url = decorateURL(url);
            return this;
        }

        public List<struct_TertiarySectionNode> getTertiaryNodes() {
            return tertiaryNodes;
        }

        public void setTertiaryNodes(List<struct_TertiarySectionNode> tertiaryNodes) {
            this.tertiaryNodes = tertiaryNodes;
        }
    }

    /**
     * 三级标题节点（子版块分类）
     */
    protected static class struct_TertiarySectionNode {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public struct_TertiarySectionNode setName(String name) {
            this.name = name;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public struct_TertiarySectionNode setUrl(String url) {
            this.url = decorateURL(url);
            return this;
        }
    }

    /**
     * 版块当前位置
     */
    protected static class struct_CurrentSection {
        private String primarySectionName;
        private String secondarySectionName;
        private String tertiarySectionName;

        public String getPrimarySectionName() {
            return primarySectionName;
        }

        public void setPrimarySectionName(String primarySectionName) {
            this.primarySectionName = primarySectionName;
        }

        public String getSecondarySectionName() {
            return secondarySectionName;
        }

        public void setSecondarySectionName(String secondarySectionName) {
            //标题太长，替换一下
            if (secondarySectionName != null && secondarySectionName.contains("新生报到"))
                secondarySectionName = "新生报到";
            this.secondarySectionName = secondarySectionName;
        }

        public String getTertiarySectionName() {
            return tertiarySectionName;
        }

        public void setTertiarySectionName(String tertiarySectionName) {
            this.tertiarySectionName = tertiarySectionName;
        }

        @Override
        public String toString() {
            return "一级标题=" + primarySectionName +
                    "  二级标题=" + secondarySectionName +
                    "  三级标题=" + tertiarySectionName;
        }
    }
}
