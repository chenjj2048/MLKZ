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
import java.io.InputStreamReader;

import ecust.mlkz.act_MLKZ_HomePage;
import ecust.mlkz.act_MLKZ_LoginPage;
import ecust.mlkz.clsLogin;
import lib.clsGlobal.Global;

/**
 * Created by 1 on 2015/5/17.
 * 用于访问获得网页的源代码
 * 【1】由Activity页面调用HttpAccess.StartNewThreadGet(StartPost)
 * 【2】新建Thread线程运行，处理联网消息GET、POST事件
 * 【3】Thread中抛出消息给mHandler处理
 */
public class clsHttpAccess {
    private static final String Exception_Tag = "[cjj_Exception]";
    private static final String handler_msg = "msg";

    /**
     * 处理返回的数据，传递给HttpHandler类进行处理
     */
    private static Handler mHandler = new MyHandler();

    //登陆梅陇客栈，获取cookie
    public static void LoginMLKZ(String username, String password) {
        new myThread(Global.action_mlkz_loginpage, username, password).start();                          //新建线程进行登陆
    }

    //开始Get动作
    public static void StartNewThreadGet(int fromActivity, String url, String cookie) {
        new myThread(fromActivity, url, cookie).start();                                       //新建线程进行Get
    }

    //////////////////////////////////////////////////////////
    //            以下public部分，新建线程运行
    //////////////////////////////////////////////////////////

    //获取网页源代码
    public static String HttpGetString(String website, String cookie) {
        String strResult = "";                                                                      //返回的结果
        String strHTTP = "http://";

        if (!website.contains(strHTTP)) {
            website = strHTTP + website;
        }
        try {
            HttpClient client = new DefaultHttpClient();                                            //1.获得浏览器实例
            HttpGet httpGet = new HttpGet(website);                                                 //2.准备请求地址

            if (cookie != null) {                                                                   //cookie设置
                if (!cookie.equals("")) {
                    httpGet.setHeader("Cookie", cookie);
                }
            }

            client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);          //HttpClient连接超时
            client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);                  //HttpClient读取超时

            HttpResponse response = client.execute(httpGet);                                        //3.发送GET请求

            StringBuilder result = new StringBuilder();                                             //4.解析返回数据
            HttpEntity entity = response.getEntity();

            String strEncoding = entity.getContentType().getValue();                                //5.获得编码格式
            strEncoding = strEncoding.substring(strEncoding.indexOf("charset="));
            strEncoding = strEncoding.replace("charset=", "");
            Global.log("[Get Success成功] " + website + "(" + strEncoding + ")");

            if (response.getStatusLine().getStatusCode() == 200) {                                  //6.获取流数据
                BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
                String temp;
                while ((temp = br.readLine()) != null) {
                    String str = new String(temp.trim().getBytes(), strEncoding);
                    result.append(str);
                    result.append("\r\n");
                }
                br.close();
                strResult = new String(result);
            }
        } catch (Exception e) {
            Global.log("[HttpGetString]" + e.toString());
            strResult = Exception_Tag + e.toString();                                               //返回错误消息
        }
        return strResult;
    }


    //////////////////////////////////////////////////////////
    //                   以下private部分
    //////////////////////////////////////////////////////////

    private static class MyHandler extends Handler {
        //Handler消息处理
        @Override
        public void handleMessage(Message msg) {                                                    //处理获得的各类消息
            try {
                String rtnMessage = msg.getData().getString(handler_msg, "");

                //对所有消息进行回调处理
                switch (msg.what) {
                    case Global.action_mlkz_loginpage:                                              //处理MLKZ登录事件
                        ((act_MLKZ_LoginPage) Global.activity).handle_Login(rtnMessage);
                        break;
                    case Global.action_mlkz_homepage:
//                        ((act_MLKZ_HomePage) Global.activity).handle_HomePage(rtnMessage);
                        break;
                    case Global.action_timeout:                                                     //超时
                        Global.toastMsg("服务器连接异常,请稍后再试");
                        break;
                    default:
                        Global.log(rtnMessage);                                                     //默认返回网络的数据
                }
            } catch (Exception e) {
                Global.log("[Handler错误 " + msg.what + "]" + e.toString());
            }
        }
    }

    /**
     * 新建子线程用于访问网络
     */
    private static class myThread extends Thread {
        private int ActionType;
        private String strURL;
        private String strCookie;
        private String username;
        private String password;

        private boolean flag_TimeOut = false;                                                       //标识网页访问是否超时

        private myThread(int ActionType, String para1, String para2) {
            if (ActionType == Global.action_mlkz_loginpage) {
                this.username = para1;
                this.password = para2;
            } else {
                this.strURL = para1;
                this.strCookie = para2;
            }
            this.ActionType = ActionType;
        }

        public void run() {                                                                         //新线程运行
            Global.addThread();
            Message message = new Message();
            Bundle bundle = new Bundle();
            String HtmlResult = "";                                                                 //保存HTML返回结果

            switch (ActionType) {
                case Global.action_mlkz_loginpage:                                                    //登陆梅陇客栈，获取cookie
                    Global.log("[Thread]梅陇客栈登陆中...");
                    HtmlResult = new clsLogin().LoginMLKZ(username, password);
                    break;
                case Global.action_mlkz_homepage:                                                   //mlkz主界面点击
                    Global.log("[Thread]梅陇客栈主页面跳转中...");
                    HtmlResult = HttpGetString(strURL, strCookie);
                    break;
                default:
                    Global.log("[Thread]HttpGetString");
                    HtmlResult=HttpGetString(strURL,strCookie);
                    break;
            }

            if (HtmlResult.contains(Exception_Tag)) {
                flag_TimeOut = true;                                                                //连接异常标志
            }

            try {
                if (flag_TimeOut) {
                    message.what = Global.action_timeout;                                           //连接异常
                    HtmlResult = HtmlResult.replace(Exception_Tag, "");                             //去除错误标记
                    flag_TimeOut = false;                                                           //重置超时标志
                } else {
                    message.what = ActionType;                                                      //正常连接
                }
                bundle.putString(handler_msg, HtmlResult);
                message.setData(bundle);


                mHandler.sendMessage(message);

            } catch (Exception e) {
                Global.log("[Thread Handler Error]" + e.toString());
            }
            Global.deleteThread();
        }
    }
}
