/**
 * .
 * Created by 彩笔怪盗基德 on 2015/12/13
 * github：https://github.com/chenjj2048
 * .
 */

package ecust.library.books_query.activity;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import ecust.main.R;

public class HistoryFragment extends Fragment {
    LinearLayout mView;

    @Override
    @SuppressLint("InflateParams")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.library_books_query_history_fragment, null);
        return mView;
    }
}
