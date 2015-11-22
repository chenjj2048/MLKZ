package ecust.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import CustomWidgets.BaseAppCompatActivity;
import ecust.demotest.testDemoActivity;
import ecust.lecture.activity_Lecture_Catalog;
import ecust.library.activity_library;
import ecust.mlkz.homePage.activity_MLKZ_Home;
import ecust.news.activity_News_Catalog;
import ecust.school_calendar.activity_School_Calendar;
import lib.clsUtils.pathFactory;
import statistics.clsUmeng;

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
public class act_MainActivity extends BaseAppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //初始化组件
        initComponents();
    }

    /**
     * 绑定clickListener
     */
    private void initComponents() {
        Button mMLKZ = (Button) findViewById(R.id.mainActivity_MLKZ);
        Button mNews = (Button) findViewById(R.id.mainActivity_News);
        Button mLecture = (Button) findViewById(R.id.mainActivity_Lecture);
        Button mTest = (Button) findViewById(R.id.mainActivity_Test);
        Button mCalendar = (Button) findViewById(R.id.mainActivity_SchoolCalendar);
        View mLibrary = findViewById(R.id.mainActivity_Library);

        mMLKZ.setOnClickListener(this);
        mNews.setOnClickListener(this);
        mLecture.setOnClickListener(this);
        mTest.setOnClickListener(this);
        mCalendar.setOnClickListener(this);
        mLibrary.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //梅陇客栈
            case R.id.mainActivity_MLKZ:
                startActivity(new Intent(this, activity_MLKZ_Home.class));
                break;
            //华理新闻
            case R.id.mainActivity_News:
                startActivity(new Intent(this, activity_News_Catalog.class));
                break;
            //讲座信息
            case R.id.mainActivity_Lecture:
                startActivity(new Intent(this, activity_Lecture_Catalog.class));
                break;
            //校历
            case R.id.mainActivity_SchoolCalendar:
                startActivity(new Intent(this, activity_School_Calendar.class));
                break;
            //图书馆
            case R.id.mainActivity_Library:
                startActivity(new Intent(this, activity_library.class));
                break;
            //测试
            case R.id.mainActivity_Test:
                startActivity(new Intent(this, testDemoActivity.class));
                break;
        }
    }

    //Todo:改
    @Override
    public void onBackPressed() {
        //询问是否返回
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("确定退出应用？");
        dialog.setNegativeButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clsUmeng.onKillProcess(act_MainActivity.this);
                System.exit(0);        //进程通通杀死，清理内存
            }
        });
        dialog.setCancelable(false);
        dialog.setPositiveButton("否", null);
        dialog.show();

        //删除部分缓存
        pathFactory.cleanCacheFiles();
    }
}
