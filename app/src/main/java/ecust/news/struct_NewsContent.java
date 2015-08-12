package ecust.news;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

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
 * Created by 彩笔怪盗基德 on 2015/8/8
 * Copyright (C) 2015 彩笔怪盗基德
 * 代码托管：https://github.com/chenjj2048
 */

//新闻详细内容
//观察者模式，监视位图内存占用
public class struct_NewsContent implements Observer {
    int sum_bytes_of_bitmap = 0;       //位图占的内存总大小
    String title = "";           //新闻标题
    String release_time = "";    //发表日期
    String news_Source = "";      //稿件来源、来稿单位
    String author = "";             //作者
    String photo_author = "";        //摄影
    String editor = "";              //编辑
    String count_of_visit = "";      //访问量
    String body = "";               //新闻文字部分（这个是临时的，解析完后就可以回收）
    List<String> pic_url;       //图片地址
    List<String> content;          //文字-图片URL交替部分（一行文字、一行图片交替）（重要数据在这里）
    HashMap<String, PicHolder> bitmapHashMap = new HashMap<>();       //在内存中暂存Bitmap（内存不够就及时回收）

    //位图占用空间变化
    @Override
    public void update(Observable observable, Object data) {
        int memory_size_changed = (int) data;
        this.sum_bytes_of_bitmap += memory_size_changed;        //修改位图占用大小标记
//        logUtil.i(this, "[位图占用内存大小变化后] " +
//                String.format("%,d", this.sum_bytes_of_bitmap) + " 字节");
    }
}

//被观察者，内存占用
class PicHolder extends Observable {
    final int max_loadTimes = 3;      //最多加载次数，避免重复陷入死循环
    ImageView imageView;        //imageView（始终保持）
    int loadTimes = 0;            //加载次数，避免不停地加载失败
    String url;         //图片地址  这个交给异步显示的任务 根据url来读取缓存文件
    boolean found = false;        //记录是否被缓存过（内存中有，或本地缓存中有图片）
    boolean show = false;         //异步逐渐显示类中设为true，recycle时设为false
    boolean isLoading = false;        //加载中
    private Bitmap bitmap;          //图片（有可能被回收）

    public Bitmap getBitmap() {
        return bitmap;
    }

    //传个东西进去，修改位图占用的总大小
    public void setBitmap(Bitmap bitmap) {
        int origin_bytes = 0;
        int input_bytes = 0;

        //获取先后的位图大小（在内存中的占用大小）
        if (this.bitmap != null)
            origin_bytes = this.bitmap.getByteCount();
        if (bitmap != null)
            input_bytes = bitmap.getByteCount();

        //修改位图占内存的变化
        int memory_size_changed = input_bytes - origin_bytes;

        //通知观察者
        super.setChanged();
        super.notifyObservers(memory_size_changed);

        this.bitmap = bitmap;
    }
}