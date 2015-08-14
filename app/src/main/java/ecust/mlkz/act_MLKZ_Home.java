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
 * Created by 彩笔怪盗基德 on 2015/8/12
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.mlkz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import ecust.main.R;
import ecust.main.act_MainActivity;
import lib.BaseActivity.MyBaseFragmentActivity;
import lib.clsGlobal.Global;

public class act_MLKZ_Home extends MyBaseFragmentActivity {
    private FragmentManager mFragmentManager;
    private fragment_MLKZ_HomePage fragment_mlkz_homePage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mlkz_home_frame);           //加载梅陇客栈 主框架布局

        fragment_mlkz_homePage = new fragment_MLKZ_HomePage();       //主布局

        //Fragment管理
        mFragmentManager = getSupportFragmentManager();

        FragmentTransaction trans = mFragmentManager.beginTransaction();
        //替换主体部分为Fragment
        trans.replace(R.id.mlkz_home_slidingmenu_body, fragment_mlkz_homePage, null);

        trans.commit();

        Global.setTitle(this, "梅陇客栈");
    }

    //回退到主页
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent homePage = new Intent(this, act_MainActivity.class);
        startActivity(homePage);
    }
}
