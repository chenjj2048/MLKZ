/**
 * .
 * Created by 彩笔怪盗基德 on 2015/11/25
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
    public int currentPageIndex = 0;

    @Override
    public String toString() {
        return "WebResponse{" +
                "booksTotalCount=" + booksTotalCount +
                ", currentPageIndex=" + currentPageIndex +
                ", mBooks=" + mBooks +
                '}';
    }

    public boolean isEmpty() {
        return booksTotalCount <= 0;
    }
}
