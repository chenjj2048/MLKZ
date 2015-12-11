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

package utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import utils.logUtils.abstract_LogUtil;
import utils.logUtils.logUtil;

/**
 * 自定义XmlPullParser的辅助工具
 */
//Todo:改改改，好难看,之后准备用ecust.lib.CustomPullParserUtils代替
@Deprecated
public class myXmlPullParserUtils {
    public final int TAG_NOT_FOUND = -1;
    private XmlPullParser parser;

    public myXmlPullParserUtils() {
    }

    public void setXmlPullParser(XmlPullParser parser) {
        this.parser = parser;
    }

    /**
     * 输出当前行的状态
     */
    @SuppressWarnings("unused")
    public void printLog(String tag) {
        if (!abstract_LogUtil.isDebug) return;

        tag = tag.equals("") ? "" : "[" + tag + "]";
        try {
            int event = parser.getEventType();
            switch (event) {
                case XmlPullParser.START_TAG:
                    abstract_LogUtil.v(this, tag + "[当前行数]" + parser.getLineNumber() + " [START TAG]" + parser.getName());
                    break;
                case XmlPullParser.TEXT:
                    abstract_LogUtil.v(this, tag + "[当前行数]" + parser.getLineNumber() + " [文本]" + parser.getText());
                    break;
                case XmlPullParser.END_TAG:
                    abstract_LogUtil.v(this, tag + "[当前行数]" + parser.getLineNumber() + " [END TAG]" + parser.getName());
                    break;
            }
        } catch (XmlPullParserException e) {
            //
        }
    }

    /**
     * 输出当前行的状态
     */
    @SuppressWarnings("unused")
    public void printLog() {
        printLog("");
    }

    /**
     * 移动至下一个
     * 避免每次都写try...catch，好麻烦
     */
    public int moveToNext() {
        //给一个初始化值，避免为空，这个值对后面结果不影响
        int event = XmlPullParser.START_DOCUMENT;
        try {
            event = parser.next();
        } catch (Exception e1) {
            logUtil.v(this, "未成对标签");
            try {
                event = parser.getEventType();
            } catch (XmlPullParserException e2) {
                /**
                 * ==================重要================
                 * next()失败的话，还要再次获取event值
                 * 有时候碰上未成对的标签，报错，返回了一个错误的event，直接导致后续解析接连出错
                 * 害我调试了半天
                 */
            }
        }
        return event;
    }

    /**
     * 移动至下一个标签
     */
    public int moveToNextStartTag() {
        int event;
        do {
            event = this.moveToNext();
            if (event == XmlPullParser.START_TAG)
                return event;
        } while (event != XmlPullParser.END_DOCUMENT);

        return XmlPullParser.END_DOCUMENT;
    }

    /**
     * 移动至下一个有对应名字的标签
     *
     * @param targetName     指定的Tag名字
     * @param stopEndTagName 结束的Tag,遇到就结束，null表示这一项无效
     * @return 返回值为-1时，代表遇到了结束位置，没找到
     */
    public int moveToNextStartTag(@NonNull String targetName, @Nullable String stopEndTagName) {
        int event;

        do {
            event = this.moveToNext();

            //找到了目标
            if (event == XmlPullParser.START_TAG && targetName.equals(parser.getName()))
                return event;

            //遇到结束位置
            if (event == XmlPullParser.END_TAG && parser.getName().equals(stopEndTagName))
                return TAG_NOT_FOUND;
        } while (event != XmlPullParser.END_DOCUMENT);

        return XmlPullParser.END_DOCUMENT;
    }

    /**
     * 移动至下一个有对应名字的标签
     *
     * @param targetName     指定的Tag名字
     * @param stopEndTagName 结束的Tag,遇到就结束，null表示这一项无效
     * @return 返回值为-1时，代表遇到了结束位置，没找到
     */
    public int moveToNextEndTag(@NonNull String targetName, @Nullable String stopEndTagName) {
        int event;
        abstract_LogUtil.Assert(this, !targetName.equals(stopEndTagName), "[moveToNextEndTag] 标签不许名称一样");
        do {
            event = this.moveToNext();

            //找到了目标
            if (event == XmlPullParser.END_TAG && targetName.equals(parser.getName()))
                return event;

            //遇到结束位置
            if (event == XmlPullParser.END_TAG && parser.getName().equals(stopEndTagName))
                return TAG_NOT_FOUND;
        } while (event != XmlPullParser.END_DOCUMENT);

        return XmlPullParser.END_DOCUMENT;
    }

    /**
     * 移动至下一个TEXT
     *
     * @param minLength Text最小的长度
     * @return 字符串
     */
    public String moveToNextText(int minLength) {
        int event;
        do {
            event = this.moveToNext();
            if (event == XmlPullParser.TEXT) {
                String string = parser.getText();
                if (string.length() >= minLength)
                    return string;
            }
        } while (event != XmlPullParser.END_DOCUMENT);

        return "";
    }



    /**
     * 封装一下，不用每次都try... catch
     */
    public int getEventType() {
        try {
            return parser.getEventType();
        } catch (XmlPullParserException e) {
            //正常使用，一般不会报错
            return -1;
        }
    }
}
