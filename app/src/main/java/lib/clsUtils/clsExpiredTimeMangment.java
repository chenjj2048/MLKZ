package lib.clsUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

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
 * Created by 彩笔怪盗基德 on 2015/8/2
 * Copyright (C) 2015 彩笔怪盗基德
 */

/**
 * 用Preferences存储一些小型数据
 * 如果已有，并未过期，则读取出来
 * 没有或已过期，则返回默认数据
 * 注意：此类的过期时间并非十分严格，跨年、跨月都会返回新数据，要求不高时使用即可,仅更改月份年份无效
 * 暂时仅支持int,后续要用时再添加新的类型
 */
public class clsExpiredTimeMangment {
    int expireHours = 8;      //数据默认8小时才过期
    SharedPreferences sp;       //SharedPreferences
    String tag = "";             //标示，用来区分Fragment中哪一个

    public clsExpiredTimeMangment(Activity activity) {
        this.sp = activity.getPreferences(Context.MODE_PRIVATE);
    }

    public clsExpiredTimeMangment(String tableName) {
        this.sp = clsApplication.getContext().getSharedPreferences(tableName, Context.MODE_PRIVATE);
        this.tag = "-" + tableName;
    }

    public clsExpiredTimeMangment setExpireHours(int expireHours) {
        this.expireHours = expireHours;
        return this;
    }

    /**
     * 获取最近的记录
     * 判断是否过期,是否要从网络加载
     * 对应下一页的数值(nextPage变量)
     *
     * @param key          参数名称
     * @param defaultValue 默认返回值
     * @return 返回值
     */
    public int getInt(String key, int defaultValue) {
        //获取记录时间
        final int saved_year = sp.getInt("year", 0);
        final int saved_month = sp.getInt("month", 0);
        final int saved_day = sp.getInt("day", 0);
        final int saved_hour = sp.getInt("hour", 0);

        //转成小时数
        final int hours_saved = saved_day * 24 + saved_hour;
        final int hours_current = timeUtil.now.getMonthDay() * 24 + timeUtil.now.getHour();

        int value;
        //如果时差超过n小时,就允许加载新的数据
        if (saved_year != timeUtil.now.getYear() || saved_month != timeUtil.now.getMonth()) {
            value = defaultValue;
        } else if (Math.abs(hours_saved - hours_current) <= expireHours) {
            value = sp.getInt(key, defaultValue);    //返回一个数值
        } else {
            value = defaultValue;
        }

        logUtil.i(this, "[SharedPreference过期管理][GET" + this.tag + "]" + key + "=" + value +
                " 上次记录时间：" + saved_year + "-" + saved_month + "-" + saved_day + " " + saved_hour + "时");
        return value;
    }

    /**
     * 数据存储
     * 数据是否过期，是否需要更新，交由调用程序判断
     * 这里只管存储
     */
    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("year", timeUtil.now.getYear());//记录当前的时间（年份）
        editor.putInt("month", timeUtil.now.getMonth());     //记录当前的时间（月份）
        editor.putInt("day", timeUtil.now.getMonthDay());       //记录当前的时间（天）
        editor.putInt("hour", timeUtil.now.getHour());     //记录当前的时间（小时）
        editor.putInt(key, value);
        logUtil.i(this, "[SharedPreference过期管理][SET" + this.tag + "]" + key + "=" + value);
        editor.apply();
    }
}
