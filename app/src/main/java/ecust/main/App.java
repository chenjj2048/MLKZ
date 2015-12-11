package ecust.main;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import utils.clsNetworkChangeReceiver;
import utils.logUtils.abstract_LogUtil;

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
 * Created by 彩笔怪盗基德 on 2015/6/15
 * Copyright (C) 2015 彩笔怪盗基德
 */
public class App extends Application {
    //todo:
    public final static clsNetworkChangeReceiver receiver = new clsNetworkChangeReceiver();    //网络消息广播
    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();

        //加载网络广播监听
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
        abstract_LogUtil.i(this, "网络广播监听器已开启");

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //注销广播
        App.getContext().unregisterReceiver(receiver);
        abstract_LogUtil.i(this, "网络广播监听器已关闭");
    }
}
