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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import ecust.mlkz.secondaryPage.struct_Forum_Information.struct_MLKZ_Data;

/**
 * 自定义VollyRequest
 * 解析请求页面的结果到struct_MLKZ_Data类
 */
public class mlkzRequest extends Request<struct_MLKZ_Data> {
    private final Response.Listener<struct_MLKZ_Data> mListener;
    private String cookie;

    /**
     * 构造函数
     */
    protected mlkzRequest(int method, String url, String cookie, Listener<struct_MLKZ_Data> mListener,
                          Response.ErrorListener listener) {
        //先转换成PC版地址
        super(method, url, listener);
        this.cookie = cookie;
        this.mListener = mListener;
    }

    /**
     * 指定Cookie内容
     *
     * @throws AuthFailureError
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        if (cookie != null && cookie.length() > 0) {
            headers.put("Cookie", cookie);
        }
        return headers;
    }

    /**
     * 解析数据到指定格式
     */
    @Override
    protected Response<struct_MLKZ_Data> parseNetworkResponse(NetworkResponse response) {
        //网页html源码
        String htmlString;
        try {
            //结果转成字符串
            htmlString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            //解析
            struct_MLKZ_Data mResponse = new htmlParser().parseAllData(htmlString);

            return Response.success(mResponse, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(struct_MLKZ_Data response) {
        mListener.onResponse(response);
    }
}
