package CustomWidgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import utils.Global;

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
 * 部分参考自http://blog.csdn.net/hellochenlian/article/details/38512561
 * .
 */
public class myCircleImageView extends ImageView {

    public myCircleImageView(Context context) {
        super(context);
    }

    public myCircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public myCircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //创建圆形图片
    public static Bitmap getCroppedBitmap(Bitmap bmp, int diameter) {
        Bitmap sbmp;
        if (bmp.getWidth() != diameter || bmp.getHeight() != diameter)
            sbmp = Bitmap.createScaledBitmap(bmp, diameter, diameter, false);
        else
            sbmp = bmp;

        final int w = sbmp.getWidth();
        final int h = sbmp.getHeight();

        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);

        //画边框
        final int border = Global.dimenConvert.dip2px(1);
        paint.setColor(Color.RED);
        canvas.drawCircle(w / 2, h / 2, diameter / 2, paint);

        int sc = canvas.saveLayer(0, 0, w, h, null, Canvas.ALL_SAVE_FLAG);

        //画圆
        canvas.drawCircle(w / 2, h / 2, diameter / 2 - border, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);
        paint.setXfermode(null);

        canvas.restoreToCount(sc);

        return output;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        if (null == b) {
            return;
        }
//        Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

        int w = this.getWidth();
        int h = this.getHeight();
        Bitmap roundBitmap = getCroppedBitmap(b, Math.min(w, h));
        if (w > h)
            canvas.drawBitmap(roundBitmap, w / 2 - h / 2, 0, null);
        else
            canvas.drawBitmap(roundBitmap, 0, h / 2 - w / 2, null);
    }
}

