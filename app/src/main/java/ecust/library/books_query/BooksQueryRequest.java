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
 * Created by 彩笔怪盗基德 on 2015/11/22
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.library.books_query;

import android.text.TextUtils;

/**
 * 书目查询的结果
 */
public class BooksQueryRequest {
    //题名
    private final String title;
    //作者
    private final String author;
    //出版社
    private final String publisher;
    //馆藏位置
    private final BooksLocation location;

    private BooksQueryRequest(Builder builder) {
        this.title = builder.title;
        this.author = builder.author;
        this.publisher = builder.publisher;
        this.location = builder.location;
    }

    public String title() {
        return this.title;
    }

    public String author() {
        return this.author;
    }

    public String publisher() {
        return this.publisher;
    }

    public String location() {
        switch (this.location) {
            case xuhui:
                return "徐汇校区";
            case fengxian:
                return "奉贤校区";
            case xuhui_and_fengxian:
            default:
                return "徐汇奉贤";
        }
    }

    /**
     * 判断请求内容是否为空
     */
    public boolean isEmpty() {
        return TextUtils.isEmpty(title) && TextUtils.isEmpty(author) && TextUtils.isEmpty(publisher);
    }

    //馆藏位置枚举类型
    public enum BooksLocation {
        xuhui, fengxian, xuhui_and_fengxian
    }

    /**
     * Builder类
     */
    public static class Builder {
        //题名
        private String title;
        //作者
        private String author;
        //出版社
        private String publisher;
        //馆藏位置
        private BooksLocation location;

        public Builder() {
            title = "";
            author = "";
            publisher = "";
            location = BooksLocation.xuhui_and_fengxian;
        }

        public Builder title(String title) {
            if (title != null) this.title = title;
            return this;
        }

        public Builder author(String author) {
            if (author != null) this.author = author;
            return this;
        }

        public Builder publisher(String publisher) {
            if (publisher != null) this.publisher = publisher;
            return this;
        }

        public Builder location(String location) {
            if (location == null || location.contains("奉贤") && location.contains("徐汇"))
                this.location = BooksLocation.xuhui_and_fengxian;
            else if (location.contains("奉贤"))
                this.location = BooksLocation.fengxian;
            else if (location.contains("徐汇"))
                this.location = BooksLocation.xuhui;
            return this;
        }

        public BooksQueryRequest build() {
            return new BooksQueryRequest(this);
        }
    }
}
