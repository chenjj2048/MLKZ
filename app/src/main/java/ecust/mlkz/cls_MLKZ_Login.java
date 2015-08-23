package ecust.mlkz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lib.clsBaseAccessInThread;
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
 * Created by 彩笔怪盗基德 on 2015/8/17
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

//梅陇客栈登陆
//用于读取保存cookie
//很早写的，类写的有点烂，囧，有时间再改吧，暂时先将就用一下
public class cls_MLKZ_Login {
    private String username;
    private String password;
    private OnLoginStatusReturn onLoginStatusReturn;        //回调接口
    private String cookieReturn = "";        //获得的cookie
    private Context context;

    public cls_MLKZ_Login(Context context) {
        this.context = context;
    }

    //设置用户名
    public cls_MLKZ_Login setUsername(String username) {
        this.username = username;
        return this;
    }

    //设置密码
    public cls_MLKZ_Login setPassword(String password) {
        this.password = password;
        return this;
    }

    //进行登陆
    public void login() {
        new loginTask().execute(this.username, this.password);
    }

    //设置接口
    public void setOnLoginStatusReturn(OnLoginStatusReturn onLoginStatusReturn) {
        this.onLoginStatusReturn = onLoginStatusReturn;
    }

    //2.获得表单的formhash值
    private String getFormHash(String strHtml) {
        int i = strHtml.indexOf("formhash");
        if (i > 0) {
            i = strHtml.indexOf("value=", i);
            String str = strHtml.substring(i, i + 20);
            String[] tmp = str.split("'");
            str = tmp[1].replace("'", "");
            return str;
        }
        return "";
    }

    //3.获得requestURL
    private String getPostURL(String strHtml) {
        int i = strHtml.indexOf("<form");
        if (i > 0) {
            i = strHtml.indexOf("action=", i);
            String str = strHtml.substring(i, i + 200);
            String[] tmp = str.split("\"");
            str = tmp[1].replace("\"", "");
            str = "http://bbs.ecust.edu.cn/" + str.replace("&amp;", "&");         //html文本转换
            return str;
        }
        return "";
    }

    //4.POST数据进行登陆
    private String LoginByUsername(String requestURL, String username, String password, String formhash) {
        try {
            String strData = "formhash=" + formhash +
                    "&referer=http%3A%2F%2Fbbs.ecust.edu.cn%2F.%2F&fastloginfield=username&username=" +
                    URLEncoder.encode(username, "utf-8") +
                    "&password=" + URLEncoder.encode(password, "utf-8") +
                    "&submit=%E7%99%BB%E5%BD%95&questionid=0&answer=&cookietime=2592000";
            byte[] data = strData.getBytes();
            URL url = new URL(requestURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(5000);                                              //设置连接超时时间
            httpURLConnection.setDoInput(true);                                                     //打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);                                                    //打开输出流，以便向服务器提交数据
            httpURLConnection.setRequestMethod("POST");                                             //设置以Post方式提交数据
            httpURLConnection.setUseCaches(false);                                                  //使用Post方式不能使用缓存

            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); //设置请求体的类型是文本类型

            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));    //设置请求体的长度

            OutputStream outputStream = httpURLConnection.getOutputStream();                        //获得输出流，向服务器写入数据
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();

            StringBuilder result = new StringBuilder();
            int response = httpURLConnection.getResponseCode();                                     //获得服务器的响应码

            //获取返回的cookie
            Map<String, List<String>> maps = httpURLConnection.getHeaderFields();
            List<String> coolist = maps.get("Set-Cookie");
            Iterator<String> it = coolist.iterator();
            StringBuffer sb = new StringBuffer();
            while (it.hasNext()) {
                String line = it.next();
                line = line.substring(0, line.indexOf(";"));
                if (!line.contains("=deleted")) {
                    this.cookieReturn = sb.toString();
                    //去除重复数据
                    if (!this.cookieReturn.contains(line)) {
                        sb.append(line + "; ");
                    }
                }
            }
            String end = "here is end";
            sb.append(end);
            this.cookieReturn = sb.toString().replace("; " + end, "");

            //判读是否获取返回数据成功
            if (response == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String temp;
                while ((temp = br.readLine()) != null) {
                    String str = new String(temp.trim().getBytes(), "utf-8");
                    result.append(str);
                    result.append("\r\n");
                }
                return new String(result);
            }
        } catch (Exception e) {
            logUtil.e(this, e.toString());
        }
        return "";
    }

    //5.返回登录状态消息
    private String LoginStatusMessage(String strHtml) {                                              //返回类似消息
        int i = strHtml.indexOf("messagetext");                                                     //欢迎您回来，彩笔怪盗基德。点击进入登录前页面
        strHtml = strHtml.substring(i, i + 80);                                                     //登录失败，您还可以尝试 4 次
        String[] tmp = strHtml.split("p>");                                                         //密码错误次数过多，请 15 分钟后重新登录
        strHtml = tmp[1].replace("</", "");
        return strHtml;
    }

    //登陆返回接口
    public interface OnLoginStatusReturn {
        void OnLoginStatusReturn(String username, String password, String rtnMessage, String cookie);
    }

    //登陆任务（子线程）
    private class loginTask extends AsyncTask<String, Void, Void> {
        //登陆地址
        private final String login_url = "http://bbs.ecust.edu.cn/member.php?mod=logging&action=login&mobile=yes";
        private final int minShowTime = 1500;
        private String returnMessage = "";       //返回的消息

        @Override
        protected Void doInBackground(String... params) {
            if (params.length != 2) throw new NullPointerException();     //必须2个参数
            final String username = params[0];        //用户名
            final String password = params[1];        //密码

            final long startTime = System.currentTimeMillis();

            try {
                //进行第一次登陆访问，获取formhash及表单提交地址
                String HtmlResult = new clsBaseAccessInThread().HttpGetString(login_url, null, 0, 0);
                //获取成功
                if (HtmlResult != null && HtmlResult.length() > 0) {
                    final String formhash = getFormHash(HtmlResult);      //获取表单hash
                    final String postURL = getPostURL(HtmlResult);     //获取Post地址
                    //提交表单信息尝试登陆
                    HtmlResult = LoginByUsername(postURL, username, password, formhash);
                    this.returnMessage = LoginStatusMessage(HtmlResult);
                }

                //至少加载状态显示一段时间
                while (System.currentTimeMillis() - startTime < minShowTime) {
                    Thread.sleep(40);
                }
            } catch (Exception e) {
                logUtil.e(this, e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //登陆成功，保存登陆信息
            if (this.returnMessage.contains("欢迎您回来")) {
                new getLoginPreference().SaveLoginInformation(username, password, cookieReturn);

                logUtil.i(this, returnMessage);
            }

            //接口返回
            onLoginStatusReturn.OnLoginStatusReturn(username, password, returnMessage, cookieReturn);
        }
    }

    public class getLoginPreference {
        private final String MLKZ_LOGIN_INFORMATION = "mlkz_login_information";   //Preference
        private final String USERNAME = "username";
        private final String PASSWORD = "password";
        private final String COOKIE = "cookie";

        //6.保存用户名、密码、cookie
        public void SaveLoginInformation(String username, String password, String cookie) {
            SharedPreferences sp = context.getSharedPreferences(this.MLKZ_LOGIN_INFORMATION, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            //保存用户名
            editor.putString(this.USERNAME, username);
            //保存密码
            editor.putString(this.PASSWORD, password);
            //保存cookie
            editor.putString(this.COOKIE, cookie);
            editor.apply();
        }

        //获取Cookie
        public String getCookie() {
            SharedPreferences sp = context.getSharedPreferences(this.MLKZ_LOGIN_INFORMATION, Context.MODE_PRIVATE);
            return sp.getString(this.COOKIE, "");       //读取cookie
        }

        //获取用户名
        public String getUsername() {
            SharedPreferences sp = context.getSharedPreferences(this.MLKZ_LOGIN_INFORMATION, Context.MODE_PRIVATE);
            return sp.getString(this.USERNAME, "");       //读取用户名
        }
    }
}
