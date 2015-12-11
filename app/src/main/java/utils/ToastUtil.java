/**
 * .
 * Created by 彩笔怪盗基德 on 2015/12/9
 * github：https://github.com/chenjj2048
 * .
 */

package utils;

import android.support.annotation.Nullable;
import android.widget.Toast;

import ecust.main.App;

public class ToastUtil {
    public static void toast(@Nullable String str) {
        Toast.makeText(App.getContext(), str, Toast.LENGTH_SHORT).show();
    }

    public static void toast(@Nullable String str, int duration) {
        Toast.makeText(App.getContext(), str, duration).show();
    }
}
