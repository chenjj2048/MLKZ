/**
 * .
 * Created by 彩笔怪盗基德 on 2015/12/8
 * github：https://github.com/chenjj2048
 * .
 */

package ecust.library.books_query.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ecust.library.books_query.modles.Book;
import ecust.main.R;

/**
 * ListView适配器
 */
public class BooksResultAdapter extends BaseAdapter {
    private Context context;

    private List<Book> mBooks = new ArrayList<>(50);

    public BooksResultAdapter(Context context) {
        this.context = context;
    }

    /**
     * 添加数据集
     */
    public void addBooks(List<Book> mBooksList) {
        for (Book mBook : mBooksList)
            if (!this.mBooks.contains(mBook))
                this.mBooks.add(mBook);
        this.notifyDataSetChanged();
    }

    /**
     * 清空数据集
     */
    public void clear() {
        mBooks.clear();
    }

    @Override
    public int getCount() {
        return (mBooks == null) ? 0 : mBooks.size();
    }

    @Override
    public Book getItem(int position) {
        return mBooks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.library_book_item, null);
            ButterKnife.bind(viewHolder, convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Book mBook = getItem(position);
        viewHolder.mTitle.setText(position + " " + mBook.getTitle());
        viewHolder.mAuthor.setText(mBook.getAuthor());
        viewHolder.mPublisher.setText(mBook.getPublisher());
        viewHolder.mPublishTime.setText(mBook.getPublishTime());
        viewHolder.mCLCIndex.setText(mBook.getCLCIndex());
        viewHolder.mBook = mBook;
        return convertView;
    }

    public class ViewHolder {
        public Book mBook;

        //书名
        @Bind(R.id.library_book_query_item_title)
        TextView mTitle;
        //作者
        @Bind(R.id.library_book_query_item_author)
        TextView mAuthor;
        //出版社
        @Bind(R.id.library_book_query_item_publisher)
        TextView mPublisher;
        //出版时间
        @Bind(R.id.library_book_query_item_publishetime)
        TextView mPublishTime;
        //索书号
        @Bind(R.id.library_book_query_item_clcindex)
        TextView mCLCIndex;
    }
}