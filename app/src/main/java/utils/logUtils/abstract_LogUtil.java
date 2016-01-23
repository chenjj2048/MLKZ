package utils.logUtils;

import android.util.Log;
import android.widget.Toast;

import java.util.Date;

import ecust.main.App;
import ecust.main.BuildConfig;

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
@Deprecated
public abstract class abstract_LogUtil {
    //Release时是否输出日志
    public static final boolean isDebug = BuildConfig.DEBUG;

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

    public static void d(Class clazz, String msg) {
        if (msg == null) return;
        if (isDebug)
            Log.d(clazz.getCanonicalName(), msg);
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

    public static void printExceptionLog(Class<?> clazz, Exception e) {
        if (isDebug) {
            Log.e(clazz.getCanonicalName(), e.toString());
            e.printStackTrace();
        }
    }

    public static void printExceptionLog(Object obj, Exception e) {
        if (isDebug) {
            Log.e(obj.getClass().getCanonicalName(), e.toString());
            e.printStackTrace();
        }
    }
}



