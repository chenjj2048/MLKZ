/**
 * .
 * Created by 彩笔怪盗基德 on 2015/12/13
 * github：https://github.com/chenjj2048
 * .
 */

package ecust.library.books_query.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import CustomWidgets.BaseFragment;
import ecust.library.books_query.adapter.BooksQueryAdapter;
import ecust.library.books_query.modles.BooksQueryRequest;
import utils.KeyBoardUtil;

public class BooksQueryFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    ListView mListView;
    BooksQueryAdapter mAdapter;
    BooksQueryActivity mainActivity;
    private Context context;

    public BooksQueryFragment setContext(Context context) {
        this.context = context;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mListView == null)
            mListView = new ListView(context);
        if (mAdapter == null)
            mAdapter = new BooksQueryAdapter(context);
        mainActivity = (BooksQueryActivity) getActivity();
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mListView.setLayoutParams(layoutParams);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        return mListView;
    }

    public void setQueryString(@NonNull String newText) {
        if (mAdapter == null)
            mAdapter = new BooksQueryAdapter(context);
        mAdapter.setQueryString(newText);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BooksQueryRequest.Builder builder = new BooksQueryRequest.Builder();
        switch (position) {
            case BooksQueryAdapter.QUERY_TITLE:
                builder.title(mAdapter.getQueryString());
                break;
            case BooksQueryAdapter.QUERY_AUTHOR:
                builder.author(mAdapter.getQueryString());
                break;
            case BooksQueryAdapter.QUERY_PUBLISHER:
                builder.publisher(mAdapter.getQueryString());
                break;
            default:
                throw new IllegalArgumentException("你点了什么呀");
        }
        BooksQueryRequest mRequest = builder.build();
        startNewQuery(mRequest);
    }

    public void startNewQuery(BooksQueryRequest mRequest) {
        KeyBoardUtil.closeIME(context, mainActivity.mSearchView);
        mainActivity.mSearchView.onActionViewCollapsed();
        //跳转到搜索结果页面
        mainActivity.toggleToFragment(mainActivity.mResultFragment);
        mainActivity.mResultFragment.searchBooks(mRequest);
        //切换至加载页
        //保存记录

    }


}
