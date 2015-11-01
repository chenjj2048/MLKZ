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
 * Created by 彩笔怪盗基德 on 2015/10/26
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */
package ecust.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import lib.logUtils.logUtil;
import statistics.clsUmeng;

/**
 * 启动页
 */
public class SplashWindow_1 extends Activity {
    Handler mHandler = new Handler();
    int DURATION = 1500;
    Runnable mStartRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_window_1);

        mStartRunnable = new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashWindow_1.this, act_MainActivity.class));
                overridePendingTransition(R.anim.activity_move_in_from_right, R.anim.activity_move_out_to_left);
            }
        };

        if (logUtil.isDebug)
            logUtil.toast("[Debuging] 渠道 = " + clsUmeng.getChannel());
        else if (clsUmeng.getChannel().contains("test"))
            logUtil.toast("请修改发行渠道");

        //设置渠道、APP key
        clsUmeng.init(this);
    }

    @Override
    protected void onResume() {
        super.onStart();

        mHandler.postDelayed(mStartRunnable, DURATION);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, DURATION * 2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //取消启动过程
        mHandler.removeCallbacks(mStartRunnable);
    }
}
