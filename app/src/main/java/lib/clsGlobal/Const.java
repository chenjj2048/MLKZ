package lib.clsGlobal;

import android.os.Environment;

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
 * 静态常量.
 */
public class Const {
    public final static String news_url = "http://news.ecust.edu.cn";
    public final static String lecture_url = "http://news.ecust.edu.cn/reports";   //讲座信息主目录
    public final static String packageName = "cjj.ecust.helper";

    //    public final static String storagePath = "";          //数据库存储地址（应用内）
    public final static String storagePath = Environment.getExternalStorageDirectory().getPath() +
            "/" + Const.packageName + "/";          //数据库存储地址(存储卡)

}
