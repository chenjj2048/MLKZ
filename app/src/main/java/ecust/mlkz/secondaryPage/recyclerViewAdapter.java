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
 * Created by 彩笔怪盗基德 on 2015/10/6
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.mlkz.secondaryPage;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;

import ecust.main.R;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_MLKZ_Data;
import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_PostNode;
import lib.InjectViewUtil;
import lib.InjectViewUtil.InjectView;
import lib.logUtils.logUtil;

/**
 * 帖子目录Adapter
 */
public class recyclerViewAdapter extends RecyclerView.Adapter<recyclerViewAdapter.ViewHolder> {
    private Context context;
    private ImageLoader mImageLoader;
    //数据集
    private struct_MLKZ_Data mData;
    //点击事件类
    private Listeners mListeners = new Listeners();

    public recyclerViewAdapter(Context context, RequestQueue mQueue) {
        this.context = context;
        this.mImageLoader = new ImageLoader(mQueue, new imageLruCache());
    }

    public struct_MLKZ_Data getData() {
        return this.mData;
    }

    protected void setData(struct_MLKZ_Data mData) {
        this.mData = mData;
        this.notifyDataSetChanged();
        new logUtil(this).d("当前帖子总数 = " + this.getItemCount());
    }

    @Override
    @SuppressWarnings("all")
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.mlkz_secondary_recyclerview_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        //数据集
        struct_PostNode node = mData.mPostsList.get(position);
        //=================第一行==================
        //标题
        viewHolder.mTitle.setText(node.getTitle());
        viewHolder.mTitle.setTextColor(this.context.getResources().getColor(R.color.black26));

        viewHolder.mAuthorName.setText(node.getAuthor().getName());
        viewHolder.mLastReplyTime.setText(node.getLastReplyTime());
        //主题分类(筛选)
        if (node.getClassificationName().equals("")) {
            viewHolder.mClassification.setVisibility(View.GONE);
        } else {
            viewHolder.mClassification.setText(node.getClassificationName());
            viewHolder.mClassification.setTextSize(lib.clsDimensionConvert.dip2px(this.context, 6));
            viewHolder.mClassification.setVisibility(View.VISIBLE);
            viewHolder.mClassification.setOnClickListener(mListeners.mClassificationClick);
            viewHolder.mClassification.setOnCreateContextMenuListener(mListeners.mClassificationContextMenu);
        }
        //回帖奖励
        if (node.getRewardSum() > 0)
            viewHolder.mRewardSum.setText("回帖奖励 " + node.getRewardSum());
        else
            viewHolder.mRewardSum.setText("");


        //==================第二行================
        viewHolder.mFirstReleaseTime.setText(node.getFirstReleaseTime());
        viewHolder.mViewsCount.setText("查看 " + node.getVisitCount());
        viewHolder.mReplyCount.setText("回复 " + node.getReplyCount());

        //设置头像图片
        loadHeadImage(viewHolder.mAuthorImage, node);
    }

    /**
     * 加载头像网络图片
     */
    private void loadHeadImage(ImageView mAuthorImage, struct_PostNode node) {
        ImageListener imageListener = ImageLoader.getImageListener(mAuthorImage,
                R.drawable.mlkz_default_head_image, R.drawable.mlkz_default_head_image);

        this.mImageLoader.get(node.getAuthor().getImageUrl(), imageListener);
    }

    @Override
    public int getItemCount() {
        return (mData == null || mData.mPostsList == null) ? 0 : mData.mPostsList.size();
    }

    /**
     * 监听集合
     */
    private class Listeners {
        /**
         * 主题分类-点击
         */
        private View.OnClickListener mClassificationClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开上下文菜单
                v.performLongClick();
            }
        };

        /**
         * 显示上下文菜单
         */
        private View.OnCreateContextMenuListener mClassificationContextMenu = new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                if (mData == null || mData.mPageInformation == null) return;
                if (mData.mPageInformation.getClassificationNodes() == null) return;
                //添加主题分类
                for (int i = 0; i < mData.mPageInformation.getClassificationNodes().size(); i++) {
                    menu.add(0, i, 0, mData.mPageInformation.getClassificationNodes().get(i).getName());
                }
                menu.setHeaderTitle("主题筛选");
            }
        };


    }

    /**
     * 缓存图片
     */
    private class imageLruCache implements ImageLoader.ImageCache {
        final int maxSize = 1024 * 1024;
        LruCache<String, Bitmap> lruCache;

        public imageLruCache() {
            this.lruCache = new LruCache<String, Bitmap>(maxSize) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount();
                }
            };
        }

        @Override
        public Bitmap getBitmap(String url) {
            return lruCache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            lruCache.put(url, bitmap);
        }
    }

    /**
     * ViewHolder集合
     */
    protected class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.mlkz_secondary_page_item_title)
        TextView mTitle;            //标题

        @InjectView(R.id.mlkz_secondary_page_item_authorname)
        TextView mAuthorName;       //作者名称

        @InjectView(R.id.mlkz_secondary_page_item_posttime)
        TextView mFirstReleaseTime; //发帖时间

        @InjectView(R.id.mlkz_secondary_page_item_lastposttime)
        TextView mLastReplyTime;    //最后回帖时间

        @InjectView(R.id.mlkz_secondary_page_item_viewscount)
        TextView mViewsCount;       //查看数量

        @InjectView(R.id.mlkz_secondary_page_item_replycount)
        TextView mReplyCount;       //回复数量

        @InjectView(R.id.mlkz_secondary_page_item_classification)
        RoundRectTextView mClassification;   //主题分类（标签筛选）

        @InjectView(R.id.mlkz_secondary_page_item_reward)
        TextView mRewardSum;        //回帖奖励

        @InjectView(R.id.mlkz_secondary_page_item_author_image)
        ImageView mAuthorImage;     //作者头像

        public ViewHolder(View itemView) {
            super(itemView);
            InjectViewUtil.inject(this, itemView);
        }
    }
}
