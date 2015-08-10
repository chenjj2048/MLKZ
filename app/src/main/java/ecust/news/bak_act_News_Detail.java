package ecust.news;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ecust.main.R;
import lib.BaseActivity.MyBaseActivity;
import lib.InputStreamUtils;
import lib.clsFailureBar;
import lib.clsGlobal.Const;
import lib.clsGlobal.Global;
import lib.clsGlobal.logUtil;
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
public class bak_act_News_Detail extends MyBaseActivity implements clsFailureBar.OnWebRetryListener,
        clsHttpAccess_CallBack.OnHttpVisitListener {
    private final clsNewsDetail mNewsDetail = new clsNewsDetail();   //封装的新闻存储、解析、显示类
    private String news_URL;            //新闻URL地址
    private String catalogName;         //版块名称
    private WidgetHolder widgetHolder = new WidgetHolder();        //控件引用集
    private clsFailureBar wFailureBar;            //失败，加载条

    @Override
    public void onHttpBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {

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

        //解析数据
        mNewsDetail.clsParse.parseAllData(rtnHtmlMessage);
        //数据显示
        mNewsDetail.Show(widgetHolder);
        //加载网页图片
        mNewsDetail.loadNextPicture(mNewsDetail.mData.collection_pic);
    }

    @Override
    public void onPictureBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {
    }

    @Override
    public void onPictureLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {
        //判断是否数据返回成功
        ImageViewTag imageViewTag = mNewsDetail.findImageViewTag(url);
        if (rtnPicBytes != null) {
            imageViewTag.loadSucceed = true;
        } else {
            String errMsg;
            if (imageViewTag != null) {
                errMsg = "[图片加载失败" + imageViewTag.failure_times + "次]" + imageViewTag.pic_url;
            } else {
                errMsg = "[图片加载失败]" + url;
            }
            logUtil.e(this, errMsg);
            //加载下一幅图，失败重试
            mNewsDetail.loadNextPicture(mNewsDetail.mData.collection_pic);
            return;
        }

        try {
            //获取图片引用
            ImageView imageView = (ImageView) widgetHolder.linearLayout.findViewWithTag(url);
            if (imageView != null) {
                //1.由byte[]转成Bitmap
                InputStream is = InputStreamUtils.byteTOInputStream(rtnPicBytes);
                BitmapFactory.Options opts = new BitmapFactory.Options();
//                    opts.inSampleSize=2;
                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap bitmap = BitmapFactory.decodeStream(is, null, opts);
                is.close();

                Global.log("[图" + opts.outWidth + "×" + opts.outHeight +
                        " Size=" + rtnPicBytes.length + "字节]" + url);

                //2.设置图片
                imageView.setImageBitmap(bitmap);

                //3.加载下一幅图片
                mNewsDetail.loadNextPicture(mNewsDetail.mData.collection_pic);
            }
        } catch (OutOfMemoryError err) {
            logUtil.e(this, "Out Of Memory");
            err.printStackTrace();
        } catch (Exception e) {
            logUtil.e(this, e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void onWebRetryCompleted() {
        //获取新闻文本内容
        Global.log("[重试刷新中]" + news_URL);
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
        wFailureBar = new clsFailureBar(this);
        wFailureBar.setOnWebRetryListener(this);

        //初始化控件引用
        initCompents(widgetHolder);

        //获取新闻文本内容
        clsHttpAccess_CallBack.getSingleton().getHttp(news_URL, this);
    }

    //初始化控件引用
    private void initCompents(WidgetHolder Holder) {
        Holder.linearLayout = (LinearLayout) findViewById(R.id.news_detail_body);
        Holder.tv_SectionName = (TextView) findViewById(R.id.news_detail_section);
        Holder.tv_Title = (TextView) findViewById(R.id.news_detail_title);
        Holder.tv_Time = (TextView) findViewById(R.id.news_detail_time);
        Holder.tv_Moreinfo = (TextView) findViewById(R.id.news_detail_moreinfo);
        Holder.tv_Author = (TextView) findViewById(R.id.news_detail_author);
    }

    public class WidgetHolder {
        //这部分都是新闻的
        LinearLayout linearLayout;    //新闻内容都存入LinearLayout
        TextView tv_SectionName;     //分类，如：校园要闻、综合新闻
        TextView tv_Title;             //新闻标题
        TextView tv_Time;            //新闻日期
        TextView tv_Moreinfo;         //更多信息
        TextView tv_Author;            //作者、编辑、摄影信息
    }

    //imagevView状态信息
    public class ImageViewTag {
        public String pic_url;                  //图片地址
        public boolean loadSucceed = false;    //加载成功
        public int failure_times = 0;          //失败次数

        public ImageViewTag(String pic_url) {
            this.pic_url = pic_url;
        }
    }

    /**
     * 用于解析数据，及显示数据
     */
    private class clsNewsDetail implements View.OnClickListener, View.OnTouchListener {
        private DataCollection mData = new DataCollection();                                                //数据集
        private clsParse clsParse = new clsParse();                                                         //解析类
        private boolean isLoadingPic;                                                                       //图片是否正在加载中，避免重复加载图片
        private String priorLoadPic = "";                                                           //优先加载的图片地址

        /**
         * imageView（新闻图片）被点击
         *
         * @param v imageview
         */
        @Override
        public void onClick(View v) {
            //从Tag中获取图片地址
            String pic_url = (String) v.getTag();
            //根据图片地址获取图片更多信息
            ImageViewTag imageViewTag = findImageViewTag(pic_url);
            if (imageViewTag == null) return;

            if (!imageViewTag.loadSucceed) {
                //图片未加载成功，则刷新加载
                if (!isLoadingPic) {
                    //清空失败次数后重试
                    cleanFailureTimes(mData.collection_pic);
                    loadNextPicture(mData.collection_pic);
                } else {
                    String url = imageViewTag.pic_url;
                    if (url.startsWith("http://172.")) {
                        Global.toastMsg("当前非校园网，无法加载内网图片");
                    } else {
                        Global.toastMsg("图片加载中，请稍后");
                    }
                }
            } else {
                //图片加载成功
                Global.log("已加载成功" + v.toString());

            }
        }

        /**
         * ImageView滑动时优先加载当前显示的图片
         *
         * @param v     ImageView
         * @param event event
         * @return i dont know
         */
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //设置当前的图片，来让loadNextPicture优先下载
                    priorLoadPic = (String) v.getTag();
                    break;
            }
            return false;
        }

        /**
         * 加载所有的图片集
         * 一幅一幅依次加载
         */
        public void loadNextPicture(ArrayList<ImageViewTag> mPicList) {
            final int max_failure_times = 3;                                                        //图片加载最大失败次数
            isLoadingPic = false;

            final int len = mPicList.size();

            //优先加载图片
            if (!priorLoadPic.equals("")) {
                //寻找图片位置
                for (int i = 0; i < len; i++) {
                    ImageViewTag pic_info = mPicList.get(i);
                    if (priorLoadPic.equals(pic_info.pic_url)) {
                        //找到需要优先加载的图片了
                        priorLoadPic = "";                                                          //清空优先加载目标
                        if (pic_info.loadSucceed) {
                            break;                                                                  //跳出循环，重新寻找新目标图片
                        } else {
                            //加载新图片
                            isLoadingPic = true;
                            //子线程去下载图片
                            pic_info.failure_times++;
                            clsHttpAccess_CallBack.getSingleton().getBitmapBytes(pic_info.pic_url, bak_act_News_Detail.this);
                            return;
                        }

                    }
                }
            }

            //遍历寻找第一张未完成的图片来加载
            for (int i = 0; i < len; i++) {
                ImageViewTag pic_info = mPicList.get(i);
                if (pic_info.loadSucceed) continue;

                //判断失败次数
                if (pic_info.failure_times < max_failure_times) {
                    isLoadingPic = true;
                    //子线程去下载图片
                    pic_info.failure_times++;
                    clsHttpAccess_CallBack.getSingleton().getBitmapBytes(pic_info.pic_url, bak_act_News_Detail.this);
                    break;
                }
            }
        }

        /**
         * 清除失败次数
         *
         * @param data 要清除的数据，清楚图片加载失败次数，重新置为0，图片最多加载N次，失败后不重复加载
         */
        public void cleanFailureTimes(ArrayList<ImageViewTag> data) {
            for (int i = 0; i < data.size(); i++) {
                ImageViewTag pic = data.get(i);
                if (!pic.loadSucceed)
                    pic.failure_times = 0;
            }
        }

        /**
         * 按照图片地址寻找数据集中数据
         *
         * @param pic_url 图片地址
         * @return 图片的信息
         */
        public ImageViewTag findImageViewTag(String pic_url) {
            for (int i = 0; i < mData.collection_pic.size(); i++) {
                ImageViewTag imageViewTag = mData.collection_pic.get(i);
                if (imageViewTag.pic_url.equals(pic_url))
                    return imageViewTag;
            }
            //失败返回null
            return null;
        }

        /**
         * 显示数据
         *
         * @param mWidget 控件引用
         */
        public void Show(WidgetHolder mWidget) {
            //设置版块标签
            DataCollection param = mData;

            mWidget.tv_SectionName.setText(catalogName);

            //设置标题
            mWidget.tv_Title.setText(param.title);
            mWidget.tv_Title.setTextColor(Color.BLACK);

            //设置新闻时间
            if (!param.release_time.equals("")) {
                String time = param.release_time + "    " + clsParse.getDiffTime(param.release_time);
                mWidget.tv_Time.setText(time.trim());
            } else {
                mWidget.tv_Time.setVisibility(View.GONE);
            }

            //设置新闻来源信息等
            String info = "";
            final String space = "    ";
            if (!param.news_Source.equals(""))
                info += "稿件来源:" + param.news_Source + space;
            if (!param.count_of_visit.equals(""))
                info += "访问量:" + param.count_of_visit + space;
            mWidget.tv_Moreinfo.setText(info);

            //设置新闻作者、摄影、编辑
            String strAuthor = "";
            if (!param.author.equals(""))
                strAuthor += "作者:" + param.author + space;
            if (!param.photo_author.equals(""))
                strAuthor += "摄影:" + param.photo_author + space;
            if (!param.editor.equals(""))
                strAuthor += "编辑:" + param.editor + space;
            if (strAuthor.length() > 0)
                mWidget.tv_Author.setText("(" + strAuthor.trim() + ")");

            //添加文字及图片部分
            String[] lines = param.body.split("\\[img\\]");
            boolean lastLineIsPic = false;
            int len = lines.length;
            for (int i = 0; i < len; i++) {
                String line = lines[i].trim();
                if (!line.equals("")) {
                    line = (i == 0 ? "" : "\r\n") + line + "\r\n";
                    line = line.replace("\r\n\r\n", "\r\n");
                    addTextView(line);                                                              //先添加文本
                    lastLineIsPic = false;
                }
                //非最后一行
                if (i != lines.length - 1) {
                    addImageView(i, lastLineIsPic);                                                 //再添加图片控件（此处为默认加载图片）
                    lastLineIsPic = true;
                }
            }
        }


        /**
         * 添加默认图片（显示图片加载中）
         *
         * @param i           第几张图片，从0开始
         * @param lastlinepic 上一行是图片的话，就加几个dp间隔
         */
        private void addImageView(int i, boolean lastlinepic) {
            //获取图片地址
            ImageViewTag pic_Info = mData.collection_pic.get(i);

            //加载图片到布局
            final ImageView imageView = new ImageView(bak_act_News_Detail.this);
            imageView.setImageResource(R.drawable.pic_loading);                                          //默认图片
            imageView.setAdjustViewBounds(true);                                            //不加这句，图片上下会有空白
            imageView.setTag(pic_Info.pic_url);                                                     //设置图片地址到TAG上
            imageView.setOnClickListener(this);
            imageView.setOnTouchListener(this);                                                     //滑动时，优先加载当前图片

            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            //根据上一行是否是图片来做出是否加间隔的判断
            int dp_margin = lastlinepic ? (int) getResources().getDimension(R.dimen.DefaultPadding) : 0;              //,设置图片间距多少多少dp
            lp.setMargins(0, dp_margin, 0, 0);

            imageView.setLayoutParams(lp);
            widgetHolder.linearLayout.addView(imageView);
        }

        /**
         * 线性布局中添加文本
         *
         * @param text 文本
         */
        private void addTextView(String text) {
            if (text == null) return;
            if (text.equals("")) return;

            final LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            TextView textView = new TextView(bak_act_News_Detail.this);
            textView.setText(text);
            textView.setPadding(0, 0, 0, 0);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.NewsTextSize_Detail));
            widgetHolder.linearLayout.addView(textView, p);
        }

        //数据解析类
        public class clsParse {
            //解析全部数据
            public void parseAllData(String html) {
                try {
                    Document doc = Jsoup.parse(html);
                    Element content = doc.getElementsByClass("content").first();                        //提取主要新闻部分

                    //开始解析
                    mData.title = parse_Title(content);                                             //1.解析新闻标题(新闻可能多行)
                    parse_Header(content, mData);                                                   //2.解析头部数据
                    mData.body = parse_Body(content);                                               //3.解析新闻主体部分
                    mData.collection_pic = parse_Img(content);                                         //4.解析图片网址
                    //解析完成

                } catch (Exception e) {
                    Global.log("[parseAllData]" + e.toString());
                    e.printStackTrace();
                }
            }

            /**
             * 解析新闻标题（可能两行，甚至更多）
             * 不适合学术讲座版块
             *
             * @param content html中对应的标签
             * @return 新闻标题
             */
            private String parse_Title(Element content) {
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
                    Global.log("[parse_Title]" + e.toString());
                    e.printStackTrace();
                    return "";
                }
            }

            /**
             * 2.解析新闻头部
             * 一般版块为；稿件来源、作者、摄影、编辑、访问量
             * 通知公告版块为：发表日期、来稿单位、访问量
             * 学术讲座版块：无头部数据
             *
             * @param content  html中对应的标签
             * @param savaData 解析后的数据要保存的位置
             */
            private void parse_Header(Element content, DataCollection savaData) {
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
                    //学术讲座版块：空（""）


                    String[] collection_info = moreinfo.split(" \\| ");                               //分割|，单写|会出错的
                    for (String part_info : collection_info) {
                        part_info = part_info.trim();

                        if (part_info.endsWith(":"))
                            continue;                                      //没有名字就跳过，分析下一个

                        String[] s = part_info.split(":");                                      //分割如  "访问量:1456"
                        String leftString = s[0].trim();
                        String rightString = s[1].trim();
                        switch (leftString) {
                            case "发表日期":
                                savaData.release_time = rightString;                                //如：2015-06-01
                                break;
                            case "稿件来源":
                            case "来稿单位":
                                savaData.news_Source = rightString;
                                break;
                            case "作者":
                                savaData.author = rightString;
                                break;
                            case "摄影":
                                savaData.photo_author = rightString;
                                break;
                            case "编辑":
                                savaData.editor = rightString;
                                break;
                            case "访问量":
                                savaData.count_of_visit = rightString;
                                break;
                        }
                    }
                    //解析底部日期(非通知公告才有底部日期)
                    if (!catalogName.equals("通知公告")) {
                        String body = Html.fromHtml(content.toString()).toString();
                        String date = body.substring(body.lastIndexOf("发布日期"));
                        date = date.substring(0, date.indexOf("分") + 1);                           //如：2015年07月02日10时32分

                        SimpleDateFormat format1 = new SimpleDateFormat("发布日期：yyyy年MM月dd日HH时mm分");
                        Date time = format1.parse(date);
                        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        date = format2.format(time);

                        savaData.release_time = date;                                               //如：2015-07-02 10:32
                    }
                } catch (Exception e) {
                    Global.log("[parse_Header]" + e.toString());
                    e.printStackTrace();
                }
            }

            /**
             * 解析新闻内容部分
             *
             * @param content html中对应的标签
             * @return 新闻消息
             */
            private String parse_Body(Element content) {
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
                    Global.log("[parse_Body]" + e.toString());
                    e.printStackTrace();
                }
                return "";
            }


            /**
             * 解析图片网址
             *
             * @param content html中对应的标签
             * @return 解析到的图片地址
             */
            private ArrayList<ImageViewTag> parse_Img(Element content) {
                ArrayList<ImageViewTag> result = new ArrayList<>();

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

                    Global.log("[图" + String.valueOf(i + 1) + "]" + img_url);

                    //添加图片地址到数据集
                    result.add(new ImageViewTag(img_url));
                }
                return result;
            }

            /**
             * 返回与当前时间的差值
             *
             * @param newsDate 时间字符串（2015-06-19或2015-01-01 08:42形式）
             * @return 返回如"几天前"
             */
            private String getDiffTime(String newsDate) {
                try {
                    SimpleDateFormat s;
                    if (newsDate.contains(" ")) {
                        s = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    } else {
                        s = new SimpleDateFormat("yyyy-MM-dd");
                    }

                    Date date = s.parse(newsDate);
                    Date now = new Date();
                    long diff = (now.getTime() - date.getTime()) / 1000;

                    if (diff < 0)
                        return "";                                                            //时间是负数，就不对

                    diff /= 60;                                                                         //分钟数
                    if (diff < 60) return diff + "分钟前";

                    diff /= 60;                                                                         //小时数
                    if (diff < 24) return diff + "小时前";

                    diff /= 24;                                                                         //天数
                    if (diff <= 30) return diff + "天前";

                    diff /= 30;                                                                         //月数
                    if (diff < 12) return diff + "个月前";

                    diff /= 12;                                                                         //年数
                    return diff + "年前";
                } catch (Exception e) {
                    Global.log(e.toString());
                    e.printStackTrace();
                    return "";
                }
            }
        }

        //存放新闻数据
        public class DataCollection {
            String title = "";           //新闻标题

            String release_time = "";    //发表日期
            String news_Source = "";      //稿件来源、来稿单位
            String author = "";             //作者
            String photo_author = "";        //摄影
            String editor = "";              //编辑
            String count_of_visit = "";      //访问量

            String body = "";               //新闻文字部分

            //图片集，按顺序对应图片内容
            ArrayList<ImageViewTag> collection_pic = new ArrayList<>();
        }
    }
}
