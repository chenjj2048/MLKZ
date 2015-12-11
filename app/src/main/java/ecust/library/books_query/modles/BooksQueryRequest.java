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

package ecust.library.books_query.modles;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import java.net.URLEncoder;

import ecust.Consts;
import ecust.library.books_query.http.OkHttpBooksQuery;

/**
 * 书目查询的请求
 */
@SuppressWarnings("unused")
public class BooksQueryRequest implements OkHttpBooksQuery.PostData {
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

    /**
     * 返回FormEncodingBuilder对象，供okHttp中POST数据使用
     * 返回一些公用的部分，部分区别内容还会在后续加上
     */
    @Override
    @NonNull
    public FormEncodingBuilder getCommonPostData(@Nullable ViewState viewState) throws Exception {
        if (viewState == null)
            throw new NullPointerException();
        return new FormEncodingBuilder()
                .add("__VIEWSTATE", viewState.__VIEWSTATE)
                .add("__EVENTVALIDATION", viewState.__EVENTVALIDATION)
                .add("RadioButtonList1", "1")                //纸版书目
                .addEncoded("TextBox1", URLEncoder.encode(this.title(), Consts.ENCODING_GB2312))        //正题名
                .addEncoded("TextBox2", URLEncoder.encode(this.author(), Consts.ENCODING_GB2312))       //作者
                .addEncoded("TextBox3", URLEncoder.encode(this.publisher(), Consts.ENCODING_GB2312))    //出版社
                .add("TextBox4", "")                         //索书号
                .add("TextBox5", "")                         //ISBN号
                .addEncoded("DropDownList2", URLEncoder.encode("出版日期", Consts.ENCODING_GB2312))
                .addEncoded("RadioButtonList3", URLEncoder.encode("降序", Consts.ENCODING_GB2312))
                .addEncoded("DropDownList3", URLEncoder.encode(this.location(), Consts.ENCODING_GB2312));      //馆藏地址
    }

    @Override
    @NonNull
    public RequestBody getFirstPostData(ViewState viewState) throws Exception {
        return getCommonPostData(viewState)
                .add("__EVENTTARGET", "")
                .add("__EVENTARGUMENT", "")
                .addEncoded("Button1", URLEncoder.encode("确定", Consts.ENCODING_GB2312))
                .build();
    }

    @Override
    @NonNull
    public RequestBody getContinuePostData(ViewState viewState, int nextPageIndex) throws Exception {
        return getCommonPostData(viewState)
                .add("__EVENTTARGET", "GridView1")
                .add("__EVENTARGUMENT", "Page$" + nextPageIndex)
                .build();
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
