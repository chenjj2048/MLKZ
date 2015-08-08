package ecust.news;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
    String news_URL;            //新闻URL地址
    String catalogName;         //版块名称
    clsFailureBar wFailureBar;            //失败，加载条
    DataCollection mNewsContent;        //存放全部新闻数据内容
    NewsAdapter mAdapter = new NewsAdapter(this);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wFailureBar.setOnWebRetryListener(null);
    }

    @Override
    public void onHttpBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {
        if (!bSucceed) return;

        //解析数据
        mNewsContent = clsNewsParse.parseAllData(rtnHtmlMessage, catalogName);
    }

    @Override
    public void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {
        //加载失败
        if (!bSucceed) {
            wFailureBar.setStateFailure();
            return;
        }

        //新闻消息到了，隐藏进度条
        wFailureBar.setStateSucceed();
    }

    @Override
    public void onPictureLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {

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

        ListView listView = (ListView) findViewById(R.id.news_detail_listview);
        listView.setAdapter(mAdapter);
    }

    class NewsAdapter extends BaseAdapter {
        Context context;

        public NewsAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return 1 + 1 + 1;
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
                returnView = getHeadView(mNewsContent);
            else if (position == getCount() - 1)
                returnView = getBottomView(mNewsContent);
            else
                returnView = getBodyView(mNewsContent);

            return returnView;
        }

        //获得头部内容(版块名称、标题、稿件信息、时间)
        public View getHeadView(DataCollection mData) {
            View header = getLayoutInflater().inflate(R.layout.news_detail_head, null);

            //获取引用
            TextView catalog = (TextView) header.findViewById(R.id.news_detail_section);
            TextView title = (TextView) header.findViewById(R.id.news_detail_title);
            TextView moreinfo = (TextView) header.findViewById(R.id.news_detail_moreinfo);
            TextView detail_time = (TextView) header.findViewById(R.id.news_detail_time);

            //设置版块名称
            catalog.setText(catalogName);

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
        public View getBodyView(DataCollection mData) {
            TextView tv = new TextView(context);
            tv.setText(mData.body);

            //设置字体大小
            float size = getResources().getDimension(R.dimen.DefaultTextSize);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);

            return tv;
        }

        //获得底部作者View（作者、摄影、编辑）
        public View getBottomView(DataCollection mData) {
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
            TextView authorInfo = new TextView(context);
            if (strAuthor.length() > 0) {
                float size = getResources().getDimension(R.dimen.NewsTextSize_Author);
                authorInfo.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
                authorInfo.setText("(" + strAuthor.trim() + ")");
                authorInfo.setGravity(Gravity.END);

                //设置Padding
                int padding = Global.dimenConvert.dip2px(20);
                authorInfo.setPadding(padding, padding, padding, padding);
            } else {
                authorInfo.setVisibility(View.GONE);
            }
            return authorInfo;
        }
    }


}

class clsNewsParse {
    //解析全部数据
    public static DataCollection parseAllData(String html, String catalogName) {
        DataCollection result = new DataCollection();
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
//            int pic_count = 1;
//            for (String t : result.pic_url) {
//                logUtil.i("图片地址", "[图" + (pic_count++) + "]" + t);
//            }

            //5.从纯文本中解析出一行行内容（图、文字交替）
            result.content = clsNewsParse.parse_BodyLine(result.body);

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
    public static void parse_Header(Element content, DataCollection targetData, String catalogName) {
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
    public static List<String> parse_BodyLine(String body) {
        List<String> result = new ArrayList<>();

        String[] lines = body.split("\\[img\\]");
        int len = lines.length;
        for (int i = 0; i < len; i++) {
            String line = lines[i].trim();
            if (!line.equals("")) {
                line = (i == 0 ? "" : "\r\n") + line + "\r\n";
                line = line.replace("\r\n\r\n", "\r\n");
                result.add(line);                     //添加文本
            }
            //非最后一行
            if (i != lines.length - 1) {
                result.add("IMG"); //再添加图片控件（此处为默认加载图片）
            }
        }
        return result;
    }
}

//新闻数据集
class DataCollection {
    String title = "";           //新闻标题

    String release_time = "";    //发表日期
    String news_Source = "";      //稿件来源、来稿单位
    String author = "";             //作者
    String photo_author = "";        //摄影
    String editor = "";              //编辑
    String count_of_visit = "";      //访问量

    String body = "";               //新闻文字部分

    List<String> pic_url;       //图片地址
    List<String> content;          //文字部分
}

