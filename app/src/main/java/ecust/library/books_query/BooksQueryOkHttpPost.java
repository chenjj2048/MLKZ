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

import android.app.Activity;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import lib.logUtils.logUtil;

/**
 * OkHttp请求——图书馆数据查询
 */
public class BooksQueryOkHttpPost {
    //图书查询URL
    private static final String URL_BOOK_QUERY = "http://lib.ecust.edu.cn:8081/webpac/querybookx.aspx";

    private OkHttpClient okHttp;
    private OnQuery mListener;
    private BooksQueryRequest mRequest;
    private Activity mActivity;

    public BooksQueryOkHttpPost(Activity activity, BooksQueryRequest request, OnQuery listener) {
        this.mActivity = activity;
        this.mRequest = request;
        this.mListener = listener;
    }

    public void start() {
        if (okHttp == null)
            okHttp = new OkHttpClient();
        init();
    }

    /**
     * 第一次访问
     * GET请求页面，从中拿到两个动态的参数
     */
    private void init() {
        Request mRequest = new Request.Builder().url(URL_BOOK_QUERY).build();

        okHttp.newCall(mRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.OnQueryFailure();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String result = response.body().string();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.OnQueryFinish(result);
                    }
                });
            }
        });
    }

    private void post() {

        //POST数据
        RequestBody mPostData = getFormPostData(mRequest);
        if (mPostData == null) {
            //编码错误
            return;
        }

        Request mRequest = new Request.Builder()
                .url(URL_BOOK_QUERY)
                .post(mPostData)
                .build();

        //访问网络
        okHttp.newCall(mRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                logUtil.e(this, "error");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String s = response.body().string();

            }
        });
    }

    /**
     * 生成表单POST请求
     */
    private RequestBody getFormPostData(BooksQueryRequest mBooksQuery) {
        try {
            String encoding = "gb2312";
            return new FormEncodingBuilder()
                    .add("__EVENTTARGET", "")
                    .add("__EVENTARGUMENT", "")
                    .add("__VIEWSTATE", "%2FwEPDwULLTEyNDQxNjM3NDEPZBYCAgMPZBYIAgEPEA9kFgIeB29uY2xpY2sFEWNoZWNrcmFkaW9saXN0eCgpZGRkAhUPDxYCHgRUZXh0ZWRkAhcPDxYCHwEFMuWFseaJvuWIsCA8c3BhbiBjbGFzcz0iZm9udHJlZCI%2BMCA8L3NwYW4%2B5p2h6K6w5b2VZGQCGQ88KwANAQAPFgQeC18hRGF0YUJvdW5kZx4LXyFJdGVtQ291bnRmZGQYAQUJR3JpZFZpZXcxDzwrAAoBCGZkw5goNjcXTkyDfPpxrpZUOyzf6S4%3D")                    //参数1
                    .add("__EVENTVALIDATION", "%2FwEWGgLxpcaFAwLn44i9AQL444i9AQL544i9AQL644i9AQL744i9AQL3jKLTDQLs0bLrBgLs0fbZDALs0Yq1BQLs0e58AuzRgtgJAvms%2BMEHApzV65sHAqCoyCACr46vsAcC8quwfgKu3dWGDAKUx9aGDAL3jPqcDAKC2IUeAorAi%2BkMAtPNn%2BQGAs3q4eUOAqqT%2FLYKAoznisYGixRwSV%2FwJASKFv9d554I72FEoHs%3D")              //参数2
                    .add("RadioButtonList1", "1")                //纸版书目
                    .add("TextBox1", URLEncoder.encode(mBooksQuery.title(), encoding))        //正题名
                    .add("TextBox2", URLEncoder.encode(mBooksQuery.author(), encoding))       //作者
                    .add("TextBox3", URLEncoder.encode(mBooksQuery.publisher(), encoding))    //出版社
                    .add("TextBox4", "")                         //索书号
                    .add("TextBox5", "")                         //ISBN号
                    .add("DropDownList2", URLEncoder.encode("出版日期", encoding))
                    .add("RadioButtonList3", URLEncoder.encode("降序", encoding))
                    .add("DropDownList3", URLEncoder.encode(mBooksQuery.location(), encoding))      //馆藏地址
                    .add("Button1", URLEncoder.encode("确定", encoding))
                    .build();
        } catch (UnsupportedEncodingException e) {
            logUtil.printException(this, e);
            return null;
        }

    }

    protected interface OnQuery {
        public void OnQueryFinish(String result);

        public void OnQueryFailure();
    }
}
