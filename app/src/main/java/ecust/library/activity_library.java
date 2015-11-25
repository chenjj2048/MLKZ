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
package ecust.library;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import CustomWidgets.BaseAppCompatActivity;
import ecust.library.books_query.activity_books_query;
import ecust.main.R;

public class activity_library extends BaseAppCompatActivity {
    //Todo:图书查询、借还信息查询、座位查询、续借、索书号排除、登陆

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library);

        initComponents();
    }

    /**
     * 初始化OnClickLister
     */
    private void initComponents() {
        SectionClickListener mClickListener = new SectionClickListener();
        findViewById(R.id.library_book_query).setOnClickListener(mClickListener);

    }

    /**
     * 版块被点击
     */
    private class SectionClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.library_book_query:
                    startActivity(new Intent(activity_library.this, activity_books_query.class));
                    break;
                default:
                    //这个View没有做过setOnClickListener啊！
            }
        }
    }
}
