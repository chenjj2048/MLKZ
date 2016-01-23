/**
 * Created on 2015/8/21
 * http://blog.csdn.net/h7870181/article/details/8332991
 * .
 */
package utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

@SuppressWarnings("unused")
public class KeyBoardUtil {
    /**
     * @param view SearchView,EditText等
     */
    public static void closeIME(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getApplicationContext().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0); //隐藏
    }

    public static void openIME(Context context) {
        if (!isImeOpen(context)) toggle(context);
    }

    public static void toggle(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Deprecated
    public static boolean isImeOpen(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isActive();//isOpen若返回true，则表示输入法打开
    }
}
