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
 * Created by 彩笔怪盗基德 on 2015/10/8
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.mlkz.secondaryPage;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 圆角TextView
 */
public class RoundRectTextView extends TextView {
    final int DEFAULT_TEXT_SIZE = 12;
    private List<Integer> colors = new ArrayList<>();

    public RoundRectTextView(Context context) {
        super(context);
        init();
    }

    public RoundRectTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundRectTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.setSingleLine();
        this.setLineSpacing(0, 1);

        final int dp_Padding = 2;
        int padding = lib.clsDimensionConvert.dip2px(getContext(), dp_Padding);
        this.setPadding(padding * 3, padding, padding * 3, padding);

        //添加色彩数据
        int[] colorRes = new int[]{android.R.color.holo_blue_light,
                android.R.color.holo_green_light, android.R.color.holo_blue_dark,
                android.R.color.holo_orange_light, android.R.color.holo_purple,
                android.R.color.holo_orange_dark, android.R.color.holo_red_light};
        for (int i : colorRes)
            colors.add(getResources().getColor(i));
    }

    @SuppressWarnings("deprecation")
    private void createRoundRectBackground() {
        //设置背景图
        float radius = this.getHeight() / 2;
        float[] outerRadii = new float[8];
        for (int i = 0; i < 8; i++) {
            outerRadii[i] = radius;
        }

        RoundRectShape roundRectShape = new RoundRectShape(outerRadii, null, null);
        ShapeDrawable drawable = new ShapeDrawable(roundRectShape);

        drawable.getPaint().setColor(this.getCurrentTextColor());
        drawable.getPaint().setStyle(Paint.Style.STROKE);

        this.setBackgroundDrawable(drawable);
    }

    @Override
    public boolean onPreDraw() {
        //随机搭配个颜色
        int currentColor = colors.get(this.getText().hashCode() % colors.size());
        this.setTextColor(currentColor);

        //默认字体大小
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                lib.clsDimensionConvert.dip2px(getContext(), DEFAULT_TEXT_SIZE));

        createRoundRectBackground();
        return super.onPreDraw();
    }
}
