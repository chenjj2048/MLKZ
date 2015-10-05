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
 * Created by 彩笔怪盗基德 on 2015/10/2
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.mlkz.secondaryPage;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import ecust.mlkz.secondaryPage.struct_Forum_Information.*;
import lib.logUtils.abstract_LogUtil;

/**
 * 网页页面操作类
 * 不涉及UI
 */
public class clsBBSConsole implements Response.ErrorListener, Response.Listener<String> {
    private Context context;
    private RequestQueue mQueue;

    private OnResponseListener onResponseListener;

    public clsBBSConsole(Context context) {
        this.context = context;
        this.mQueue = Volley.newRequestQueue(context);
    }

    /**
     * 设置监听
     */
    protected void setOnResponseListener(OnResponseListener onResponseListener) {
        this.onResponseListener = onResponseListener;
    }

    //新页面加载
    protected void openNewURlAddress(String url, final String cookie) {
        StringRequest stringRequest = new StringRequest(url, this, this) {
            //设置Cookie
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                if (cookie != null && cookie.length() > 0) {
                    headers.put("Cookie", cookie);
                }
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        abstract_LogUtil.e(this, error.getMessage());
    }

    /**
     * 消息返回
     */
    @Override
    public void onResponse(String response) {
        //数据解析
        htmlParser htmlParser = new htmlParser();

        struct_MLKZ_Data mlkz_data = htmlParser.parseAllData(response);

        if (onResponseListener != null) {
            onResponseListener.onResponse(response);
        }
    }

    /**
     * 命令操作的回调接口
     */
    public interface OnResponseListener {
        void onResponse(String htmlResult);
    }
}

//主版块跳转
//子版块跳转

//下一页

//收藏版块

//发帖
//发投票
//发悬赏

//精华

//排序：默认排序、发帖时间、回复/查看、查看、最后发表、热门

