package ecust.news;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ecust.main.R;
import lib.clsGlobal.Const;
import lib.clsGlobal.Global;
import lib.clsGlobal.clsApplication;
import lib.clsGlobal.clsExpiredTimeMangment;
import lib.clsGlobal.logUtil;
import lib.clsGlobal.timeUtil;
import lib.clsHttpAccess_CallBack;

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
 * Created by 彩笔怪盗基德 on 2015/7/31
 * Copyright (C) 2015 彩笔怪盗基德
 */

// 华理新闻Fragment
public class fragment_News_Catalog extends Fragment implements
        PullToRefreshBase.OnLastItemVisibleListener,
        PullToRefreshBase.OnRefreshListener2, AdapterView.OnItemClickListener,
        clsHttpAccess_CallBack.OnHttpVisitListener {
    private final clsNewsCatalog mNewsCatalog = new clsNewsCatalog();


    HashMap<String, List<struct_NewsCatalogItem>> cacheData = new HashMap<>();
    boolean currentIsVisible = false;     //当前Fragment是否可见
    private String catalogName;   //版块名称，如校园要闻、综合新闻、图说华理等
    private String catalogUrl;
    private PullToRefreshListView wListView;
    private NewsAdapter mAdapter = new NewsAdapter();
    private boolean isLoading = false;

    public fragment_News_Catalog() {
    }

    /**
     * 先加载缓存数据，没有就加载网络数据
     */
    public void initializeNewsData() {
//        if (!currentIsVisible) return;

        if (mNewsCatalog.mList.size() <= 0) {
            //加载数据库数据
            mNewsCatalog.mList = new DataBase_News(catalogName).catalog.getAllData();
            mAdapter.notifyDataSetChanged();

            //获取地址访问
            if (mAdapter.getCount() <= 0) {
                isLoading = true;
                logUtil.i(this, "initializeNewsData");
                clsHttpAccess_CallBack.getSingleton().getHttp(catalogUrl, this);
            }
        }
    }

    /**
     * ListView下拉刷新
     */
    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        new loadData().execute();   // 执行加载任务
        initializeNewsData();  //没数据的话就加载
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
    }

    /**
     * 设置初始化参数(替代构造函数无法设参数)
     *
     * @param catalogName 目录名称（如：综合新闻等）
     * @param catalogUrl  主目录URL
     */
    public void setParameter(String catalogName, String catalogUrl) {
        this.catalogName = catalogName;
        this.catalogUrl = catalogUrl;
    }

    /**
     * ListView滚动到了底部最后一条项目
     */
    @Override
    public void onLastItemVisible() {
        loadNextPage();
    }

    //加载下一页
    public void loadNextPage() {
        if (mNewsCatalog.nextPage == mNewsCatalog.itemIsLastOne) {
            Global.toastMsg("已无更多数据");
        } else {
            //不要重复加载
            if (!isLoading && clsApplication.receiver.isWebConnected()) {
                isLoading = true;
                //获取“下一页”url，对应的下一个网址略有不同
                String website = catalogUrl +
                        String.valueOf(catalogName.equals("通知公告") ? "?page=" : "&page=") +
                        mNewsCatalog.nextPage;
                logUtil.i(this, "[加载下一页数据]" + website);
                clsHttpAccess_CallBack.getSingleton().getHttp(website, this);
            }
        }
    }

    /**
     * 点击到具体新闻，准备开始展开
     *
     * @param parent   parent
     * @param view     view
     * @param position 序号
     * @param id       id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        position--;
        struct_NewsCatalogItem item = mNewsCatalog.mList.get(position);

        //打开新的窗体,显示具体新闻内容
        Intent new_activity = new Intent();
        new_activity.setClass(getActivity(), act_News_Detail.class);
        //传进去URL和新闻目录分类
        new_activity.putExtra("URL", item.url);              //发送要打开的URL
        new_activity.putExtra("catalogName", catalogName);       //发送所在分类
        startActivity(new_activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_catalog_fragment, container, false);
        //设置Adapter
        wListView = (PullToRefreshListView) view.findViewById(R.id.news_listview_pulltorefresh);
        // 只允许下拉刷新
        wListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        wListView.setAdapter(mAdapter);
        //PullToRefresh刷新事件
        wListView.setOnRefreshListener(this);

        //监听滚动到底部
        wListView.setOnLastItemVisibleListener(this);
        //新闻点击事件
        wListView.setOnItemClickListener(this);

        mNewsCatalog.nextPage = new clsExpiredTimeMangment(catalogName).getInt("nextPage", 1);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        save_NextPage();        //保存数据，下次加载，若数据未过期，则不必重复下载
    }

    /**
     * 设置管理下一页的URL
     * 避免短时间内反复加载网页数据
     * 例如，8h才允许访问一次网络，更新重复数据
     * 其他时段通通获取本地数据
     */
    public void save_NextPage() {
        //先获取数据，比较后，判断是否需要存入
        clsExpiredTimeMangment expiredTimeMangment = new clsExpiredTimeMangment(catalogName);
        int value_obtained = expiredTimeMangment.getInt("nextPage", 1);

        //保存数据
        // 下一页是还没加载到的数据，所以要-1，得到当前位置
        int value_current = (mNewsCatalog.nextPage != mNewsCatalog.itemIsLastOne) ?
                mNewsCatalog.nextPage : mNewsCatalog.itemIsLastOne;
        if (value_current > value_obtained) {
            expiredTimeMangment.putInt("nextPage", value_current);
        }
    }

    /**
     * 数据解析、数据存储放在子线程中
     */
    @Override
    public void onHttpBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {
        if (bSucceed) {
            //解析新闻数据
            List<struct_NewsCatalogItem> result = mNewsCatalog.parseNewsCatalog(rtnHtmlMessage);

            if (result.size() <= 0)
                mNewsCatalog.nextPage = mNewsCatalog.itemIsLastOne;   //标记这是最后一页了，之后不再加载数据
            else if (mNewsCatalog.nextPage != mNewsCatalog.itemIsLastOne)
                mNewsCatalog.nextPage++;

            //去除重复数据
            Iterator<struct_NewsCatalogItem> iterator = result.iterator();
            while (iterator.hasNext()) {
                struct_NewsCatalogItem item = iterator.next();
                if (mNewsCatalog.mList.contains(item))
                    iterator.remove();
            }

            //存入缓存，到主线程后取出，避免多线程错误操作
            cacheData.put(url, result);

            //SQLite数据存储
            if (result.size() > 0)
                new DataBase_News(catalogName).catalog.InsertOrReplaceData(result);
        }
    }

    @Override
    public void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {
        if (!bSucceed) {
            logUtil.toast("服务器连接失败,请稍后再试");
            isLoading = false;
            return;
        }

        //缓存中取出数据
        List<struct_NewsCatalogItem> result = cacheData.get(url);

        //添加数据至mList(去除重复数据)
        if (result == null || result.size() <= 0) {
            isLoading = false;
            return;
        }

        //合并结果(放在主线程里，避免出错)
        Iterator<struct_NewsCatalogItem> iterator = result.iterator();
        while (iterator.hasNext()) {
            mNewsCatalog.mList.add(iterator.next());
        }
//        Collections.sort(mNewsCatalog.mList);

        //更新ListView中数据
        if (result.size() > 0) {
            mAdapter.notifyDataSetChanged();
            logUtil.i(this, "[NotifyDataSetChanged]当前数量=" + mAdapter.getCount());
        }

        cacheData.remove(url);
        isLoading = false;
    }

    @Override
    public void onPictureLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {
    }

    @Override
    public void onPictureBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {
    }

    //设置当前Fragment是否为可见,初始化加载下数据
    public void setSelected(boolean visible) {
        this.currentIsVisible = visible;
        if (visible)
            initializeNewsData();      //可见时刷新数据
    }

    public void initView() {
        initializeNewsData();
    }


    private class clsNewsCatalog {
        private final int itemIsLastOne = Integer.MAX_VALUE; //已加载至最后一页，标记
        public List<struct_NewsCatalogItem> mList = new ArrayList<>();          // 数据集
        private int nextPage = 1;             //对应新闻索引的页数

        /**
         * 根据新闻Html页面数据解析数据
         *
         * @param html 页面
         */
        public List<struct_NewsCatalogItem> parseNewsCatalog(String html) {
            List<struct_NewsCatalogItem> rtnList = new ArrayList<>();       //解析完成后得到的数据集
            try {
                Document doc = Jsoup.parse(html);
                Element left = doc.getElementsByClass("left").first();

                //提取分类标题
                String catalogTitle = left.getElementsByClass("left_title").text().trim();
                //日志
//                logUtil.i(this, "=========" + catalogTitle + "=========");

                //提取新闻目录主体部分
                Elements collection_li = left.getElementsByClass("content").first().select("ul").first().select("li");
                for (Element li : collection_li) {
                    struct_NewsCatalogItem item = new struct_NewsCatalogItem();
                    item.title = li.select("span").last().text();
                    item.time = li.getElementsByClass("time").first().text();
                    item.url = Const.news_url + li.select("a").attr("href");
                    rtnList.add(item);

                    //日志
//                    logUtil.i(this, item.title + " " + item.url + " " + item.time);
                }
                return rtnList;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    //华理新闻主页面Adapter
    private class NewsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mNewsCatalog.mList.size();
        }

        @Override
        public Object getItem(int index) {
            return mNewsCatalog.mList.get(index);
        }

        @Override
        public long getItemId(int index) {
            return index;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup arg2) {
            ViewHolder viewHolder;
            if (convertView == null) {
                Context context = getActivity();
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.news_catalog_fragment_item, null);

                viewHolder = new ViewHolder();
                viewHolder.title = (TextView) convertView.findViewById(R.id.listview_news_title);
                viewHolder.time = (TextView) convertView.findViewById(R.id.listview_news_updatetime);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();     //获取控件集
            }

            struct_NewsCatalogItem item = mNewsCatalog.mList.get(position);  //取得当前的数据

            //返回几天前
            String timeMessage = item.time + "  " + timeUtil.getDeltaDate(item.time);
            viewHolder.time.setText(timeMessage);
            viewHolder.title.setText(item.title);


            //预加载网络数据
            final int preLoadCount = 100;       //预加载的数据数量,每页为20条数据
            if (!isLoading && mNewsCatalog.nextPage != mNewsCatalog.itemIsLastOne && clsApplication.receiver.isWebConnected())
                if (position > mNewsCatalog.nextPage * 20 - preLoadCount) {
                    loadNextPage();
                }

            return convertView;
        }

        private class ViewHolder {
            public TextView title;  //标题
            public TextView time;   //发表时间，含几天前
        }
    }

    private class loadData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(700);   //停留显示数秒刷新信息
            } catch (Exception e) {
                logUtil.e(this, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            wListView.onRefreshComplete();
            mAdapter.notifyDataSetChanged();     //通知更新数据
        }
    }
}
