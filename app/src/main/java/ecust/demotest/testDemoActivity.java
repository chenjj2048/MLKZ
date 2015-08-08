package ecust.demotest;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ecust.main.R;
import lib.BaseActivity.MyBaseActivity;
import lib.clsGlobal.logUtil;

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
 * Created by 彩笔怪盗基德 on 2015/8/6
 * Copyright (C) 2015 彩笔怪盗基德
 */
public class testDemoActivity extends MyBaseActivity implements View.OnClickListener {
    TextView tv;
    ListView listview;
    List<structMy> mData = new ArrayList<>();
    myadapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_demo);


        tv = (TextView) findViewById(R.id.demo_textview);
        listview = (ListView) findViewById(R.id.demo_listview);


        mData.add(new structMy("url=12", 5));
        mData.add(new structMy("d", 1));
        mData.add(new structMy("cc", 4));
        mData.add(new structMy("cc", 2));
        mData.add(new structMy("ww", 1));
        mData.add(new structMy("z", 3));
        mData.add(new structMy("z", 3));
        mData.add(new structMy("tt", 0));

        Collections.sort(mData);

        logUtil.i(this, "==============");
        Iterator<structMy> ggg = mData.iterator();
        while (ggg.hasNext()) {
            structMy tmp = ggg.next();
            logUtil.i(this, tmp.toString());
            if (tmp.age == 1)
                ggg.remove();
        }
        logUtil.i(this, "==============");
        ggg = mData.iterator();
        while (ggg.hasNext()) {
            structMy tmp = ggg.next();
            logUtil.i(this, tmp.toString());
        }


        adapter = new myadapter(this);
        listview.setAdapter(adapter);

    }

    @Override
    public void onClick(View v) {
    }

    public class myadapter extends BaseAdapter {
        private Context context;

        public myadapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView tv = new TextView(context);
//            structMy f[] = (structMy[]) mData.toArray();
//            String str =f[i].toString();
            tv.setText("aaa");
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            return tv;
        }
    }
}

class structMy implements Comparable<structMy> {
    String name;
    int age;

    public structMy(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public boolean equals(Object o) {
        logUtil.e(this, "equals");

        structMy t = (structMy) o;
        if (this.age == t.age)
            return true;
        else
            return false;
    }

    @Override
    public int hashCode() {
        logUtil.e(this, "hashcode");
        int result = name.hashCode();
        result = 31 * result + age;
        return result;
    }

    @Override
    public int compareTo(structMy another) {
        if (this.age < another.age)
            return 1;
        else if (this.age > another.age)
            return -1;
        else
            return 0;
    }

    @Override
    public String toString() {
        return "姓名：" + name + "  年龄：" + age;
    }
}