package ecust.news;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.List;

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
public class struct_NewsContent {
    String title = "";           //新闻标题

    String release_time = "";    //发表日期
    String news_Source = "";      //稿件来源、来稿单位
    String author = "";             //作者
    String photo_author = "";        //摄影
    String editor = "";              //编辑
    String count_of_visit = "";      //访问量

    String body = "";               //新闻文字部分（这个也是临时的，解析完后就可以回收）

    List<String> pic_url;       //图片地址（临时性的存储下，只是在解析时用到，其他时候无用）
    List<String> content;          //文字-图片URL交替部分（一行文字、一行图片交替）（重要数据在这里）

    HashMap<String, PicHolder> bitmapHashMap=new HashMap<>();       //在内存中暂存Bitmap（内存不够就及时回收）
}

class PicHolder {
    Bitmap bitmap;          //图片
    pic_state state=pic_state.none;        //图片的状态
    ImageView imageView;        //imageView（始终保持）

    enum pic_state {
        inMemory,       //内存中已存在，已存储进文件夹
        none,           //内存中没有，文件夹中也未存储过
        existInFile,    //内存中没有，但文件夹中肯定有
        isLoading       //加载中
    }
}