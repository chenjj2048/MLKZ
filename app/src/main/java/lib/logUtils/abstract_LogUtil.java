package lib.logUtils;

import android.util.Log;
import android.widget.Toast;

import java.util.Date;

import ecust.main.BuildConfig;
import lib.clsApplication;

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
 * 这个专门作为静态类
 * <p/>
 * 用于输出Log及Toast消息.
 * ============================================
 * log调用方法
 * abstract_LogUtil.log(this,"消息1")
 * abstract_LogUtil.log("标签","消息2")
 * ============================================
 * toast调用方法:
 * abstract_LogUtil.toast("消息");
 * ============================================
 */
@Deprecated
public abstract class abstract_LogUtil {
    //Release时是否输出日志
    public static final boolean isDebug = BuildConfig.DEBUG;
    //Toast时间间隔(重复消息的最短间隔)
    private static final long DeltaSeconds = 2000;

    //最后的toast时间
    private static long lastToastTime;
    //最后toast的消息
    private static String lastToastMsg = "";

    /**
     * 断言
     * android studio里assert不知道怎么就不起作用，自己写个输出日志
     */
    @SuppressWarnings("deprecation")
    public static void Assert(Object object, boolean condition, String errorMessage) {
        if (isDebug && !condition) {
            abstract_LogUtil.w(object.getClass().getCanonicalName(), errorMessage);
        }
    }

    /**
     * ============================================================
     * 保证Toast不会短时间内产生大量消息
     * ============================================================
     *
     * @param msg 内容
     */
    public static void toast(String msg) {
        if (msg == null || msg.equals("")) return;

        //取得当前时间
        long nowTime = new Date().getTime();

        //同样消息，太过频繁就退出
        if (nowTime - lastToastTime <= DeltaSeconds && msg.equals(lastToastMsg)) return;

        lastToastMsg = msg;
        lastToastTime = nowTime;
        Toast.makeText(clsApplication.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Deprecated
    public static void v(Object object, String msg) {
        if (msg == null) return;
        if (isDebug)
            Log.v(object.getClass().getCanonicalName(), msg);
    }

    @Deprecated
    public static void v(String tag, String msg) {
        if (msg == null) return;
        if (isDebug)
            Log.v(tag, msg);
    }

    @Deprecated
    public static void d(Object object, String msg) {
        if (msg == null) return;
        if (isDebug)
            Log.d(object.getClass().getCanonicalName(), msg);
    }

    @Deprecated
    public static void d(String tag, String msg) {
        if (msg == null) return;
        if (isDebug)
            Log.d(tag, msg);
    }

    @Deprecated
    public static void i(Object object, String msg) {
        if (msg == null) return;
        if (isDebug)
            Log.i(object.getClass().getCanonicalName(), msg);
    }

    @Deprecated
    public static void i(String tag, String msg) {
        if (msg == null) return;
        if (isDebug)
            Log.i(tag, msg);
    }

    @Deprecated
    public static void w(Object object, String msg) {
        if (msg == null) return;
        if (isDebug)
            Log.w(object.getClass().getCanonicalName(), msg);
    }

    @Deprecated
    public static void w(String tag, String msg) {
        if (msg == null) return;
        if (isDebug)
            Log.w(tag, msg);
    }

    @Deprecated
    public static void e(Object object, String msg) {
        if (msg == null) return;
        if (isDebug)
            Log.e(object.getClass().getCanonicalName(), msg);
    }

    @Deprecated
    public static void e(String tag, String msg) {
        if (msg == null) return;
        if (isDebug)
            Log.e(tag, msg);
    }

    @SuppressWarnings("unused")
    public static void printException(Object object, Exception e) {
        if (isDebug) {
            Log.e(object.getClass().getCanonicalName(), e.toString());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public static void printException(String tag, Exception e) {
        if (isDebug) {
            Log.e(tag, e.toString());
            e.printStackTrace();
        }
    }
}



