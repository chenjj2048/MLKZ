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
 * 参考自http://blog.csdn.net/lmj623565791/article/details/39257409
 * <p/>
 * 侧滑菜单+主体部分
 */

public class SlidingMenu extends HorizontalScrollView {
    private int mScreenWidth;       //屏幕宽度
    private int mMenuWidth;         //侧滑菜单宽度
    private boolean canShow;        //允许侧滑菜单显示

    public SlidingMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mScreenWidth = ScreenUtil.getScreenWidth(context);        //获取屏幕宽度
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//判断第一次运行？？？？？？
        measureSize();

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //测量尺寸
    public void measureSize() {
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
        int action = ev.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:


                //进行判断，如果显示区域大于菜单宽度一半则完全显示，否则隐藏
                int scrollX = getScrollX();
                if (scrollX > mMenuWidth / 2)
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

