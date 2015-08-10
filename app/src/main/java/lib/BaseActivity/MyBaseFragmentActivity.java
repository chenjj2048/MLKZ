package lib.BaseActivity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

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
 * Created by 彩笔怪盗基德 on 2015/7/11
 * Copyright (C) 2015 彩笔怪盗基德
 */
/**
 * 没啥用
 * 和MyBaseActivity保持一致，就是继承的类不同
 */
public class MyBaseFragmentActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Global.clsActivity.onCreate(this);
        Global.activity = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Global.clsActivity.onDestory(this);
        System.gc();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Global.clsActivity.onResume(this);
        Global.activity = this;
        System.gc();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Global.clsActivity.onPause(this);
    }
}
