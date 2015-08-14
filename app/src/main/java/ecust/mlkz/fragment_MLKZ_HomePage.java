package ecust.mlkz;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import ecust.main.R;
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
 * Created by 彩笔怪盗基德 on 2015/8/12
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */
public class fragment_MLKZ_HomePage extends Fragment implements clsHttpAccess_CallBack.OnHttpVisitListener {

    private List<struct_mlkz_home_primary> content = new ArrayList<>();     //存储所有数据内容

    public fragment_MLKZ_HomePage() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences sp = getActivity().getSharedPreferences(Global.sp_Config, 0);
        String cookie = sp.getString(Global.sp_Cookie, "");

        //返回布局
        return inflater.inflate(R.layout.mlkz_home_page_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        //加载网页
        String url = "http://bbs.ecust.edu.cn/forum.php?mobile=yes";
        String cookie = "";
        clsHttpAccess_CallBack.getSingleton().getHttp(url, cookie, this);
    }

    @Override
    public void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {


    }

    @Override
    public void onHttpBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {
        if (!bSucceed) return;

        //解析数据
        content = parseAllData(rtnHtmlMessage);

        //输出日志
        printAllDataLog(content);
    }

    @Override
    public void onPictureLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {
    }

    @Override
    public void onPictureBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {
    }

    //输出所有数据
    public void printAllDataLog(List<struct_mlkz_home_primary> mData) {
        for (struct_mlkz_home_primary s0 : mData) {
            logUtil.i(this, "====" + s0.getSectionName() + "====");
            for (struct_mlkz_home_secondary s1 : s0.getContentList()) {
                logUtil.i(this, s1.getTitle() + " = " + s1.getUrl() + " = " + s1.getNew_Message_Count());
            }
        }
    }

    //解析全部数据
    public List<struct_mlkz_home_primary> parseAllData(String html) {
        List<struct_mlkz_home_primary> mData = new ArrayList<>();

        //开始解析
        Document document = Jsoup.parse(html);
        Elements fl_collection = document.getElementsByClass("fl");
        for (Element fl : fl_collection) {
            //创建存储的数据集
            struct_mlkz_home_primary mContent = new struct_mlkz_home_primary();
            //取得标题（华理信息、励志书院、谈天说地、娱乐休闲、特色板块、站务管理）
            mContent.parseCatalog(fl.toString());
            mContent.parseStruct(fl.toString());   //取得子集
            mData.add(mContent);
        }
        return mData;
    }
}

//大板块（一级）
class struct_mlkz_home_primary {
    private String section;     //版块名称
    private List<struct_mlkz_home_secondary> contentList = new ArrayList<>();   //版块内容

    public String getSectionName() {
        return section;
    }

    public List<struct_mlkz_home_secondary> getContentList() {
        return contentList;
    }

    //解析版块名称（如特色板块）
    public void parseCatalog(String html) {
        Document document = Jsoup.parse(html);
        this.section = document.getElementsByClass("bm_h").first().text();
    }

    //解析单一版块
    public void parseStruct(String html) {

        html = html.replace("bm_c even", "bm_c add");         //替换蓝白间隔的背景样式
        html = html.replace("bm_c add", "bm_c_add");          //去除div中空格,否则Jsoup无法正确解析

        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByClass("bm_c_add");
        for (Element element : elements) {
            //创建数据集
            struct_mlkz_home_secondary mContent = new struct_mlkz_home_secondary();
            mContent.parseStruct(element.toString());
            this.contentList.add(mContent);
        }
    }
}

//大板块下的小分类（二级）
class struct_mlkz_home_secondary {
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
    public void parseStruct(String html) {
        Document document = Jsoup.parse(html);

        //设置标题
        this.title = document.text().trim();
        if (this.title.contains("("))
            this.title = this.title.substring(0, this.title.indexOf("("));

        //Url地址
        this.url = Global.bbs + "/" + document.getElementsByTag("a").first().attr("href");

        //新消息数量
        if (html.contains("<font"))             //有新消息则进行解析，否则为0
            this.new_message_count = document.getElementsByTag("font").first().text()
                    .replace("(", "").replace(")", "");
        else
            this.new_message_count = "0";
    }
}