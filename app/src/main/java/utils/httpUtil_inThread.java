package utils;

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
 * Created by 彩笔怪盗基德 on 2015/8/17
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

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

import utils.logUtils.abstract_LogUtil;

/**
 * 用于网络访问
 * 获取文本及图片(字节流)
 * 必须在子线程中执行
 */
//Todo:准备撤掉
    @Deprecated
public class httpUtil_inThread {
    private static final int timeout_text_connection = 3000;         //文本连接超时
    private static final int timeout_text_read = 5000;               //文本读取超时
    private static final int timeout_pic_connection = 5000;          //图片连接超时
    private static final int timeout_pic_read = 15000;               //图片读取超时

    /**
     * 获取纯本文
     *
     * @return 返回值一定不会是null，放心使用
     */
    public String HttpGetString(String website, String cookie, int timeout_connection, int timeout_read) {
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
                BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), strEncoding));
                String temp;
                while ((temp = br.readLine()) != null) {
                    String str = new String(temp.trim().getBytes());
                    sb.append(str);
                    sb.append("\r\n");
                }
                br.close();

                //数据获取成功
                result = new String(sb);
            } else {
                //ResponseCode非200，失败
                result = "";
                abstract_LogUtil.i(this, "[ResponseCode]" + ResponseCode);
            }
        } catch (Exception e) {
            result = "";
            abstract_LogUtil.e(this, "[网页获取失败]" + website);
            abstract_LogUtil.e(this, "[GetHttp失败]" + e.toString());
        }
        return result;
    }

    /**
     * 获取网络图片(转成字节传输)
     *
     * @return 返回值可能为null，需要判断
     */
    public byte[] HttpGetBitmap(String url, String cookie, int timeout_connection, int timeout_read) {
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
            abstract_LogUtil.e(this, "[图片加载失败]" + e.toString());
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (Exception e) {
                abstract_LogUtil.e(this, "[InputStream]关闭失败");
            }
        }
        return bytes;
    }
}