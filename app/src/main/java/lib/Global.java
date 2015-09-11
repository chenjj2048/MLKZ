package lib;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import java.util.List;
import java.util.zip.CRC32;

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


    //根据url获得唯一的hash值（碰到一样的几乎不可能）
    public static String getStringHash(String url) {
        //计算CRC32值
        CRC32 crc32 = new CRC32();
        crc32.update(url.getBytes());

        String result = url.hashCode() + "" + crc32.getValue();
        result = result.replace("-", "");

        return result;
    }

    /**
     * 设置标题栏title
     */
    public static void setTitle(Activity v, String title) {
        TextView tv = (TextView) v.findViewById(R.id.title_bar);
        tv.setText(title);
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

}


