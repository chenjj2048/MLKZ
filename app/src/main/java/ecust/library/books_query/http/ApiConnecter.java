/**
 * .
 * Created by 彩笔怪盗基德 on 2015/12/8
 * github：https://github.com/chenjj2048
 * .
 */

package ecust.library.books_query.http;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import ecust.library.books_query.modles.BooksQueryRequest;
import ecust.library.books_query.modles.ViewState;
import ecust.library.books_query.modles.WebResponse;

public class ApiConnecter {
    private static ApiConnecter mInstance;
    private QueryBooksCallBack mCallBack;
    private Activity mUiThreadActivity;
    private boolean isLoading = false;

    /**
     * 与OkHttp的交互
     */
    private OkHttpBooksQuery mOkHttpBooksQuery = new OkHttpBooksQuery() {
        /**
         * @param itemCount 单条记录数量
         * @param singlePageItemCount 一页中包含的记录数，此处为30
         * @return 1-30返回1、31-60返回2、61-90返回3、以此类推
         */
        private int getPageCount(int itemCount, int singlePageItemCount) {
            double i = 1.0f * itemCount / singlePageItemCount;
            return (int) Math.ceil(i);
        }

        //这里的方法都是在子线程中的
        @Override
        @WorkerThread
        public ViewState updateViewState(String stringResult) {
            return HtmlParser.parseViewState(stringResult);
        }


        @Override
        @WorkerThread
        public void onOkHttpFailure(final Exception e) {
            mUiThreadActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCallBack.onFailure(e);
                    isLoading = false;
                }
            });
        }

        @Override
        @WorkerThread
        public void onOkHttpFirstQuerySuccess(String result) throws Exception {
            final WebResponse webResponse = HtmlParser.parseWebResult(result);
            final int maxPageIndex = getPageCount(webResponse.booksTotalCount, 30);
            mOkHttpBooksQuery.setMaxPageIndex(maxPageIndex);

            mUiThreadActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCallBack.onFirstQuerySuccess(webResponse);
                }
            });
        }

        @Override
        @WorkerThread
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
        mUiThreadActivity = null;
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
    @UiThread
    public void startNewBooksQuery(@NonNull final BooksQueryRequest mRequest) {
        if (isLoading)
            return;
        else
            isLoading = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                mOkHttpBooksQuery.init();
                mOkHttpBooksQuery.startNewBooksQuery(mRequest);
                isLoading = false;
            }
        }).start();
    }

    @UiThread
    public void continueBooksQuery() {
        if (mOkHttpBooksQuery.isReachToBottom()) {
            mCallBack.onQueryReachToBottom();
            return;
        }

        if (isLoading)
            return;
        else
            isLoading = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                mOkHttpBooksQuery.continueBooksQuery();
                isLoading = false;
            }
        }).start();
    }

    @UiThread
    public interface QueryBooksCallBack {
        void onFailure(Exception e);

        void onFirstQuerySuccess(WebResponse webResponse);

        void onContinueQuerySuccess(WebResponse webResponse);

        void onQueryReachToBottom();
    }
}
