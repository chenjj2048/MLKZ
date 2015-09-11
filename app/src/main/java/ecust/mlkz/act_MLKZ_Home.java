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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import ecust.main.R;
import ecust.main.act_MainActivity;
import lib.Global;

public class act_MLKZ_Home extends FragmentActivity {
    private fragment_MLKZ_LeftMenu leftMenu_Fragment;
    private fragment_MLKZ_HomePage body_Fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mlkz_home_frame);

        init();

        Global.setTitle(this, "梅陇客栈");
    }

    private void init() {
        //Fragment管理
        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction trans = mFragmentManager.beginTransaction();

        //1.设置左侧侧滑菜单Fragment
        leftMenu_Fragment = new fragment_MLKZ_LeftMenu();
        trans.replace(R.id.mlkz_home_slidingmenu_left, leftMenu_Fragment, null);

        //2.设置主体部分Fragment
        body_Fragment = new fragment_MLKZ_HomePage();
        trans.replace(R.id.mlkz_home_slidingmenu_body, body_Fragment, null);

        trans.commit();
    }

    //回退到主页
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //回首页
        Intent homePage = new Intent(this, act_MainActivity.class);
        startActivity(homePage);
    }

    //页面返回了登陆的cookie，主页面尝试用cookie登陆，会出现新版块
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;

        //拿到cookie
        String cookie = data.getStringExtra(act_MLKZ_Login.COOKIE);

        //凭cookie登陆，出现新版块
        body_Fragment.loginMLKZ(cookie);
    }
}
