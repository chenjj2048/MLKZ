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
 * Created by 彩笔怪盗基德 on 2015/10/3
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package lib;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;

import lib.clsUtils.logUtil;

/**
 * 自定义XmlPullParser的辅助工具
 */
public class myXmlPullParserUtils {

    public void printCurrentLine(XmlPullParser parser) {
        logUtil.v(this, "[当前行数] " + parser.getLineNumber());
    }

    /**
     * 移动至下一个
     * 避免每次都写try...catch，好麻烦
     */
    public int moveToNext(XmlPullParser parser) {
        //给一个初始化值，避免为空，这个值对后面结果不影响
        int event = XmlPullParser.START_DOCUMENT;
        try {
            event = parser.next();
        } catch (Exception e) {
        }
        return event;
    }

    /**
     * 移动至下一个标签
     */
    public int moveToNextStartTag(XmlPullParser parser) {
        int event;
        do {
            event = moveToNext(parser);
            if (event == XmlPullParser.START_TAG)
                return event;
        } while (event != XmlPullParser.END_DOCUMENT);

        return XmlPullParser.END_DOCUMENT;
    }

    /**
     * 移动至下一个有对应名字的标签
     *
     * @param tagName    指定的Tag名字
     * @param endTagName 结束的Tag,遇到就结束，null表示这一项无效
     * @return 返回值为-1时，代表遇到了结束位置，没找到
     */
    public int moveToNextStartTag(XmlPullParser parser, @NonNull String tagName, @Nullable String endTagName) {
        int event;
        do {
            event = moveToNext(parser);

            //找到了目标
            if (event == XmlPullParser.START_TAG && tagName.equals(parser.getName()))
                return event;

            //遇到结束位置
            if (event == XmlPullParser.END_TAG && parser.getName().equals(endTagName))
                return -1;
        } while (event != XmlPullParser.END_DOCUMENT);

        return XmlPullParser.END_DOCUMENT;
    }


    /**
     * 移动至下一个TEXT
     *
     * @param minLength Text最小的长度
     * @return 字符串
     */
    public String moveToNextText(XmlPullParser parser, int minLength) {
        int event;
        do {
            event = moveToNext(parser);
            if (event == XmlPullParser.TEXT) {
                String string = parser.getText();
                if (string.length() >= minLength)
                    return string;
            }
        } while (event != XmlPullParser.END_DOCUMENT);

        return "";
    }
}
