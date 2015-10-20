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
 * Created by 彩笔怪盗基德 on 2015/10/18
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import com.github.clans.fab.FloatingActionButton;

import ecust.main.R;
import lib.clsDimensionConvert;

/**
 * github没找到水平的FloatingActionButton菜单,只有垂直的
 * 自己造个轮子,把FloatingActionButton组装起来
 */
public class HorizontalFabMenu extends LinearLayout {
    //按钮组开关
    private FloatingActionButton mFabMenuButton;
    //按钮间距
    private int mButtonSpacing = clsDimensionConvert.dip2px(getContext(), 8);

    public HorizontalFabMenu(Context context) {
        super(context);
    }

    public HorizontalFabMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalFabMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 宽：尽可能宽点，有动画，不然会显示不完全
     * 高：按钮高度
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //第一次运行时加载开关按钮，并且隐藏其他按钮
        if (mFabMenuButton == null) {
            addFabMenuButton();
            hideOtherFABs();
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 初始化按钮，按钮不可见
     */
    private void hideOtherFABs() {
        for (int i = 0; i < this.getChildCount(); i++)
            if (this.getChildAt(i) != mFabMenuButton)
                this.getChildAt(i).setAlpha(0);
    }

    /**
     * 所有按钮尽可能放在右下角
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int leftX = this.getWidth();

        //从右往左放Button
        for (int i = this.getChildCount() - 1; i >= 0; i--) {
            View child = this.getChildAt(i);

            if (child == mFabMenuButton) {
                //按钮开关放最右边
                child.layout(this.getWidth() - child.getMeasuredWidth(), this.getHeight() - child.getMeasuredHeight(),
                        this.getWidth(), this.getHeight());
                leftX -= child.getMeasuredWidth();
            } else {
                //要画的起始X坐标
                leftX -= mButtonSpacing + child.getMeasuredWidth();
                child.layout(leftX, this.getHeight() - child.getMeasuredHeight(),
                        leftX + child.getMeasuredWidth(), this.getHeight());
            }
        }
    }

    /**
     * 初始化视图
     */
    private void addFabMenuButton() {
        //添加一个开关按钮
        mFabMenuButton = new FloatingActionButton(getContext());
        mFabMenuButton.setImageResource(R.drawable.fab_add);
        mFabMenuButton.setOnClickListener(new FabMenuButtonStatus());
        this.addView(mFabMenuButton);
    }

    /**
     * 关联RecyclerView的滚动事件，用来显示、隐藏当前组件
     */
    public void attachToRecyclerView(RecyclerView mRecyclerView) {

    }

    /**
     * 开关按钮
     */
    private class FabMenuButtonStatus implements OnClickListener {
        //动画时间
        private static final int DURATION = 200;
        //开关旋转角度
        private static final int ROTATION = 45 + 90;
        //FAB是否展开
        private boolean isOpen = false;

        @Override
        public void onClick(View v) {
            toggle();
        }

        /**
         * 开关按钮
         */
        private void toggle() {
            if (isOpen)
                close();
            else
                open();
            isOpen = !isOpen;
        }

        /**
         * 关闭菜单
         */
        private void close() {
            for (int i = 0; i < HorizontalFabMenu.this.getChildCount(); i++) {
                final View fab = HorizontalFabMenu.this.getChildAt(i);
                final int distance = mFabMenuButton.getLeft() - fab.getLeft();

                if (fab == mFabMenuButton) continue;

                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.setDuration(DURATION);

                //按钮组向右收缩，透明度减小
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        fab.setTranslationX(distance * value);
                        fab.setAlpha(1 - value);
                        mFabMenuButton.setRotation(ROTATION * (1 - value));
                    }
                });
                valueAnimator.start();
            }
        }

        /**
         * 展开菜单
         */
        private void open() {
            for (int i = 0; i < HorizontalFabMenu.this.getChildCount(); i++) {
                final View fab = HorizontalFabMenu.this.getChildAt(i);
                final int distance = mFabMenuButton.getLeft() - fab.getLeft();

                if (fab == mFabMenuButton) {
                    //开关按钮
                    mFabMenuButton.setShowShadow(false);
                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mFabMenuButton, "rotation", 0, ROTATION);
                    objectAnimator.setDuration(DURATION).start();
                    //保证阴影方向正确！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
                    mFabMenuButton.setShowShadow(true);
                    mFabMenuButton.invalidate();

                } else {
                    //其他按钮
                    ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                    valueAnimator.setInterpolator(new OvershootInterpolator());

                    //按钮组向左展开，透明度增加
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float value = (float) animation.getAnimatedValue();
                            fab.setTranslationX(distance * (1 - value));
                            fab.setAlpha(value);
                        }
                    });
                    valueAnimator.setDuration(DURATION).start();
                }
            }
        }
    }
}
