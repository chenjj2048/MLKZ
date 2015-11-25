package lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.widget.Toast;

import ecust.main.clsApplication;
import lib.logUtils.abstract_LogUtil;

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
 * 广播接收，监听网络变化
 */
public class clsNetworkChangeReceiver extends BroadcastReceiver {
    private OnNetworkStateChangeListener networkStateChangeListener;
    private boolean webConnected = false;        //网络是否可连

    /**
     * 返回网络是否可用
     */
    public boolean isWebConnected() {
        return webConnected;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

            //判断是否网络可用
            webConnected = isWebOK(wifiState, mobileState);
        } catch (Exception e) {
            //没有网络权限
            //AndroidManifest大小写有错误也会报错的！！！
            webConnected = false;
            abstract_LogUtil.e(this, e.toString());
        } finally {
            //回调消息
            if (this.networkStateChangeListener != null)
                this.networkStateChangeListener.onNetworkStateChangeCompleted(webConnected);
        }
    }

    /**
     * 返回网络是否可用
     *
     * @param wifi   wifi状态
     * @param mobile mobile状态
     * @return 是否可用
     */
    private boolean isWebOK(State wifi, State mobile) {
        String strStatus;      //返回状态
        boolean result;

        if (!isStateOK(wifi) && isStateOK(mobile)) {
            strStatus = "移动网络已连接";
            showDialog();                           //显示提示消息，提醒流量资费！！！
            result = true;
        } else if (!isStateOK(wifi) && !isStateOK(mobile)) {
            strStatus = "无可用网络";
            result = false;
        } else if (isStateOK(wifi)) {
            strStatus = "Wifi已连接";
            result = true;
        } else {
            strStatus = "未知网络";
            result = false;
        }
        abstract_LogUtil.i(this, "[网络状态改变]" + strStatus);
        return result;
    }

    /**
     * 判断状态是否可用
     *
     * @param state state
     * @return 是否可用
     */
    private boolean isStateOK(State state) {
        if (state == null) return false;
        if (state == State.CONNECTED)
            return true;
        else
            return false;
    }

    /**
     * 供外部调用的方法，获取网络状态的改变
     *
     * @param listener 回调函数
     */
    public void setOnNetworkStateChange(OnNetworkStateChangeListener listener) {
        this.networkStateChangeListener = listener;
    }

    //显示网络提示消息，避免没注意，在用移动网络
    public void showDialog() {
        String msg = "您当前正在使用移动网络\r\n可能产生较大数据流量，敬请注意";
        Toast.makeText(clsApplication.getContext(), msg, Toast.LENGTH_LONG).show();
    }

    public interface OnNetworkStateChangeListener {
        void onNetworkStateChangeCompleted(boolean webConnected);
    }
}

