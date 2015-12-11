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

package CustomWidgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import com.github.clans.fab.FloatingActionButton;

import java.lang.ref.SoftReference;

import ecust.main.R;
import utils.clsDimensionConvert;

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
            initFabMenuButton();
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
                this.getChildAt(i).setVisibility(INVISIBLE);
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
    private void initFabMenuButton() {
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
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private Animation mAnimShow;
            private Animation mAnimHide;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (mAnimShow == null)
                    mAnimShow = AnimationUtils.loadAnimation(getContext(), R.anim.fab_show_from_bottom);
                if (mAnimHide == null)
                    mAnimHide = AnimationUtils.loadAnimation(getContext(), R.anim.fab_hide_to_bottom);

                if (dy > 0) {
                    //向下滑，隐藏
                    if (HorizontalFabMenu.this.getVisibility() == VISIBLE) {
                        mAnimShow.cancel();
                        HorizontalFabMenu.this.startAnimation(mAnimHide);
                        HorizontalFabMenu.this.setVisibility(INVISIBLE);
                    }
                } else if (dy < 0) {
                    //向上滑，显示
                    if (HorizontalFabMenu.this.getVisibility() != VISIBLE) {
                        mAnimHide.cancel();
                        HorizontalFabMenu.this.startAnimation(mAnimShow);
                        HorizontalFabMenu.this.setVisibility(VISIBLE);
                    }
                }
            }
        });
    }

    /**
     * 开关按钮
     */
    private class FabMenuButtonStatus implements OnClickListener {
        //动画时间
        private static final int DURATION = 200;
        //开关旋转角度
        private static final int ROTATION = 90 + 45;
        //FAB是否展开
        private boolean isOpen = false;
        //+号图标
        private Bitmap mRawAddBitmap;
        //图片缓存
        private RotateBitmapDrawableCache mRotateBitmapDrawableCache;

        public FabMenuButtonStatus() {
            BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.fab_add);
            if (drawable != null)
                mRawAddBitmap = drawable.getBitmap();

            //最多放N张图
            mRotateBitmapDrawableCache = new RotateBitmapDrawableCache(15);
        }

        /**
         * 开关按钮
         */
        @Override
        public void onClick(View v) {
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

                ValueAnimator valueAnimator;
                if (fab == mFabMenuButton) {
                    //旋转动画
                    valueAnimator = ValueAnimator.ofFloat(1, 0);
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            //旋转图标
                            final float degree = (float) animation.getAnimatedValue() * ROTATION;

                            //调用图片缓存
                            BitmapDrawable drawable = mRotateBitmapDrawableCache.getBitmapDrawable(degree);
                            if (drawable == null) {
                                drawable = mRotateBitmapDrawableCache.createRotateBitmapDrawable(mRawAddBitmap, degree);
                                mRotateBitmapDrawableCache.putBitmapDrawable(degree, drawable);
                            }
                            mFabMenuButton.setImageDrawable(drawable);
                        }
                    });
                } else {
                    valueAnimator = ValueAnimator.ofFloat(0, 1);

                    //按钮组向右收缩，透明度减小
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float value = (float) animation.getAnimatedValue();
                            fab.setTranslationX(distance * value);
                            fab.setAlpha(1 - value);
                        }
                    });
                    valueAnimator.setInterpolator(new OvershootInterpolator());
                }
                valueAnimator.setDuration(DURATION).start();
            }
        }

        /**
         * 展开菜单
         */
        private void open() {
            for (int i = 0; i < HorizontalFabMenu.this.getChildCount(); i++) {
                final View fab = HorizontalFabMenu.this.getChildAt(i);
                final int distance = mFabMenuButton.getLeft() - fab.getLeft();

                ValueAnimator valueAnimator;
                if (fab == mFabMenuButton) {
                    //开关按钮
                    valueAnimator = ValueAnimator.ofFloat(0, 1);
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            //旋转图标
                            final float degree = (float) animation.getAnimatedValue() * ROTATION;

                            //调用图片缓存
                            BitmapDrawable drawable = mRotateBitmapDrawableCache.getBitmapDrawable(degree);
                            if (drawable == null) {
                                drawable = mRotateBitmapDrawableCache.createRotateBitmapDrawable(mRawAddBitmap, degree);
                                mRotateBitmapDrawableCache.putBitmapDrawable(degree, drawable);
                            }
                            mFabMenuButton.setImageDrawable(drawable);
                        }
                    });
                } else {
                    //其他按钮
                    fab.setVisibility(VISIBLE);

                    valueAnimator = ValueAnimator.ofFloat(0, 1);

                    //按钮组向左展开，透明度增加
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float value = (float) animation.getAnimatedValue();
                            fab.setTranslationX(distance * (1 - value));
                            fab.setAlpha(value);
                        }
                    });
                    valueAnimator.setInterpolator(new OvershootInterpolator());
                }
                valueAnimator.setDuration(DURATION).start();
            }
        }

        /**
         * 管理旋转的图片,避免频繁创造
         */
        private class RotateBitmapDrawableCache extends LruCache<Integer, SoftReference<BitmapDrawable>> {
            //划分的最小度数
            final int minDegree = 10;

            public RotateBitmapDrawableCache(int maxSize) {
                super(maxSize);
            }

            //每10个度数合并成一个区块，减少对象
            private int combineDegrees(float degree) {
                //特殊值单独存放
                if (degree == 0 || degree == ROTATION)
                    return (int) degree;

                //将数值取整到minDegree的倍数
                int result = (int) degree / minDegree;
                result *= minDegree;
                if (result == 0 || result == ROTATION)
                    result += degree > 0 ? minDegree : -minDegree;
                return result;
            }

            //获取图片
            public BitmapDrawable getBitmapDrawable(float rotateValue) {
                final int value = combineDegrees(rotateValue);

                SoftReference<BitmapDrawable> softReference = this.get(value);
                if (softReference == null)
                    return null;
                else
                    return softReference.get();
            }

            //存放图片
            public void putBitmapDrawable(float rotateValue, BitmapDrawable drawable) {
                final int value = combineDegrees(rotateValue);
                this.put(value, new SoftReference<>(drawable));
            }

            /**
             * 旋转图片
             *
             * @param bitmap 原始图片
             * @param degree 旋转角度
             */
            public BitmapDrawable createRotateBitmapDrawable(Bitmap bitmap, float degree) {
                Matrix matrix = new Matrix();
                matrix.setRotate(degree);
                Bitmap bitmap_new = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                return new BitmapDrawable(getResources(), bitmap_new);
            }
        }
    }
}
