/**
 * .
 * Created by 彩笔怪盗基德 on 2015/9/20
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package utils;

import android.content.Context;

/**
 * 尺寸转换类
 */
@Deprecated
public class clsDimensionConvert {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px 转成为 dp
     */
    public static int px2dip(Context context, int px) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f * (px >= 0 ? 1 : -1));
    }
}
