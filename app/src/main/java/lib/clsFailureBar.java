package lib;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import ecust.main.R;
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

// include layout中加载栏
// 用于显示加载进度，显示加载失败消息
public class clsFailureBar implements View.OnClickListener, clsNetworkChangeReceiver.OnNetworkStateChangeListener {
    private OnWebRetryListener onWebRetryListenerListener;        //回调函数
    private ViewHolder viewHolder;
    private int currentState;

    //获取控件引用
    public clsFailureBar(Activity v) {
        viewHolder = new ViewHolder();
        viewHolder.progressBar = (ProgressBar) v.findViewById(R.id.failurebar_progressbar);
        viewHolder.failureLayout = (LinearLayout) v.findViewById(R.id.failure_msg_layout);
        viewHolder.allLayout = (LinearLayout) v.findViewById(R.id.failurebar_all);
    }

    //设置重试刷新的回调函数
    public void setOnWebRetryListener(OnWebRetryListener listener) {
        this.onWebRetryListenerListener = listener;
        //关闭所有监听
        if (listener == null)
            clsApplication.receiver.setOnNetworkStateChange(null);
    }

    /**
     * 点击刷新
     *
     * @param view view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.failurebar_all:
                if (onWebRetryListenerListener != null)
                    onWebRetryListenerListener.onWebRetryCompleted();
                break;
        }
    }

    /**
     * 来自网络广播类lib→clsNetworkChangeReceiver
     *
     * @param webConnected 网络是否可连
     */
    @Override
    public void onNetworkStateChangeCompleted(boolean webConnected) {
        //收到网络状态改变消息
        if (currentState != State.Failure) return;

        //无可用网络则返回
        if (!webConnected) return;

        //寻找是否有重试刷新界面
        if (viewHolder.allLayout != null) {
            //发送重试消息
            if (onWebRetryListenerListener != null)
                onWebRetryListenerListener.onWebRetryCompleted();
            abstract_LogUtil.i(this, "网络可用，重试刷新");
        }
    }

    //加载失败
    public void setStateFailure() {
        if (currentState == State.Succeed) return;

        currentState = State.Failure;

        viewHolder.allLayout.setVisibility(View.VISIBLE);
        viewHolder.failureLayout.setVisibility(View.VISIBLE);  //显示失败信息
        viewHolder.progressBar.setVisibility(View.GONE);
        viewHolder.allLayout.setOnClickListener(this);      //监听点击刷新事件

        //设置需要监听的网络状态改变，网络改变可以通知重新加载
        clsApplication.receiver.setOnNetworkStateChange(this);
    }

    //加载成功
    public void setStateSucceed() {
        currentState = State.Succeed;

        if (viewHolder != null) {
            viewHolder.allLayout.setOnClickListener(null);
            viewHolder.allLayout.setVisibility(View.GONE);     //隐藏失败布局，显示正常界面
        }
        this.onWebRetryListenerListener = null;
        this.viewHolder = null;

        //释放监听
        clsApplication.receiver.setOnNetworkStateChange(null);
    }

    //加载中
    public void setStateLoading() {
        if (currentState == State.Succeed) return;

        currentState = State.Loading;

        viewHolder.allLayout.setVisibility(View.VISIBLE);
        viewHolder.progressBar.setVisibility(View.VISIBLE);     //显示旋转的进度条
        viewHolder.failureLayout.setVisibility(View.GONE);
        viewHolder.allLayout.setOnClickListener(null);
    }

    //接口，供Activity调用
    public interface OnWebRetryListener {
        void onWebRetryCompleted();
    }

    private class State {
        public static final int Loading = 1;
        public static final int Failure = 2;
        public static final int Succeed = 3;
    }

    private class ViewHolder {
        public ProgressBar progressBar;    //旋转的进度条
        public LinearLayout failureLayout; //显示的失败信息
        public LinearLayout allLayout;     //失败栏的全部布局（含以上两部分）
    }
}
