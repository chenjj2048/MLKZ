package ecust.lecture;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ecust.main.R;
import lib.Const;
import lib.clsApplication;
import lib.clsUtils.clsExpiredTimeMangment;
import lib.clsUtils.httpUtil;
import lib.clsUtils.timeUtil;
import lib.logUtils.abstract_LogUtil;
import CustomWidgets.BaseAppCompatActivity;
import statistics.clsUmeng;

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
 * Created by 彩笔怪盗基德 on 2015/6/5
 * Copyright (C) 2015 彩笔怪盗基德
 */

//讲座版块详细目录
public class activity_Lecture_Catalog extends BaseAppCompatActivity implements
        PullToRefreshBase.OnLastItemVisibleListener,
        PullToRefreshBase.OnRefreshListener2, AdapterView.OnItemClickListener,
        httpUtil.OnHttpVisitListener {

    private final int itemIsLastOne = Integer.MAX_VALUE;       // 已加载至最后一页标记
    private List<struct_LectureCatalogItem> mList = new ArrayList<>();      //数据集

    private int nextPage = 1;        //对应新闻索引的页数

    private PullToRefreshListView wListView;
    private LectureAdapter mAdapter;
    private boolean flag_isLoading = false;      //是否正在访问的标记

    private DataBase_Lecture dataBase_lecture = new DataBase_Lecture(this);    //获取数据库
    private HashMap<String, List<struct_LectureCatalogItem>> cacheData = new HashMap<>();  //临时存放解析出来的数据

    /**
     * 子线程中运行，避免listview卡顿
     */
    @Override
    public void onHttpBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {
        if (!bSucceed) return;

        //解析新闻数据
        List<struct_LectureCatalogItem> result = parseLectureCatalog(rtnHtmlMessage);

        //添加数据至mList(去除重复数据)
        if (result == null) return;
        if (result.size() <= 0) {
            //标记这是最后一页了，之后不再加载数据
            nextPage = itemIsLastOne;
            return;
        } else if (nextPage != itemIsLastOne)
            nextPage++;   //有新数据加载后，就设置页数加一

        //去除重复数据
        Iterator<struct_LectureCatalogItem> iterator = result.iterator();
        while (iterator.hasNext()) {
            if (mList.contains(iterator.next()))
                iterator.remove();
        }

        //存入数据库
        if (result.size() > 0)
            dataBase_lecture.catalog.InsertOrReplaceData(result);

        //存入缓存，等到主线程再更新,避免ListView、adapter与数据不同步
        cacheData.put(url, result);
    }

    @Override
    public void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {
        if (!bSucceed) {
            abstract_LogUtil.toast("服务器连接失败,请稍后再试");
        } else {
            //取出缓存数据
            List<struct_LectureCatalogItem> result = cacheData.get(url);

            if (result != null && result.size() > 0) {
                // 添加数据，重复数据已被过滤
                Iterator<struct_LectureCatalogItem> iterator = result.iterator();
                while (iterator.hasNext()) {
                    mList.add(iterator.next());
                }
                Collections.sort(mList);

                cacheData.remove(url);      //去除引用

                //ListView更新
                mAdapter.notifyDataSetChanged();
                abstract_LogUtil.i(this, "[NotifyDataSetChanged]当前数量=" + mAdapter.getCount());
            }
        }
        flag_isLoading = false;
    }

    @Override
    public void onPictureLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {
    }

    @Override
    public void onPictureBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecture_catalog);

        wListView = (PullToRefreshListView) findViewById(R.id.lecture_listview_pulltorefresh);
        wListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);   //只允许下拉刷新

        mAdapter = new LectureAdapter(this);         //添加Adapter
        wListView.setAdapter(mAdapter);

        wListView.setOnRefreshListener(this);           //PullToRefresh刷新事件
        wListView.setOnLastItemVisibleListener(this);   //监听滚动到底部
        wListView.setOnItemClickListener(this);         //新闻点击

        //设置空白页
        wListView.setEmptyView(findViewById(R.id.lecture_pulltorefresh_emptyview));

        nextPage = new clsExpiredTimeMangment(this).getInt("nextPage", 1);

        initializeLoadData();       //初次尝试加载

        //标题
        initToolBar();
    }

    private void initToolBar() {
        getSupportToolBar(this).setTitle("讲座信息");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        save_NextPage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //统计事件
        clsUmeng.onEvent(this);
        clsUmeng.onPause(this);
    }

    /**
     * 设置管理下一页的URL
     * 避免短时间内反复加载网页数据
     * 例如，8h才允许访问一次网络，更新重复数据
     * 其他时段通通获取本地数据
     */
    public void save_NextPage() {
        //先获取数据，比较后，判断是否需要存入
        clsExpiredTimeMangment expiredTimeMangment = new clsExpiredTimeMangment(this);
        int value_obtained = expiredTimeMangment.getInt("nextPage", 1);

        //保存数据
        // 下一页是还没加载到的数据，所以要-1，得到当前位置
        int value_current = nextPage != itemIsLastOne ? nextPage : itemIsLastOne;
        if (value_current > value_obtained) {
            expiredTimeMangment.putInt("nextPage", value_current);
        }
    }

    /**
     * ListView滚动到了底部最后一条项目
     */
    @Override
    public void onLastItemVisible() {
        //滑到底部，加载下一页
        loadNextPage();
    }

    public void loadNextPage() {
        if (nextPage == itemIsLastOne)
            abstract_LogUtil.toast("已无更多数据");
        else {
            //不要重复加载
            if (!flag_isLoading) {
                if (clsApplication.receiver.isWebConnected()) {
                    flag_isLoading = true;
                    //获取“下一页”url
                    String website = Const.lecture_url + "?page=" + nextPage;
                    abstract_LogUtil.i(this, "[正在加载下一页]" + website);
                    httpUtil.getSingleton().getHttp(website, this);
                } else
                    abstract_LogUtil.toast("网络不可用");
            } else
                abstract_LogUtil.i(this, "[正在加载下一页]加载中，请勿重复");
        }
    }

    /**
     * ListView下拉刷新
     */
    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        new loadData().execute();      //停留数秒
        initializeLoadData();
    }

    /**
     * ListView上拉加载数据，这里没用
     */
    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
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
        struct_LectureCatalogItem item = mList.get(position - 1);

        //打开新的窗体,显示具体新闻内容
        Intent new_activity = new Intent();
        new_activity.setClass(this, act_Lecture_Detail.class);
        //发送要打开的URL
        new_activity.putExtra("URL", item.url);
        startActivity(new_activity);
    }

    /**
     * 先获取本地SQLite缓存数据
     * 加载新的数据
     */
    public void initializeLoadData() {
        //获取本地缓存
        mList = dataBase_lecture.catalog.getAllData();
        mAdapter.notifyDataSetChanged();

        //获取地址访问
        if (mAdapter.getCount() <= 0) {
            flag_isLoading = true;
            httpUtil.getSingleton().getHttp(Const.lecture_url, this);
        }
    }

    /**
     * 根据讲座Html页面数据解析数据
     */
    private List<struct_LectureCatalogItem> parseLectureCatalog(String html) {
        //解析完成后得到的数据集
        List<struct_LectureCatalogItem> rtnList = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(html);
            //提取讲座主要部分
            Element left = doc.getElementsByClass("left").first();

            //提取分类标题
//            String catalogTitle = left.getElementsByClass("left_title").text().trim();
//            abstract_LogUtil.i(this, "=========" + catalogTitle + "=========");         //日志

            //提取讲座目录主体部分
            Elements collection_li = left.getElementsByClass("content").first().select("ul").first().select("li");
            for (Element li : collection_li) {
                /**
                 * 设置返回的数据
                 * 标题、时间、地址
                 */
                struct_LectureCatalogItem item = new struct_LectureCatalogItem();
                item.title = li.select("span").last().text();
                item.time = li.getElementsByClass("time").first().text();
                item.time = item.time.replace("开讲时间：", "").trim();
                item.url = Const.news_url + li.select("a").attr("href");

                rtnList.add(item);

//                abstract_LogUtil.i(this, item.title + " " + item.url + " " + item.time);     //日志
            }
            return rtnList;
        } catch (Exception e) {
            abstract_LogUtil.e(this, e.toString());
            e.printStackTrace();
        }
        return null;
    }

    private class LectureAdapter extends BaseAdapter {
        private Context context;

        public LectureAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int index) {
            return mList.get(index);
        }

        @Override
        public long getItemId(int index) {
            return index;
        }

        /**
         * 获取新布局
         */
        @Override
        public View getView(int position, View convertView, ViewGroup arg2) {
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                convertView = layoutInflater.inflate(R.layout.lecture_catalog_item, null);
                viewHolder = new ViewHolder();
                viewHolder.title = (TextView) convertView.findViewById(R.id.lecture_catalog_item_title);
                viewHolder.time = (TextView) convertView.findViewById(R.id.lecture_catalog_item_date);
                viewHolder.state = (TextView) convertView.findViewById(R.id.lecture_catalog_item_state);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            struct_LectureCatalogItem item = mList.get(position);

            //设置标题
            viewHolder.title.setText(item.title);

            //设置时间
            String deltaTime;
            if (item.deltaTime != null)
                deltaTime = item.deltaTime;
            else {
                deltaTime = timeUtil.getDeltaDate(item.time);
                item.deltaTime = deltaTime;
            }
            viewHolder.time.setText(item.time + "  " + deltaTime);  //形如2015-01-01  20天前

            //设置状态
            if (!deltaTime.equals(""))
                if (deltaTime.contains("前") || deltaTime.equals("昨天"))
                    viewHolder.state.setText("已过期");
                else
                    viewHolder.state.setText("未开讲");

            //这是最后一条数据，如果加载了，说明所有数据已全部加载了，日期写死了，不会有变动
            if (item.time.contains("2004-12-09"))
                nextPage = itemIsLastOne;

            //预加载网络数据
            final int preLoadCount = 100;       //预加载的数据数量,每页为20条数据
            if (!flag_isLoading && nextPage != itemIsLastOne && clsApplication.receiver.isWebConnected())
                if (position > nextPage * 20 - preLoadCount) {
                    loadNextPage();
                }

            return convertView;
        }

        private class ViewHolder {
            TextView title;
            TextView time;
            TextView state;     //未开始、已过期
        }
    }


    private class loadData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                //停留显示数秒刷新信息
                Thread.sleep(700);
            } catch (Exception e) {
                abstract_LogUtil.e(this, e.toString());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            wListView.onRefreshComplete();

            //通知更新数据
            mAdapter.notifyDataSetChanged();
        }
    }
}

