/**
 * .
 * Created by 彩笔怪盗基德 on 2015/12/11
 * github：https://github.com/chenjj2048
 * .
 */

package ecust.library.books_query.interfaces;

import ecust.library.books_query.modles.WebResponse;

public interface QueryBooksCallBack {
    public void onFailure(Exception e);

    public void onFirstQuerySuccess(WebResponse webResponse);

    public void onContinueQuerySuccess(WebResponse webResponse);
}
