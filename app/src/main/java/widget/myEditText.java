package widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import ecust.main.R;
import lib.Global;

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
 * Created by 彩笔怪盗基德 on 2015/8/16
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

//重新设置EditText边框等属性
public class myEditText extends EditText implements View.OnTouchListener {
    final int dp_drawable = 20;       //图片尺寸
    final int drawable_size = Global.dimenConvert.dip2px(this.dp_drawable);

    public myEditText(Context context) {
        super(context);
        init();
    }

    public myEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public myEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        //设置边框背景
        setBackgroundResource(R.drawable.selector_edittext);

        //设置清空按钮
        Drawable drawable_right = getResources().getDrawable(R.drawable.edittext_clear);
        if (drawable_right != null) {
            drawable_right.setBounds(0, 0, drawable_size, drawable_size);
        }
        //左侧drawable
        Drawable drawable_left = getCompoundDrawables()[0];
        if (drawable_left != null) {
            drawable_left.setBounds(0, 0, drawable_size, drawable_size);
        }

        if (getText().length() <= 0) {
            drawable_right = null;
        }

        //设置左右drawable
        setCompoundDrawables(drawable_left, null, drawable_right, null);

        //设置X点击事件
        setOnTouchListener(this);

        //输入事件
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //设置清空按钮
                Drawable drawable_right = getResources().getDrawable(R.drawable.edittext_clear);
                if (drawable_right != null) {
                    drawable_right.setBounds(0, 0, drawable_size, drawable_size);
                }

                if (getText().length() <= 0) {
                    drawable_right = null;
                }
                setCompoundDrawables(getCompoundDrawables()[0], null, drawable_right, null);
            }
        });
    }

    //×点击事件，清空 EditView
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Drawable drawable_right = getCompoundDrawables()[2];
        if (drawable_right == null)
            return false;

        if (event.getAction() != MotionEvent.ACTION_UP)
            return false;

        //drawable.getIntrinsicWidth() 获取drawable资源图片呈现的宽度
        if (event.getX() > this.getWidth() - this.getPaddingRight() - drawable_right.getIntrinsicWidth()) {
            setText("");     //清空EditView
            setCompoundDrawables(getCompoundDrawables()[0], null, null, null);      //取消×
        }

        return false;
    }
}
