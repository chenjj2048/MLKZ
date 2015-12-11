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
 * Created by 彩笔怪盗基德 on 2015/11/24
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import utils.logUtils.logUtil;

/**
 * 自定义封装下XmlPullParser
 * 提供点新功能，更利于搜索内容
 */
//Todo:
@SuppressWarnings("unused")
public class CustomXmlPullParserUtil {
    private XmlPullParser parser;
    private int event;

    /**
     * 关联内容，新建XmlPullParser,默认编码 UTF-8
     *
     * @param input 输入的文本
     */
    public CustomXmlPullParserUtil(String input) {
        this(input, "UTF-8");
    }

    /**
     * 关联内容，新建XmlPullParser
     *
     * @param input         输入的文本
     * @param inputEncoding 编码
     */
    public CustomXmlPullParserUtil(String input, String inputEncoding) {
        if (input == null || inputEncoding == null)
            throw new IllegalArgumentException("输入内容或编码不能为空");

        //转义&，否则Pull会报错
        byte[] bytes = input.replace("&", "&amp;").getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);

        //设置内容
        try {
            parser = Xml.newPullParser();
            parser.setInput(inputStream, inputEncoding);
            event = parser.getEventType();
        } catch (XmlPullParserException e) {
            logUtil.printExceptionLog(this, e);
        }
    }

    /**
     * 获得对象
     *
     * @return 获得XmlPullParser对象，已经设置好了数据
     */
    public XmlPullParser getXmlPullParser() {
        return parser;
    }

    /**
     * 通过key获取Value
     */
    public String getAttributeValue(@NonNull String key) {
        return parser.getAttributeValue(null, key);
    }

    /**
     * 是否含有某个键值对
     *
     * @param key   key
     * @param value value
     */
    public boolean containValue(@NonNull String key, @NonNull String value) {
        return value.equals(parser.getAttributeValue(null, key));
    }

    public boolean isStartlDocument() {
        return event == XmlPullParser.START_DOCUMENT;
    }

    public boolean isEndDocument() {
        return event == XmlPullParser.END_DOCUMENT;
    }

    public boolean isStartTag() {
        return event == XmlPullParser.START_TAG;
    }

    public boolean isStartTag(@NonNull String tagName) {
        return event == XmlPullParser.START_TAG && tagName.equals(parser.getName());
    }

    public boolean isEndTag() {
        return event == XmlPullParser.END_TAG;
    }

    public boolean isEndTag(@NonNull String tagName) {
        return event == XmlPullParser.END_TAG && tagName.equals(parser.getName());
    }

    public boolean isText() {
        return event == XmlPullParser.TEXT;
    }


    /**
     * 移动xmlPull到指定位置
     *
     * @param searchCondition 搜索条件
     */
    public void moveTo(SearchCondition searchCondition) {


    }

    /**
     * 移动至下一条
     * 这条语句大多数时候是在循环当中的，这里用了try..catch是不好，但木有办法只能这么写
     * 经常网页源码里会有缺失的未配对标签，导致XmlPullParser.next()报错，影响了event的赋值
     * 假如一遇到错误就罢工，余下的数据就没法解析了，只能在循环当中忽略下这个
     */
    public int next() {
        try {
            event = parser.next();
            return event;
        } catch (Exception e1) {
            logUtil.v(this, "未成对标签");
            try {
                event = parser.getEventType();
                return event;
            } catch (XmlPullParserException e2) {
                logUtil.printExceptionLog(this, e2);
                return event;
            }
        }
    }

    @NonNull
    @Deprecated
    public String getNextText() {
        //尽量不要用这个，有时候经常返回一些空的内容
        return this.getNextText(0);
    }

    /**
     * 移动至下一个TEXT
     *
     * @param minLength Text最小的长度
     * @return 字符串
     */
    @NonNull
    @Deprecated
    public String getNextText(int minLength) {
        do {
            this.next();
            if (event == XmlPullParser.TEXT) {
                String string = parser.getText();
                if (string.length() >= minLength)
                    return string;
            }
        } while (event != XmlPullParser.END_DOCUMENT);
        return "";
    }

    /**
     * 获得非空的字符串（不返回全是空白和换行符的字符串）
     */
    @NonNull
    public String getNextNonBlankText() {
        while (event != XmlPullParser.END_DOCUMENT) {
            String tmp = getNextText(0);
            if (!TextUtils.isEmpty(tmp.replaceAll("\r|\n|\t| ", "")))
                return tmp;
        }
        return "";
    }

    public String getName() {
        return parser.getName();
    }

    /**
     * 搜索条件
     */
    public static class SearchCondition {
        /**
         * 目标类型，对应
         * XmlPullParser.START_TAG
         * XmlPullParser.END_TAG
         */
        private int targetType;
        //搜索的标签名称
        private String targetName;
        //END_TAG中遇到下面名称，停止搜索
        private String endTagName;

        public int getTargetType() {
            return this.targetType;
        }

        public void setTargetType(int type) {
            if (type != XmlPullParser.START_TAG && type != XmlPullParser.END_TAG)
                throw new IllegalArgumentException("参数只能限定两者之一");
            this.targetType = type;
        }

        public String getTargetName() {
            return this.targetName;
        }

        public void setTargetName(String targetName) {
            this.targetName = targetName;
        }

        public String getEndTagName() {
            return this.endTagName;
        }

        public void setEndTagName(String endTagName) {
            this.endTagName = endTagName;
        }
    }
}
