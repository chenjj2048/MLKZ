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

package ecust.library.books_query;

import android.support.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;

import ecust.library.books_query.structWebData.Book;
import ecust.library.books_query.structWebData.ViewState;
import ecust.library.books_query.structWebData.WebResult;
import lib.CustomXmlPullParserUtil;
import lib.logUtils.logUtil;

/**
 * 解析网页，并且存储Asp.net中类似Session的信息（我不造究竟怎么称呼他）
 */
public class HtmlParser {

    /**
     * 解析书本集合
     *
     * @param str 网页结果
     */
    public WebResult parseWebResult(String str) {
        CustomXmlPullParserUtil xmlUtil = new CustomXmlPullParserUtil(str);
        WebResult mWebResult = new WebResult();

        //移动至接近主要内容的地方
        while (!xmlUtil.isEndDocument()) {
            if (xmlUtil.isStartTag("span") && xmlUtil.containValue("id", "Label1")) {
                //跳过一处字符( "共找到"字样 )
                xmlUtil.getNextNonBlankText();
                //找到了共多少条记录
                try {
                    mWebResult.booksTotalCount = Integer.parseInt(xmlUtil.getNextNonBlankText().trim());
                } catch (NumberFormatException e) {
                    logUtil.printExceptionLog(this, e);
                }
                break;
            } else
                xmlUtil.next();
        }

        logUtil.d(this, "共查询到 " + mWebResult.booksTotalCount + " 条记录");
        if (mWebResult.booksTotalCount <= 0) return mWebResult;

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
                            mWebResult.pageIndex = Integer.parseInt(pageIndex);
                        } catch (NumberFormatException e) {
                            logUtil.printExceptionLog(this, e);
                        }
                        break;
                    }
                    return mWebResult;
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
                        mWebResult.mBooks.add(mBook);
                    }
                }
            }
            xmlUtil.next();
        }
        return mWebResult;
    }

    /**
     * 返回网页结果中两个动态的参数值
     */
    @Nullable
    public ViewState parseValue(String str) {
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
            logUtil.printExceptionLog(this, e);
        }
        return null;
    }

}
