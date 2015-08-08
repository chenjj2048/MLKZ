package lib;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import lib.clsGlobal.logUtil;

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
public class clsHttpAccess_CallBack {
    static int uniqueID = 0;
    private static clsHttpAccess_CallBack mSingleton;
    public HashMap<String, requestCollection> mDataCollection = new HashMap<>();     //保存所有的请求
    private Handler mHandler = new MyHandler();

    /**
     * 单例模式,这里没什么用,主要是不想每次都new
     */
    public static clsHttpAccess_CallBack getSingleton() {
        if (mSingleton == null)
            mSingleton = new clsHttpAccess_CallBack();
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

//        logUtil.d(this, url);    //日志

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

//        logUtil.d(this, url);

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
        void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage);

        //子线程，用来处理数据，避免UI卡顿
        void onHttpBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage);

        //pic为图片字节流
        void onPictureLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes);
    }

    /**
     * 处理收到的Handler消息
     */
    private static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            //获取操作号
            int id = msg.getData().getInt("id");
            HashMap<String, requestCollection> mData = clsHttpAccess_CallBack.getSingleton().mDataCollection;

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
                byte[] result = new BaseAccessUnit().HttpGetBitmap(item.url, item.cookie, 0, 0);
                item.pic = result;                    //设置返回消息
                item.succeed = (result != null);     //设置是否成功
            } else {
                //下载文本
                String result = new BaseAccessUnit().HttpGetString(item.url, item.cookie, 0, 0);
                item.html = result;                      //设置返回消息
                item.succeed = result.length() > 0;     //设置是否成功
            }

            //子线程送一遍消息（仅限文本）
            if (item.listener != null && !item.requestIsPic)
                item.listener.onHttpBackgroundThreadLoadCompleted(item.url, item.cookie, item.succeed, item.html);

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

    /**
     * 用于网络访问
     * 获取文本及图片(字节流)
     * 必须在子线程中执行
     */
    private class BaseAccessUnit {
        private final int timeout_text_connection = 3000;         //文本连接超时
        private final int timeout_text_read = 5000;               //文本读取超时
        private final int timeout_pic_connection = 5000;          //图片连接超时
        private final int timeout_pic_read = 15000;               //图片读取超时

        /**
         * 获取纯本文
         *
         * @return 返回值一定不会是null，放心使用
         */
        private String HttpGetString(String website, String cookie, int timeout_connection, int timeout_read) {
            String result;
            try {
                String strHTTP = "http://";
                if (website != null && !website.contains(strHTTP))
                    website = strHTTP + website;          //补齐http://

                //1.获得浏览器实例
                HttpClient client = new DefaultHttpClient();
                //2.准备请求地址
                HttpGet httpGet = new HttpGet(website);

                if (cookie != null && !cookie.equals(""))
                    httpGet.setHeader("Cookie", cookie);

                //超时过小，重新设置
                if (timeout_connection <= 1000)
                    timeout_connection = timeout_text_connection;
                if (timeout_read < 1000)
                    timeout_read = timeout_text_read;

                //HttpClient连接超时
                client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout_connection);
                //HttpClient读取超时
                client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout_read);

                //3.发送GET请求
                HttpResponse response = client.execute(httpGet);

                //4.解析返回数据
                StringBuilder sb = new StringBuilder();
                HttpEntity entity = response.getEntity();

                //5.获得编码格式
                String strEncoding = entity.getContentType().getValue();
                strEncoding = strEncoding.substring(strEncoding.indexOf("charset="));
                strEncoding = strEncoding.replace("charset=", "");

                //6.获取流数据
                int ResponseCode = response.getStatusLine().getStatusCode();
                if (ResponseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
                    String temp;
                    while ((temp = br.readLine()) != null) {
                        String str = new String(temp.trim().getBytes(), strEncoding);
                        sb.append(str);
                        sb.append("\r\n");
                    }
                    br.close();

                    //数据获取成功
                    result = new String(sb);
                } else {
                    //ResponseCode非200，失败
                    result = "";
                    logUtil.i(this, "[ResponseCode]" + ResponseCode);
                }
            } catch (Exception e) {
                result = "";
                logUtil.e(this, "[网页获取失败]" + website);
                logUtil.e(this, "[GetHttp失败]" + e.toString());
            }
            return result;
        }

        /**
         * 获取网络图片(转成字节传输)
         *
         * @return 返回值可能为null，需要判断
         */
        private byte[] HttpGetBitmap(String url, String cookie, int timeout_connection, int timeout_read) {
            byte[] bytes = null;
            InputStream is = null;
            try {
                //超时过小，重新设置
                if (timeout_connection <= 1000)
                    timeout_connection = timeout_pic_connection;
                if (timeout_read < 1000)
                    timeout_read = timeout_pic_read;

                URL bitmapURL = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) bitmapURL.openConnection();
                if (cookie != null && !cookie.equals(""))
                    conn.setRequestProperty("Cookie", cookie);     //cookie设置
                conn.setConnectTimeout(timeout_connection);
                conn.setReadTimeout(timeout_read);
                conn.setDoInput(true);
                conn.connect();

                //返回数据
                is = conn.getInputStream();
                bytes = InputStreamUtils.InputStreamTOByte(is);
            } catch (Exception e) {
                logUtil.e(this, "[图片加载失败]" + e.toString());
            } finally {
                try {
                    if (is != null)
                        is.close();
                } catch (Exception e) {
                    logUtil.e(this, "[InputStream]关闭失败");
                }
            }
            return bytes;
        }
    }
}
