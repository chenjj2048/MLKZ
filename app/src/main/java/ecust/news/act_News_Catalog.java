package ecust.news;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

import ecust.main.R;
import ecust.main.act_MainActivity;
import lib.BaseActivity.MyBaseFragmentActivity;
import lib.clsGlobal.Global;

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
 * Created by 彩笔怪盗基德 on 2015/6/30
 * Copyright (C) 2015 彩笔怪盗基德
 */
public class act_News_Catalog extends MyBaseFragmentActivity implements ViewPager.OnPageChangeListener {
    private int currentFragmentPosition = 0;        //当前Fragment的索引值
    private List<Fragment> mFragments = new ArrayList<>();   //数据集
    private final FragmentPagerAdapter mFragmentAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        //返回ViewPager中标题
        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getStringArray(R.array.news_section_name)[position];
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    };


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        //滚动后加载新闻数据
        //不这样（写Fragment的creat,show），总是会加载临近的页面，而不加载当前页面
        currentFragmentPosition = position;

        //遍历设置Fragment是否可见
        for (int i = 0; i < mFragments.size(); i++) {
            fragment_News_Catalog fragment = (fragment_News_Catalog) mFragments.get(i);
            if (i != currentFragmentPosition) {
                fragment.setSelected(false);        //界面不可见
            } else {
                fragment.setSelected(true);         //界面可见
            }

            //加载邻近页面
            if (currentFragmentPosition == i - 1 || currentFragmentPosition == i + 1)
                fragment.initView();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_catalog);

        if (savedInstanceState != null)
            currentFragmentPosition = savedInstanceState.getInt("para1");

        Global.setTitle(this, "华理新闻");
        initComponents();
    }

    /**
     * 初始化
     */
    private void initComponents() {
        //取得分类的标题
        String[] catalogArray = getResources().getStringArray(R.array.news_section_name);
        //取得分类的URL首页地址
        String[] urlArray = getResources().getStringArray(R.array.news_section_url);

        //新建多个新闻Fragment
        for (int i = 0; i < catalogArray.length; i++) {
            //加载每个Fragment界面
            fragment_News_Catalog mFragment_News = new fragment_News_Catalog();
            mFragment_News.setParameter(catalogArray[i], urlArray[i]);
            //List新增Fragment数据源
            mFragments.add(mFragment_News);
        }

        //Set the pager with an adapter
        ViewPager wViewPager = (ViewPager) findViewById(R.id.news_Catalog_Viewpager);
        wViewPager.setAdapter(mFragmentAdapter);

        //Bind the title indicator to the adapter
        TabPageIndicator tabIndicator = (TabPageIndicator) findViewById(R.id.news_Catalog_TabIndicator);
        tabIndicator.setViewPager(wViewPager);
        tabIndicator.setOnPageChangeListener(this);

        //设置要加载的Fragment位置(有可能被销毁，因此不一定每次都是第一个)
        tabIndicator.setCurrentItem(currentFragmentPosition);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //跳回至主界面
        startActivity(new Intent(this, act_MainActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //初始化要加载的Fragment
        onPageSelected(currentFragmentPosition);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //注释掉下面的调用，保证fragment一起被销毁
//        super.onSaveInstanceState(outState);
        outState.putInt("para1", currentFragmentPosition);
    }
}


