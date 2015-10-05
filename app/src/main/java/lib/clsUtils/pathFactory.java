package lib.clsUtils;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lib.clsApplication;
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
 * Created by 彩笔怪盗基德 on 2015/9/4
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

//工厂模式
public class pathFactory {
    private final static Context context = clsApplication.getContext();

    //Cache路径
    //storage/sdcard0/Android/data/cjj.ecust.helper/cache
    private final static String packageCachePath = context.getExternalCacheDir().getPath();

    //获取文件的保存路径
    public static String getFileSavedPath(PathType name) {
        String result = "";
        switch (name) {
            case LECTURE_DATABASE:
                //讲座版块数据库
                result = getSQLDataBaseStoragePath() + "lecture.db";
                break;
            case NEWS_DATABASE:
                //新闻版块数据库
                result = getSQLDataBaseStoragePath() + "news.db";
                break;
            case NEWS_DETAIL_CONTENT_CACHE:
                //新闻详细内容缓存
                result = packageCachePath + "/NEWS_Content_Cache/";
                break;
            case NEWS_DETAIL_PICTURE_CACHE:
                //新闻图片缓存
                result = packageCachePath + "/NEWS_Picture_Cache/";
                break;
            case MLKZ_HOMEPAGE_SERIAL_OBJECT:
                result = context.getExternalFilesDir("MLKZ") + "/mlkz_home.cfg";
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
            result = context.getExternalFilesDir("Sqlite").getPath() + "/";
        } else {
            //数据库存储地址（应用内）
            result = "";
        }
        return result;
    }

    /**
     * 删除路径下的缓存文件
     *
     * @param path          路径
     * @param maxFilesCount 最多N个留存
     */
    private static void cleanCacheFiles(String path, int maxFilesCount) {
        //避免误删文件，路径中必须包含cache字样
        if (!path.toLowerCase().contains("cache")) return;

        File filePath = new File(path);
        if (!filePath.exists()) return;

        File[] files = filePath.listFiles();
        if (files == null) return;

        //文件集合
        List<File> list = new ArrayList<>(100);
        for (File file : files) {
            if (file.isFile())
                list.add(file);
        }

        //按修改时间升序
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                if (lhs.lastModified() > rhs.lastModified())
                    return 1;
                else if (lhs.lastModified() < rhs.lastModified())
                    return -1;
                else
                    return 0;
            }
        });

        int clean_files_count = 0;
        //删除旧的缓存文件
        while (list.size() != 0 && list.size() > maxFilesCount) {
            File tmp_file = list.get(0);
            tmp_file.deleteOnExit();
            list.remove(tmp_file);
            clean_files_count++;
        }

        if (clean_files_count > 0) {
            String msg = "[缓存清理][最大数量=" + maxFilesCount + " 已清除=" + clean_files_count + "]" + path;
            abstract_LogUtil.i("pathFactory", msg);
        }
    }

    //文件夹下的内容会被清理
    public static void cleanCacheFiles(PathType pathType, int maxFilesCount) {
        cleanCacheFiles(getFileSavedPath(pathType), maxFilesCount);
    }

    //处理部分缓存文件
    public static void cleanCacheFiles() {
        cleanCacheFiles(PathType.NEWS_DETAIL_PICTURE_CACHE, 200);
        cleanCacheFiles(PathType.NEWS_DETAIL_CONTENT_CACHE, 100);
    }

    //文件存储的位置标识
    public enum PathType {
        MLKZ_HOMEPAGE_SERIAL_OBJECT,                //梅陇客栈
        LECTURE_DATABASE,                            //讲座版块
        NEWS_DATABASE, NEWS_DETAIL_CONTENT_CACHE, NEWS_DETAIL_PICTURE_CACHE    //新闻版块
    }
}
