/**
 * .
 * Created by 彩笔怪盗基德 on 2015/12/13
 * github：https://github.com/chenjj2048
 * .
 */

package ecust.library.books_query.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import CustomWidgets.BaseFragment;
import ecust.library.books_query.adapter.BooksResultAdapter;
import ecust.library.books_query.http.ApiConnecter;
import ecust.library.books_query.modles.Book;
import ecust.library.books_query.modles.BooksQueryRequest;
import ecust.library.books_query.modles.WebResponse;
import utils.ToastUtil;
import utils.logUtils.logUtil;

public class BooksResultFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    BooksQueryActivity mainActivity;
    private ListView mListView;
    private BooksResultAdapter mAdapter;
    private Context context;
    private ApiCallback mApiCallback;

    public BooksResultFragment setContext(Context context) {
        this.context = context;
        return this;
    }

    public Activity getActivityFromContext() {
        return (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mListView == null)
            mListView = new ListView(context);
        if (mAdapter == null)
            mAdapter = new BooksResultAdapter(context);
        mainActivity = (BooksQueryActivity) getActivity();
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mListView.setLayoutParams(layoutParams);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(new OnScroll());
        mListView.setOnItemClickListener(this);
        return mListView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mainActivity.mSearchView.onActionViewCollapsed();
        BooksResultAdapter.ViewHolder viewHolder = (BooksResultAdapter.ViewHolder) view.getTag();
        Book mBook = viewHolder.mBook;
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        mBuilder.setTitle(mBook.getTitle());
        mBuilder.setMessage(mBook.getAuthor() + "\r\n" + mBook.getPublisher() + "\r\n" + mBook.getPublishTime() + "\r\n" + mBook.getCLCIndex());
        AlertDialog mDailog = mBuilder.create();
        mDailog.show();
    }

    public void setData(WebResponse webResponse) {
        if (mAdapter == null)
            mAdapter = new BooksResultAdapter(context);
        mAdapter.clear();
        mAdapter.addBooks(webResponse.mBooks);
    }

    public void addData(WebResponse webResponse) {
        mAdapter.addBooks(webResponse.mBooks);
    }

    public void searchBooks(BooksQueryRequest request) {
        //搜索新书籍
        if (mApiCallback == null)
            mApiCallback = new ApiCallback();
        ApiConnecter.getInstance().init(getActivityFromContext(), mApiCallback);
        ApiConnecter.getInstance().startNewBooksQuery(request);
    }

    class OnScroll implements AbsListView.OnScrollListener {
        int totalItemCount;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE &&
                    (view.getLastVisiblePosition() + 1 >= totalItemCount)) {
                //滑动到底部
                logUtil.e(this, "ListView到底了");
                ApiConnecter.getInstance().continueBooksQuery();
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            this.totalItemCount = totalItemCount;
        }
    }

    @UiThread
    class ApiCallback implements ApiConnecter.QueryBooksCallBack {
        @Override
        public void onFailure(final Exception e) {
            ToastUtil.toast(e.toString());
        }

        @Override
        public void onFirstQuerySuccess(WebResponse webResponse) {
            if (!webResponse.isEmpty()) {
                logUtil.d(this, String.format("【网页查询成功】当前页%d条，共%d条",
                        webResponse.mBooks.size(), webResponse.booksTotalCount));
            } else {
                ToastUtil.toast("没有相关信息");
            }
            setData(webResponse);
            mainActivity.toggleToFragment(mainActivity.mResultFragment);
        }

        @Override
        public void onContinueQuerySuccess(WebResponse webResponse) {
            if (!webResponse.isEmpty()) {
                addData(webResponse);
            } else
                ToastUtil.toast("结果空2");
        }

        @Override
        public void onQueryReachToBottom() {
            ToastUtil.toast("已到达底部");
        }
    }
}
