package ecust.news;

import android.content.Context;
import android.graphics.Bitmap;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ecust.main.R;
import lib.BaseActivity.MyBaseActivity;
import lib.InputStreamUtils;
import lib.clsFailureBar;
import lib.clsGlobal.Const;
import lib.clsGlobal.Global;
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
 * Created by 彩笔怪盗基德 on 2015/6/30
 * Copyright (C) 2015 彩笔怪盗基德
 */
public class act_News_Detail extends MyBaseActivity implements clsFailureBar.OnWebRetryListener,
        clsHttpAccess_CallBack.OnHttpVisitListener {
    final int max_Thread_Pic_Download = 3;      //最大的下载线程数
    int current_Thread_Pic_Download = 0;        //当前下载线程数

    String news_URL;            //新闻URL地址
    String catalogName;         //版块名称
    clsFailureBar wFailureBar;            //失败，加载条
    struct_NewsContent mNewsContent;        //存放全部新闻数据内容
    NewsAdapter mAdapter = new NewsAdapter(this);           //BaseAdapter
    ListView wListView;         //ListView控件

    boolean activity_destoryed = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activity_destoryed = true;
        wFailureBar.setOnWebRetryListener(null);
    }

    //这个函数只会被执行一次
    @Override
    public void onHttpBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {
        if (!bSucceed) return;

        //解析数据
        mNewsContent = clsNewsParse.parseAllData(rtnHtmlMessage, catalogName);

        //填充图片的引用，等会来这个数据集中找
        for (String item_url : mNewsContent.pic_url) {
            mNewsContent.bitmapHashMap.put(item_url, new PicHolder());
        }
    }

    @Override
    public void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {
        //加载失败
        if (!bSucceed) {
            wFailureBar.setStateFailure();
            return;
        }

        //传入数据进行更新
        mAdapter.setCatalogName(this.catalogName);
        mAdapter.setNewsContent(mNewsContent);

        //新闻消息到了，隐藏进度条
        wFailureBar.setStateSucceed();
    }

    //加载单张图片(调用自getView，有时图片线程加载完也会调用这里)
    public void loadSinglePicture(String url) {
        //程序已退出就不再加载图片了
        if (activity_destoryed) return;

        //避免线程过多
        if (current_Thread_Pic_Download >= max_Thread_Pic_Download) return;

        PicHolder picHolder = mNewsContent.bitmapHashMap.get(url);
        //根据状态判断要做的行为
        switch (picHolder.state) {
            case inMemory:
                //已经有了就不再加载了
                return;
            case existInFile:
                //
                break;
            case isLoading:
                //正在加载就不再加载了
                return;
            case none:
                logUtil.i(this, "[图片开始下载]" + url);
                current_Thread_Pic_Download++;                  //线程加一
                picHolder.state = PicHolder.pic_state.isLoading;     //修改状态
                clsHttpAccess_CallBack.getSingleton().getBitmapBytes(url, this);
                break;
        }
    }

    //图片加载完成
    @Override
    public void onPictureLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {
        PicHolder picHolder = mNewsContent.bitmapHashMap.get(url);

        //设置图片
        if (bSucceed) {
            Bitmap bitmap = InputStreamUtils.bytesToBitmap(rtnPicBytes);        //获取图片
            if (bitmap != null) {
                //加载成功
                picHolder.state = PicHolder.pic_state.inMemory;      //修改状态
                picHolder.bitmap = bitmap;
                if (picHolder.imageView != null)
                    picHolder.imageView.setImageBitmap(bitmap);         //显示图片
            } else {
                //有消息回来，但转换失败
                picHolder.state = PicHolder.pic_state.none;          //修改状态
            }
        } else {
            picHolder.state = PicHolder.pic_state.none;     //修改状态
        }

        //已经是最后一个线程的话，就继续加载图片
        current_Thread_Pic_Download--;          //线程减一
        if (current_Thread_Pic_Download <= 0)
            loadResidualPicture();       //查找未被加载的图片继续加载
    }

    //查找是否还有其他未被加载的图片需要进行加载
    public void loadResidualPicture() {
        //优先加载当前可见位置开始以下的图片
        for (int pos = wListView.getFirstVisiblePosition() - 1; pos < mAdapter.getCount(); pos++) {
            if (pos >= mNewsContent.content.size()) return;      //防止数组越界

            String pic_url = mNewsContent.content.get(Math.abs(pos));
            //不是URL就继续找下一个
            if (pic_url == null || !pic_url.contains("http://")) continue;

            //图片已经有了，就继续加载下一个
            PicHolder picHolder = mNewsContent.bitmapHashMap.get(pic_url);
            if (picHolder.state != PicHolder.pic_state.none) continue;

            loadSinglePicture(pic_url);
            return;
        }

        //可见区域的图片都已加载，加载上面的
        for (String pic_url : mNewsContent.pic_url) {
            PicHolder picHolder = mNewsContent.bitmapHashMap.get(pic_url);
            if (picHolder.state != PicHolder.pic_state.none) continue;

            loadSinglePicture(pic_url);     //拉新图片
            return;
        }
    }

    @Override
    public void onWebRetryCompleted() {
        //获取新闻文本内容
        logUtil.i(this, "[重试刷新中]" + news_URL);
        wFailureBar.setStateLoading();
        clsHttpAccess_CallBack.getSingleton().getHttp(news_URL, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_detail);

        //获取新闻地址URL、所在分类版块
        news_URL = getIntent().getStringExtra("URL");
        catalogName = getIntent().getStringExtra("catalogName");
        Global.log("[新闻详细内容加载]" + news_URL);

        Global.setTitle(this, "新闻详情");
        initCompents();     //初始化组件引用

        //获取新闻文本内容
        clsHttpAccess_CallBack.getSingleton().getHttp(news_URL, this);
    }

    private void initCompents() {
        wFailureBar = new clsFailureBar(this);
        wFailureBar.setOnWebRetryListener(this);

        wListView = (ListView) findViewById(R.id.news_detail_listview);
        wListView.setAdapter(mAdapter);
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
class NewsAdapter extends BaseAdapter implements View.OnClickListener {
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

    //点击事件
    @Override
    public void onClick(View v) {
        String str = (String) v.getTag();
        logUtil.toast(str + "\r\n" +
                this.mActivity.wListView.getFirstVisiblePosition() + " " + this.mActivity.wListView.getLastVisiblePosition());
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
        if (str.startsWith("http://"))
            return createNewImageView(str);          //返回图片
        else
            return createNewTextView(str);          //返回文本
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
        ImageView imageView;        //返回的图片

        //已经存在imageView了，就不再创建新的
        if (picHolder.imageView != null) {
            imageView = picHolder.imageView;
        } else {
            //没有则创建新的imageView
            imageView = new ImageView(context);
            imageView.setTag(url);                      //设置Tag
            imageView.setOnClickListener(this);         //设置点击事件

            if (picHolder.bitmap != null) {
                imageView.setImageBitmap(picHolder.bitmap);                 //内存已缓存的话就加载
            } else {
                imageView.setImageResource(R.drawable.pic_loading);      //设置默认图片
            }

            //不加这句，图片上下会有空白
            imageView.setAdjustViewBounds(true);

            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            //,设置图片间距多少多少dp
            int padding = (int) context.getResources().getDimension(R.dimen.DefaultPadding);
            imageView.setPadding(padding, padding / 2, padding, padding / 2);
            imageView.setLayoutParams(lp);

            //保存imageView，类似于将ViewHolder存至tag
            picHolder.imageView = imageView;
        }
        this.mActivity.loadSinglePicture(url);         //不管如何，加载下图片，图片有可能没有

        return imageView;
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