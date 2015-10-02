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
 * Created by 彩笔怪盗基德 on 2015/9/29
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.mlkz.secondaryPage;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import ecust.main.R;
import ecust.mlkz.cls_MLKZ_Login;
import lib.Global;
import lib.clsUtils.httpUtil;


public class activity_MLKZ_Secondary_Page extends Activity implements httpUtil.OnHttpVisitListener {
    //Cookie
    private String cookie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mlkz_secondary_page);

        Global.setTitle(this, "梅陇客栈");


        //设置当前版块标题、URL
        final String sectionTitle = getIntent().getStringExtra("title");
        String sectionURL = getIntent().getStringExtra("url");

        //地址由WAP版转换为电脑版
        sectionURL = adressConvertToWAP(sectionURL, false);

        //当前cookie
        cookie = new cls_MLKZ_Login(this).getPreference().getCookie();
        httpUtil.getSingleton().getHttp(sectionURL, cookie, this);

    }


    /**
     * 地址格式转换，能获取不同的网页结果
     * From: http://bbs.ecust.edu.cn/forum.php?mod=forumdisplay&fid=91&mobile=yes
     * To:   http://bbs.ecust.edu.cn/forum.php?mod=forumdisplay&fid=91&mobile=no
     *
     * @param url          地址
     * @param convertToWAP true转为WAP格式，false转为PC格式
     * @return URL
     */
    private String adressConvertToWAP(String url, boolean convertToWAP) {
        final String style_WAP = "&mobile=yes";
        final String style_PC = "&mobile=no";

        if (convertToWAP) {
            //WAP登陆
            return url.replace(style_PC, style_WAP);
        } else {
            //PC登陆
            return url.replace(style_WAP, style_PC);
        }
    }

    @Override
    public void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String returnHtmlMessage) {
        if (!bSucceed) return;

        TextView textView = (TextView) findViewById(R.id.mlkz_secondary_page_textview);
        textView.setText(returnHtmlMessage);

        //解析数据
        htmlParser.parseHtmlData(returnHtmlMessage);
    }

    @Override
    public void onHttpBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, String returnHtmlMessage) {

    }

    @Override
    public void onPictureLoadCompleted(String url, String cookie, boolean bSucceed, byte[] returnPicBytes) {

    }

    @Override
    public void onPictureBackgroundThreadLoadCompleted(String url, String cookie, boolean bSucceed, byte[] returnPicBytes) {

    }
}
