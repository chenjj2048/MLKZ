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

import java.util.ArrayList;
import java.util.List;

import ecust.main.R;
import lib.clsUtils.logUtil;

/**
 * 梅陇客栈-版块发帖目录
 */
public class act_MLKZ_Secondary_Page extends Activity {

    //数据集
    private forumDataRoot forumData = new forumDataRoot();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mlkz_secondary_page);

        //设置当前版块标题、URL
        forumData.setSectionTitle(getIntent().getStringExtra("title"));
        forumData.setSectionURL(getIntent().getStringExtra("url"));


        logUtil.toast(forumData.getSectionTitle() + " " + forumData.getSectionURL());
    }

    /**
     * 论坛贴子类型
     */
    private enum forumPostType {
        normal, //正常状态
        top,    //置顶
        lock,   //贴子已被锁定
    }

    /**
     * 论坛数据解析结果-根节点
     */
    private class forumDataRoot {
        //当前版块名称
        private String sectionTitle;
        //当前版块URL
        private String sectionURL;
        //今日发帖
        private int today_item_count = 0;
        //主题数量
        private int all_item_count = 0;
        //贴子汇总
        private List<forumPostNode> forumPosts = new ArrayList<>(100);
        //主题分类
        private List<subjectClassificationNode> subjectClassification = new ArrayList<>(10);
        //子版块
        private List<childSectionNode> childSections = new ArrayList<>(10);

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
    private class childSectionNode {
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
    private class forumPostNode {
        //贴子题目
        private String title;
        //作者
        private String author;
        //发帖时间
        private String firstReleaseTime;
        //回复数量
        private int reply = 0;
        //贴子类型
        private forumPostType forumPostType;
    }

    /**
     * 主题分类
     */
    private class subjectClassificationNode {
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
