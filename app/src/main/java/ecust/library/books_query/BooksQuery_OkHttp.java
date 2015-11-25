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
import android.support.annotation.Nullable;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ecust.library.books_query.structWebData.ViewState;
import ecust.library.books_query.structWebData.WebResult;

/**
 * OkHttp请求——图书馆数据查询
 */
public class BooksQuery_OkHttp {
    //图书查询URL
    private static final String URL_BOOK_QUERY = "http://lib.ecust.edu.cn:8081/webpac/querybookx.aspx";
    //okHttp
    private OkHttpClient okHttp;
    //回调函数
    private OnQuery mListener;
    //用于返回主线程上，runOnUiThread
    private Activity mActivity;
    //存储连接状态
    private ViewState mViewState;
    //解析器
    private HtmlParser mParser;

    public BooksQuery_OkHttp(Activity activity, OnQuery listener) {
        this.mActivity = activity;
        this.mListener = listener;
        this.okHttp = new OkHttpClient();
        this.mParser = new HtmlParser();
    }

    /**
     * 第一次登陆网页，获取一点信息
     * 获得网页中 __VIEWSTATE 和 __EVENTVALIDATION 两个动态值
     */
    @Nullable
    private ViewState initViewState() throws IOException {
        //访问图书馆
        Request mRequest = new Request.Builder().url(URL_BOOK_QUERY).build();
        Response response = okHttp.newCall(mRequest).execute();
        return mParser.parseValue(response.body().string());
    }

    /**
     * 开始新的搜索
     *
     * @param mRequest 书名、作者等等信息
     */
    public void startNewSearch(final BooksQueryRequest mRequest) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //判断是否访问过第一次，拿到过ViewState状态
                    if (BooksQuery_OkHttp.this.mViewState == null) {
                        ViewState state = initViewState();
                        if (state != null)
                            BooksQuery_OkHttp.this.mViewState = state;
                        else
                            throw new IllegalStateException("访问失败");
                    }

                    //携带ViewState，以及搜索内容，进行POST获取网页
                    RequestBody requestBody = makeRequestBody(mRequest, mViewState);
                    Request mRequest = new Request.Builder()
                            .url(URL_BOOK_QUERY)
                            .post(requestBody)
                            .build();
                    Response response = okHttp.newCall(mRequest).execute();

                    //返回的网页结果
                    final String stringResult = response.body().string();
                    final WebResult mWebResult = mParser.parseWebResult(stringResult);

                    //输送结果
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onQueryFinish(true,stringResult, mWebResult);
                        }
                    });
                } catch (final Exception e) {
                    //抛出异常
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onQueryFailure(e);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 生成请求体
     */
    @Nullable
    private RequestBody makeRequestBody(BooksQueryRequest mBooksQuery, ViewState mViewState)
            throws UnsupportedEncodingException {
        String encoding = "gb2312";
        return new FormEncodingBuilder()
                .add("__EVENTTARGET", "")
                .add("__EVENTARGUMENT", "")
                .add("__VIEWSTATE", mViewState.__VIEWSTATE)                    //参数1
                .add("__EVENTVALIDATION", mViewState.__EVENTVALIDATION)        //参数2
                .add("RadioButtonList1", "1")                //纸版书目
                .addEncoded("TextBox1", URLEncoder.encode(mBooksQuery.title(), encoding))        //正题名
                .addEncoded("TextBox2", URLEncoder.encode(mBooksQuery.author(), encoding))       //作者
                .addEncoded("TextBox3", URLEncoder.encode(mBooksQuery.publisher(), encoding))    //出版社
                .add("TextBox4", "")                         //索书号
                .add("TextBox5", "")                         //ISBN号
                .addEncoded("DropDownList2", URLEncoder.encode("出版日期", encoding))
                .addEncoded("RadioButtonList3", URLEncoder.encode("降序", encoding))
                .addEncoded("DropDownList3", URLEncoder.encode(mBooksQuery.location(), encoding))      //馆藏地址
                .addEncoded("Button1", URLEncoder.encode("确定", encoding))
                .build();
    }

    public void getNextPage() {
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    protected interface OnQuery {
        void onQueryFinish(boolean searchNewItem,String stringResult, WebResult webResult);

        void onQueryFailure(Exception e);
    }
}
