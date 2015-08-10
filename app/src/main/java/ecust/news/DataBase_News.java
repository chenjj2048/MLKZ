package ecust.news;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ecust.main.R;
import lib.clsGlobal.Const;
import lib.clsGlobal.clsApplication;
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
 * Created by 彩笔怪盗基德 on 2015/7/31
 * Copyright (C) 2015 彩笔怪盗基德
 */

// 新闻内容缓存数据库
// 包含新闻目录，以及详细信息两部分，图片另外算
public class DataBase_News extends SQLiteOpenHelper {
    private static final int currentVersion = 1;                    //当前版本
    private static final String dataBaseName = Const.getSQLDataBaseStoragePath() + "news.db";        //数据库名称
    public clsCatalog catalog = new clsCatalog();       //主目录
    private String currentCatalogTableName;     //版块名称

    public DataBase_News(String currentCatalogTableName) {
        super(clsApplication.getContext(), dataBaseName, null, currentVersion);
        this.currentCatalogTableName = currentCatalogTableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        logUtil.i(this, "[数据库创建]当前版本=" + currentVersion + " " + dataBaseName);

        String[] arr_catalog = clsApplication.getContext()
                .getResources().getStringArray(R.array.news_section_name);

        //创建8个表，对应8个版块目录
        for (String item : arr_catalog)
            db.execSQL(catalog.getCreateTableSQL(item));

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        logUtil.i(this, "[数据库版本更新]" + oldVersion + "→" + newVersion);
    }

    public class clsCatalog {
        /**
         * 标题-title,发布时间-time,新闻地址-url（主键）
         */
        public String getCreateTableSQL(String tableName) {
            return "CREATE TABLE " + tableName +
                    "(title TEXT,time TEXT,url TEXT PRIMARY KEY)";
        }

        /**
         * 获取全部数据
         */
        public List<struct_NewsCatalogItem> getAllData() {
            SQLiteDatabase db = null;   //数据库
            Cursor cursor = null;       //游标
            List<struct_NewsCatalogItem> result = new ArrayList<>();    //返回数据集
            try {
                db = getReadableDatabase();
                //从表中读取数据
                cursor = db.rawQuery("SELECT * FROM " + currentCatalogTableName +
                        " ORDER BY time DESC,url DESC", null);
                while (cursor.moveToNext()) {
                    //构造数据
                    struct_NewsCatalogItem item = new struct_NewsCatalogItem();
                    item.title = cursor.getString(cursor.getColumnIndex("title"));
                    item.time = cursor.getString(cursor.getColumnIndex("time"));
                    item.url = cursor.getString(cursor.getColumnIndex("url"));

                    //添加数据
                    result.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //关闭连接
                if (cursor != null) cursor.close();
                if (db != null) db.close();
            }
            logUtil.i(this, "[数据库查询成功][" + currentCatalogTableName + "]共" + result.size() + "条数据");
            return result;
        }

        /**
         * 批量插入数据
         * 保证不会有重复数据
         */
        public void InsertOrReplaceData(List<struct_NewsCatalogItem> mData) {
            if (mData == null) return;

            int count_of_item_added = 0;
            SQLiteDatabase db = null;
            try {
                db = getWritableDatabase();
                db.beginTransaction();  //开启事务

                for (struct_NewsCatalogItem item : mData) {
                    if (item != null) {
                        //执行添加命令
                        db.execSQL("REPLACE INTO " + currentCatalogTableName + "(title,time,url) values(?,?,?)",
                                new Object[]{item.title, item.time, item.url});
                        count_of_item_added++;
                    }
                }
                db.setTransactionSuccessful();
                logUtil.i(this, "[数据库数据添加成功][" + currentCatalogTableName + "]共" +
                        count_of_item_added + "条");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.endTransaction();
                    db.close();
                }
            }
        }
    }
}
