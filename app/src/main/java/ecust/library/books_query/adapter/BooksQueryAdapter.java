/**
 * .
 * Created by 彩笔怪盗基德 on 2015/12/8
 * github：https://github.com/chenjj2048
 * .
 */

package ecust.library.books_query.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ecust.main.R;

public class BooksQueryAdapter extends BaseAdapter {
    public static final int QUERY_TITLE = 0;
    public static final int QUERY_AUTHOR = 1;
    public static final int QUERY_PUBLISHER = 2;
    private String mQueryString;
    private Context context;
    private String[] mSearchType = new String[]{"搜标题：", "搜作者：", "搜出版社："};

    public BooksQueryAdapter(Context context) {
        this.context = context;
    }

    public void setQueryString(String queryString) {
        this.mQueryString = queryString;
    }

    public String getQueryString() {
        return mQueryString;
    }

    @Override
    public int getCount() {
        return TextUtils.isEmpty(mQueryString) ? 0 : mSearchType.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressWarnings("all")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //才几个View，不会滑动的，就不用convertView了
        View view = LayoutInflater.from(context).inflate(R.layout.library_books_query_request_item, null);
        TextView mSearchTypeTextView = (TextView) view.findViewById(R.id.library_book_query_type);
        TextView mQueryStringTextView = (TextView) view.findViewById(R.id.library_book_query_querystring);

        mSearchTypeTextView.setText(mSearchType[position]);
        mQueryStringTextView.setText(mQueryString);

        view.setTag(position);
        return view;
    }
}
