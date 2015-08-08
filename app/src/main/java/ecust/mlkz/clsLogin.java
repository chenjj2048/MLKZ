package ecust.mlkz;

import android.content.SharedPreferences;

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

import lib.clsGlobal.Global;
import lib.clsHttpAccess;

/**
 * Created by 1 on 2015/5/23.
 * 登陆类
 * 用于读取保存cookie
 */
public class clsLogin {
    private final String Class_Tag = "Class_clsLogin";                                              //标签Tag
    private String cookie = "";

    //0.登陆梅陇客栈全部过程
    public String LoginMLKZ(String username, String password) {
        //进行第一次登陆访问，获取formhash及表单提交地址
        String HtmlResult = GetFirstHandShakeResult();
        String formhash = GetFormHash(HtmlResult);
        String requestURL = GetRequestURL(HtmlResult);

        //提交表单信息尝试登陆
        HtmlResult = LoginByUsername(requestURL, username, password, formhash);
        String str_return = LoginStatusMessage(HtmlResult);

        //登陆成功，保存登陆信息
        if (str_return.contains("欢迎您回来")) {
            SaveLoginInformation(username, password, cookie);
        }
        return str_return;
    }

    //1.进行握手，Get登陆数据，(已位于子线程中)
    private String GetFirstHandShakeResult() {
        String URL_MLKZ_Login = "http://bbs.cjj.ecust.edu.cn/member.php?mod=logging&action=login&mobile=yes";
        //无参数访问登陆页面
        return clsHttpAccess.HttpGetString(URL_MLKZ_Login, null);
    }

    //2.获得表单的formhash值
    private String GetFormHash(String strHtml) {
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
    private String GetRequestURL(String strHtml) {
        int i = strHtml.indexOf("<form");
        if (i > 0) {
            i = strHtml.indexOf("action=", i);
            String str = strHtml.substring(i, i + 200);
            String[] tmp = str.split("\"");
            str = tmp[1].replace("\"", "");
            str = "http://bbs.cjj.ecust.edu.cn/" + str.replace("&amp;", "&");                           //html文本转换
            return str;
        }
        return "";
    }

    //4.POST数据进行登陆
    private String LoginByUsername(String requestURL, String username, String password, String formhash) {
        try {
            String strData = "formhash=" + formhash +
                    "&referer=http%3A%2F%2Fbbs.cjj.ecust.edu.cn%2F.%2F&fastloginfield=username&username=" +
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
                    this.cookie = sb.toString();
                    //去除重复数据
                    if (!this.cookie.contains(line)) {
                        sb.append(line + "; ");
                    }
                }
            }
            String end = "here is end";
            sb.append(end);
            this.cookie = sb.toString().replace("; " + end, "");

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
            Global.log(e.toString());
        }
        return "";
    }

    //5.返回登录状态消息
    private String LoginStatusMessage(String strHtml) {                                              //返回类似消息
        int i = strHtml.indexOf("messagetext");                                                     //欢迎您回来，彩笔怪盗基德。点击进入登录前页面
        strHtml = strHtml.substring(i, i + 80);                                                     //登录失败，您还可以尝试 4 次
        String[] tmp = strHtml.split("p>");                                                         //密码错误次数过多，请 15 分钟后重新登录
        strHtml = tmp[1].replace("</", "");
        Global.log( strHtml);
        return strHtml;
    }

    //6.保存用户名、密码、cookie
    private void SaveLoginInformation(String username, String password, String cookie) {
        SharedPreferences sp = Global.activity.getSharedPreferences(Global.sp_Config, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Global.sp_Username, username);                                             //保存用户名
        editor.putString(Global.sp_Password, password);                                             //保存密码
        editor.putString(Global.sp_Cookie,cookie);                                                  //保存cookie
        editor.apply();
        Global.log( "[登录成功，信息已保存]\r\n" + username + " " + password + " " + cookie);
    }
}
