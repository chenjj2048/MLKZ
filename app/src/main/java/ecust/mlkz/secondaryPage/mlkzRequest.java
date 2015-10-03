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

package ecust.mlkz.secondaryPage;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;

import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_ForumPageAllData;

/**
 * 自定义VollyRequest
 * 解析请求页面的结果到struct_ForumPageAllData类
 */
public class mlkzRequest extends Request<struct_ForumPageAllData> {
    private final Response.Listener<struct_ForumPageAllData> mListener;

    /**
     * 构造函数
     */
    public mlkzRequest(int method, String url, Listener<struct_ForumPageAllData> mListener,
                       Response.ErrorListener listener) {
        super(method, url, listener);
        this.mListener = mListener;
    }

    /**
     * 解析数据到指定格式
     */
    @Override
    protected Response<struct_ForumPageAllData> parseNetworkResponse(NetworkResponse response) {
        //网页html源码
        String htmlString;
        try {
            //结果转成字符串
            htmlString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            //解析
            struct_ForumPageAllData mResponse = null;

            return Response.success(mResponse, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(struct_ForumPageAllData response) {
        mListener.onResponse(response);
    }
}
