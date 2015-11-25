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
 * Created by 彩笔怪盗基德 on 2015/11/25
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.library.books_query;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 对应的网页数据
 * http://202.120.96.42:8081/webpac/querybookx.aspx
 */
public class structWebData {

    //这名字不是我启的，对应Asp.net中网页原代码里的名字
    protected static class ViewState {
        protected String __VIEWSTATE;
        protected String __EVENTVALIDATION;
    }

    /**
     * 网页解析出来的结果
     */
    protected static class WebResult {
        //书本集合
        @NonNull
        public List<Book> mBooks = new ArrayList<>(30);
        //查询到的书的总数
        int booksTotalCount = 0;
        //当前的页面下标（第几页）
        int pageIndex = 0;
    }

    protected static class Book {
        //标题
        private String title;
        //作者
        private String author;
        //出版社
        private String publisher;
        //出版时间
        private String publishTime;
        //中图分类号（Chinese Library Classification）
        private String CLCIndex;

        public Book title(String title) {
            this.title = title.trim();
            return this;
        }

        public String getTitle() {
            return this.title;
        }

        public Book author(String author) {
            this.author = author.trim();
            return this;
        }

        public String getAuthor() {
            return this.author;
        }

        public String getPublisher() {
            return this.publisher;
        }

        public Book publisher(String publisher) {
            this.publisher = publisher.trim();
            return this;
        }

        public String getPublishTime() {
            return this.publishTime;
        }

        public Book publishTime(String publishTime) {
            this.publishTime = publishTime.trim();
            return this;
        }

        public String getCLCIndex() {
            return this.CLCIndex;
        }

        public Book CLCIndex(String CLCIndex) {
            this.CLCIndex = CLCIndex.trim();
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Book book = (Book) o;

            if (title != null ? !title.equals(book.title) : book.title != null) return false;
            if (author != null ? !author.equals(book.author) : book.author != null) return false;
            if (publisher != null ? !publisher.equals(book.publisher) : book.publisher != null)
                return false;
            if (publishTime != null ? !publishTime.equals(book.publishTime) : book.publishTime != null)
                return false;
            return !(CLCIndex != null ? !CLCIndex.equals(book.CLCIndex) : book.CLCIndex != null);
        }

        @Override
        public int hashCode() {
            return CLCIndex != null ? CLCIndex.hashCode() : 0;
        }
    }


}
