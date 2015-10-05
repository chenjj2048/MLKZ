package lib.clsUtils;

import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.Date;

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
 * 返回类似几天前等字样
 */
public class timeUtil {
    private final static Date now = new Date();

    /**
     * 2015-01-01
     */
    public static String getDeltaDate(String dateParameter) {
        Date date = clsParse.parseDate(dateParameter);
        if (date == null) return "";

        now.setTime(System.currentTimeMillis());
        final long now_time = now.getTime();
        final long para_time = date.getTime();
        long delta = (now_time - para_time) / 1000 / 86400;

        String postfix = now_time > para_time ? "前" : "后";

        delta = Math.abs(delta);

        if (delta == 0)
            if (now_time > para_time)
                return "今天";
            else
                return "明天";

        if (delta == 1 && postfix.equals("前")) return "昨天";

        if (delta <= 30 && postfix.equals("前")) return delta + "天" + postfix;
        if (delta <= 30 && postfix.equals("后")) return (delta + 1) + "天" + postfix;

        delta /= 30;
        if (delta < 12) return delta + "个月" + postfix;
        delta /= 12;
        return delta + "年" + postfix;
    }

    /**
     * 返回与当前时间的差值
     *
     * @param newsDate 时间字符串（2015-06-19或2015-01-01 08:42形式）
     * @return 返回如"几天前"
     */
    public static String getDeltaTime(String newsDate) {
        try {
            SimpleDateFormat s;
            if (newsDate.contains(" ")) {
                s = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            } else {
                s = new SimpleDateFormat("yyyy-MM-dd");
            }

            Date date = s.parse(newsDate);
            Date now = new Date();
            long diff = (now.getTime() - date.getTime()) / 1000;

            if (diff < 0)
                return "";                                                            //时间是负数，就不对

            diff /= 60;                                                                         //分钟数
            if (diff < 60) return diff + "分钟前";

            diff /= 60;                                                                         //小时数
            if (diff < 24) return diff + "小时前";

            diff /= 24;                                                                         //天数
            if (diff <= 30) return diff + "天前";

            diff /= 30;                                                                         //月数
            if (diff < 12) return diff + "个月前";

            diff /= 12;                                                                         //年数
            return diff + "年前";
        } catch (Exception e) {
            abstract_LogUtil.e("[时间解析]", e.toString());
            e.printStackTrace();
            return "";
        }
    }

    public static class clsParse {
        /**
         * 2015-01-01
         */
        public static Date parseDate(String dateParameter) {
            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
            Date date = null;
            try {
                date = s.parse(dateParameter);
            } catch (Exception e) {
                abstract_LogUtil.e("[时间解析]", e.toString());
            }
            return date;
        }
    }

    /**
     * 获取当前时间
     */
    public static class now {
        public static int getYear() {
            Time t = new Time();
            t.setToNow();
            return t.year;
        }

        public static int getMonth() {
            Time t = new Time();
            t.setToNow();
            return t.month + 1;     //这个必须加1，会差一个月
        }

        public static int getMonthDay() {
            Time t = new Time();
            t.setToNow();
            return t.monthDay;
        }

        public static int getHour() {
            Time t = new Time();
            t.setToNow();
            return t.hour;
        }
    }
}
