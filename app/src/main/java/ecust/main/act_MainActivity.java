package ecust.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ecust.demotest.testDemoActivity;
import ecust.lecture.act_Lecture_Catalog;
import ecust.mlkz.act_MLKZ_HomePage;
import ecust.news.act_News_Catalog;
import lib.BaseActivity.MyBaseActivity;
import lib.clsGlobal.logUtil;
import lib.clsGlobal.timeUtil;
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
 * Created by 彩笔怪盗基德 on 2015/5/20
 * Copyright (C) 2015 彩笔怪盗基德
 */
public class act_MainActivity extends MyBaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //初始化组件
        initComponents();

        //仅供测试使用
        final boolean demotest = false;
        if (demotest)
            startActivity(new Intent(this, testDemoActivity.class));
    }

    private void initComponents() {
        Button mlkz = (Button) findViewById(R.id.mainActivity_MLKZ);
        Button news = (Button) findViewById(R.id.mainActivity_News);
        Button lecture = (Button) findViewById(R.id.mainActivity_Lecture);

        mlkz.setOnClickListener(this);
        news.setOnClickListener(this);
        lecture.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //梅陇客栈
            case R.id.mainActivity_MLKZ:
                startActivity(new Intent(this, act_MLKZ_HomePage.class));
                finish();
                break;
            //华理新闻
            case R.id.mainActivity_News:
                startActivity(new Intent(this, act_News_Catalog.class));
                finish();
                break;
            //讲座信息
            case R.id.mainActivity_Lecture:
                startActivity(new Intent(this, act_Lecture_Catalog.class));
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //通通杀死，清理内存
        System.exit(0);
    }
}