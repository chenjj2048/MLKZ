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

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import lib.clsUtils.logUtil;

public class htmlParser {
    private static final String tag = "mlkz.secondaryPage.htmlParser";


    /**
     * 解析数据
     *
     * @param html html数据
     */
    protected static void parseHtmlData(String html) {
        XmlPullParser parser = Xml.newPullParser();

        //事件消息类型
        int event;

        //转义&，否则Pull会报错
        byte[] bytes = html.replace("&", "&amp;").getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);

        //设置内容
        try {
            parser.setInput(inputStream, "UTF-8");
            event = parser.getEventType();
        } catch (XmlPullParserException e) {
            logUtil.printException(tag, e);
            return;
        }

        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    final String id = parser.getAttributeValue(null, "id");
                    final String className = parser.getAttributeValue(null, "class");
                    logUtil.e(tag,parser.getLineNumber()+" "+id+" "+className);
                    if ("div".equals(parser.getName())) {
                        if ("pt".equals(id) && "bm cl".equals(className)) {
                            //解析版块目录
                            //如：论坛›特色版块›学联学代会› 志愿服务总队
                            logUtil.d(tag, "========头部信息========");
                        } else if ("forumleftside".equals(id) && "tbn".equals(className)) {
                            //解析版块导航
                            logUtil.d(tag, "========版块导航========");
                        } else if ("vfboxs".equals(className)) {
                            //解析子版块
                            logUtil.d(tag, "========子版块========");
                        } else if ("th".equals(className)) {
                            //解析排序方式、精华帖
                            logUtil.d(tag, "========排序方式、精华帖========");
                        } else if ("bm_c".equals(className)) {
                            //解析贴子（主要内容啊！）
                            logUtil.d(tag, "========帖子内容========");
                        }
                    } else if ("ul".equals(parser.getName())) {
                        if ("thread_types".equals(id) && "ttp bm cl".equals(className)) {
                            //解析贴子分类筛选
                            logUtil.d(tag, "=========分类筛选========");
                        } else if ("newspecial_menu".equals(id) && "p_pop".equals(className)) {
                            //解析发布（发表帖子、投票、悬赏、活动）
                            logUtil.d(tag, "=========发布内容========");
                        }
                    }
                    break;
            }


            //继续寻找下一个
            try {
                event = parser.next();
            } catch (Exception e) {
                logUtil.w(tag, "[未成对标签] Line=" + parser.getLineNumber());
            }
        }
    }
}
