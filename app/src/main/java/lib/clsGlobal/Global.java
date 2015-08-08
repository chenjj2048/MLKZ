package lib.clsGlobal;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

import ecust.main.R;

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
public class Global {
    //HttpAccess类中常量
    public static final int action_mlkz_loginpage = 1;
    public static final int action_mlkz_homepage = 2;
    public static final int action_timeout = 3;
    //SharedPreferences配置信息
    public static final String sp_Config = "config";
    public static final String sp_Username = "username";
    public static final String sp_Password = "password";
    public static final String sp_Cookie = "cookie";

    private static final boolean isDebug = true;                                                           //Debug、Releas标签
    private static final boolean showActivityThreadChangeLog = false;
    public static String bbs = "http://bbs.ecust.edu.cn";
    //公共的Activity
    public static Activity activity;
    private static int count_of_thread;
    private static String lastToastMsg = "";
    private static long lastToastTime;

    /**
     * 保证Toast不会短时间内产生大量消息
     */
    public static void toastMsg(String str) {
        final long diffSec = 2000;                                                                  //Toast时间间隔

        if (str == null) return;
        if (str.equals("")) return;

        long nowTime = new Date().getTime();

        if (nowTime - lastToastTime <= diffSec && str.equals(lastToastMsg)) return;

        lastToastMsg = str;
        lastToastTime = nowTime;
        Toast.makeText(clsApplication.getContext(), str, Toast.LENGTH_SHORT).show();
    }

    public static void addThread() {
        count_of_thread++;
        if (showActivityThreadChangeLog)
            log("[线程增加]thread=" + count_of_thread);
    }

    public static void deleteThread() {
        count_of_thread--;
        if (showActivityThreadChangeLog)
            log("[线程减少]thread=" + count_of_thread);
    }

    public static void log(String msg) {
        String tag = "";

        if (isDebug == false) return;

        if (clsActivity.currentVisibleActivity != null) {
            tag = clsActivity.currentVisibleActivity.toString();
        }

        if (msg.contains("Exception")) {
            Log.e(tag, msg);
        } else {
            Log.i(tag, msg);
        }
    }

    /**
     * 设置标题栏title
     */
    public static void setTitle(Activity v, String title) {
        TextView tv = (TextView) v.findViewById(R.id.title_bar);
        tv.setText(title);
    }

    // 字符串转换为ASCII码   
    public static String String2Ascii(String s) {
        StringBuilder sb = new StringBuilder();
        char[] chars = s.toCharArray(); // 把字符中转换为字符数组   
        for (int i = 0; i < chars.length; i++) {
            sb.append(chars[i] + " " + (int) chars[i] + " ");
        }
        String result = sb.toString();
        Global.log(result);
        return result;
    }

    /**
     * 产生唯一的EventBus事件ID
     */
    public static class eventBus {
        private static long uniqueEventBusID = 0;

        //获取唯一的EventID
        public static long getID() {
            return uniqueEventBusID++;
        }
    }

    /**
     * 尺寸转换类
     */
    public static class dimenConvert {
        /**
         * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
         */
        public static int dip2px(float dp) {
            Context context = clsApplication.getContext();
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dp * scale + 0.5f);
        }

        /**
         * 根据手机的分辨率从 px 转成为 dp
         */
        public static int px2dip(int px) {
            Context context = clsApplication.getContext();
            float scale = context.getResources().getDisplayMetrics().density;
            return (int) (px / scale + 0.5f * (px >= 0 ? 1 : -1));
        }
    }

    /**
     * 管理页面的开启关闭
     */
    public static class clsActivity {

        public static Activity currentVisibleActivity;                                             //当前可见的Activity
        private static int activity_exist_count = 0;                                                //存在页面数量

        //页面加载
        public static void onCreate(Activity v) {

            activity_exist_count++;
            v.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            logUtil.i("global","[onCreate][页面数量=" + activity_exist_count + "]" + v.getClass().getCanonicalName());
            System.gc();
        }

        //页面销毁
        public static void onDestory(Activity v) {
            activity_exist_count--;
//            logUtil.i("global","[onDestory][页面数量=" + activity_exist_count + "]" + v.getClass().getCanonicalName());
            System.gc();
        }

        public static void onResume(Activity v) {
            currentVisibleActivity = v;
//            Global.i("[onResume][页面数量=" + activity_exist_count + "]" + v.getClass().getCanonicalName());
        }

        public static void onPause(Activity v) {
//            Global.i("[onPause][页面数量=" + activity_exist_count + "]" + v.getClass().getCanonicalName());
        }

        /**
         * 判断当前程序是否在前台
         *
         * @return true or false
         */
        public static boolean isVisible() {
            Context context = clsApplication.getContext();
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.processName.equals(context.getPackageName())) {
                    if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                        Global.log("[程序正在后台]" + appProcess.processName);
                        return false;
                    } else {
                        Global.log("[程序正在前台]" + appProcess.processName);
                        return true;
                    }
                }
            }
            //可能程序已经结束，找不到了
            return true;
        }
    }
}


