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
 * Created by 彩笔怪盗基德 on 2015/10/6
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.mlkz.secondaryPage;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

/**
 * 最多添加2-3个，过多效果不好
 */
public class HeadBar extends LinearLayout {
    private HashMap<String, OnTagClickListener> hashMap;
    private int textFocusedColor;

    public HeadBar(Context context) {
        this(context, null);
    }

    public HeadBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeadBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.hashMap = new HashMap<>(5);
        this.setOrientation(HORIZONTAL);
    }

    /**
     * 添加可供点击的标签
     *
     * @param name               标签名称
     * @param onTagClickListener 回调函数
     */
    protected void addTab(String name, OnTagClickListener onTagClickListener) {
        this.hashMap.put(name, onTagClickListener);
        //新建TextView
        TextView textView = new TextView(getContext());
        textView.setText(name);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //新建LinearLayout
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(HORIZONTAL);
        linearLayout.setWeightSum(1);
        linearLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //添加至布局中
        linearLayout.addView(textView);
        this.addView(linearLayout);
    }

    protected interface OnTagClickListener {
        void onHeadBarTagClick(String name);
    }
}
