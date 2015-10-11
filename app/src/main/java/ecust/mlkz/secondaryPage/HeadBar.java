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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import ecust.main.R;
import lib.clsDimensionConvert;
import lib.clsUtils.ScreenUtil;

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
    //PopupWindow
    private PopupWindow mPopupWindow;
    //监听器
    private OnTagClickListener listener;
    //画笔
    private Paint mPaint;
    //箭头图标
    private Drawable mDrawableUp;
    private Drawable mDrawableDown;

    public HeadBar(Context context) {
        this(context, null);
    }

    public HeadBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeadBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 加载特性
     */
    private void initAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        //设置下默认属性
        mTextUnfocusedColor = new TextView(getContext()).getCurrentTextColor();
        mTextFocusedColor = mTextUnfocusedColor;
        mTextSize = clsDimensionConvert.dip2px(getContext(), DEFAULT_TEXT_SIZE_DP);

        //加载属性
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
        this.setOrientation(HORIZONTAL);
        this.setGravity(Gravity.CENTER);
        //画笔
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(DEFAULT_PAINT_COLOR));
        mPaint.setStrokeWidth(clsDimensionConvert.dip2px(getContext(), DEFAULT_PAINT_SIZE_DP));
        mPaint.setAntiAlias(true);
        //刷新
        invalidateLayout();
        //绘制Drawable
        this.mDrawableUp = createArrowDrawable(this.mTextFocusedColor, false);
        this.mDrawableDown = createArrowDrawable(this.mTextUnfocusedColor, true);
    }

    /**
     * 调用onDraw,不然画不出来,内容随意
     * 使用invalidate()无效
     */
    protected void invalidateLayout() {
        this.setBackgroundColor(0);
    }

    /**
     * 添加可供点击的标签
     *
     * @param name 标签名称
     */
    protected HeadBar addTab(String name) {
        //新建TextView
        TextView textView = new TextView(getContext());
        textView.setText(name);
        textView.setLineSpacing(0, 1.1f);
        textView.setTextSize(mTextSize);
        textView.setSingleLine();
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, mPadding, 0, mPadding);
        changeTabStatus(textView, false);

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
        linearLayout.setTag(this.getChildCount() - 1);
        linearLayout.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        linearLayout.setOnClickListener(this);

        return this;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float width = this.getWidth();
        final float height = this.getHeight();
        final float half_PaintStrokeWidth = mPaint.getStrokeWidth() / 2;

        //背景色
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
        if (focused) {
            textView.setTextColor(mTextFocusedColor);
            textView.setCompoundDrawables(null, null, mDrawableUp, null);
        } else {
            textView.setTextColor(mTextUnfocusedColor);
            textView.setCompoundDrawables(null, null, mDrawableDown, null);
        }
        textView.setCompoundDrawablePadding(lib.clsDimensionConvert.dip2px(getContext(), 5));
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

                    //抛出事件
                    if (this.listener != null)
                        listener.onHeadBarTagClick(item, new NewWindow());
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

    /**
     * 设置监听
     */
    public void setOnTagClickListener(OnTagClickListener listener) {
        this.listener = listener;
    }

    /**
     * 创建一个Drawable
     */
    @SuppressWarnings("deprecation")
    private Drawable createArrowDrawable(int color, boolean arrowDown) {
        final int widths = (int) (this.mTextSize * 0.6f);
        final int heights = widths * 2;

        //画笔
        Paint mDrawablePaint = new Paint();
        mDrawablePaint.setColor(color);
        mDrawablePaint.setAntiAlias(true);
        mDrawablePaint.setStrokeWidth(clsDimensionConvert.dip2px(getContext(), DEFAULT_PAINT_SIZE_DP));

        //新建位图
        Bitmap bitmap = Bitmap.createBitmap(heights, widths, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        if (arrowDown) {
            //向下箭头
            canvas.drawLine(0, 0, widths, widths, mDrawablePaint);
            canvas.drawLine(widths, widths, heights, 0, mDrawablePaint);
        } else {
            //向上箭头
            canvas.drawLine(0, widths, widths, 0, mDrawablePaint);
            canvas.drawLine(widths, 0, heights, widths, mDrawablePaint);
        }

        //转成Drawable
        Drawable drawable = new BitmapDrawable(bitmap);
        drawable.setBounds(0, 0, heights, widths);
        return drawable;
    }

    /**
     * 关闭当前的View
     */
    public void closeCurrentView() {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        } else {
            //重置下TextView状态
            this.onDismiss();
        }
    }

    public interface OnTagClickListener {
        void onHeadBarTagClick(int position, NewWindow mWindow);
    }

    /**
     * 弹出View的一个类，供接口调用
     */
    public class NewWindow {
        /**
         * 显示View（会添加一个半透明黑色背景）
         */
        public void popupView(@Nullable View view) {
            if (view == null) {
                HeadBar.this.closeCurrentView();
                return;
            }

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
            backgroundView.setOnClickListener(HeadBar.this);

            //容器内添加要显示的View和半透明背景
            LinearLayout viewGroup = new LinearLayout(getContext());
            viewGroup.setOrientation(VERTICAL);
            viewGroup.addView(view);
            viewGroup.addView(backgroundView);

            //显示
            mPopupWindow = new PopupWindow(viewGroup, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            mPopupWindow.setOnDismissListener(HeadBar.this);
            mPopupWindow.showAsDropDown(HeadBar.this);
        }
    }
}
