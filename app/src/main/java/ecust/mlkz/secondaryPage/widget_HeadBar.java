///**
// * =============================================================================
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License version 2,
// * as published by the Free Software Foundation.
// * .
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * .
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// * =============================================================================
// * .
// * Created by 彩笔怪盗基德 on 2015/9/19
// * Copyright (C) 2015 彩笔怪盗基德
// * 托管地址：https://github.com/chenjj2048
// * .
// */
//
//package ecust.mlkz.secondaryPage;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.util.AttributeSet;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.PopupWindow;
//import android.widget.TextView;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import ecust.main.R;
//import ecust.mlkz.secondaryPage_needBeRefractored.struct_Forum_Items.struct_forumChildSectionNode;
//import ecust.mlkz.secondaryPage_needBeRefractored.struct_Forum_Items.struct_forumDataRoot;
//import ecust.mlkz.secondaryPage_needBeRefractored.struct_Forum_Items.struct_forumPosition;
//import ecust.mlkz.secondaryPage_needBeRefractored.struct_Forum_Items.struct_forumSubjectClassificationNode;
//import lib.clsDimensionConvert;
//import lib.clsUtils.ScreenUtil;
//
///**
// * 梅陇客栈二级页面 顶部的bar
// * 含以下内容：
// * 1.版块目录(必选)及子版块(可选项)
// * 2.主题分类(可选项)
// * 3.新帖排序
// */
//public class widget_HeadBar extends LinearLayout implements View.OnClickListener,
//        PopupWindow.OnDismissListener, View.OnTouchListener {
//    //按发帖时间排序
//    protected final static int SORT_BY_POSTTIME = 0;
//    //按回复时间排序
//    protected final static int SORT_BY_REPLYTIME = 1;
//    //对应bar的标签
//    private final static int LEFT_CATALOG = 1;
//    private final static int MIDDLE_CLASSIFICATION = 2;
//    private final static int RIGHT_SORT = 3;
//    //记录ListView点击下的item
//    private int listViewLastItem;
//    //移动的距离
//    private int moveDistance;
//    //上次的纵坐标
//    private int lastY;
//    //点击事件回调
//    private OnHeadbarClickListener onHeadbarClickListener;
//    //弹出框
//    private PopupWindow popupWindow;
//    //弹出的View
//    private View popView;
//    //all
//    private LinearLayout linearLayout;
//    //版块目录
//    private TextView section;
//    //主题分类(可选项)
//    private TextView classification;
//    //新帖排序
//    private TextView sort;
//    //画笔
//    private Paint paint;
//    //上下文
//    private Context context;
//    //数据集
//    private struct_forumDataRoot mData = new struct_forumDataRoot();
//    //字体颜色
//    private int textUnfocusedColor;
//    private int textFocusedColor;
//
//    public widget_HeadBar(Context context) {
//        super(context);
//        this.context = context;
//        init();
//    }
//
//    public widget_HeadBar(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        this.context = context;
//        init();
//    }
//
//    public widget_HeadBar(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        init();
//        this.context = context;
//    }
//
//    private void init() {
//    }
//
//    //设置默认字体颜色
//    private void setDefaultTextColor() {
//        section.setTextColor(textUnfocusedColor);
//        classification.setTextColor(textUnfocusedColor);
//        sort.setTextColor(textUnfocusedColor);
//    }
//
//    @Override
//    public void onDismiss() {
//        setDefaultTextColor();
//        popView = null;
//    }
//
//    //ListView项目被点击,代替OnItemClickListener
//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        ListView listView;
//        if (!(v instanceof ListView)) {
//            return false;
//        } else {
//            listView = (ListView) v;
//        }
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                //记录点击下的位置是否没变
//                listViewLastItem = listView.pointToPosition((int) event.getX(), (int) event.getY());
//                moveDistance = 0;
//                lastY = (int) event.getY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                moveDistance += Math.abs(event.getY() - lastY);
//                lastY = (int) event.getY();
//                break;
//            case MotionEvent.ACTION_UP:
//                //确定点击item的位置(点击项与listView第一项均没变，才算UP事件)
//                final int pos = listView.pointToPosition((int) event.getX(), (int) event.getY());
//                final int limitMoveDistance = clsDimensionConvert.dip2px(this.context, 5);
//                if (pos < 0 || pos != listViewLastItem || moveDistance > limitMoveDistance)
//                    break;
//
//                String str = listView.getAdapter().getItem(pos).toString();
//                //传递点击事件
//                notifyClickEvent((int) popView.getTag(), str);
//
//                popupWindow.dismiss();
//                break;
//        }
//
//        return false;
//    }
//
//    /**
//     * 接收到点击事件
//     *
//     * @param view_tag 标示是哪块View被点击了，LEFT_CATALOG，MIDDLE_CLASSIFICATION，RIGHT_SORT
//     * @param str      点击到的文字提示
//     */
//    private void notifyClickEvent(int view_tag, String str) {
//        if (onHeadbarClickListener == null) return;
//
//        switch (view_tag) {
//            case LEFT_CATALOG:
//                onHeadbarClickListener.onChildSectionSelected(str);
//                break;
//            case MIDDLE_CLASSIFICATION:
//                onHeadbarClickListener.onClassificationSelected(str);
//                break;
//            case RIGHT_SORT:
//                if (str.equals("按发帖时间排序")) {
//                    onHeadbarClickListener.onSortSelected(SORT_BY_POSTTIME);
//                } else if (str.equals("按回复时间排序")) {
//                    onHeadbarClickListener.onSortSelected(SORT_BY_REPLYTIME);
//                }
//                break;
//        }
//        popupWindow.dismiss();
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            //=================大类==========================
//            case R.id.mlkz_secondary_page_headbar_section:
//                //目录区域
//                if (popView != null) {
//                    popupWindow.dismiss();
//                } else {
//                    showLeftSectionView();
//                    ((TextView) v).setTextColor(textFocusedColor);
//                }
//                break;
//            case R.id.mlkz_secondary_page_headbar_classification:
//                //主题分类
//                if (popView != null) {
//                    popupWindow.dismiss();
//                } else {
//                    showMiddleClassificationView();
//                    ((TextView) v).setTextColor(textFocusedColor);
//                }
//                break;
//            case R.id.mlkz_secondary_page_headbar_sort:
//                //排序方式
//                if (popView != null) {
//                    popupWindow.dismiss();
//                } else {
//                    showRightSortView();
//                    ((TextView) v).setTextColor(textFocusedColor);
//                }
//                break;
//            default:
//                //点击了背景空白处
//                popupWindow.dismiss();
//        }
//    }
//
//    /**
//     * 左侧-版块所在
//     */
//    private void showLeftSectionView() {
//        View view = LayoutInflater.from(this.context).inflate(R.layout.old_mlkz_headbar_left_section, null);
//        view.setTag(LEFT_CATALOG);
//
//        //1.设置三级版块标题
//        TextView title1 = (TextView) view.findViewById(R.id.mlkz_secondary_page_headbar_section_1st_title);
//        TextView title2 = (TextView) view.findViewById(R.id.mlkz_secondary_page_headbar_section_2rd_title);
//        TextView title3 = (TextView) view.findViewById(R.id.mlkz_secondary_page_headbar_section_3rd_title);
//
//        //设置数据
//        struct_forumPosition forumPosition = mData.getForumPosition();
//        title1.setText(forumPosition.getHomePageName());
//        title2.setText(forumPosition.getSecondaryPageName());
//        title3.setText(forumPosition.getThirdPageName());
//
//        title1.setTag(forumPosition.getHomePageURL());
//        title2.setTag(forumPosition.getSecondaryPageURL());
//
//        //点击事件
//        OnClickListener leftViewListener = new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!(v instanceof TextView)) return;
//
//                TextView textView = (TextView) v;
//                String href = (String) v.getTag();
//                if (onHeadbarClickListener != null)
//                    onHeadbarClickListener.jumpToNewSection(textView.getText().toString(), href);
//                popupWindow.dismiss();
//            }
//        };
//
//        title1.setOnClickListener(leftViewListener);
//        title2.setOnClickListener(leftViewListener);
//
//        //2.设置子版块
//        ListView listView = (ListView) view.findViewById(R.id.mlkz_secondary_page_headbar_section_listview);
//        List<String> childSectionsName = new ArrayList<>();
//        for (struct_forumChildSectionNode i : mData.childSections) {
//            //子版块名称
//            childSectionsName.add(i.getName());
//        }
//        if (childSectionsName.size() == 0)
//            childSectionsName.add("无");
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.context, android.R.layout.simple_list_item_1, childSectionsName);
//        listView.setAdapter(adapter);
//        listView.setOnTouchListener(this);
//
//        popupView(view);
//    }
//
//    /**
//     * 中间-主题分类
//     */
//    private void showMiddleClassificationView() {
//        //添加ListView
//        ListView listView = new ListView(this.context);
//        listView.setTag(MIDDLE_CLASSIFICATION);
//        listView.setLayoutParams(new LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        listView.setBackgroundColor(Color.WHITE);
//
//        //读取数据
//        List<struct_forumSubjectClassificationNode> data = mData.getSubjectClassification();
//        //生成名字集合
//        List<String> classificationName = new ArrayList<>();
//        for (struct_forumSubjectClassificationNode i : data)
//            classificationName.add(i.getName());
//        if (!classificationName.contains("全部"))
//            classificationName.add(0, "全部");
//
//        //适配器
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.context,
//                android.R.layout.simple_list_item_1, classificationName);
//
//        listView.setOnTouchListener(this);
//        listView.setAdapter(adapter);
//
//        popupView(listView);
//    }
//
//    /**
//     * 右侧-排序方式
//     */
//    private void showRightSortView() {
//        //添加ListView
//        ListView listView = new ListView(this.context);
//        listView.setTag(RIGHT_SORT);
//        listView.setLayoutParams(new LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        listView.setBackgroundColor(Color.WHITE);
//
//        //数据集
//        String[] array = new String[]{"按发帖时间排序", "按回复时间排序"};
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.context, android.R.layout.simple_list_item_1, array);
//
//        listView.setOnTouchListener(this);
//        listView.setAdapter(adapter);
//
//        popupView(listView);
//    }
//
//    /**
//     * 显示View（会添加一个半透明黑色背景）
//     */
//    private void popupView(View view) {
//        popView = view;
//
//        //View最大只显示7成
//        final int screenHeight = ScreenUtil.getScreenHeight(this.context);
//        final float maxHeight = screenHeight * 0.7f;
//        view.measure(0, MeasureSpec.makeMeasureSpec((int) maxHeight, MeasureSpec.AT_MOST));
//        view.setLayoutParams(new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT, view.getMeasuredHeight()));
//
//        //设置半透明背景
//        View backgroundView = new View(this.context);
//        backgroundView.setBackgroundColor(Color.argb(0x80, 0, 0, 0));
//
//        //容器内添加要显示的View和半透明背景
//        LinearLayout viewGroup = new LinearLayout(this.context);
//        viewGroup.setOrientation(VERTICAL);
//        viewGroup.addView(popView);
//        viewGroup.addView(backgroundView);
//        viewGroup.setOnClickListener(this);
//
//        //显示
//        popupWindow = new PopupWindow(viewGroup, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//        popupWindow.setOnDismissListener(this);
//        popupWindow.showAsDropDown(linearLayout);
//    }
//
//
//
//}