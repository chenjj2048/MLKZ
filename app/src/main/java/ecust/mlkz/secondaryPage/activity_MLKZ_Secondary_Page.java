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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import ecust.main.R;
import ecust.mlkz.cls_MLKZ_Login;
import lib.Global;
import lib.clsUtils.httpUtil;
import lib.clsUtils.logUtil;

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
        //From: http://bbs.ecust.edu.cn/forum.php?mod=forumdisplay&fid=91&mobile=yes
        //To:   http://bbs.ecust.edu.cn/forum.php?mod=forumdisplay&fid=91
        sectionURL=sectionURL.replace("&mobile=yes","");


        //当前cookie
        cookie = new cls_MLKZ_Login(this).getPreference().getCookie();
        httpUtil.getSingleton().getHttp(sectionURL, cookie, this);
    }

    @Override
    public void onHttpLoadCompleted(String url, String cookie, boolean bSucceed, String returnHtmlMessage) {
        if (!bSucceed) return;

        logUtil.e(this, returnHtmlMessage);

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
