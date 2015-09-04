package ecust.mlkz;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ecust.main.R;
import lib.Const;
import lib.clsUtils.pathFactory;
import lib.clsUtils.pathFactory.PathType;
import lib.clsUtils.fileUtil;
import lib.clsUtils.httpUtil;
import lib.clsUtils.logUtil;

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
 * Created by 彩笔怪盗基德 on 2015/8/12
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */
public class fragment_MLKZ_HomePage extends Fragment implements httpUtil.OnHttpVisitListener {
    //数据的保存地址，文件名
    private final String object_save_path = pathFactory.getFileSavedPath(PathType.MLKZ_HOMEPAGE_SERIAL_OBJECT);
    //存储所有数据内容
    private List<struct_MLKZ_Home_Section> mContent = new ArrayList<>();
    //适配器
    private bbsCatalogAdapter mAdapter;
    //ListView
    private ListView listView;
    //空页面
    private TextView emptyView;

    public fragment_MLKZ_HomePage() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mlkz_home_page_fragment, container, false);
        //适配器
        mAdapter = new bbsCatalogAdapter(getActivity(), mContent);

        //空页面
        emptyView = (TextView) view.findViewById(R.id.mlkz_home_page_emptyview);

        //ListView
        listView = (ListView) view.findViewById(R.id.mlkz_home_page_listview);
        listView.setAdapter(mAdapter);
        listView.setEmptyView(emptyView);

        //取得本地数据
        Object object = fileUtil.getObjectData(object_save_path);
        List<struct_MLKZ_Home_Section> data = (List<struct_MLKZ_Home_Section>) object;
        if (data != null) {
            //先使用本地缓存
            this.mContent = data;
            printAllDataLog(data);
            updateListViewContent();
        }

        loginMLKZ("");    //再加载刷新网页

        return view;    //返回布局
    }

    //凭cookie登陆梅陇客栈
    public void loginMLKZ(String cookie) {
        String url = "http://bbs.ecust.edu.cn/forum.php?mobile=yes";
        httpUtil.getSingleton().getHttp(url, cookie, this);
    }

    @Override
    public void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {
        if (!bSucceed) return;

        //刷新ListView
        updateListViewContent();
    }

    public void updateListViewContent() {
        //设置数据，通知更新
        mAdapter.setList(mContent);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onHttpBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {
        if (!bSucceed) return;

        //解析数据
        mContent = parseAllData(rtnHtmlMessage);

        //输出日志
        printAllDataLog(mContent);

        //偷个懒，序列化保存结构，供离线使用
        fileUtil.saveObjectData(object_save_path, mContent);
    }

    @Override
    public void onPictureLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {
    }

    @Override
    public void onPictureBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {
    }


    //输出所有数据

    public void printAllDataLog(List<struct_MLKZ_Home_Section> mData) {
        for (struct_MLKZ_Home_Section s0 : mData) {
            logUtil.i(this, "====" + s0.getSectionName() + "====");
            for (struct_MLKZ_Home_SubSection s1 : s0.getContentList()) {
                logUtil.i(this, s1.getTitle() + "  " + s1.getUrl() + "  " + s1.getNew_Message_Count());
            }
        }
    }

    //解析全部数据
    public List<struct_MLKZ_Home_Section> parseAllData(String html) {
        List<struct_MLKZ_Home_Section> mData = new ArrayList<>();

        //开始解析
        Document document = Jsoup.parse(html);
        Elements fl_collection = document.getElementsByClass("fl");
        for (Element fl : fl_collection) {
            //创建存储的数据集
            struct_MLKZ_Home_Section mContent = new struct_MLKZ_Home_Section();
            //取得标题（华理信息、励志书院、谈天说地、娱乐休闲、特色板块、站务管理）
            mContent.parseCatalogName(fl.toString());
            mContent.parseDetailStruct(fl.toString());   //取得子集
            mData.add(mContent);
        }
        return mData;
    }
}

//大板块（一级）
class struct_MLKZ_Home_Section implements Serializable {
    private String section;     //版块名称
    private List<struct_MLKZ_Home_SubSection> contentList = new ArrayList<>();   //版块内容

    public String getSectionName() {
        return section;
    }

    public List<struct_MLKZ_Home_SubSection> getContentList() {
        return contentList;
    }

    //解析版块名称（如特色板块）
    public void parseCatalogName(String html) {
        Document document = Jsoup.parse(html);
        this.section = document.getElementsByClass("bm_h").first().text();
    }

    //解析单一版块
    public void parseDetailStruct(String html) {

        html = html.replace("bm_c even", "bm_c add");         //替换蓝白间隔的背景样式
        html = html.replace("bm_c add", "bm_c_add");          //去除div中空格,否则Jsoup无法正确解析

        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByClass("bm_c_add");
        for (Element element : elements) {
            //创建数据集
            struct_MLKZ_Home_SubSection mContent = new struct_MLKZ_Home_SubSection();
            mContent.parseStruct(element.toString());       //取得名称、URL、新消息数量
            this.contentList.add(mContent);
        }
    }
}

//大板块下的小分类（二级）
class struct_MLKZ_Home_SubSection implements Serializable {
    private String title;   //子标题
    private String url;     //链接地址
    private String new_message_count;   //新消息数量

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getNew_Message_Count() {
        return new_message_count;
    }

    //解析结构
    //取得名称、URL、新消息数量
    public void parseStruct(String html) {
        Document document = Jsoup.parse(html);

        //设置标题
        this.title = document.text().trim();
        if (this.title.contains("("))
            this.title = this.title.substring(0, this.title.indexOf("("));

        //新生报到！！！标题一定要长！！！（这版块名字这么长做甚！！！），去掉后面后缀，只显示新生报到
        if (this.title.contains("！"))
            this.title = this.title.substring(0, this.title.indexOf("！"));

        //处理收藏版块的后缀（如 梅陇水库 [x] → 梅陇水库）
        this.title = this.title.replace("[x]", "").trim();

        //Url地址
        this.url = Const.bbs + "/" + document.getElementsByTag("a").first().attr("href");

        //新消息数量
        if (html.contains("<font"))             //有新消息则进行解析，否则为0
            this.new_message_count = document.getElementsByTag("font").first().text()
                    .replace("(", "").replace(")", "");
        else
            this.new_message_count = "0";
    }
}

//ListView适配器
class bbsCatalogAdapter extends BaseAdapter implements View.OnClickListener, View.OnTouchListener {
    private final int total_columns = 2;        //双栏
    private int count_of_data = 0;              //ListView行的数量
    private Context context;
    private List<struct_MLKZ_Home_Section> mList;       //数据集
    private List<View> mView;           //视图页面

    public bbsCatalogAdapter(Context context, List<struct_MLKZ_Home_Section> mList) {
        this.context = context;
        this.mList = mList;
    }

    public void setList(List<struct_MLKZ_Home_Section> mList) {
        this.mList = mList;
        //数据集改变时，修改数量尺寸（1为最后一个底部空白）
        this.count_of_data = getCount_Of_Data(mList) + 1;
        this.mView = initViews();
    }

    @Override
    public void onClick(View v) {
    }

    //onClick点击后经常无反应，滚动后才会响应点击事件，这里用onTouch替代
    //但又来了个新问题，selector不起作用
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                //取代灵异的onClick事件
                struct_MLKZ_Home_SubSection tag = (struct_MLKZ_Home_SubSection) v.getTag();

                if (tag != null)
                    logUtil.toast(tag.getTitle() + " = " + tag.getUrl());
                break;
        }
        return false;
    }

    //初始化所有视图,一次设置索引，方便查找
    public List<View> initViews() {
        List<View> returnViews = new ArrayList<>(30);     //结果集合

        //寻找对应项目
        Iterator<struct_MLKZ_Home_Section> sectionIterator = mList.iterator();
        while (sectionIterator.hasNext()) {
            //获取单一版块
            struct_MLKZ_Home_Section section = sectionIterator.next();
            returnViews.add(createSectionView(section));          //添加版块名称视图

            //搜索子项
            Iterator<struct_MLKZ_Home_SubSection> subSectionIterator = section.getContentList().iterator();
            while (subSectionIterator.hasNext()) {
                //左栏
                struct_MLKZ_Home_SubSection left_subsection = subSectionIterator.next();
                //右栏（可能为空）
                struct_MLKZ_Home_SubSection right_subsection = null;
                if (subSectionIterator.hasNext())
                    right_subsection = subSectionIterator.next();

                returnViews.add(createSubSectionView(left_subsection, right_subsection));  //返回子版块视图
            }
        }

        //添加底部空白边距
        if (mList != null && mList.size() > 0) {
            View bottomView = createSectionView(mList.get(0));
            bottomView.setVisibility(View.INVISIBLE);
            returnViews.add(bottomView);
        }

        return returnViews;
    }

    //返回需要多少行内容
    public int getCount_Of_Data(List<struct_MLKZ_Home_Section> mData) {
        int count = 0;      //总共多少行
        Iterator<struct_MLKZ_Home_Section> sectionIterator = mData.iterator();
        while (sectionIterator.hasNext()) {
            //获取单一版块
            int current_column = 0;   //列数
            struct_MLKZ_Home_Section section = sectionIterator.next();
            count++;
            Iterator<struct_MLKZ_Home_SubSection> subSectionIterator = section.getContentList().iterator();
            while (subSectionIterator.hasNext()) {
                //一条条的子项
                struct_MLKZ_Home_SubSection subsection = subSectionIterator.next();

                if (current_column++ == 0)
                    count++;     //行数加一
                current_column = current_column % this.total_columns;     //取模，此处为双栏
            }
        }
        return count;
    }

    //返回数据量
    @Override
    public int getCount() {
        return this.count_of_data;       //返回ListView行数
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return this.mView.get(position);
    }

    //创建版块视图（版块合集的名称）
    public View createSectionView(struct_MLKZ_Home_Section para) {
        //版块名称
        LayoutInflater layoutInflater = LayoutInflater.from(this.context);

        //加载布局
        View view = layoutInflater.inflate(R.layout.mlkz_home_page_fragment_listview_item_section, null);
        TextView textView = (TextView) view.findViewById(R.id.mlkz_home_page_sectionname);

        textView.setText(para.getSectionName());        //设置版块名称
        return view;
    }

    //创建具体的子版块视图
    public View createSubSectionView(struct_MLKZ_Home_SubSection left,
                                     struct_MLKZ_Home_SubSection right) {
        //版块名称
        LayoutInflater layoutInflater = LayoutInflater.from(this.context);

        //加载布局
        View view = layoutInflater.inflate(R.layout.mlkz_home_page_fragment_listview_item_subsection, null);

        //查找引用（左侧）
        ViewGroup leftViewGroup = (ViewGroup) view.findViewById(R.id.mlkz_home_page_viewgroup_left);
        TextView leftSubSectionName = (TextView) view.findViewById(R.id.mlkz_home_page_subsectionname_left);
        TextView leftNewMessage = (TextView) view.findViewById(R.id.mlkz_home_page_subsection_new_message_left);
        //查找引用（右侧）
        ViewGroup rightViewGroup = (ViewGroup) view.findViewById(R.id.mlkz_home_page_viewgroup_right);
        TextView rightSubSectionName = (TextView) view.findViewById(R.id.mlkz_home_page_subsectionname_right);
        TextView rightNewMessage = (TextView) view.findViewById(R.id.mlkz_home_page_subsection_new_message_right);

        //设置数据（左侧）
        if (left != null) {
            leftViewGroup.setTag(left);
            leftViewGroup.setOnTouchListener(this);
            leftViewGroup.setOnClickListener(this);                 //设置点击事件
            leftSubSectionName.setText(left.getTitle());            //设置版块名称（左侧）
            leftNewMessage.setText(left.getNew_Message_Count());    //设置新消息数量（左侧）
        } else {
            throw new NullPointerException();       //肯定不会为空
        }

        //设置数据（右侧）
        if (right != null) {
            rightViewGroup.setTag(right);
            rightViewGroup.setOnTouchListener(this);
            rightViewGroup.setOnClickListener(this);                //设置点击事件
            rightSubSectionName.setText(right.getTitle());          //设置版块名称（左侧）
            rightNewMessage.setText(right.getNew_Message_Count());  //设置新消息数量（左侧）
        } else {
            rightViewGroup.setVisibility(View.GONE);       //隐藏
        }

        return view;
    }
}




