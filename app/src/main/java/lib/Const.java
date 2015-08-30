package lib;

import android.os.Environment;

import java.io.File;

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
    public final static String bbs = "http://bbs.ecust.edu.cn";                     //梅陇客栈

    //工厂模式
    public static class PathFactory {
        //包名
        private final static String packageName = "cjj.ecust.helper";
        //默认路径名称
        private final static String packagePath = Environment.getExternalStorageDirectory().getPath() +
                "/" + packageName;

        //获取文件的保存路径
        public static String getFileSavedPath(PathType name) {
            String result = "";
            switch (name) {
                case PACKAGE_PATH:
                    result = packagePath;
                    break;
                case LECTURE_DATABASE:
                    result = Const.PathFactory.getSQLDataBaseStoragePath() + "Lecture/DataBase/lecture.db";
                    break;
                case NEWS_DATABASE:
                    result = Const.PathFactory.getSQLDataBaseStoragePath() + "News/DataBase/news.db";
                    break;
                case NEWS_DETAIL_MESSAGE:
                    result = packagePath + "/NEWS/ContentCache/";
                    break;
                case MLKZ_HOMEPAGE_SERIAL_OBJECT:
                    result = packagePath + "/MLKZ/mlkz_home.obj";
                    break;
            }

            //保证路径一定存在
            File file = new File(result);
            if (result.endsWith("/"))
                file.mkdirs();      //本身是目录就创建
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();          //先创建多层文件夹
            }

            return result;
        }

        //获取SQL数据库存放位置
        private static String getSQLDataBaseStoragePath() {
            final boolean saved_in_SDCard = true;
            String result;
            if (saved_in_SDCard) {
                //数据库存储地址(存储卡)
                result = Environment.getExternalStorageDirectory().getPath() + "/" + packageName + "/";
            } else {
                //数据库存储地址（应用内）
                result = "";
            }
            return result;
        }

        //文件存储的位置标识
        public enum PathType {
            PACKAGE_PATH,
            MLKZ_HOMEPAGE_SERIAL_OBJECT,
            LECTURE_DATABASE,
            NEWS_DATABASE, NEWS_DETAIL_MESSAGE
        }
    }
}

