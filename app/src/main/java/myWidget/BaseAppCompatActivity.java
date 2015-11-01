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
 * Created by 彩笔怪盗基德 on 2015/10/23
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package myWidget;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import ecust.main.R;
import statistics.clsUmeng;

/**
 * 所有Activity都继承这里
 */
public class BaseAppCompatActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * 获取ToolBar
     */
    public Toolbar getSupportToolBar(Activity mActivity) {
        this.mActivity = mActivity;

        if (mToolBar != null)
            return mToolBar;

        mToolBar = (Toolbar) mActivity.findViewById(R.id.toolbar);
        if (mToolBar == null)
            throw new NullPointerException("ToolBar你没设置么？");
        else {
            mToolBar.setBackgroundResource(R.color.blue_i_like);
            mToolBar.setTitleTextColor(Color.argb(240, 255, 255, 255));
            setSupportActionBar(mToolBar);
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            return mToolBar;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && mActivity != null) {
            mActivity.onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //统计每一个页面情况
        clsUmeng.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //统计每一个页面情况
        clsUmeng.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
