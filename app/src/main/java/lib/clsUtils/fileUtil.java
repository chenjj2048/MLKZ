package lib.clsUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lib.Const;
import lib.Const.PathFactory.PathType;

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
 * Created by 彩笔怪盗基德 on 2015/8/9
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */


///////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////
//                   缓存文件需要被删掉！！！！！
///////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////

//用来管理缓存文件
public class fileUtil {
    final static String cacheDirectory = "/Cache/";

    /**
     * 存储缓存文件
     *
     * @param cacheCatalog 缓存的目录，区分不同目录版块
     * @param fileName     文件名
     * @param bytes        字节流
     */
    public static void saveCacheFile(String cacheCatalog, String fileName, byte[] bytes) {
        if (fileName.contains("/") || !fileName.contains("."))
            throw new NullPointerException();       //提个醒，是否错传了URL，应该是文件名

        FileOutputStream outputStream = null;
        try {
            //保存位置
            String path = cacheDirectory + cacheCatalog;         //放到缓存里
            File file = new File(Const.PathFactory.getFileSavedPath(PathType.PACKAGE_PATH) + path, fileName);     //设置文件地址
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();          //先创建多层文件夹

            //写入文件
            outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
            outputStream.flush();

            logUtil.i("缓存文件保存", "[缓存文件保存成功 - " + getIntegearFormat(bytes.length) + " 字节]" + file.toString());
        } catch (Exception e) {
            logUtil.e("文件读取错误", e.toString());
            e.printStackTrace();
        } finally {
            //关闭连接
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
                logUtil.e("文件关闭错误", e.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * 从缓存中读取出文件字节流
     *
     * @param cacheCatalog 缓存目录名称
     * @param fileName     文件名
     * @return 字节流
     */
    public static byte[] getCacheFile(String cacheCatalog, String fileName) {
        if (fileName.contains("/") || !fileName.contains("."))
            throw new NullPointerException();       //提个醒，是否错传了URL，应该是文件名

        //保存位置
        String path = cacheDirectory + cacheCatalog;         //缓存地址
        File file = new File(Const.PathFactory.getFileSavedPath(PathType.PACKAGE_PATH)+ path, fileName);     //设置文件地址

        if (!file.exists()) return null;    //没文件就返回空

        //读取文件
        byte[] result = null;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);

            //获取文件大小
            int size = inputStream.available();
            result = new byte[size];
            inputStream.read(result);

            logUtil.i("缓存文件读取", "[缓存文件读取成功 - " + getIntegearFormat(size) + " 字节]" + fileName);
        } catch (Exception e) {
            logUtil.e("文件读取失败", e.toString());
            e.printStackTrace();
        } finally {
            //关闭文件
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                logUtil.e("文件关闭失败", e.toString());
                e.printStackTrace();
            }
        }


        logUtil.e("fileUtil.java", "=============缓存文件需要及时删除========");
        return result;
    }

    //返回形如123,456
    private static String getIntegearFormat(int i) {
        return String.format("%,d", i);
    }

    /**
     * 判断缓存文件是否存在
     *
     * @param cacheCatalog 缓存版块
     * @param fileName     文件名
     */
    public static boolean existCacheFile(String cacheCatalog, String fileName) {
        String path = cacheDirectory + cacheCatalog;         //缓存地址
        File file = new File(Const.PathFactory.getFileSavedPath(PathType.PACKAGE_PATH)+ path, fileName);     //设置文件地址

        logUtil.i("文件存在", fileName + " 是否存在：" + file.exists());
        return file.exists();
    }


    //保存对象
    public static void saveObjectData(String filePath, Object data) {
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(filePath));
            objectOutputStream.writeObject(data);
        } catch (FileNotFoundException e) {
            logUtil.e("fileUtil", e.toString());
        } catch (IOException e) {
            logUtil.e("fileUtil", e.toString());
        } catch (Exception e) {
            logUtil.e("fileUtil", e.toString());
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.flush();
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //读取对象
    public static Object getObjectData(String filePath) {
        Object result = null;
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(new FileInputStream(filePath));
            result = objectInputStream.readObject();
        } catch (FileNotFoundException e) {
            logUtil.e("fileUtil", e.toString());
        } catch (IOException e) {
            logUtil.e("fileUtil", e.toString());
        } catch (ClassNotFoundException e) {
            logUtil.e("fileUtil", e.toString());
        } catch (Exception e) {
            logUtil.e("fileUtil", e.toString());
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
