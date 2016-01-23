/**
 * .
 * Created by 彩笔怪盗基德 on 2015/12/9
 * github：https://github.com/chenjj2048
 * .
 */

package utils;

import android.text.TextUtils;
import android.widget.Toast;

import ecust.main.App;

public class ToastUtil {
    //Toast时间间隔(重复消息的最短间隔)
    private static final long minDeltaSeconds = 2000;

    //最后的toast时间
    private static long lastToastTime;
    //最后toast的消息
    private static String lastToastMsg = "";

    public static void toast(String msg) {
        ToastUtil.toast(msg, Toast.LENGTH_SHORT);
    }

    public static void toast(String msg, int duration) {
        if (TextUtils.isEmpty(msg)) return;

        //取得当前时间
        long nowTime = System.currentTimeMillis();

        //同样消息，太过频繁就退出
        if (nowTime - lastToastTime <= minDeltaSeconds && msg.equals(lastToastMsg)) return;

        lastToastMsg = msg;
        lastToastTime = nowTime;
        Toast.makeText(App.getContext(), msg, duration).show();
    }
}
