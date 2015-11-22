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

package statistics;

import android.content.Context;
import android.text.TextUtils;

import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

import ecust.lecture.activity_Lecture_Catalog;
import ecust.lecture.act_Lecture_Detail;
import ecust.main.BuildConfig;
import ecust.news.activity_News_Catalog;
import ecust.news.act_News_Detail;
import lib.SecretKey;
import lib.logUtils.logUtil;

/**
 * 友盟统计
 */
@SuppressWarnings("all")
public class clsUmeng {
    //onEvent事件
    public static final String EVENT_NEWS_CATALOG = "News_Catalog";
    public static final String EVENT_LECTURE_CATALOG = "Lecture_Catalog";
    private static final String EVENT_NEWS_DETAIL = "News_Detail";
    private static final String EVENT_LECTURE_DETAIL = "Lecture_Detail";

    //友盟统计是否启用
    public static boolean isEnable() {
        return false;
    }

    /**
     * 获得应用渠道
     */
    public static String getChannel() {
        String channel = null;
        //360手机市场
//        channel = "360 Market";

        if (BuildConfig.DEBUG || TextUtils.isEmpty(channel))
            channel = "test Channel";

        return channel;
    }

    /**
     * 设置APP_KEY和渠道
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        if (!isEnable()) return;
        AnalyticsConfig.setAppkey(context, SecretKey.get_UMENG_KEY());
        AnalyticsConfig.setChannel(getChannel());
    }

    /**
     * 日志是否加密
     */
    public static void enableEncrypt(boolean enable) {
        if (!isEnable()) return;
        AnalyticsConfig.enableEncrypt(enable);
    }

    /**
     * 强退程序时使用，保存相关日志
     */
    public static void onKillProcess(Context context) {
        if (!isEnable()) return;
        MobclickAgent.onKillProcess(context);
    }

    /**
     * 页面统计
     */
    public static void onResume(Context context) {
        if (!isEnable()) return;
        MobclickAgent.onResume(context);
    }

    /**
     * 页面统计
     */
    public static void onPause(Context context) {
        if (!isEnable()) return;
        MobclickAgent.onPause(context);
    }

    /**
     * 统计计数事件
     *
     * @param context 根据上下文，确定事件
     */
    public static void onEvent(Context context) {
        if (!isEnable()) return;
        String event;
        if (context instanceof activity_News_Catalog) {
            event = EVENT_NEWS_CATALOG;
        } else if (context instanceof act_News_Detail) {
            event = EVENT_NEWS_DETAIL;
        } else if (context instanceof activity_Lecture_Catalog) {
            event = EVENT_LECTURE_CATALOG;
        } else if (context instanceof act_Lecture_Detail) {
            event = EVENT_LECTURE_DETAIL;
        } else {
            logUtil.throwException("clsUmeng - onEvent 中未发现指定事件");
            return;
        }
        MobclickAgent.onEvent(context, event);
    }
}
