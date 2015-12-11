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
 * Created by 彩笔怪盗基德 on 2015/11/25
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.library.books_query.modles;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 网页解析出来的结果
 * http://202.120.96.42:8081/webpac/querybookx.aspx
 */
public class WebResponse {
    //书本集合
    @NonNull
    public List<Book> mBooks = new ArrayList<>(30);
    //查询到的书的总数
    public int booksTotalCount = 0;
    //当前的页面下标（第几页）
    public int pageIndex = 0;
}
