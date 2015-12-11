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
 * Created by 彩笔怪盗基德 on 2015/11/23
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.library.books_query.http;

import android.support.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;

import ecust.library.books_query.modles.Book;
import ecust.library.books_query.modles.ViewState;
import ecust.library.books_query.modles.WebResponse;
import utils.CustomXmlPullParserUtil;
import utils.logUtils.logUtil;

/**
 * 解析网页结果
 * http://202.120.96.42:8081/webpac/querybookx.aspx
 */
public class HtmlParser {

    /**
     * 解析书本集合
     *
     * @param str 网页结果
     */
    public static WebResponse parseWebResult(String str) throws Exception {
        CustomXmlPullParserUtil xmlUtil = new CustomXmlPullParserUtil(str);
        WebResponse mWebResponse = new WebResponse();

        //移动至接近主要内容的地方
        while (!xmlUtil.isEndDocument()) {
            if (xmlUtil.isStartTag("span") && xmlUtil.containValue("id", "Label1")) {
                //跳过一处字符( "共找到"字样 )
                xmlUtil.getNextNonBlankText();
                //找到了共多少条记录
                try {
                    mWebResponse.booksTotalCount = Integer.parseInt(xmlUtil.getNextNonBlankText().trim());
                } catch (NumberFormatException e) {
                    logUtil.printExceptionLog(HtmlParser.class, e);
                }
                break;
            } else
                xmlUtil.next();
        }

        logUtil.d(HtmlParser.class, "共查询到 " + mWebResponse.booksTotalCount + " 条记录");
        if (mWebResponse.booksTotalCount <= 0) return mWebResponse;

        //主体部分开始
        while (!xmlUtil.isEndDocument()) {
            if (xmlUtil.isStartTag("tr")) {
                if (xmlUtil.containValue("align", "center")) {
                    //最底部，表明当前第几页
                    for (int i = 0; i <= 10; i++) {
                        String pageIndex = xmlUtil.getNextNonBlankText();
                        if (xmlUtil.next() != XmlPullParser.END_TAG || !"span".equals(xmlUtil.getName()))
                            continue;
                        try {
                            mWebResponse.pageIndex = Integer.parseInt(pageIndex);
                        } catch (NumberFormatException e) {
                            logUtil.printExceptionLog(HtmlParser.class, e);
                        }
                        break;
                    }
                    return mWebResponse;
                } else {
                    //内容部分
                    String title = xmlUtil.getNextNonBlankText();
                    //跳过第一行表名
                    if (!title.startsWith("题名")) {
                        Book mBook = new Book()
                                .title(title)
                                .author(xmlUtil.getNextNonBlankText())
                                .publisher(xmlUtil.getNextNonBlankText())
                                .publishTime(xmlUtil.getNextNonBlankText())
                                .CLCIndex(xmlUtil.getNextNonBlankText());
                        //添加到集合
                        mWebResponse.mBooks.add(mBook);
                    }
                }
            }
            xmlUtil.next();
        }
        return mWebResponse;
    }

    @Nullable
    public static ViewState parseViewState(String str) {
        XmlPullParser xmlPullParser = new CustomXmlPullParserUtil(str)
                .getXmlPullParser();

        ViewState mViewState = new ViewState();
        int event = XmlPullParser.START_DOCUMENT;
        try {
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG
                        && "input".equals(xmlPullParser.getName())) {
                    //找下面两个动态的值
                    switch (xmlPullParser.getAttributeValue(null, "name")) {
                        case "__VIEWSTATE":
                            mViewState.__VIEWSTATE = xmlPullParser.getAttributeValue(null, "value");
                            break;
                        case "__EVENTVALIDATION":
                            mViewState.__EVENTVALIDATION = xmlPullParser.getAttributeValue(null, "value");
                            //返回结果
                            return mViewState;
                    }
                }
                event = xmlPullParser.next();
            }
        } catch (Exception e) {
            logUtil.printExceptionLog(HtmlParser.class, e);
        }
        return null;
    }
}
