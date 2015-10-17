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
 * Created by 彩笔怪盗基德 on 2015/10/11
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.mlkz.secondaryPage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import ecust.main.R;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_CurrentSection;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_ForumPageAllData;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_PrimarySectionNode;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_SecondarySectionNode;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_TertiarySectionNode;
import lib.logUtils.logUtil;
import lib.logUtils.logUtil.LogStatus;

/**
 * 点击版块目录，弹出一组关联的ListView
 */
public class catalogListView extends LinearLayout {
    private Context context;
    private ListView mListView1;
    private ListView mListView2;
    private ListView mListView3;
    private Adapter1 mAdapter1;
    private Adapter2 mAdapter2;
    private Adapter3 mAdapter3;

    //背景主色调
    private int color_1st;
    private int color_2nd;
    private int color_3rd;
    private int color_focused_text;
    private int color_unfocused_text;

    //数据
    private List<struct_PrimarySectionNode> mPrimaryNodes;
    private struct_CurrentSection mCurrentSection;
    private IdentityHashMap<String, struct_TertiarySectionNode> mChildSectionIdentityHashMap;

    //当前位置
    private int mCurrentPosition_1;
    private int mCurrentPosition_2;
    private int mCurrentPosition_3;


    //Callback
    private OnChildSectionSelectedListener onChildSectionSelectedListener;

    @SuppressLint("InflateParams")
    public catalogListView(Context context) {
        super(context);
        //初始化主色调
        initColors();
        //加载View
        View mParent = LayoutInflater.from(context).inflate(R.layout.mlkz_secondary_headbar_left_section, null);
        this.addView(mParent);
        this.setBackgroundColor(Color.WHITE);
        this.context = context;
        this.mListView1 = (ListView) findViewById(R.id.mlkz_secondary_headbar_listview1);
        this.mListView2 = (ListView) findViewById(R.id.mlkz_secondary_headbar_listview2);
        this.mListView3 = (ListView) findViewById(R.id.mlkz_secondary_headbar_listview3);
        this.mListView1.setBackgroundColor(color_1st);
        this.mListView2.setBackgroundColor(color_2nd);
        this.mListView3.setBackgroundColor(color_3rd);
    }

    /**
     * 设置数据
     *
     * @param mData 数据
     */
    public void setData(struct_ForumPageAllData mData) {
        //设置数据
        this.mPrimaryNodes = mData.getPrimarySectionNodes();

        //从本地文件加载三级版块的内容（从网上加载会加载不全，仅能加载当前二级板块的子版块）
        initTertiarySectionData();
        //初始化当前位置
        mCurrentSection = mData.getCurrentSection();
        initCurrentFocusedPosition(mCurrentSection);
        //显示一级版块
        parsePrimaryListView();
        //显示二级版块
        parseSecondaryListView();
        //显示三级版块
        parseTertiaryListView();
    }

    /**
     * 加载三级版块信息
     */
    @LogStatus(aliasName = "三级版块信息加载")
    private void initTertiarySectionData() {
        InputStream inputStream = getResources().openRawResource(R.raw.mlkz_tertiary_section_struct);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        logUtil log = new logUtil(this);

        String line;
        try {
            mChildSectionIdentityHashMap = new IdentityHashMap<>();
            while ((line = reader.readLine()) != null) {
                //结构为：0-二级父版块名称|1-三级子版块名称|2-三级子版块URL
                String[] attr = line.split("\\|");

                //创建数据
                struct_TertiarySectionNode item_3rd = new struct_TertiarySectionNode(attr[1], attr[2]);
                //添加数据
                mChildSectionIdentityHashMap.put(attr[0], item_3rd);
            }
        } catch (Exception e) {
            log.e(e.toString());
        } finally {
            //关闭连接
            try {
                reader.close();
            } catch (IOException e) {
                log.e(e.toString());
            }
        }
    }

    /**
     * 初始化当前获得焦点的View位置
     */
    public void initCurrentFocusedPosition(struct_CurrentSection mCurrentSection) {
        //初值
        mCurrentPosition_1 = 0;
        mCurrentPosition_2 = 0;
        mCurrentPosition_3 = 0;

        position_found:
        for (int i = 0; i < mPrimaryNodes.size(); i++) {
            struct_PrimarySectionNode item1 = mPrimaryNodes.get(i);
            if (!item1.getName().equals(mCurrentSection.getPrimarySectionName())) continue;

            //找到了一级节点
            mCurrentPosition_1 = i;
            for (int j = 0; j < item1.getSecondaryNodes().size(); j++) {
                struct_SecondarySectionNode item2 = item1.getSecondaryNodes().get(j);
                if (!item2.getName().equals(mCurrentSection.getSecondarySectionName())) continue;

                //找到了二级节点
                mCurrentPosition_2 = j;
                if (item2.getTertiaryNodes() != null) {
                    for (int k = 0; k < item2.getTertiaryNodes().size(); k++) {
                        struct_TertiarySectionNode item3 = item2.getTertiaryNodes().get(k);
                        if (item3.getName().equals(mCurrentSection.getTertiarySectionName())) {
                            //找到了三级节点
                            mCurrentPosition_3 = k;
                            break position_found;
                        }
                    }
                }

                break position_found;
            }
        }
    }

    //设置下TextView属性
    private TextView decorateTextView(TextView v) {
        v.setGravity(Gravity.CENTER);
        v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        return v;
    }

    /**
     * 初始化配色方案
     */
    private void initColors() {
        this.color_1st = Color.rgb(0xff, 0xff, 0xff);
        this.color_2nd = Color.rgb(0xf0, 0xf0, 0xf0);
        this.color_3rd = Color.rgb(0xe0, 0xe0, 0xe0);
        this.color_focused_text = getResources().getColor(android.R.color.holo_red_light);
        this.color_unfocused_text = new TextView(getContext()).getCurrentTextColor();
    }

    /**
     * 一级版块
     */
    private void parsePrimaryListView() {
        //装载数据
        mAdapter1 = new Adapter1(this.context, android.R.layout.simple_list_item_1, mPrimaryNodes);
        mListView1.setAdapter(mAdapter1);
    }

    /**
     * 二级版块
     */
    private void parseSecondaryListView() {
        //适配器
        mAdapter2 = new Adapter2();
        mListView2.setAdapter(mAdapter2);
    }

    /**
     * 三级版块
     */
    private void parseTertiaryListView() {
        mAdapter3 = new Adapter3();
        mListView3.setAdapter(mAdapter3);
    }

    protected void setOnChildSectionSelectedListener(OnChildSectionSelectedListener listener) {
        this.onChildSectionSelectedListener = listener;
    }

    /**
     * 版块被选择的消息接口
     */
    protected interface OnChildSectionSelectedListener {
        //哪一项被点击
        void OnChildSectionSelected(struct_TertiarySectionNode mSection_3rd);
    }

    /**
     * 一级适配器
     */
    class Adapter1 extends ArrayAdapter<struct_PrimarySectionNode> implements View.OnClickListener {
        public Adapter1(Context context, int resource, @NonNull List<struct_PrimarySectionNode> objects) {
            super(context, resource, objects);
        }

        @Override
        @LogStatus(aliasName = "一级ListView被点击")
        public void onClick(View v) {
            //切换当前选中节点
            final struct_PrimarySectionNode item_1st = (struct_PrimarySectionNode) v.getTag();
            //更新当前项
            if (mCurrentPosition_1 != getPosition(item_1st)) {
                mCurrentPosition_1 = getPosition(item_1st);
                mCurrentPosition_2 = 0;
            }

            //Adapter刷新
            mAdapter1.notifyDataSetChanged();
            mAdapter2.notifyDataSetChanged();

            catalogListView.this.getLayoutParams().height = mListView1.getLayoutParams().height;
        }

        @Override
        @LogStatus(aliasName = "一级Adapter")
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
                convertView.setOnClickListener(this);
                textView = decorateTextView((TextView) convertView);
            } else {
                textView = (TextView) convertView;
            }

            //获取数据集
            struct_PrimarySectionNode item_1st = getItem(position);

            //设置文本内容
            textView.setText(item_1st.getName());
            textView.setTag(item_1st);

            if (position == mCurrentPosition_1) {
                //当前节点
                textView.setBackgroundColor(color_2nd);
                textView.setTextColor(color_focused_text);
            } else {
                //非当前节点
                textView.setBackgroundColor(color_1st);
                textView.setTextColor(color_unfocused_text);
            }
            return convertView;
        }
    }

    /**
     * 二级适配器
     */
    class Adapter2 extends BaseAdapter implements OnClickListener {

        @Override
        public void onClick(View v) {
            final int position = (int) v.getTag();

            if (mCurrentPosition_2 != position) {

                //更新当前项
                mCurrentPosition_2 = position;

                //Adapter刷新（三级版块会随之更新）
                mAdapter2.notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return mPrimaryNodes.get(mCurrentPosition_1).getSecondaryNodes().size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public struct_SecondarySectionNode getItem(int position) {
            return mPrimaryNodes.get(mCurrentPosition_1).getSecondaryNodes().get(position);
        }

        @Override
        @LogStatus(aliasName = "二级Adapter")
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
                convertView.setOnClickListener(this);
                textView = decorateTextView((TextView) convertView);
            } else {
                textView = (TextView) convertView;
            }

            //设置数据
            String title_2nd = getItem(position).getName();
            textView.setText(title_2nd);
            textView.setTag(position);

            if (position == mCurrentPosition_2) {
                //找到对应项目
                textView.setBackgroundColor(color_3rd);
                textView.setTextColor(color_focused_text);

                //更新三级版块
                mAdapter3.updateListViewContent_3rd(getItem(position));
            } else {
                //不是对应项目
                textView.setBackgroundColor(color_2nd);
                textView.setTextColor(color_unfocused_text);
            }
            return convertView;
        }
    }

    /**
     * 三级Adapter
     * 所有的数据都从本地存储的数据中查找
     * 因为一次网络请求仅能获得一次子版块
     * 版块相对固定，写死就行
     */
    class Adapter3 extends BaseAdapter implements OnClickListener {
        //数据集
        private List<struct_TertiarySectionNode> mList_3rd = new ArrayList<>();

        /**
         * 更新三级ListView内容
         *
         * @param mSecondaryNode 二级版块信息
         */
        public void updateListViewContent_3rd(struct_SecondarySectionNode mSecondaryNode) {
            mList_3rd.clear();
            //添加第一项内容
            mList_3rd.add(new struct_TertiarySectionNode("全部", mSecondaryNode.getUrl()));

            //通过二级版块标题寻找关联的三级版块信息
            Set<Entry<String, struct_TertiarySectionNode>> allSet = mChildSectionIdentityHashMap.entrySet();
            Iterator<Entry<String, struct_TertiarySectionNode>> iterator = allSet.iterator();
            for (; iterator.hasNext(); ) {
                Entry<String, struct_TertiarySectionNode> entry = iterator.next();
                if (!mSecondaryNode.getName().equals(entry.getKey())) continue;

                //添加数据
                mList_3rd.add(entry.getValue());
            }

            mAdapter3.notifyDataSetChanged();
        }

        @Override
        public void onClick(View v) {
            final int position = (int) v.getTag();

            if (onChildSectionSelectedListener != null)
                onChildSectionSelectedListener.OnChildSectionSelected(getItem(position));
        }

        @Override
        public int getCount() {
            return mList_3rd.size();
        }

        @Override
        public struct_TertiarySectionNode getItem(int position) {
            return mList_3rd.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        @LogStatus(aliasName = "三级版块 getView")
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
                convertView.setOnClickListener(this);
                convertView.setBackgroundColor(color_3rd);
                textView = decorateTextView((TextView) convertView);
            } else {
                textView = (TextView) convertView;
            }

            //标题名称
            String title_3rd = getItem(position).getName();
            textView.setText(title_3rd);
            textView.setTag(position);

            if (mCurrentSection.getTertiarySectionName() == null) {
                //当前在二级版块

                //判断“全部”是否为选中项
                if ("全部".equals(title_3rd))
                    textView.setTextColor(color_focused_text);
                else
                    textView.setTextColor(color_unfocused_text);
            } else {
                //当前在三级子版块

                if (title_3rd.equals(mCurrentSection.getTertiarySectionName())) {
                    //找到对应项目
                    textView.setTextColor(color_focused_text);
                } else {
                    //不是对应项目
                    textView.setTextColor(color_unfocused_text);
                }
            }
            return convertView;
        }
    }
}
