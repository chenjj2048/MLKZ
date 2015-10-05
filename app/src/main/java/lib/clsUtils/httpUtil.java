package lib.clsUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.HashMap;

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
 * Created by 彩笔怪盗基德 on 2015/6/15
 * Copyright (C) 2015 彩笔怪盗基德
 */

// 网络访问类
public class httpUtil {
    static int uniqueID = 0;
    private static httpUtil mSingleton;
    public HashMap<String, requestCollection> mDataCollection = new HashMap<>();     //保存所有的请求
    private Handler mHandler = new MyHandler();

    /**
     * 单例模式,这里没什么用,主要是不想每次都new
     */
    public static synchronized httpUtil getSingleton() {
        if (mSingleton == null)
            mSingleton = new httpUtil();
        return mSingleton;
    }

    private int getUniqueID() {
        return uniqueID++;      //返回操作ID
    }

    /**
     * getHttp方法1
     */
    public void getHttp(String url, OnHttpVisitListener listener) {
        getHttp(url, null, listener);
    }

    /**
     * getHttp方法2
     */
    public void getHttp(String url, String cookie, OnHttpVisitListener listener) {
        final int id = getUniqueID();   //产生一条操作id

//        abstract_LogUtil.d(this, url);    //日志

        //初始化数据集
        requestCollection item = new requestCollection(id, url, cookie, listener);
        item.requestIsPic = false;

        //插入数据
        mDataCollection.put(String.valueOf(id), item);

        //新建线程开始下载
        new myThread(id).start();
    }

    /**
     * getBitmapBytes方法1
     */
    public void getBitmapBytes(String url, OnHttpVisitListener listener) {
        getBitmapBytes(url, null, listener);
    }

    /**
     * getBitmapBytes方法2
     */
    public void getBitmapBytes(String url, String cookie, OnHttpVisitListener listener) {
        final int id = getUniqueID();   //产生一条操作id

//        abstract_LogUtil.d(this, url);

        //初始化数据集
        requestCollection item = new requestCollection(id, url, cookie, listener);
        item.requestIsPic = true;

        //插入数据
        mDataCollection.put(String.valueOf(id), item);

        //新建线程开始下载
        new myThread(id).start();
    }


    /**
     * 回调函数
     */
    public interface OnHttpVisitListener {
        void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String returnHtmlMessage);

        //子线程，用来处理数据，避免UI卡顿
        void onHttpBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, String returnHtmlMessage);

        //pic为图片字节流
        void onPictureLoadCompleted(String url, String cookie, boolean bSucceed, byte[] returnPicBytes);

        //子线程中
        void onPictureBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, byte[] returnPicBytes);
    }

    /**
     * 处理收到的Handler消息
     */
    private static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            //获取操作号
            int id = msg.getData().getInt("id");
            HashMap<String, requestCollection> mData = httpUtil.getSingleton().mDataCollection;

            //取出请求信息及返回结果
            requestCollection item = mData.get(String.valueOf(id));

            if (item.listener != null)
                if (item.requestIsPic) {
                    //下载为图片
                    item.listener.onPictureLoadCompleted(item.url, item.cookie, item.succeed, item.pic);
                } else {
                    //下载为文本
                    item.listener.onHttpLoadCompleted(item.url, item.cookie, item.succeed, item.html);
                }

            //移除引用
            mData.remove(id + "");
        }
    }

    private class myThread extends Thread {
        int execID;     //操作号

        //根据唯一的操作号，执行命令
        public myThread(int execID) {
            this.execID = execID;
        }

        @Override
        public void run() {
            //获取数据集
            requestCollection item = mDataCollection.get(String.valueOf(execID));

            if (item.requestIsPic) {
                //下载图片
                byte[] result = new httpUtil_inThread().HttpGetBitmap(item.url, item.cookie, 0, 0);
                item.pic = result;                    //设置返回消息
                item.succeed = (result != null);     //设置是否成功
                //子线程送一遍消息（图片）
                if (item.listener != null)
                    item.listener.onPictureBackgroundThreadLoadCompleted(item.url, item.cookie, item.succeed, item.pic);
            } else {
                //下载文本
                String result = new httpUtil_inThread().HttpGetString(item.url, item.cookie, 0, 0);
                item.html = result;                      //设置返回消息
                item.succeed = result.length() > 0;     //设置是否成功
                //子线程送一遍消息（文本）
                if (item.listener != null)
                    item.listener.onHttpBackgroundThreadLoadCompleted(item.url, item.cookie, item.succeed, item.html);
            }

            //设置Handler返回消息
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putInt("id", execID);
            message.setData(bundle);
            mHandler.sendMessage(message);
        }
    }

    /**
     * 存储所有请求信息
     * 操作序号（唯一）、请求地址、请求Cookie、请求为图片、数据返回是否成功、返回的数据（文本或图片）、回调地址
     */
    private class requestCollection {
        int id;
        String url;
        String cookie;
        boolean requestIsPic;
        boolean succeed;
        String html;
        byte[] pic;
        OnHttpVisitListener listener;

        public requestCollection(int id, String url, String cookie, OnHttpVisitListener listener) {
            this.id = id;
            this.url = url;
            this.cookie = cookie;
            this.listener = listener;
        }
    }
}
