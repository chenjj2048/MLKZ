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

import java.util.ArrayList;
import java.util.List;

/**
 * 二级页面
 * 论坛结构信息
 */
public class struct_Forum_Information {
    /**
     * 论坛贴子类型
     */
    protected static final int POST_STATUS_NORMAL = 0;
    protected static final int POST_STATUS_TOP = 1;
    protected static final int POST_STATUS_LOCK = 2;
    protected static final int POST_STATUS_VOTE = 3;
    protected static final int POST_STATUS_BOUNTY = 4;

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

        //===============================
        //帖子-火
        private boolean isHot = false;
        //帖子-精
        private boolean isExcellent = false;
        //贴子-含图
        private boolean hasPicture = false;
        //贴子-被评分
        private boolean hasPraise = false;

        //贴子类型
        private int forumPostType = POST_STATUS_NORMAL;

        //贴子分类名称
        private String classificationName;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
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

        public boolean isHot() {
            return isHot;
        }

        public void setIsHot(boolean isHot) {
            this.isHot = isHot;
        }

        public boolean isExcellent() {
            return isExcellent;
        }

        public void setIsExcellent(boolean isExcellent) {
            this.isExcellent = isExcellent;
        }

        public boolean isHasPicture() {
            return hasPicture;
        }

        public void setHasPicture(boolean hasPicture) {
            this.hasPicture = hasPicture;
        }

        public boolean isHasPraise() {
            return hasPraise;
        }

        public void setHasPraise(boolean hasPraise) {
            this.hasPraise = hasPraise;
        }

        public int getForumPostType() {
            return forumPostType;
        }

        public void setForumPostType(int forumPostType) {
            this.forumPostType = forumPostType;
        }

        public String getClassificationName() {
            return classificationName;
        }

        public void setClassificationName(String classificationName) {
            this.classificationName = classificationName;
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
    protected static class struct_ForumDataCollection{
      //当前位置
        private struct_CurrentSection currentSection;
        //版块集合
        private List<struct_PrimarySectionNode> primarySectionNodes;
        //发帖地址集合
        private struct_CommitPostURL commitPostURL;
        //精品帖地址
    //主题分类
     private  List<  struct_ClassificationNode> classificationNodes;

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
    protected static class struct_PrimarySectionNode{
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
    protected static class struct_CurrentSection{
        protected String primarySectionName;
        protected String secondarySectionName;
        protected String tertiarySectionName;
    }
}
