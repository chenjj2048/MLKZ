package ecust.mlkz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ecust.main.R;
import ecust.main.act_MainActivity;
import lib.BaseActivity.MyBaseActivity;
import lib.clsGlobal.Global;
import lib.clsHttpAccess_CallBack;

public class act_MLKZ_Home_bak extends MyBaseActivity {

    //存放梅陇客栈首页结构
    //如：华理知道itemBean(标题，新消息数量，超链接)→ArrayList数组→华理信息Map
    private Map<String, ArrayList<itemBean>> allMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mlkz_home_page);

        load_HomePage();                                                                            //加载梅陇客栈首页
    }


    @Override
    protected void onResume() {
        Global.activity = this;                                                                       //设置当前活动窗体
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, act_MainActivity.class));                                //跳回至主界面
    }

    /**
     * 加载梅陇客栈首页
     */
    private void load_HomePage() {
        SharedPreferences sp = getSharedPreferences(Global.sp_Config, 0);
        String cookie = sp.getString(Global.sp_Cookie, "");
        //MLKZ首页页面
        clsHttpAccess_CallBack.getSingleton().getHttp("http://bbs.ecust.edu.cn/forum.php?mobile=yes", cookie,
                new clsHttpAccess_CallBack.OnHttpVisitListener() {
                    @Override
                    public void onHttpBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, String rtnHtmlMessage) {

                    }

                    @Override
                    public void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String htmlResult) {
                        if (bSucceed) {
                            allMap = parseHtml(htmlResult);         //解析数据
                            showViewByMap();
                        }
                    }

                    @Override
                    public void onPictureLoadCompleted(String url, String cookie, boolean bSucceed, byte[] pic) {

                    }


                    @Override
                    public void onPictureBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, byte[] rtnPicBytes) {
                    }

                });
    }

    /**
     * 解析生成数据成Map结构
     *
     * @param html
     * @return
     */
    private Map<String, ArrayList<itemBean>> parseHtml(String html) {
        Map<String, ArrayList<itemBean>> all =
                new LinkedHashMap<String, ArrayList<itemBean>>();                                //存放全部数据

        html = html.replace("bm_c even", "bm_c add");                                               //替换间隔的背景样式
        html = html.replace("bm_c add", "bm_c_add");                                                //去除div中空格,否则Jsoup无法正确解析

        Document doc = Jsoup.parse(html);
        Elements collection_fl = doc.getElementsByClass("fl");                                      //我收藏的板块、华理信息、梅陇水库等

        for (Element single_fl : collection_fl) {                                                   //枚举版块信息（我收藏的板块、华理信息、梅陇水库等）
            String parent_name = single_fl.getElementsByClass("bm_h").text().trim();
            ArrayList<itemBean> arrlist_parent = new ArrayList<itemBean>();

            if (!parent_name.contains("我收藏的版块")) {
//                Global.log(Activity_Tag, "=======" + parent_name + "=======");

                Elements collection_bm_c_add = single_fl.getElementsByClass("bm_c_add");

                for (Element single_bm_c_add : collection_bm_c_add) {                               //枚举子版块信息(梅陇水库、情感驿站、华理鹊桥等)
                    String title = single_bm_c_add.select("a").first().text();                       //小标题名称（如华理知道）
                    String href = Global.bbs + "/" + single_bm_c_add.select("a").first().attr("href");    //超链接
                    String count_of_new_message = "0";                                               //新消息数量
                    if (single_bm_c_add.toString().contains("<font")) {                             //有新消息则进行解析，否则为0
                        count_of_new_message = single_bm_c_add.select("font").first().text()
                                .replace("(", "")
                                .replace(")", "")
                                .trim();
                    }

                    //新建结构体
                    itemBean map_child = new itemBean(title, count_of_new_message, href);
                    arrlist_parent.add(map_child);

//                    Global.log(Activity_Tag, title + " " + count_of_new_message + " " + href);
                }
                all.put(parent_name, arrlist_parent);
            }
        }
        return all;
    }

    /**
     * 根据this.allMap来显示出View
     */
    private void showViewByMap() {
        //枚举Map结构
        for (Map.Entry<String, ArrayList<itemBean>> entry : allMap.entrySet()) {
            String big_title = entry.getKey();                                                      //大标题
            Global.log("======" + big_title + "======");
            ArrayList<itemBean> itemBeans = entry.getValue();

            for (itemBean tmp : itemBeans) {
                String title = tmp.title;
                String message_count = tmp.new_msg_count;
                String href = tmp.href;
                Global.log(title + " " + message_count + " " + href);                      //小标题
            }


        }


        ArrayList<HashMap<String, Object>> lstImageItem2 = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < 20; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemImage", R.drawable.icon);//添加图像资源的ID
            map.put("ItemText", "bb" + String.valueOf(i));//按序号做ItemText
            lstImageItem2.add(map);
        }
        //生成适配器的ImageItem <====> 动态数组的元素，两者一一对应
        SimpleAdapter saImageItems2 = new SimpleAdapter(this, //没什么解释
                lstImageItem2,//数据来源
                R.layout.gv,//night_item的XML实现

                //动态数组与ImageItem对应的子项
                new String[]{"ItemImage", "ItemText"},

                //ImageItem的XML文件里面的一个ImageView,两个TextView ID
                new int[]{R.id.gv_iv, R.id.gv_tv});

        GridView gv2 = (GridView) findViewById(R.id.homepage_gridview3);
        gv2.setAdapter(saImageItems2);
        gv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                HashMap<String, Object> item = (HashMap<String, Object>) parent.getItemAtPosition(position);
                //显示所选Item的ItemText
                Global.toastMsg("bb" + position);
            }
        });
        setGridViewHeightBasedOnChildren(gv2);
    }

    /**
     * 动态设置gridview的高度
     *
     * @param gridView
     */
    public void setGridViewHeightBasedOnChildren(GridView gridView) {
        ListAdapter gridAdapter = gridView.getAdapter();
        if (gridAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < gridAdapter.getCount(); i += 2) {
            View gridItem = gridAdapter.getView(i, null, gridView);
            gridItem.measure(0, 0);
            totalHeight += gridItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);
    }

    //存放小标题的数据结构(如梅陇水库、华理知道、情感驿站等)
    class itemBean {
        String title;                                                                               //子版块标题
        String new_msg_count;                                                                       //子版块新消息数量
        String href;                                                                                //子版块超链接地址

        public itemBean(String title, String new_msg_count, String href) {
            this.title = title;
            this.new_msg_count = new_msg_count;
            this.href = href;
        }
    }
}
