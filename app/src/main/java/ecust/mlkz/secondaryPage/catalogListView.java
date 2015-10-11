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

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ecust.main.R;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_CurrentSection;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_ForumPageAllData;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_PrimarySectionNode;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_SecondarySectionNode;

/**
 * 点击版块目录，弹出一组关联的ListView
 */
public class catalogListView {
    private Context context;
    private View mParent;
    private ListView mListView1;
    private ListView mListView2;

    private List<struct_PrimarySectionNode> mPrimaryNodes;
    private struct_CurrentSection mCurrentSection;

    public catalogListView(Context context, View mParent, struct_ForumPageAllData mData) {
        //设置数据
        this.context = context;
        this.mParent = mParent;
        this.mListView1 = (ListView) mParent.findViewById(R.id.mlkz_secondary_headbar_listview1);
        this.mListView2 = (ListView) mParent.findViewById(R.id.mlkz_secondary_headbar_listview2);
        this.mPrimaryNodes = mData.getPrimarySectionNodes();
        this.mCurrentSection = mData.getCurrentSection();

        //显示一级版块
        parsePrimaryListView();
        //显示二级版块
        parseSecondaryListView();
    }

    /**
     * 二级版块
     */
    private void parseSecondaryListView() {
        Adapter1 adapter1 = (Adapter1) mListView1.getAdapter();
        List<struct_SecondarySectionNode> nodes2 = null;

        nodes2 = adapter1.getCurrentNode().getSecondaryNodes();

//        Adapter2 adapter2 = new Adapter2(this.context, android.R.layout.simple_list_item_1, nodes2);
//        mListView2.setAdapter(adapter2);
    }

    /**
     * 一级版块
     */
    private void parsePrimaryListView() {
        Adapter1 adapter1 = new Adapter1(this.context, android.R.layout.simple_list_item_1, mPrimaryNodes);
        mListView1.setAdapter(adapter1);
    }

    public View getView() {
        return this.mParent;
    }

    /**
     * 一级适配器
     */
    class Adapter1 extends ArrayAdapter<struct_PrimarySectionNode> {
        //当前对应的二级节点集合
        private struct_PrimarySectionNode mCurrentNode;

        public Adapter1(Context context, int resource, List<struct_PrimarySectionNode> objects) {
            super(context, resource, objects);
        }

        public struct_PrimarySectionNode getCurrentNode() {

            return mCurrentNode;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(catalogListView.this.context)
                        .inflate(android.R.layout.simple_list_item_1, null);
            }
            //获取数据集
            struct_PrimarySectionNode item = getItem(position);
            //初始化当前节点位置
            if (mCurrentSection == null) {
                if (item.getName().equals(mCurrentNode.getName())) {
                    mCurrentNode = item;
                }
            }

            //设置文本内容
            TextView textView = (TextView) convertView;
            textView.setText(item.getName());

            if (item.getName().equals(mCurrentNode.getName())) {
                //当前节点
                textView.setBackgroundColor(Color.CYAN);
            } else {
                //非当前节点
                textView.setBackgroundColor(Color.WHITE);
            }
            return convertView;
        }
    }

    /**
     * 二级适配器
     */
    class Adapter2 extends ArrayAdapter<struct_SecondarySectionNode> {
        public Adapter2(Context context, int resource, List<struct_SecondarySectionNode> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(catalogListView.this.context)
                        .inflate(android.R.layout.simple_list_item_1, null);
            }

            struct_SecondarySectionNode item = getItem(position);

            //设置数据
            TextView textView = (TextView) convertView;
            textView.setText(item.getName());

            return convertView;
        }
    }


}
