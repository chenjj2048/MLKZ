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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import CustomWidgets.BaseAppCompatActivity;
import ecust.library.books_query.structWebData.Book;
import ecust.library.books_query.structWebData.WebResult;
import ecust.main.R;
import lib.InjectViewUtil;
import lib.InjectViewUtil.InjectView;
import lib.clsUtils.clsSoftKeyBoard;
import lib.logUtils.logUtil;

public class activity_books_query extends BaseAppCompatActivity {
    View mParentView;
    //标题
    @InjectView(R.id.library_book_query_title)
    private EditText mTitle;
    //作者
    @InjectView(R.id.library_book_query_author)
    private EditText mAuthor;
    //出版社
    @InjectView(R.id.library_book_query_publisher)
    private EditText mPublisher;
    //馆藏位置
    @InjectView(R.id.library_book_query_location_spinner)
    private Spinner mLocationSpinner;

    //OkHttp相关类
    private BooksQuery_OkHttp mOkHttp;
    //ListView适配器
    private BooksAdapter mBooksAdapter;
    //ListView
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initComponents();
        setSupportActionBar(getSupportToolBar(this));
    }

    @SuppressLint("InflateParams")
    private void initComponents() {
        //加载布局
        mParentView = getLayoutInflater().inflate(R.layout.library_books_query, null);
        setContentView(mParentView);
        InjectViewUtil.inject(this, mParentView);

        //设置下拉选框
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new String[]{"徐汇奉贤", "徐汇校区", "奉贤校区"});
        mLocationSpinner.setAdapter(mAdapter);

        //清空输入
        findViewById(R.id.library_book_query_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTitle.setText("");
                mAuthor.setText("");
                mPublisher.setText("");
                mLocationSpinner.setSelection(0);
            }
        });

        //查找书目
        findViewById(R.id.library_book_query_search).setOnClickListener(new SearchBooks());

        //ListView
        mListView = (ListView) findViewById(R.id.library_book_query_listview);
        mBooksAdapter = new BooksAdapter();
        mListView.setAdapter(mBooksAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                        && mListView.getLastVisiblePosition() >= mListView.getCount() - 1) {
                    logUtil.toast("滑动到底部了");
                    mOkHttp.getNextPage();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    /**
     * 书目查找
     */
    class SearchBooks implements View.OnClickListener, BooksQuery_OkHttp.OnQuery {

        @Override
        public void onClick(View v) {
            clsSoftKeyBoard.hideIME(activity_books_query.this, mParentView);

            //封装一个请求(保存UI中的相关信息)
            BooksQueryRequest mBooksQueryRequest = new BooksQueryRequest.Builder()
                    .title(mTitle.getText().toString())
                    .author(mAuthor.getText().toString())
                    .publisher(mPublisher.getText().toString())
                    .location(mLocationSpinner.getSelectedItem().toString())
                    .build();

            if (mBooksQueryRequest.isEmpty())
                logUtil.toast("请输入查询内容！");
            else {
                if (mOkHttp == null)
                    mOkHttp = new BooksQuery_OkHttp(activity_books_query.this, this);

                //发送请求，获得网络数据
                mOkHttp.startNewSearch(mBooksQueryRequest);
            }
        }

        @Override
        public void onQueryFinish(boolean searchNewItem, String stringResult, WebResult webResult) {
            //添加数据
            if (searchNewItem) {
                mBooksAdapter.clear();
                mBooksAdapter.addBooks(webResult.mBooks);
            }
        }

        @Override
        public void onQueryFailure(Exception e) {
            logUtil.toast(e.toString());
        }
    }

    /**
     * ListView适配器
     */
    class BooksAdapter extends BaseAdapter {
        private List<Book> mBooks = new ArrayList<>();

        public void addBooks(List<Book> mBooksList) {
            for (Book mBook : mBooksList)
                if (!this.mBooks.contains(mBook))
                    this.mBooks.add(mBook);
            this.notifyDataSetChanged();
        }

        public void clear() {
            mBooks.clear();
        }

        @Override
        public int getCount() {
            return (mBooks == null) ? 0 : mBooks.size();
        }

        @Override
        public Book getItem(int position) {
            return mBooks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        @SuppressLint("InflateParams")
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.library_book_item, null);
                InjectViewUtil.inject(viewHolder, convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Book mBook = getItem(position);
            viewHolder.mTitle.setText(position + " " + mBook.getTitle());
            viewHolder.mAuthor.setText(mBook.getAuthor());
            viewHolder.mPublisher.setText(mBook.getPublisher());
            viewHolder.mPublishTime.setText(mBook.getPublishTime());
            viewHolder.mCLCIndex.setText(mBook.getCLCIndex());
            return convertView;
        }

        class ViewHolder {
            //书名
            @InjectView(R.id.library_book_query_item_title)
            TextView mTitle;
            //作者
            @InjectView(R.id.library_book_query_item_author)
            TextView mAuthor;
            //出版社
            @InjectView(R.id.library_book_query_item_publisher)
            TextView mPublisher;
            //出版时间
            @InjectView(R.id.library_book_query_item_publishetime)
            TextView mPublishTime;
            //索书号
            @InjectView(R.id.library_book_query_item_clcindex)
            TextView mCLCIndex;
        }
    }
}
