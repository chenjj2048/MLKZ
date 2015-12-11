package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import utils.logUtils.abstract_LogUtil;

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

//用来管理缓存文件
public class fileUtil {

    /**
     * 保存文件
     *
     * @param file  文件
     * @param bytes 字节流
     */
    public static void saveBytesToFile(File file, byte[] bytes) {
        FileOutputStream outputStream = null;
        try {
            //写入文件
            outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
            outputStream.flush();

            abstract_LogUtil.i("缓存文件保存", "[缓存文件保存成功 - " + getIntegearFormat(bytes.length) + " 字节]" + file.toString());
        } catch (Exception e) {
            abstract_LogUtil.e("文件读取错误", e.toString());
        } finally {
            //关闭连接
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
                abstract_LogUtil.e("文件关闭错误", e.toString());
            }
        }
    }


    /**
     * 读取字节流
     *
     * @param file 文件
     * @return 字节流
     */
    public static byte[] getBytesFromFile(File file) {
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

            abstract_LogUtil.i("缓存文件读取", "[缓存文件读取成功 - " + getIntegearFormat(size) + " 字节]" + file.getName());
        } catch (Exception e) {
            abstract_LogUtil.e("文件读取失败", e.toString());
        } finally {
            //关闭文件
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                abstract_LogUtil.e("文件关闭失败", e.toString());
            }
        }

        return result;
    }

    //返回形如123,456
    private static String getIntegearFormat(int i) {
        return String.format("%,d", i);
    }

    //保存对象
    public static void saveObjectData(String filePath, Object data) {
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(filePath));
            objectOutputStream.writeObject(data);
        } catch (Exception e) {
            abstract_LogUtil.e("fileUtil", e.toString());
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
        } catch (Exception e) {
            abstract_LogUtil.e("fileUtil", e.toString());
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
