/**
 * .
 * Created by 彩笔怪盗基德 on 2015/12/9
 * github：https://github.com/chenjj2048
 * .
 */

package ecust.library.books_query.http;

import android.support.annotation.WorkerThread;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import ecust.library.books_query.modles.BooksQueryRequest;
import ecust.library.books_query.modles.ViewState;
import utils.logUtils.logUtil;

public abstract class OkHttpBooksQuery {
    private static final String URL_BOOK_QUERY = "http://lib.ecust.edu.cn:8081/webpac/querybookx.aspx";
    private OkHttpClient okHttp = new OkHttpClient();
    private ViewState viewState;
    private BooksQueryRequest mBooksQueryRequest;
    private int nextPageIndex;
    private int maxPageIndex;

    public void setMaxPageIndex(int maxPageIndex) {
        this.maxPageIndex = maxPageIndex;
    }

    /**
     * 第一次访问，更新握手后的ViewState
     */
    @WorkerThread
    public void init() {
        try {
            Request mRequest = new Request.Builder().url(URL_BOOK_QUERY).build();
            Response response = okHttp.newCall(mRequest).execute();
            viewState = updateViewState(response.body().string());
            logUtil.d(this, "init成功");
        } catch (Exception e) {
            onOkHttpFailure(e);
        }
    }

    /**
     * 继init()后，进行访问获取数据
     *
     * @param queryRequest 书籍信息
     */
    @WorkerThread
    public void startNewBooksQuery(BooksQueryRequest queryRequest) {
        try {
            RequestBody mRequest = queryRequest
                    .getFirstPostData(viewState);
            String result = executeQuery(mRequest);
            //设置要访问的下一页索引
            nextPageIndex = 2;
            //记录最后一次的访问请求
            mBooksQueryRequest = queryRequest;
            logUtil.d(this, String.format("网络数据获取成功 length=%d", result.length()));
            //回调处理结果
            onOkHttpFirstQuerySuccess(result);
        } catch (Exception e) {
            onOkHttpFailure(e);
        }
    }

    /**
     * 继续查询书籍信息
     */
    @WorkerThread
    public void continueBooksQuery() {
        if (isReachToBottom())
            return;

        try {
            RequestBody mRequestBody = mBooksQueryRequest
                    .getContinuePostData(viewState, nextPageIndex);
            String result = executeQuery(mRequestBody);
            nextPageIndex++;
            logUtil.d(this, String.format("网络数据获取成功 length=%d", result.length()));
            onOkHttpContinueQuerySuccess(result);
        } catch (Exception e) {
            onOkHttpFailure(e);
        }
    }

    /**
     * @param requestBody POST提交的数据
     * @return 网页数据结果
     */
    @WorkerThread
    private String executeQuery(RequestBody requestBody) throws Exception {
        Request mRequest = new Request.Builder()
                .url(URL_BOOK_QUERY)
                .post(requestBody)
                .build();
        String result = okHttp.newCall(mRequest).execute()
                .body().string();
        viewState = updateViewState(result);
        return result;
    }

    public boolean isReachToBottom() {
        return nextPageIndex > maxPageIndex;
    }

    @WorkerThread
    public abstract ViewState updateViewState(String stringResult);

    @WorkerThread
    public abstract void onOkHttpFirstQuerySuccess(String result) throws Exception;

    @WorkerThread
    public abstract void onOkHttpContinueQuerySuccess(String result) throws Exception;

    @WorkerThread
    public abstract void onOkHttpFailure(Exception e);

    public interface PostData {
        public FormEncodingBuilder getCommonPostData(ViewState viewState) throws Exception;

        public RequestBody getFirstPostData(ViewState viewState) throws Exception;

        public RequestBody getContinuePostData(ViewState viewState, int nextPageIndex) throws Exception;
    }
}
