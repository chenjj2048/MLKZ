/**
 * Created by 彩笔怪盗基德 on 2015/11/22
 * 托管地址：https://github.com/chenjj2048
 * .
 */
package ecust.library.books_query.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import CustomWidgets.BaseAppCompatActivity;
import ecust.library.books_query.adapter.BooksQueryRequestAdapter;
import ecust.library.books_query.http.ApiConnecter;
import ecust.library.books_query.interfaces.QueryBooksCallBack;
import ecust.library.books_query.modles.BooksQueryRequest;
import ecust.library.books_query.modles.WebResponse;
import ecust.main.R;
import utils.logUtils.logUtil;

public class BooksQueryActivity extends BaseAppCompatActivity {

    /**
     * 网络访问回调
     */
    private QueryBooksCallBack mCallBack = new QueryBooksCallBack() {
        //都在主线程中
        @Override
        public void onFailure(final Exception e) {

        }

        @Override
        public void onFirstQuerySuccess(WebResponse webResponse) {
            logUtil.toast(webResponse.booksTotalCount + webResponse.mBooks.get(0).getTitle());
        }

        @Override
        public void onContinueQuerySuccess(WebResponse webResponse) {

        }
    };

    private BooksQueryRequestAdapter mRequestAdapter = new BooksQueryRequestAdapter(this) {
        @Override
        public void onBooksQueryRequestClick(int type, String typeDescription, String queryString) {
            BooksQueryRequest.Builder builder = new BooksQueryRequest.Builder();
            switch (type) {
                case SEARCH_TITLE:
                    builder.title(queryString);
                    break;
                case SEARCH_AUTHOR:
                    builder.author(queryString);
                    break;
                case SEARCH_PUBLISHER:
                    builder.publisher(queryString);
                    break;
                default:
                    throw new IllegalArgumentException("没有这种搜索情况:" + typeDescription);
            }
            //搜索新书籍
            ApiConnecter.getInstance().init(BooksQueryActivity.this, mCallBack);
            ApiConnecter.getInstance().startNewBooksQuery(builder.build());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_books_query);
        initComponents();
        setSupportActionBar(getSupportToolBar(this));
    }

    @SuppressLint("InflateParams")
    private void initComponents() {
        SearchView mSearchView = (SearchView) findViewById(R.id.library_book_query_searchview);
        mSearchView.setOnQueryTextListener(new BooksQuerySearchViewListener());
        mSearchView.onActionViewExpanded();

        ListView mSearchListView = (ListView) findViewById(R.id.library_book_query_search_listview);
        mSearchListView.setAdapter(mRequestAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ApiConnecter.getInstance().recycle();
    }

    /**
     * SearchView事件Listener
     */
    private class BooksQuerySearchViewListener implements SearchView.OnQueryTextListener {
        private final ViewGroup mHistory = (ViewGroup) findViewById(R.id.library_book_query_history);

        @Override
        public boolean onQueryTextSubmit(String query) {
            if (!TextUtils.isEmpty(query))
                mRequestAdapter.onBooksQueryRequestClick(0, null, query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mRequestAdapter.setQueryString(newText);
            if (TextUtils.isEmpty(newText)) {
                //显示搜索历史，隐藏搜索请求(自动消失的)
                mHistory.setVisibility(View.VISIBLE);
            } else {
                //显示搜索请求（自动显示），隐藏搜索历史
                mHistory.setVisibility(View.INVISIBLE);
            }
            return false;
        }
    }

}
