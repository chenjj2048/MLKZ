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
import android.widget.TextView;

import ecust.main.R;
import ecust.mlkz.cls_MLKZ_Login;
import lib.Global;


public class activity_MLKZ_Secondary_Page extends Activity implements clsBBSConsole.OnResponseListener {
    private String cookie;
    private clsBBSConsole mBBSConsole;

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

        //连接
        initBBSConsole();
        mBBSConsole.openNewURlAddress(sectionURL, cookie);
    }

    //初始化
    private void initBBSConsole() {
        mBBSConsole = new clsBBSConsole(this);
        mBBSConsole.setOnResponseListener(this);
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

    /**
     * 消息返回
     */
    @Override
    public void onResponse(String htmlResult) {
        TextView textView = (TextView) findViewById(R.id.mlkz_secondary_page_textview);
        textView.setText(htmlResult);
    }
}
