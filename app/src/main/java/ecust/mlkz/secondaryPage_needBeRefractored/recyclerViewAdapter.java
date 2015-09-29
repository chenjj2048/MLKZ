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
 * Created by 彩笔怪盗基德 on 2015/9/27
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.mlkz.secondaryPage_needBeRefractored;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ecust.main.R;
import ecust.mlkz.secondaryPage_needBeRefractored.struct_Forum_Items.struct_forumDataRoot;
import ecust.mlkz.secondaryPage_needBeRefractored.struct_Forum_Items.struct_forumPostNode;

/**
 * RecyclcerView适配器
 */
public class recyclerViewAdapter extends RecyclerView.Adapter<recyclerViewAdapter.myViewHolder>
        implements View.OnClickListener {
    private Context context;
    private struct_forumDataRoot mData;
    private OnClickListener onClickListener;

    public recyclerViewAdapter(Context context, struct_forumDataRoot mData) {
        this.context = context;
        this.mData = mData;
    }

    public void setOnClickListener(recyclerViewAdapter.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    //传入数据集
    public void setData(struct_forumDataRoot data) {
        this.mData = data;
    }

    @Override
    public myViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.old_mlkz_seconary_page_item, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(myViewHolder viewHolder, int i) {
        //获取数据
        struct_forumPostNode node = mData.forumPosts.get(i);

        //设置数据
        viewHolder.node = node;
        viewHolder.authorImage.setBackgroundResource(R.drawable.icon);
        viewHolder.title.setText(node.getTitle());
        viewHolder.authorName.setText(node.getAuthorName());
        viewHolder.postTime.setText(node.getFirstReleaseTime());
        viewHolder.replyCount.setText("回复 " + node.getReply());

        //设置点击事件
        viewHolder.itemView.setOnClickListener(this);
        viewHolder.authorImage.setOnClickListener(this);
        viewHolder.authorName.setOnClickListener(this);
        viewHolder.itemView.setTag(viewHolder);
        viewHolder.authorImage.setTag(viewHolder);
        viewHolder.authorName.setTag(viewHolder);
    }

    @Override
    public int getItemCount() {
        return mData.forumPosts.size();
    }

    @Override
    public void onClick(View v) {
        if (onClickListener == null) return;

        myViewHolder viewHolder = (myViewHolder) v.getTag();
        if (viewHolder == null)
            throw new NullPointerException();

        if (v instanceof ImageView || v instanceof TextView) {
            //头像或用户名被点击
            onClickListener.OnAuthorSelected(viewHolder.node);
        } else {
            //整块item被点击
            onClickListener.OnPostItemSelected(viewHolder.node);
        }
    }

    public interface OnClickListener {
        //作者被点击
        void OnAuthorSelected(struct_forumPostNode node);

        //贴子被选中
        void OnPostItemSelected(struct_forumPostNode node);
    }

    //ViewHolder
    static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView authorImage;
        TextView title;
        TextView authorName;
        TextView postTime;
        TextView replyCount;
        struct_forumPostNode node;

        public myViewHolder(View itemView) {
            super(itemView);
            this.authorImage = (ImageView) itemView.findViewById(R.id.mlkz_secondary_page_item_author_image);
            this.title = (TextView) itemView.findViewById(R.id.mlkz_secondary_page_item_title);
            this.authorName = (TextView) itemView.findViewById(R.id.mlkz_secondary_page_item_authorname);
            this.postTime = (TextView) itemView.findViewById(R.id.mlkz_secondary_page_item_posttime);
            this.replyCount = (TextView) itemView.findViewById(R.id.mlkz_secondary_page_item_replycount);
        }
    }
}