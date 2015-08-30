package ecust.lecture;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import lib.Const;
import lib.clsApplication;
import lib.clsUtils.logUtil;
import lib.Const.PathFactory.PathType;
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
 * Created by 彩笔怪盗基德 on 2015/7/21
 * Copyright (C) 2015 彩笔怪盗基德
 */

//讲座版块数据库
public class DataBase_Lecture extends SQLiteOpenHelper {
    private static final int currentVersion = 1;                    //当前版本
    private static final String dataBaseName = Const.PathFactory.getFileSavedPath(PathType.LECTURE_DATABASE);        //数据库名称

    private static DataBase_Lecture db_singleton;       //单例模式
    public clsCatalog catalog;        //catalog对象（讲座目录）
    public clsDetail detail;        //detail对象（讲座详情）

    //构造函数
    public DataBase_Lecture() {
        //DataBase_Lecture(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
        super(clsApplication.getContext(), dataBaseName, null, currentVersion);
        logUtil.i(this, "[数据库地址]" + dataBaseName);
        this.catalog = new clsCatalog();
        this.detail = new clsDetail();
    }

    /**
     * 获取单例
     */
    public static DataBase_Lecture getSingleton() {
        if (db_singleton == null)
            db_singleton = new DataBase_Lecture();
        return db_singleton;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        logUtil.i(this, "[数据库创建]当前版本=" + currentVersion + " " + dataBaseName);
        //创建新表
        db.execSQL(catalog.CreateTableCatalog);
        db.execSQL(detail.CreateTableDetail);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        logUtil.i(this, "[数据库版本更新]" + oldVersion + "→" + newVersion);
    }

    /**
     * 讲座主目录
     */
    public class clsCatalog {
        /**
         * 标题-title,发布时间-time,讲座地址-url
         */
        private final String CreateTableCatalog = "CREATE TABLE catalog" +
                "(title TEXT,time TEXT,url TEXT PRIMARY KEY)";

        /**
         * 插入数据
         * 保证不会有重复数据
         */
        public void InsertOrReplaceData(List<struct_LectureCatalogItem> mData) {
            if (mData == null) return;

            SQLiteDatabase db = null;
            try {
                //开启事务
                db = getWritableDatabase();
                db.beginTransaction();

                /**
                 *添加数据至SQLite
                 */
                int count_of_added = 0;
                for (struct_LectureCatalogItem item : mData) {
                    if (item != null) {
                        db.execSQL("REPLACE INTO catalog(title,time,url) values(?,?,?)",
                                new Object[]{item.title, item.time, item.url});
                        count_of_added++;
                    }
                }
                db.setTransactionSuccessful();
                logUtil.i(this, "[数据库添加]共" + count_of_added + "条");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //释放连接
                if (db != null) {
                    db.endTransaction();
                    db.close();
                }
            }
        }

        /**
         * 获取全部数据
         */
        public List<struct_LectureCatalogItem> getAllData() {
            SQLiteDatabase db = null;   //数据库
            Cursor cursor = null;       //游标
            List<struct_LectureCatalogItem> result = new ArrayList<>();    //返回数据集
            try {
                db = db_singleton.getReadableDatabase();
                //从catalog表中读取数据
                cursor = db.rawQuery("SELECT * FROM catalog ORDER BY time DESC,url DESC", null);
                while (cursor.moveToNext()) {
                    //构造数据
                    struct_LectureCatalogItem item = new struct_LectureCatalogItem();
                    item.title = cursor.getString(cursor.getColumnIndex("title"));
                    item.time = cursor.getString(cursor.getColumnIndex("time"));
                    item.url = cursor.getString(cursor.getColumnIndex("url"));

                    //添加数据
                    result.add(item);
                }
            } catch (Exception e) {
                logUtil.e(this, e.toString());
                e.printStackTrace();
            } finally {
                //关闭连接
                if (cursor != null) cursor.close();
                if (db != null) db.close();
            }
            logUtil.i(this, "[数据库查询成功]共" + result.size() + "条数据");
            return result;
        }
    }

    /**
     * 讲座详细内容
     */
    public class clsDetail {
        /**
         * 报告题目-title,开始时间-time,报告地点-address,报告人-reporter,主办单位-organization,
         * 备注-remark,讲座信息地址-url（主键）
         */
        private final String CreateTableDetail = "CREATE TABLE detail" +
                "(title TEXT,time TEXT,address TEXT,reporter TEXT,organization TEXT," +
                "remark TEXT,url PRIMARY KEY)";

        /**
         * 插入数据
         * 保证不会有重复数据
         * Insert可能会有重复插入
         */
        public void SaveData(struct_LectureDetail mData) {
            final String insertSQL = "REPLACE INTO detail(title,time,address,reporter,organization," +
                    "remark,url) values(?,?,?,?,?,?,?)";
            SQLiteDatabase db = null;
            try {
                db = db_singleton.getWritableDatabase();
                db.execSQL(insertSQL, new Object[]{mData.title, mData.startTime, mData.address,
                        mData.reporter, mData.organization, mData.remark, mData.url});
            } catch (Exception e) {
                logUtil.e(this, e.toString());
                e.printStackTrace();
            } finally {
                if (db != null)
                    db.close();
            }
        }

        /**
         * 读取数据库数据
         */
        public struct_LectureDetail GetData(String url) {
            final String selectSQL = "SELECT * FROM detail WHERE url=?";

            SQLiteDatabase db = null;
            Cursor cursor = null;
            struct_LectureDetail result = new struct_LectureDetail();
            try {
                db = db_singleton.getReadableDatabase();
                cursor = db.rawQuery(selectSQL, new String[]{url});

                if (cursor.moveToNext()) {
                    //构造数据
                    result.title = cursor.getString(cursor.getColumnIndex("title"));
                    result.startTime = cursor.getString(cursor.getColumnIndex("time"));
                    result.address = cursor.getString(cursor.getColumnIndex("address"));
                    result.reporter = cursor.getString(cursor.getColumnIndex("reporter"));
                    result.organization = cursor.getString(cursor.getColumnIndex("organization"));
                    result.remark = cursor.getString(cursor.getColumnIndex("remark"));
                    result.url = cursor.getString(cursor.getColumnIndex("url"));
                }
            } catch (Exception e) {
                logUtil.e(this, e.toString());
                e.printStackTrace();
            } finally {
                if (cursor != null) cursor.close();
                if (db != null) db.close();
            }
            return result;
        }
    }
}
