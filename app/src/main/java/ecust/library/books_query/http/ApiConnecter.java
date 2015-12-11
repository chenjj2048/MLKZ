/**
 * .
 * Created by 彩笔怪盗基德 on 2015/12/8
 * github：https://github.com/chenjj2048
 * .
 */

package ecust.library.books_query.http;

import android.app.Activity;
import android.support.annotation.NonNull;

import ecust.library.books_query.interfaces.QueryBooksCallBack;
import ecust.library.books_query.modles.BooksQueryRequest;
import ecust.library.books_query.modles.ViewState;
import ecust.library.books_query.modles.WebResponse;

public class ApiConnecter {
    private static ApiConnecter mInstance;
    private QueryBooksCallBack mCallBack;
    private Activity mUiThreadActivity;


    /**
     * 与OkHttp的交互
     */
    private OkHttpBooksQuery mOkHttpBooksQuery = new OkHttpBooksQuery() {
        //这里的方法都是在子线程中的
        @Override
        public ViewState updateViewState(String stringResult) {
            return HtmlParser.parseViewState(stringResult);
        }

        @Override
        public void onOkHttpFailure(final Exception e) {
            mUiThreadActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCallBack.onFailure(e);
                }
            });
        }

        @Override
        public void onOkHttpFirstQuerySuccess(String result) throws Exception {
            final WebResponse webResponse = HtmlParser.parseWebResult(result);
            mUiThreadActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCallBack.onFirstQuerySuccess(webResponse);
                }
            });
        }

        @Override
        public void onOkHttpContinueQuerySuccess(String result) throws Exception {
            final WebResponse webResponse = HtmlParser.parseWebResult(result);
            mUiThreadActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCallBack.onContinueQuerySuccess(webResponse);
                }
            });
        }
    };

    public static ApiConnecter getInstance() {
        if (mInstance == null)
            mInstance = new ApiConnecter();
        return mInstance;
    }

    public void init(@NonNull Activity activity, @NonNull final QueryBooksCallBack mCallBack) {
        this.mUiThreadActivity = activity;
        this.mCallBack = mCallBack;
    }

    public void recycle() {
        mInstance = null;
    }


    /**
     * ================================================
     * 以下为针对Activity的
     * ================================================
     */

    /**
     * 访问网络，查询书籍信息
     *
     * @param mRequest 书籍信息
     */
    public void startNewBooksQuery(@NonNull final BooksQueryRequest mRequest) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                mOkHttpBooksQuery.init();
                mOkHttpBooksQuery.startNewBooksQuery(mRequest);
            }
        }).start();
    }

    public void continueBooksQuery() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mOkHttpBooksQuery.continueBooksQuery();
            }
        }).start();
    }
}
