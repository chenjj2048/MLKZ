/**
 * .
 * Created by 彩笔怪盗基德 on 2015/12/9
 * github：https://github.com/chenjj2048
 * .
 */

package ecust.library.books_query.http;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import ecust.library.books_query.modles.BooksQueryRequest;
import ecust.library.books_query.modles.ViewState;

/**
 * 所有方法均在子线程中
 */
public abstract class OkHttpBooksQuery {
    private static final String URL_BOOK_QUERY = "http://lib.ecust.edu.cn:8081/webpac/querybookx.aspx";
    private OkHttpClient okHttp = new OkHttpClient();
    private ViewState viewState;
    private BooksQueryRequest mBooksQueryRequest;
    private int nextPageIndex;

    /**
     * 第一次访问，更新握手后的ViewState
     */
    public void init() {
        try {
            Request mRequest = new Request.Builder().url(URL_BOOK_QUERY).build();
            Response response = okHttp.newCall(mRequest).execute();
            viewState = updateViewState(response.body().string());
        } catch (Exception e) {
            onOkHttpFailure(e);
        }
    }

    /**
     * 继init()后，进行访问获取数据
     *
     * @param queryRequest 书籍信息
     */
    public void startNewBooksQuery(BooksQueryRequest queryRequest) {
        try {
            RequestBody mRequest = queryRequest
                    .getFirstPostData(viewState);
            String result = executeQuery(mRequest);
            //设置要访问的下一页索引
            nextPageIndex = 2;
            //记录最后一次的访问请求
            mBooksQueryRequest = queryRequest;
            //回调处理结果
            onOkHttpFirstQuerySuccess(result);
        } catch (Exception e) {
            onOkHttpFailure(e);
        }
    }

    /**
     * 继续查询书籍信息
     */
    public void continueBooksQuery() {
        try {
            RequestBody mRequestBody = mBooksQueryRequest
                    .getContinuePostData(viewState, nextPageIndex);
            String result = executeQuery(mRequestBody);
            nextPageIndex++;
            onOkHttpContinueQuerySuccess(result);
        } catch (Exception e) {
            onOkHttpFailure(e);
        }
    }

    /**
     * @param requestBody POST提交的数据
     * @return 网页数据结果
     */
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

    public abstract ViewState updateViewState(String stringResult);

    public abstract void onOkHttpFirstQuerySuccess(String result) throws Exception;

    public abstract void onOkHttpContinueQuerySuccess(String result) throws Exception;

    public abstract void onOkHttpFailure(Exception e);

    public interface PostData {
        public FormEncodingBuilder getCommonPostData(ViewState viewState) throws Exception;

        public RequestBody getFirstPostData(ViewState viewState) throws Exception;

        public RequestBody getContinuePostData(ViewState viewState, int nextPageIndex) throws Exception;
    }
}
