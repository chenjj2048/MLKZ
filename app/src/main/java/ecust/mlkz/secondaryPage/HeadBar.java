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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.HashMap;

import ecust.main.R;
import lib.clsDimensionConvert;
import lib.clsUtils.ScreenUtil;
import lib.logUtils.logUtil;

/**
 * 顶部的Tab
 */
public class HeadBar extends LinearLayout implements View.OnClickListener, PopupWindow.OnDismissListener {
    //标记
    private static final int BACKGROUND_VIEW_CLICKED = -1;
    //默认画笔颜色
    private static final int DEFAULT_PAINT_COLOR = R.color.grey230;
    //默认画笔粗细
    private static final int DEFAULT_PAINT_SIZE_DP = 1;
    //默认字体大小
    private static final int DEFAULT_TEXT_SIZE_DP = 12;
    //未点击时的字体颜色
    private int mTextUnfocusedColor;
    //点击时的字体颜色
    private int mTextFocusedColor;
    //设置的字体大小
    private int mTextSize;
    //TextViewPadding
    private int mPadding = 0;
    //Tab的数量
    private int mCount = 0;
    //PopupWindow
    private PopupWindow mPopupWindow;
    //数据集
    private HashMap<String, OnTagClickListener> mHashMap;
    //画笔
    private Paint mPaint;

    public HeadBar(Context context) {
        this(context, null);
    }

    public HeadBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeadBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initAttributes(context, attrs, defStyleAttr);
    }

    /**
     * 加载特性
     */
    private void initAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HeadBar, defStyleAttr, 0);
        for (int i = 0; i < a.getIndexCount(); i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.HeadBar_textUnfocusedColor:
                    //未点击时字体颜色
                    this.mTextUnfocusedColor = a.getColor(attr, mTextUnfocusedColor);
                    break;
                case R.styleable.HeadBar_textFocusedColor:
                    //未点击时字体颜色
                    this.mTextFocusedColor = a.getColor(attr, mTextFocusedColor);
                    break;
                case R.styleable.HeadBar_tabTextSize:
                    //字体大小
                    this.mTextSize = a.getDimensionPixelSize(attr, mTextSize);
                    break;
                case R.styleable.HeadBar_tabPadding:
                    //字体Padding
                    this.mPadding = a.getDimensionPixelSize(attr, mPadding);
                    break;
            }
        }
        a.recycle();
    }

    /**
     * 初始化
     */
    private void init() {
        this.mHashMap = new HashMap<>(5);
        this.setOrientation(HORIZONTAL);
        this.setGravity(Gravity.CENTER);
        //默认字体大小
        this.mTextSize = clsDimensionConvert.dip2px(getContext(), DEFAULT_TEXT_SIZE_DP);
        //画笔
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(DEFAULT_PAINT_COLOR));
        mPaint.setStrokeWidth(clsDimensionConvert.dip2px(getContext(), DEFAULT_PAINT_SIZE_DP));
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        //刷新
        invalidateLayout();
    }

    /**
     * 调用onDraw,不然画不出来,内容随意
     * 使用invalidate()无效
     */
    protected void invalidateLayout() {
        this.setBackgroundColor(0);
    }

    /**
     * 显示View（会添加一个半透明黑色背景）
     */
    private void popupView(View view) {
        //View最大只显示7成
        final int screenHeight = ScreenUtil.getScreenHeight(getContext());
        final float maxHeight = screenHeight * 0.7f;
        view.measure(0, MeasureSpec.makeMeasureSpec((int) maxHeight, MeasureSpec.AT_MOST));
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, view.getMeasuredHeight()));

        //设置半透明背景
        View backgroundView = new View(getContext());
        backgroundView.setBackgroundColor(Color.argb(0x80, 0, 0, 0));
        //背景设置点击事件
        backgroundView.setTag(BACKGROUND_VIEW_CLICKED);
        backgroundView.setOnClickListener(this);

        //容器内添加要显示的View和半透明背景
        LinearLayout viewGroup = new LinearLayout(getContext());
        viewGroup.setOrientation(VERTICAL);
        viewGroup.addView(view);
        viewGroup.addView(backgroundView);

        //显示
        mPopupWindow = new PopupWindow(viewGroup, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mPopupWindow.setOnDismissListener(this);
        mPopupWindow.showAsDropDown(this);
    }

    /**
     * 添加可供点击的标签
     *
     * @param name               标签名称
     * @param onTagClickListener 回调函数
     */
    protected void addTab(String name, OnTagClickListener onTagClickListener) {
        this.mHashMap.put(name, onTagClickListener);

        //新建TextView
        TextView textView = new TextView(getContext());
        textView.setText(name);
        textView.setLineSpacing(0, 1.1f);
        if (mTextUnfocusedColor != 0) {
            //非默认颜色
            textView.setTextColor(mTextUnfocusedColor);
        } else {
            //获取默认颜色
            mTextUnfocusedColor = textView.getCurrentTextColor();
        }
        textView.setTextSize(mTextSize);
        textView.setSingleLine();
        textView.setPadding(0, mPadding, 0, mPadding);
        textView.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        //新建LinearLayout
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        LayoutParams lp = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.weight = 1;
        //添加至布局中
        linearLayout.addView(textView);
        this.addView(linearLayout, lp);

        //设置点击事件
        linearLayout.setTag(mCount++);
        linearLayout.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        linearLayout.setOnClickListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float width = this.getWidth();
        final float height = this.getHeight();
        final float half_PaintStrokeWidth = mPaint.getStrokeWidth() / 2;

        canvas.drawColor(Color.WHITE);
        //绘制底线
        canvas.drawLine(0, height - half_PaintStrokeWidth,
                width, height - half_PaintStrokeWidth, mPaint);

        //绘制分割线
        final int iCount = this.getChildCount();
        if (iCount == 0) return;
        final float sectionWidth = width / iCount;
        for (int i = 1; i < iCount; i++) {
            float x = sectionWidth * i;
            canvas.drawLine(x, 0, x, height, mPaint);
        }
    }

    /**
     * 改变Tab颜色
     *
     * @param focused 是否为选中项
     */
    private void changeTabStatus(TextView textView, boolean focused) {
        if (focused)
            textView.setTextColor(mTextFocusedColor);
        else
            textView.setTextColor(mTextUnfocusedColor);
    }

    /**
     * Tab被点击
     */
    @Override
    public void onClick(View v) {
        int item = (int) v.getTag();
        switch (item) {
            case BACKGROUND_VIEW_CLICKED:
                //背景被点击
                mPopupWindow.dismiss();
                break;
            default:
                //顶部栏被点击
                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                } else {
                    //获取点击下对应的TextView
                    TextView textView = (TextView) ((LinearLayout) v).getChildAt(0);
                    changeTabStatus(textView, true);


                    logUtil.toast(item + "");
                    TextView tv = new TextView(getContext());
                    tv.setBackgroundResource(android.R.color.holo_orange_light);
                    tv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50));
                    tv.setGravity(Gravity.CENTER);
                    tv.setText("aaa " + item);
                    tv.setTextSize(40);
                    tv.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
                    popupView(tv);
                }
                break;
        }
    }

    /**
     * 窗口消失,Tab颜色修改
     */
    @Override
    public void onDismiss() {
        mPopupWindow = null;
        for (int i = 0; i < this.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) this.getChildAt(i);
            TextView textView = (TextView) linearLayout.getChildAt(0);
            changeTabStatus(textView, false);
        }
    }

    protected interface OnTagClickListener {
        void onHeadBarTagClick(String name);
    }
}
