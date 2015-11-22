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
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import CustomWidgets.BaseAppCompatActivity;
import ecust.main.R;
import lib.InjectViewUtil;
import lib.InjectViewUtil.InjectView;

public class activity_books_query extends BaseAppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initComponents();
        setSupportActionBar(getSupportToolBar(this));
    }

    @SuppressLint("InflateParams")
    private void initComponents() {
        //加载布局
        View mParentView = getLayoutInflater().inflate(R.layout.activity_library_books_query, null);
        setContentView(mParentView);
        InjectViewUtil.inject(this, mParentView);

        //设置下拉选框
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new String[]{"徐汇奉贤", "徐汇校区", "奉贤校区"});
        mLocationSpinner.setAdapter(mAdapter);
        mLocationSpinner.setGravity(Gravity.RIGHT);

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
    }

    /**
     * 书目查找
     */
    private class SearchBooks implements View.OnClickListener, BooksQueryOkHttpPost.OnQuery {
        public SearchBooks() {
        }

        @Override
        public void onClick(View v) {
            //新建一个请求
            BooksQueryRequest mBooksQueryRequest = new BooksQueryRequest.Builder()
                    .title(mTitle.getText().toString())
                    .author(mAuthor.getText().toString())
                    .publisher(mPublisher.getText().toString())
                    .location(mLocationSpinner.getSelectedItem().toString())
                    .build();

            //发送请求
            new BooksQueryOkHttpPost(activity_books_query.this, mBooksQueryRequest, this).start();
        }

        @Override
        public void OnQueryFinish(String result) {
            TextView tv = (TextView) findViewById(R.id.library_book_query_text);
            tv.setText(result);
        }

        @Override
        public void OnQueryFailure() {

        }
    }
}
