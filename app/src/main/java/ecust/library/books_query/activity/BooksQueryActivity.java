/**
 * Created by 彩笔怪盗基德 on 2015/11/22
 * 托管地址：https://github.com/chenjj2048
 * .
 */
package ecust.library.books_query.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.SearchView;

import CustomWidgets.BaseAppCompatActivity;
import butterknife.Bind;
import butterknife.ButterKnife;
import ecust.library.books_query.http.ApiConnecter;
import ecust.library.books_query.modles.BooksQueryRequest;
import ecust.main.R;

public class BooksQueryActivity extends BaseAppCompatActivity {
    @Bind(R.id.library_book_query_searchview)
    SearchView mSearchView;

    BooksResultFragment mResultFragment;
    BooksQueryFragment mQueryFragment;
    HistoryFragment mHistoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_books_query);
        ButterKnife.bind(this);
        setSupportActionBar(getSupportToolBar(this));

        init();
    }

    private void init() {
        mResultFragment = new BooksResultFragment().setContext(this);
        mQueryFragment = new BooksQueryFragment().setContext(this);
        mHistoryFragment = new HistoryFragment();


        toggleToFragment(mHistoryFragment);
        mSearchView.onActionViewExpanded();
        mSearchView.setOnQueryTextListener(new SearchViewListener());
    }

    public void toggleToFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.library_book_query_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ApiConnecter.getInstance().recycle();
    }

    private class SearchViewListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String newText) {
            //默认搜索标题
            if (!TextUtils.isEmpty(newText)) {
                BooksQueryRequest mRequest = new BooksQueryRequest.Builder().title(newText).build();
                mQueryFragment.startNewQuery(mRequest);
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (TextUtils.isEmpty(newText)) {
                toggleToFragment(mHistoryFragment);
            } else {
                toggleToFragment(mQueryFragment);
                mQueryFragment.setQueryString(newText);
            }
            return false;
        }
    }

}
