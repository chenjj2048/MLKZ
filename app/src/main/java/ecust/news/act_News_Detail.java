package ecust.news;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ecust.main.R;
import lib.Const;
import lib.Global;
import lib.clsApplication;
import lib.clsFailureBar;
import lib.clsUtils.InputStreamUtils;
import lib.clsUtils.fileUtil;
import lib.clsUtils.httpUtil;
import lib.clsUtils.logUtil;
import lib.clsUtils.pathFactory;
import lib.clsUtils.pathFactory.PathType;
import lib.clsUtils.timeUtil;

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
 * Created by 彩笔怪盗基德 on 2015/6/30
 * Copyright (C) 2015 彩笔怪盗基德
 */
public class act_News_Detail extends Activity implements clsFailureBar.OnWebRetryListener,
        httpUtil.OnHttpVisitListener, View.OnClickListener,
        AbsListView.OnScrollListener {
    static int listview_scroll_items_per_second;   //ListView滚动速度
    final int MAX_THREAD_PIC_DOWNLOAD = 3;      //最大的下载线程数
    final int MAX_BITMAP_MEMORY_SUM = 20 * 1024 * 1024;       //图片总共占用多少内存，超过就回收内存
    final float LISTVIEW_SCROLL_STATISTICS_TIME = 3000;     //ListView滚动速度统计时间
    boolean activity_destoryed = false;         //页面销毁，标记，用来停止其他正在进行的线程
    int current_Thread_Pic_Download = 0;        //当前下载线程数
    Queue<struct_Time_Data> queue = new LinkedList<>();   //时间-数据 队列
    private String news_URL;            //新闻URL地址
    private String catalogName;         //版块名称
    private clsFailureBar wFailureBar;            //失败，加载条
    private struct_NewsContent mNewsContent;        //存放全部新闻数据内容
    private NewsAdapter mAdapter = new NewsAdapter(this);           //BaseAdapter
    private ListView wListView;         //ListView控件
    private boolean nearToTop = true;      //标记更靠近头还是更靠近尾
    private int lastVisibleItem;    //上次ListView最顶的位置

    //根据url获得唯一的hash值（碰到一样的几乎不可能）
    public static String getPicHash(String url) {
        String result = Global.getStringHash(url);

        //设置后缀为.jpg
        return result + ".jpg";
    }

    //根据url获得唯一的hash值（碰到一样的几乎不可能）
    public static String getHtmlHash(String url) {
        //设置后缀
        return Global.getStringHash(url);
    }

    @Override
    protected void onDestroy() {
        activity_destoryed = true;
        wFailureBar.setOnWebRetryListener(null);

        recycleAllPictures();       //收回全部图片

        super.onDestroy();
    }

    //ListView滚动状态
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case SCROLL_STATE_IDLE:
                listview_scroll_items_per_second = 0;
                break;
            case SCROLL_STATE_TOUCH_SCROLL:
                break;
            case SCROLL_STATE_FLING:
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (lastVisibleItem == firstVisibleItem && queue.peek() != null)
            return;

        //回收图片
        recyclePictureForMoreMemory();

        //ListView当前项更靠近头还是更靠近尾
        nearToTop = (firstVisibleItem <= totalItemCount / 2);

        int data = Math.abs(firstVisibleItem - lastVisibleItem);    //变化的差值
        lastVisibleItem = firstVisibleItem;

        queue.offer(new struct_Time_Data(data));    //添加一个数据

        //先移除过期数据
        while (true) {
            struct_Time_Data t = queue.peek();
            if (Math.abs(System.currentTimeMillis() - t.time) > LISTVIEW_SCROLL_STATISTICS_TIME) {
                //超过统计时间
                queue.poll();
            } else {
                //在统计时间内
                break;
            }
        }

        //计算平均数，结束时至少有一个数据，不必检查Null
        float sum = 0;
        for (struct_Time_Data t : queue)
            sum += t.data;

        //算出平均值
        listview_scroll_items_per_second = (int) (sum / LISTVIEW_SCROLL_STATISTICS_TIME * 1000);
    }

    //这个函数只会被执行一次
    @Override
    public void onHttpBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {
        if (!bSucceed) return;

        //解析数据
        mNewsContent = clsNewsParse.parseAllData(rtnHtmlMessage, catalogName);

        //保存对象
        String filepath = pathFactory.getFileSavedPath(PathType.NEWS_DETAIL_CONTENT_CACHE);
        filepath += getHtmlHash(url);
        fileUtil.saveObjectData(filepath, mNewsContent);
    }

    //初始化图片集合
    public void initPicHolders() {
        mNewsContent.bitmapHashMap = new HashMap<>();

        //填充图片的引用，等会来这个数据集中找
        for (String url : mNewsContent.pic_url) {
            PicHolder picHolder = new PicHolder();

            //第一次加载，创建新的imageView
            ImageView imageView = new ImageView(this);

            //关联imageView，类似于将ViewHolder存至tag
            picHolder.imageView = imageView;

            imageView.setTag(url);                      //设置Tag
            imageView.setOnClickListener(this);         //设置点击事件

            imageView.setImageResource(R.drawable.pic_loading);      //设置默认图片

            //不加这句，图片上下会有空白
            imageView.setAdjustViewBounds(true);

            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            //,设置图片间距多少多少dp
            int padding = (int) getResources().getDimension(R.dimen.DefaultPadding);
            imageView.setPadding(padding, padding / 2, padding, padding / 2);
            imageView.setLayoutParams(lp);

            //设置图片地址（类没写好，又重复了一次，悲剧）
            picHolder.url = url;

            //添加观察者，观察位图总共的内存占用变化
            picHolder.addObserver(mNewsContent);

            //添加到数据集
            mNewsContent.bitmapHashMap.put(url, picHolder);
        }
    }

    @Override
    public void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {
        //加载失败
        if (!bSucceed) {
            wFailureBar.setStateFailure();
            return;
        }

        //初始化图片集合
        initPicHolders();

        //传入数据进行更新
        mAdapter.setCatalogName(this.catalogName);
        mAdapter.setNewsContent(mNewsContent);

        //新闻消息到了，隐藏进度条
        wFailureBar.setStateSucceed();
    }

    /**
     * 从网络加载图片（此时数据集中肯定没有图片，缓存文件中也没有）
     *
     * @param url 图片地址
     */
    public void loadSinglePictureOnWebsite(String url) {
        //程序已退出就不再加载图片了
        if (activity_destoryed) return;

        //避免线程过多
        if (current_Thread_Pic_Download >= MAX_THREAD_PIC_DOWNLOAD) return;

        //没网络就不加载图片了
        if (!clsApplication.receiver.isWebConnected()) return;

        PicHolder picHolder = mNewsContent.bitmapHashMap.get(url);

        //没有在加载
        if (!picHolder.isLoading) {
            picHolder.loadTimes++;          //网络尝试次数+1
            current_Thread_Pic_Download++;                  //线程加一

            picHolder.isLoading = true;     //正在加载

            httpUtil.getSingleton().getBitmapBytes(url, this);        //下载
            logUtil.i(this, "[图片开始下载-线程数量" + current_Thread_Pic_Download + "]" + url);
        }
    }

    @Override
    public void onPictureBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {
        if (!bSucceed) return;

        //保存文件
        String path = pathFactory.getFileSavedPath(PathType.NEWS_DETAIL_PICTURE_CACHE);
        path += getPicHash(url);
        fileUtil.saveBytesToFile(new File(path), rtnPicBytes);

        //设置图片
        PicHolder picHolder = mNewsContent.bitmapHashMap.get(url);
        picHolder.setBitmap(InputStreamUtils.bytesToBitmap(rtnPicBytes));
    }

    //图片加载完成
    @Override
    public void onPictureLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {
        PicHolder picHolder = mNewsContent.bitmapHashMap.get(url);

        if (bSucceed && picHolder.getBitmap() != null) {
            //加载成功
            picHolder.found = true;

            //处于可见位置区域
            if (wListView.indexOfChild(picHolder.imageView) >= 0) {
                new pictureShowAsyncTask().setActivity_Class(this).execute(picHolder);   //图片下载完成，逐渐显示图片
            }
        }
        picHolder.isLoading = false;

        //已经是最后一个线程的话，就继续加载图片
        current_Thread_Pic_Download--;          //线程减一
        if (current_Thread_Pic_Download < 2)       //最多开两个线程
            loadResidualPicture();       //查找未被加载的图片继续加载
    }

    //查找是否还有其他未被加载的图片需要进行加载
    public void loadResidualPicture() {
        //1.优先查找当前页面
        final int start = wListView.getFirstVisiblePosition();
        final int end = wListView.getLastVisiblePosition();

        for (int i = start; i <= end; i++) {
            //去头去尾
            if (i == 0 || i == mAdapter.getCount() - 1) continue;
            int index = i - 1;      //去除顶部的一块View
            String url = mNewsContent.content.get(index);
            //跳过TextView
            if (!url.startsWith("http://")) continue;

            //取到图片引用
            PicHolder picHolder = mNewsContent.bitmapHashMap.get(url);
            if (picHolder.isLoading || picHolder.found) continue;      //跳过正在加载的和已找到的

            //加载没有找到的网络图片
            loadSinglePictureOnWebsite(url);
            return;
        }

        //2.再找一遍是否有遗漏的图片没有
        for (String url : mNewsContent.pic_url) {
            PicHolder picHolder = mNewsContent.bitmapHashMap.get(url);

            if (picHolder.found) continue;      //如果已经有了就找下一张图

            if (picHolder.loadTimes > picHolder.max_loadTimes) continue;    //失败次数过多，找一张

            loadSinglePictureOnWebsite(url);       //加载网络图片

            break;
        }
    }

    //回收图片，空出更多内存
    public void recyclePictureForMoreMemory() {
        //尚未达到占用上限
        if (mNewsContent == null) return;
        if (mNewsContent.sum_bytes_of_bitmap <= MAX_BITMAP_MEMORY_SUM) return;

        //循环找图
        for (int i = 0; i < mNewsContent.pic_url.size(); i++) {
            //判断回收从顶部还是底部开始
            int index = nearToTop ? mNewsContent.pic_url.size() - 1 - i : i;
            String url = mNewsContent.pic_url.get(index);

            PicHolder picHolder = mNewsContent.bitmapHashMap.get(url);

            if (picHolder.getBitmap() == null) continue;

            //判断图片是否可见
            index = mNewsContent.content.indexOf(url);
            int start = wListView.getFirstVisiblePosition();
            int end = wListView.getLastVisiblePosition();

            final int distance = 3;    //前后多少图片距离
            if (start - distance <= index && index <= end + distance) continue;

            //找到图片了
            recyclePicture(url);

            //递归调用
            recyclePictureForMoreMemory();

            return;
        }
    }

    //手工回收图片
    public void recyclePicture(String url) {
        try {
            PicHolder picHolder = mNewsContent.bitmapHashMap.get(url);
            if (picHolder.getBitmap() != null && !picHolder.getBitmap().isRecycled()) {
                picHolder.imageView.setImageResource(R.drawable.pic_loading);      //先还原默认图片
                picHolder.show = false;
                picHolder.getBitmap().recycle();
                picHolder.setBitmap(null);
//                logUtil.i(this, "[回收后Bitmap占用总大小] " +
//                        String.format("%,d", mNewsContent.sum_bytes_of_bitmap));
            }
        } catch (Exception e) {
            logUtil.e(this, e.toString());
            e.printStackTrace();
        }
    }

    //回收全部图片(onDestory时)
    public void recycleAllPictures() {
        if (mNewsContent != null && mNewsContent.pic_url != null)
            for (String url : mNewsContent.pic_url)
                recyclePicture(url);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        String url = (String) v.getTag();       //取得URL

        //失败的话，继续尝试加载
        PicHolder picHolder = mNewsContent.bitmapHashMap.get(url);
        if (picHolder.getBitmap() == null) {
            picHolder.loadTimes = 0;        //重置失败次数
        }

        if (url.contains("http://172.") && picHolder.getBitmap() == null) {
            logUtil.toast("当前非校园网，无法加载内网图片!");
        }
    }

    @Override
    public void onWebRetryCompleted() {
        //获取新闻文本内容
        logUtil.i(this, "[重试刷新中]" + news_URL);
        wFailureBar.setStateLoading();
        httpUtil.getSingleton().getHttp(news_URL, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_detail);

        //获取新闻地址URL、所在分类版块
        news_URL = getIntent().getStringExtra("URL");
        catalogName = getIntent().getStringExtra("catalogName");
        logUtil.i(this, "[新闻详细内容加载]" + news_URL);

        Global.setTitle(this, "新闻详情");
        initCompents();     //初始化组件引用

        //获取缓存
        Object object = readObject(news_URL);
        if (object != null) {
            //获取成功，加载本地数据
            mNewsContent = (struct_NewsContent) object;
            onHttpLoadCompleted(news_URL, null, true, null);
        } else {
            //获取新闻文本内容
            httpUtil.getSingleton().getHttp(news_URL, this);
        }
    }

    //读取存储对象
    public Object readObject(String url) {
        //保存对象
        String filepath = pathFactory.getFileSavedPath(PathType.NEWS_DETAIL_CONTENT_CACHE);
        filepath += getHtmlHash(url);
        return fileUtil.getObjectData(filepath);
    }

    private void initCompents() {
        wFailureBar = new clsFailureBar(this);
        wFailureBar.setOnWebRetryListener(this);

        wListView = (ListView) findViewById(R.id.news_detail_listview);
        wListView.setAdapter(mAdapter);
        wListView.setOnScrollListener(this);
    }
}

class clsNewsParse {
    //解析全部数据
    public static struct_NewsContent parseAllData(String html, String catalogName) {
        struct_NewsContent result = new struct_NewsContent();
        try {
            //提取主要新闻部分
            Document doc = Jsoup.parse(html);
            Element content = doc.getElementsByClass("content").first();

            //开始解析
            //1.解析新闻标题(新闻可能多行)
            result.title = clsNewsParse.parse_Title(content);

            //2.解析头部数据
            clsNewsParse.parse_Header(content, result, catalogName);

            //3.解析新闻主体部分
            result.body = clsNewsParse.parse_Body(content);

            //4.解析图片网址
            result.pic_url = clsNewsParse.parse_Img(content);

            //5.从纯文本中解析出一行行内容（图、文字交替）
            result.content = clsNewsParse.parse_BodyLine(result);
        } catch (Exception e) {
            logUtil.e("详细新闻", "[数据解析错误]" + e.toString());
            e.printStackTrace();
        }
        return result;
    }

    //解析新闻标题（可能两行，甚至更多）
    public static String parse_Title(Element content) {
        try {
            Element content_title = content.getElementsByClass("content_title").first();
            Elements collection_h2 = content_title.getElementsByTag("h2");

            String title = "";
            for (int i = 0; i < collection_h2.size(); i++) {
                Element h2 = collection_h2.get(i);
                String line = h2.text().trim();
                if (!line.equals("")) {
                    if (title.length() == 0)
                        title += line;
                    else
                        title += "\r\n" + line;
                }
            }
            return title;
        } catch (Exception e) {
            logUtil.e("[解析标题]", e.toString());
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 2.解析新闻头部
     * 一般版块为；稿件来源、作者、摄影、编辑、访问量
     * 通知公告版块为：发表日期、来稿单位、访问量
     *
     * @param content     html中对应的标签
     * @param targetData  解析后的数据要保存的位置
     * @param catalogName 版块名称
     */
    public static void parse_Header(Element content, struct_NewsContent targetData, String catalogName) {
        try {
            //解析头部信息
            Element titles = content.getElementsByClass("titles").first();
            String moreinfo = titles.toString().replace("amp;", "").replace("&nbsp;", " ");
            moreinfo = Html.fromHtml(moreinfo).toString();
            moreinfo = moreinfo.replace("：", ":");
            moreinfo = moreinfo.replace("\r", "").replace("\n", "").trim();

            //moreinfo此时类似于以下形式
            //一般版块：稿件来源: 社会学院  |   作者:社会学院  |  摄影:社会学院  |  编辑:亦枫  |  访问量:452
            //通知公告版块：发表日期：2015-06-01 |  来稿单位:党委宣传部  |  访问量:894

            String[] collection_info = moreinfo.split(" \\| ");       //分割|，单写|会出错的
            for (String part_info : collection_info) {
                part_info = part_info.trim();

                if (part_info.endsWith(":"))
                    continue;                                      //没有名字就跳过，分析下一个

                String[] s = part_info.split(":");               //分割如  "访问量:1456"
                String leftString = s[0].trim();
                String rightString = s[1].trim();
                switch (leftString) {
                    case "发表日期":
                        targetData.release_time = rightString;                   //如：2015-06-01
                        break;
                    case "稿件来源":
                    case "来稿单位":
                        targetData.news_Source = rightString;
                        break;
                    case "作者":
                        targetData.author = rightString;
                        break;
                    case "摄影":
                        targetData.photo_author = rightString;
                        break;
                    case "编辑":
                        targetData.editor = rightString;
                        break;
                    case "访问量":
                        targetData.count_of_visit = rightString;
                        break;
                }
            }

            //解析底部日期(非通知公告才有底部日期)
            if (!catalogName.equals("通知公告")) {
                String body = Html.fromHtml(content.toString()).toString();
                String date = body.substring(body.lastIndexOf("发布日期"));
                date = date.substring(0, date.indexOf("分") + 1);      //如：2015年07月02日10时32分

                SimpleDateFormat format1 = new SimpleDateFormat("发布日期：yyyy年MM月dd日HH时mm分");
                Date time = format1.parse(date);
                SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                date = format2.format(time);

                targetData.release_time = date;                       //如：2015-07-02 10:32
            }
        } catch (Exception e) {
            logUtil.e("[解析头部]", e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 解析新闻内容部分
     */
    public static String parse_Body(Element content) {
        try {
            Element content_main = content.getElementsByClass("content_main").first();
            Element titles = content_main.getElementsByClass("titles").first();

            String body = Html.fromHtml(content_main.toString()).toString();
            String title = Html.fromHtml(titles.toString()).toString();
            //去除头部消息
            body = body.replace(title, "");
            //去除尾部消息
            int pos = body.lastIndexOf("发布日期");
            if (pos <= 0) {
                pos = body.lastIndexOf("分享文章");
            }
            body = body.substring(0, pos);

            //替换出图片标签[img]
            final String flag_img = Html.fromHtml("<img src=\"/UploadFile/1234.jpg\" alt=\"\" />").toString();
            body = body.replace(flag_img, "[img]");

            return body;
        } catch (Exception e) {
            logUtil.e("[解析Body]", e.toString());
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 解析出图片部分
     *
     * @param content html
     * @return 返回的图片地址数据
     */
    public static List<String> parse_Img(Element content) {
        List<String> result = new ArrayList<>();

        //获取图片标签
        //也可以用这句 Elements collection_img = content.select("img");
        Elements collection_img = content.getElementsByTag("img");

        //遍历取出图片
        for (int i = 0; i < collection_img.size(); i++) {
            Element img = collection_img.get(i);
            String img_url = img.attr("src");             //网页图片的地址

            //加入src="/UploadFile/DES/2015/143617254256974.jpg"
            //这时就添加http://news.ecust.edu.cn前缀
            if (!img_url.startsWith("http://"))
                img_url = Const.news_url + img_url;

            //添加图片地址到数据集
            result.add(img_url);
        }
        return result;
    }

    /**
     * 解析出纯文本
     * 如果有图片，则为空行
     * 否则是文本内容
     */
    public static List<String> parse_BodyLine(struct_NewsContent mData) {
        List<String> result = new ArrayList<>();
        try {
            String[] lines = mData.body.split("\\[img\\]");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.equals("")) {
                    line = (i == 0 ? "" : "\r\n") + line + "\r\n";
                    line = line.replace("\r\n\r\n", "\r\n");
                    result.add(line);                     //添加文本
                }
                //非最后一行
                if (i != lines.length - 1) {
                    result.add(mData.pic_url.get(i));     //添加图片加载地址
                }
            }
        } catch (Exception e) {
            logUtil.e("parse_BodyLine", "数据解析错误");
            e.printStackTrace();
        }
        return result;
    }
}

//BaseAdapter
class NewsAdapter extends BaseAdapter {
    Context context;
    struct_NewsContent mNewsContent;
    String catalogName;
    act_News_Detail mActivity;

    public NewsAdapter(act_News_Detail mActivity) {
        this.context = mActivity;
        this.mActivity = mActivity;
    }

    //只能这么写，写在构造函数时，传进来的会是null
    public void setNewsContent(struct_NewsContent mNewsContent) {
        this.mNewsContent = mNewsContent;
    }

    //用构造函数传过来时，还是空
    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @Override
    public int getCount() {
        int body_content_lines;     //新闻内容共多少行

        if (mNewsContent == null)
            return 0;               //避免空指针
        else if (mNewsContent.content == null || mNewsContent.content.size() <= 0)
            body_content_lines = 0;
        else
            body_content_lines = mNewsContent.content.size();

        //头 + 主体部分 + 尾
        return 1 + body_content_lines + 1;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View returnView;

        //有图有字,不用convertView和ViewHolder
        if (position == 0)
            returnView = getHeadView(mNewsContent);     //加载头部
        else if (position == getCount() - 1)
            returnView = getBottomView(mNewsContent);   //加载底部
        else
            returnView = getBodyView(mNewsContent, position - 1);        //加载正文部分

        return returnView;
    }

    //获得头部内容(版块名称、标题、稿件信息、时间)
    public View getHeadView(struct_NewsContent mData) {
        View header = LayoutInflater.from(context).inflate(R.layout.news_detail_head, null);

        //获取引用
        TextView catalog = (TextView) header.findViewById(R.id.news_detail_section);
        TextView title = (TextView) header.findViewById(R.id.news_detail_title);
        TextView moreinfo = (TextView) header.findViewById(R.id.news_detail_moreinfo);
        TextView detail_time = (TextView) header.findViewById(R.id.news_detail_time);

        //设置版块名称
        catalog.setText(this.catalogName);

        //设置标题
        title.setText(mData.title);

        //设置新闻来源信息等
        String info = "";
        final String space = "    ";
        if (!mData.news_Source.equals(""))
            info += "稿件来源:" + mData.news_Source + space;
        if (!mData.count_of_visit.equals(""))
            info += "访问量:" + mData.count_of_visit + space;
        moreinfo.setText(info);

        //设置发表时间
        if (!mData.release_time.equals("")) {
            String time = mData.release_time + "    " + timeUtil.getDeltaTime(mData.release_time);
            detail_time.setText(time.trim());
        } else {
            detail_time.setVisibility(View.GONE);
        }
        return header;
    }

    //获得新闻主体部分（图片及文字）
    public View getBodyView(struct_NewsContent mData, int pos) {
        String str = mData.content.get(pos);
        //判断是文本还是图片
        if (str.startsWith("http://")) {
            return createNewImageView(str);         //返回图片
        } else {
            return createNewTextView(str);          //返回文本
        }
    }


    //建立新闻内容的TextView
    public TextView createNewTextView(String str) {
        TextView textView = new TextView(context);
        textView.setText(str);       //数据内容

        //设置Padding
        int padding = (int) context.getResources().getDimension(R.dimen.DefaultPadding);
        textView.setPadding(padding, 0, padding, 0);

        return textView;
    }

    //建立新闻内容的ImageView
    public ImageView createNewImageView(String url) {
        //从表中查下有木有Bitmap
        PicHolder picHolder = mNewsContent.bitmapHashMap.get(url);

        if (picHolder==null)
            return new ImageView(context);

        //没有图片
        if (picHolder.getBitmap() == null) {
            //1.先查找是否有本地缓存
            String path = pathFactory.getFileSavedPath(PathType.NEWS_DETAIL_PICTURE_CACHE);
            path += act_News_Detail.getPicHash(url);

            boolean found = new File(path).exists();

            if (found) {
                picHolder.found = true;

                //异步任务根据URL读取文件加载，避免主线程卡顿
                new pictureShowAsyncTask().setActivity_Class(mActivity).execute(picHolder);   //getView，显示图片
            }

            //2.加载网络图片
            if (!found) {
                this.mActivity.loadSinglePictureOnWebsite(url);
            }
        } else {
            //图片已在内存，但没显示
            if (!picHolder.show) {
                new pictureShowAsyncTask().setActivity_Class(mActivity).execute(picHolder);   //getView，显示图片
            }
        }
        return picHolder.imageView;
    }

    //获得底部作者View（作者、摄影、编辑）
    public View getBottomView(struct_NewsContent mData) {
        //设置新闻作者、摄影、编辑
        final String space = "    ";
        String strAuthor = "";
        if (!mData.author.equals(""))
            strAuthor += "作者:" + mData.author + space;
        if (!mData.photo_author.equals(""))
            strAuthor += "摄影:" + mData.photo_author + space;
        if (!mData.editor.equals(""))
            strAuthor += "编辑:" + mData.editor + space;

        //设置内容
        TextView authorInfo = createNewTextView("");
        if (strAuthor.length() > 0) {
            authorInfo.setText("(" + strAuthor.trim() + ")");       //数据内容
            authorInfo.setGravity(Gravity.END);
        } else {
            authorInfo.setVisibility(View.GONE);
        }
        return authorInfo;
    }
}

//图片透明度变化，逐渐显示
class pictureShowAsyncTask extends AsyncTask<PicHolder, Float, Void> {
    private final int length_Of_IntervalTime = 40;    //刷新时间(ms)
    private int length_Of_ShowTime = 200;    //总的显示时长(ms)
    private boolean firstLoad = true;
    private PicHolder picHolder;        //获取图片及ImageView

    private act_News_Detail activity_class;

    //为了提供recyclePictureForMoreMemory()用
    public pictureShowAsyncTask setActivity_Class(act_News_Detail activity_class) {
        this.activity_class = activity_class;
        return this;
    }

    //透明度渐变
    @Override
    protected Void doInBackground(PicHolder... params) {
        //回收图片
        activity_class.recyclePictureForMoreMemory();

        //滚动过快的话，就缩短时间
        if (act_News_Detail.listview_scroll_items_per_second > 5)
            length_Of_ShowTime /= 10;
        else if (act_News_Detail.listview_scroll_items_per_second > 2)
            length_Of_ShowTime /= 2;


        //传进来1个参数
        if (params.length != 1) throw new NullPointerException();

        picHolder = params[0];          //第一个参数

        //如果bitmap为空，但缓存文件中有，就读取之
        if (picHolder.getBitmap() == null) {
            //获取图片路径
            String path = pathFactory.getFileSavedPath(PathType.NEWS_DETAIL_PICTURE_CACHE);
            path += act_News_Detail.getPicHash(picHolder.url);

            //读取字节流
            byte[] bytes = fileUtil.getBytesFromFile(new File(path));

            if (bytes == null) throw new NullPointerException();

            //设置图片
            picHolder.setBitmap(InputStreamUtils.bytesToBitmap(bytes));
        }

        //正式开始透明度渐变过程
        float current_time = 0;   //时间进度
        while (current_time <= length_Of_ShowTime) {
            publishProgress(current_time / length_Of_ShowTime);
            current_time += length_Of_IntervalTime;             //进度增加

            //休息一会
            try {
                Thread.sleep(length_Of_IntervalTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //UI更新
    @Override
    protected void onProgressUpdate(Float... values) {
        super.onProgressUpdate(values);

        //设置透明度
        float alpha = values[0];
        if (values[0] < 0.3)
            alpha = 0.3f;
        else if (values[0] > 1)
            alpha = 1;

        if (picHolder.imageView == null) throw new NullPointerException();

        //设置图片透明度
        if (picHolder.getBitmap() != null) {
            if (firstLoad) {
                firstLoad = false;
                picHolder.show = true;
                picHolder.imageView.setImageBitmap(picHolder.getBitmap());       //第一次时设置图片
            }
            picHolder.imageView.setAlpha(alpha);
            picHolder.imageView.invalidate();       //刷新
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        picHolder.imageView.setAlpha(1f);
        picHolder.imageView.invalidate();
    }
}

//求ListView滚动速度的一个辅助类
class struct_Time_Data {
    long time;      //记录的时间
    int data;       //数据

    public struct_Time_Data(int data) {
        this.data = data;
        this.time = System.currentTimeMillis();   //自动记录当前时间
    }
}
