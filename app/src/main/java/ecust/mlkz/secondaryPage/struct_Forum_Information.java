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

import java.util.List;

import lib.clsUtils.logUtil;

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
    private static String decorateURL(String url) {
        url = url.replace("&amp;", "&");
        return url;
    }

    /**
     * 帖子结构
     */
    protected static class struct_PostNode {
        //===============================
        //帖子标题
        private String title;
        //帖子URL
        private String postUrl;

        //===============================
        //作者信息
        private struct_Person author;

        //===============================
        //发帖时间
        private String firstReleaseTime;
        //最后回复时间
        private String lastReplyTime;
        //回复数量
        private int replyCount = 0;
        //查看数量
        private int readCount = 0;

        //=============================
        //贴子所在分类主题
        private String classificationName = "";
        //贴子特性
        private struct_PostAttribute postAttribute = new struct_PostAttribute();
        //回帖奖励
        private int rewardSum = 0;

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
            this.title = decorateURL(title);
        }

        public String getPostUrl() {
            return postUrl;
        }

        public void setPostUrl(String postUrl) {
            this.postUrl = postUrl;
        }

        public struct_Person getAuthor() {
            return author;
        }

        public void setAuthor(struct_Person author) {
            this.author = author;
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
            this.lastReplyTime = lastReplyTime;
        }

        public int getReplyCount() {
            return replyCount;
        }

        public void setReplyCount(int replyCount) {
            this.replyCount = replyCount;
        }

        public int getReadCount() {
            return readCount;
        }

        public void setReadCount(int readCount) {
            this.readCount = readCount;
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
                    this.isVote=true;
                    break;
                case "template/eis_012/img/folder_new.gif":
                    //正常贴，什么都没有
                    break;
                case "template/eis_012/img/pin_1.gif":
                    //置顶帖（前面已经处理过了，这里就不处理了）
                    break;
                default:
                    logUtil.w(this, "[未知贴子类型]" + postAttribute);
            }
        }
    }

    /**
     * 会员结构
     */
    protected static class struct_Person {
        //id号
        private int id;
        //名称
        private String name;
        //空间URL
        private String spaceUrl;
        //头像地址
        private String imageUrl;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSpaceUrl() {
            return spaceUrl;
        }

        public void setSpaceUrl(String spaceUrl) {
            this.spaceUrl = spaceUrl;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    /**
     * 论坛数据集合
     */
    protected static class struct_ForumPageAllData {
        //当前位置
        private struct_CurrentSection currentSection;
        //版块集合
        private List<struct_PrimarySectionNode> primarySectionNodes;
        //发帖地址集合
        private struct_CommitPostURL commitPostURL;
        //精品帖地址
        private String excelentPostsURL;
        //主题分类
        private List<struct_ClassificationNode> classificationNodes;

        public String getExcelentPostsURL() {
            return excelentPostsURL;
        }

        public void setExcelentPostsURL(String excelentPostsURL) {
            this.excelentPostsURL = excelentPostsURL;
        }

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

        public struct_CommitPostURL getCommitPostURL() {
            return commitPostURL;
        }

        public void setCommitPostURL(struct_CommitPostURL commitPostURL) {
            this.commitPostURL = commitPostURL;
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
     * 发帖地址（发帖、发投票、发悬赏，暂不包含发活动）
     */
    protected static class struct_CommitPostURL {
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

        public void setName(String name) {
            this.name = name;
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
     * 版块当前位置
     */
    protected static class struct_CurrentSection {
        protected String primarySectionName;
        protected String secondarySectionName;
        protected String tertiarySectionName;
    }
}
