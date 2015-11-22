package CustomWidgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import lib.Global;
import lib.clsUtils.ScreenUtil;

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
 * Created on 2015/8/12
 * 侧滑菜单+主体部分
 */

public class mlkzSlidingMenu extends HorizontalScrollView implements Runnable {
    //屏幕宽度
    private int mScreenWidth;
    //侧滑菜单宽度
    private int mMenuWidth;
    //决定是否可以滚动
    private boolean canScroll;


    public mlkzSlidingMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mScreenWidth = ScreenUtil.getScreenWidth(context);        //获取屏幕宽度
    }

    //延迟一段时间执行,否则滑动距离总有问题
    @Override
    public void run() {
        try {
            //用new Timer().schedule()又会有明显延时，起不到作用，很难看
            //知道这样不好，但只执行一次同样会有明显延时,加了次数以后，界面滑动延时现象消失
            for (int i = 0; i < 10; i++) {
                Thread.sleep(10);
                switchToBody();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureSize();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //延迟一段时间执行,否则滑动距离总有问题
        new Thread(this).start();
    }

    //测量尺寸
    private void measureSize() {
        LinearLayout wrapper = (LinearLayout) getChildAt(0);

        ViewGroup menu = (ViewGroup) wrapper.getChildAt(0);     //左侧菜单
        ViewGroup content = (ViewGroup) wrapper.getChildAt(1);  //主体部分

        //测量menu宽度
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        menu.measure(w, h);

        //设置宽度
        mMenuWidth = menu.getMeasuredWidth();

        menu.getLayoutParams().width = mMenuWidth;
        content.getLayoutParams().width = mScreenWidth;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //设置ScrollView是否能够滚动
                if (this.getScrollX() != 0 &&
                        event.getX() >= Math.max(mMenuWidth / 8, Global.dimenConvert.dip2px(5)))
                    canScroll = false;
                else
                    canScroll = true;
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            this.scrollTo(mMenuWidth, 0);     //菜单隐藏，滚动至主体部分
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                //屏蔽滚动
                if (!canScroll)
                    return false;
                break;
            case MotionEvent.ACTION_UP:
                //进行判断，如果显示区域大于菜单宽度一半则完全显示，否则隐藏
                if (this.getScrollX() > mMenuWidth / 2)
                    switchToBody();
                else
                    switchToLeftMenu();
                return true;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return super.onTouchEvent(event);
    }

    //滑动至主页
    public void switchToBody() {
        this.smoothScrollTo(mMenuWidth, 0);
    }

    //滑动至左侧菜单
    public void switchToLeftMenu() {
        this.smoothScrollTo(0, 0);
    }
}

