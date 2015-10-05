package ecust.news;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
 * Created by 彩笔怪盗基德 on 2015/9/4
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

//自定义一个ViewPageIndicator,取代原来用的第三方组件
//名字和TabPageIndicator尽可能一样
public class myWidgetTabPageIndicator extends HorizontalScrollView implements View.OnClickListener, ViewPager.OnPageChangeListener {
    //深色
    final int textViewFocusedColor = Color.WHITE;
    //淡色
    final int textViewUnfocusedColor = Color.argb(180, 255, 255, 255);
    //下划线的高度
    private final int dp_FocusedLineHeight = 5;
    //关联的ViewPager
    private ViewPager viewPager;
    //线性布局框架
    private LinearLayout linearLayout;
    //回调
    private ViewPager.OnPageChangeListener listener;
    //画笔
    private Paint paint = new Paint();

    public myWidgetTabPageIndicator(Context context) {
        super(context);
        init();
    }

    public myWidgetTabPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public myWidgetTabPageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.setBackgroundResource(android.R.color.holo_green_light);
        this.setHorizontalScrollBarEnabled(false);
        linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        this.addView(linearLayout);
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        viewPager.setOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        PagerAdapter pagerAdapter = this.viewPager.getAdapter();

        linearLayout.removeAllViews();

        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            //获取标题
            String title = pagerAdapter.getPageTitle(i).toString();
            //添加TextView
            addTab(i, title);
        }
    }

    @Override
    public void onClick(View v) {
        if (!(v instanceof TextView)) return;

        TextView currentView = (TextView) v;

        //找position避免使用Tag，万一别人也用了就不好了
        int position;
        for (position = 0; position < linearLayout.getChildCount(); position++) {
            TextView textView = (TextView) linearLayout.getChildAt(position);
            //判断字符串是否一致
            if (textView.getText().toString().equals(currentView.getText().toString()))
                break;
        }

        //没找到
        if (position >= linearLayout.getChildCount()) return;

        //切换页面
        viewPager.setCurrentItem(position, true);
    }

    //添加一项可供点击的标签
    private void addTab(int i, String title) {
        TextView textView = new TextView(getContext());
        textView.setText(title);

        //装饰模式
        textView = decorateTextView(textView);

        if (i == 0)
            textView.setTextColor(textViewFocusedColor);

        textView.setOnClickListener(this);

        //添加View
        linearLayout.addView(textView);
    }

    //装饰TextView
    public TextView decorateTextView(TextView textView) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Global.dimenConvert.dip2px(20));
        textView.setSingleLine();
        textView.setTextColor(textViewUnfocusedColor);
        textView.setFocusable(true);
        textView.setClickable(true);

        //设置Padding
        final int paddingLeftRight = Global.dimenConvert.dip2px(12);
        final int paddingTop = Global.dimenConvert.dip2px(8);
        final int paddingBottom = Global.dimenConvert.dip2px(0);
        textView.setPadding(paddingLeftRight, paddingTop, paddingLeftRight, paddingBottom);

        return textView;
    }

    //设置监听器
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //画出底上的白线
        int w = Global.dimenConvert.dip2px(dp_FocusedLineHeight) / 2;
        int y = canvas.getHeight() - w / 2;

        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(w);
        canvas.drawLine(0, y, canvas.getMaximumBitmapWidth(), y, paint);
    }

    //选中当前项
    public void setCurrentItem(int position) {
        //设置默认字体颜色
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            TextView textView = (TextView) linearLayout.getChildAt(i);
            if (i != position) {
                textView.setTextColor(textViewUnfocusedColor);
                textView.setCompoundDrawables(null, null, null, null);
            } else {
                textView.setTextColor(textViewFocusedColor);

                //设置drawable
                textView.measure(0, 0);
                int w = textView.getMeasuredWidth() - textView.getPaddingLeft() - textView.getPaddingRight();
                int h = Global.dimenConvert.dip2px(dp_FocusedLineHeight);
                Drawable drawable = new ColorDrawable(Color.WHITE);
                drawable.setBounds(0, 0, w, h);
                textView.setCompoundDrawables(null, null, null, drawable);
            }

        }

        //计算当前textView距离最左边的距离
        int sum_width = 0;
        for (int i = 0; i < position; i++) {
            sum_width += linearLayout.getChildAt(i).getWidth();
        }

        //计算居中位置
        sum_width -= this.getWidth() / 2 - linearLayout.getChildAt(position).getWidth() / 2;

        //滑动
        this.smoothScrollTo(sum_width, 0);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (listener != null)
            listener.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        //设置选中项的变化
        setCurrentItem(position);
        if (listener != null)
            listener.onPageSelected(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (listener != null)
            listener.onPageScrollStateChanged(state);
    }
}
