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

import java.util.ArrayList;
import java.util.List;

import lib.logUtils.abstract_LogUtil;

/**
 * 论坛数据结构
 */
public class struct_Forum_Items {
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
    protected static class struct_forumDataRoot {
        //贴子汇总
        protected List<struct_forumPostNode> forumPosts = new ArrayList<>(100);
        //子版块（不一定存在）
        List<struct_forumChildSectionNode> childSections = new ArrayList<>();
        //当前版块名称
        private String sectionTitle;
        //当前版块URL
        private String sectionURL;
        //论坛所处版块
        private struct_forumPosition forumPosition = new struct_forumPosition();
        //头部信息
        private struct_forumHeadInfo headInfo = new struct_forumHeadInfo();
        //发帖地址
        private struct_forumCommitPostURL commitPostURL = new struct_forumCommitPostURL();
        //主题分类（不一定存在）
        private List<struct_forumSubjectClassificationNode> subjectClassification = new ArrayList<>();

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

        public struct_forumPosition getForumPosition() {
            return forumPosition;
        }

        public void setForumPosition(struct_forumPosition forumPosition) {
            this.forumPosition = forumPosition;
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

        public List<struct_forumSubjectClassificationNode> getSubjectClassification() {
            return subjectClassification;
        }

        public void setSubjectClassification(List<struct_forumSubjectClassificationNode> subjectClassification) {
            this.subjectClassification = subjectClassification;
        }
    }

    /**
     * 当前论坛版块所处位置
     */
    protected static class struct_forumPosition {
        private String homePageURL;
        private String secondaryPageURL;
        private String homePageName;
        private String secondaryPageName;
        private String thirdPageName;

        public String getHomePageURL() {
            return homePageURL;
        }

        public void setHomePageURL(String homePageURL) {
            this.homePageURL = homePageURL;
        }

        public String getSecondaryPageURL() {
            return secondaryPageURL;
        }

        public void setSecondaryPageURL(String secondaryPageURL) {
            this.secondaryPageURL = secondaryPageURL;
        }

        public String getHomePageName() {
            return homePageName;
        }

        public void setHomePageName(String homePageName) {
            this.homePageName = homePageName;
        }

        public String getSecondaryPageName() {
            return secondaryPageName;
        }

        public void setSecondaryPageName(String secondaryPageName) {
            this.secondaryPageName = secondaryPageName;
        }

        public String getThirdPageName() {
            return thirdPageName;
        }

        public void setThirdPageName(String thirdPageName) {
            this.thirdPageName = thirdPageName;
        }
    }

    /**
     * 头部信息
     */
    protected static class struct_forumHeadInfo {
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
                abstract_LogUtil.e(this, str);
                abstract_LogUtil.printException(this, e);
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
                abstract_LogUtil.e(this, str);
                abstract_LogUtil.printException(this, e);
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
    protected static class struct_forumCommitPostURL {
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
    protected static class struct_forumChildSectionNode {
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
    protected static class struct_forumPostNode {
        //贴子题目
        private String title;
        //贴子URL
        private String postUrl;
        //作者
        private String authorName;
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
                    "[URL]" + postUrl + "\r\n" +
                    "[作者]" + authorName + "\r\n" +
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
                    abstract_LogUtil.w(this, "[未能解析的贴子类型-图片]" + picURL);
            }
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            title = title.replace("\r", "").replace("\n", "").trim();
            this.title = title;
        }

        public String getPostUrl() {
            return postUrl;
        }

        public void setPostUrl(String postUrl) {
            this.postUrl = postUrl;
        }

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
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
    protected static class struct_forumSubjectClassificationNode {
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
