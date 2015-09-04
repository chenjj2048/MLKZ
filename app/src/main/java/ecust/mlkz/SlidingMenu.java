package ecust.mlkz;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

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

public class SlidingMenu extends HorizontalScrollView {
    private int mScreenWidth;       //屏幕宽度
    private int mMenuWidth;         //侧滑菜单宽度
    private boolean needSwitchToBody = false;

    public SlidingMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mScreenWidth = ScreenUtil.getScreenWidth(context);        //获取屏幕宽度
    }

    public void setNeedSwitchToBody(boolean needSwitchToBody) {
        this.needSwitchToBody = needSwitchToBody;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureSize();

        //延迟一段时间执行,否则滑动距离总有问题
        if (needSwitchToBody) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < 50; i++) {
                            Thread.sleep(10);
                            switchToBody();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            needSwitchToBody = false;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            this.scrollTo(mMenuWidth, 0);     //菜单隐藏，滚动至主体部分
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //过滤掉事件，不触发ACTION_UP，手指在最左边才能滑出菜单
                if (this.getScrollX() != 0 && ev.getX() >= mMenuWidth / 8)
                    return false;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                //进行判断，如果显示区域大于菜单宽度一半则完全显示，否则隐藏
                if (this.getScrollX() > mMenuWidth / 2)
                    switchToBody();
                else
                    switchToLeftMenu();

                return true;
        }
        return super.onTouchEvent(ev);
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

