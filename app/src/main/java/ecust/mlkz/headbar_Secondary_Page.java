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
 * Created by 彩笔怪盗基德 on 2015/9/19
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.mlkz;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ecust.main.R;
import ecust.mlkz.act_MLKZ_Secondary_Page.forum_Structs_Collection.struct_forumSubjectClassificationNode;
import lib.clsDimensionConvert;
import lib.clsUtils.logUtil;

/**
 * 梅陇客栈二级页面 顶部的bar
 * 含以下内容：
 * 1.版块目录(必选)及子版块(可选项)
 * 2.主题分类(可选项)
 * 3.新帖排序
 */
public class headbar_Secondary_Page extends LinearLayout implements View.OnClickListener,
        PopupWindow.OnDismissListener, View.OnTouchListener {
    //按发帖时间排序
    protected final static int SORT_BY_POSTTIME = 0;
    //按回复时间排序
    protected final static int SORT_BY_REPLYTIME = 1;
    //对应bar的标签
    private final static int LEFT_CATALOG = 1;
    private final static int MIDDLE_CLASSIFICATION = 2;
    private final static int RIGHT_SORT = 3;
    //ListView点击下的纵坐标位置
    private float listViewLastYPosition;
    //点击事件回调
    private OnHeadbarClickListener onHeadbarClickListener;
    //弹出框
    private PopupWindow popupWindow;
    //弹出的View
    private View popView;
    //all
    private LinearLayout linearLayout;
    //版块目录
    private TextView section;
    //主题分类(可选项)
    private TextView classification;
    //新帖排序
    private TextView sort;
    //画笔
    private Paint paint;
    //上下文
    private Context context;
    //主题分类数据数组
    private List<String> arraySubjectClassification = new ArrayList<>();
    //字体颜色
    private int textUnfocusedColor;
    private int textFocusedColor;

    public headbar_Secondary_Page(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public headbar_Secondary_Page(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public headbar_Secondary_Page(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        this.context = context;
    }

    private void init() {
        //加载布局
        LayoutInflater.from(this.context).inflate(R.layout.mlkz_secondary_page_headbar, this);

        //设置控件引用
        section = (TextView) findViewById(R.id.mlkz_secondary_page_headbar_section);
        classification = (TextView) findViewById(R.id.mlkz_secondary_page_headbar_classification);
        sort = (TextView) findViewById(R.id.mlkz_secondary_page_headbar_sort);
        linearLayout = (LinearLayout) this.getChildAt(0);

        //设置监听
        section.setOnClickListener(this);
        classification.setOnClickListener(this);
        sort.setOnClickListener(this);

        //设置画笔
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.grey230));
        final int dp = 1;
        paint.setStrokeWidth(clsDimensionConvert.dip2px(this.context, dp));

        //调用onDraw,不然画不出来,内容随意
        this.setBackgroundColor(0);

        //设置字体颜色
        textUnfocusedColor = getResources().getColor(R.color.black26);
        textFocusedColor = getResources().getColor(android.R.color.holo_orange_dark);

        setDefaultTextColor();

        this.arraySubjectClassification.add("全部");
    }

    //设置默认字体颜色
    private void setDefaultTextColor() {
        section.setTextColor(textUnfocusedColor);
        classification.setTextColor(textUnfocusedColor);
        sort.setTextColor(textUnfocusedColor);
    }

    @Override
    public void onDismiss() {
        setDefaultTextColor();
        popView = null;
    }

    //ListView项目被点击,代替OnItemClickListener
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ListView listView;
        if (!(v instanceof ListView)) {
            return false;
        } else {
            listView = (ListView) v;
        }

        //return true会屏蔽滚动事件
        //return false照样会触发ACTION_UP事件
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //记录点击下的位置
                listViewLastYPosition = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                //移动距离过大，则有可能为ListView的滚动事件
                float distance = event.getY() - listViewLastYPosition;
                if (Math.abs(distance) >= clsDimensionConvert.dip2px(this.context, 5)) break;

                //确定点击item的位置
                final int position = listView.pointToPosition((int) event.getX(), (int) event.getY());
                if (position < 0) break;

                String str = listView.getAdapter().getItem(position).toString();
                //传递点击事件
                notifyClickEvent((int) popView.getTag(), str);

                popupWindow.dismiss();
                break;
        }

        return false;
    }

    private void notifyClickEvent(int view_tag, String str) {
        switch (view_tag) {
            case LEFT_CATALOG:
                break;
            case MIDDLE_CLASSIFICATION:
                logUtil.toast(str);
                break;
            case RIGHT_SORT:
                if (onHeadbarClickListener != null) {
                    if (str.equals("按发帖时间排序")) {
                        onHeadbarClickListener.onSortButtonClick(SORT_BY_POSTTIME);
                    } else if (str.equals("按回复时间排序")) {
                        onHeadbarClickListener.onSortButtonClick(SORT_BY_REPLYTIME);
                    }
                }
                popupWindow.dismiss();
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //=================大类==========================
            case R.id.mlkz_secondary_page_headbar_section:
                //目录区域
                if (popView != null) {
                    popupWindow.dismiss();
                } else {
                    showLeftSectionView();
                    ((TextView) v).setTextColor(textFocusedColor);
                }
                break;
            case R.id.mlkz_secondary_page_headbar_classification:
                //主题分类
                if (popView != null) {
                    popupWindow.dismiss();
                } else {
                    showMiddleClassificationView();
                    ((TextView) v).setTextColor(textFocusedColor);
                }
                break;
            case R.id.mlkz_secondary_page_headbar_sort:
                //排序方式
                if (popView != null) {
                    popupWindow.dismiss();
                } else {
                    showRightSortView();
                    ((TextView) v).setTextColor(textFocusedColor);
                }
                break;

            default:
                //点击了背景空白处
                popupWindow.dismiss();
        }
    }

    /**
     * 左侧-版块所在
     */
    private void showLeftSectionView() {
        View view = LayoutInflater.from(this.context).inflate(R.layout.mlkz_headbar_left_section, null);
        view.setTag(LEFT_CATALOG);
        popupView(view);
    }

    /**
     * 中间-主题分类
     */
    private void showMiddleClassificationView() {
        //添加ListView
        ListView listView = new ListView(this.context);
        listView.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        listView.setBackgroundColor(Color.WHITE);

        //数据集
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.context,
                android.R.layout.simple_list_item_1, this.arraySubjectClassification);

        listView.setOnTouchListener(this);
        listView.setAdapter(adapter);

        listView.setTag(MIDDLE_CLASSIFICATION);
        popupView(listView);
    }

    /**
     * 右侧-排序方式
     */
    private void showRightSortView() {
        //添加ListView
        ListView listView = new ListView(this.context);
        listView.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        listView.setBackgroundColor(Color.WHITE);

        //数据集
        String[] array = new String[]{"按发帖时间排序", "按回复时间排序"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.context, android.R.layout.simple_list_item_1, array);

        listView.setOnTouchListener(this);
        listView.setAdapter(adapter);

        listView.setTag(RIGHT_SORT);
        popupView(listView);
    }

    /**
     * 显示View
     */
    private void popupView(View view) {
        popView = view;

        //设置半透明背景
        View backgroundView = new View(this.context);
        backgroundView.setBackgroundColor(Color.argb(0x80, 0, 0, 0));
        backgroundView.setOnClickListener(this);

        //容器内添加要显示的View和半透明背景
        LinearLayout viewGroup = new LinearLayout(this.context);
        viewGroup.setOrientation(VERTICAL);
        viewGroup.addView(popView);
        viewGroup.addView(backgroundView);

        //显示
        popupWindow = new PopupWindow(viewGroup, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        popupWindow.setOnDismissListener(this);
        popupWindow.showAsDropDown(linearLayout);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        int width = this.getWidth();
        int height = this.getHeight();

        //画分割线
        if (classification.getVisibility() == VISIBLE) {
            //共三块部分
            canvas.drawLine(width / 3, 0, width / 3, height, paint);
            canvas.drawLine(width / 3 * 2, 0, width / 3 * 2, height, paint);
        } else {
            //共二块部分
            canvas.drawLine(width / 2, 0, width / 2, height, paint);
        }

        //底线
        canvas.drawLine(0, height - paint.getStrokeWidth() / 2,
                width, height - paint.getStrokeWidth() / 2, paint);
    }

    protected void setOnHeadbarClickListener(OnHeadbarClickListener listener) {
        this.onHeadbarClickListener = listener;
    }

    /**
     * 设置主题分类数据
     *
     * @param subjectClassificationList 分类数据
     */
    public void setSubjectClassificationData(List<struct_forumSubjectClassificationNode> subjectClassificationList) {
        arraySubjectClassification = new ArrayList<>(20);
        for (struct_forumSubjectClassificationNode node : subjectClassificationList)
            arraySubjectClassification.add(node.getName());
    }

    interface OnHeadbarClickListener {
        void onSortButtonClick(int sortByTime);
    }
}
