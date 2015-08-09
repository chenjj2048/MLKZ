package ecust.lecture;

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
 * Created by 彩笔怪盗基德 on 2015/7/26
 * Copyright (C) 2015 彩笔怪盗基德
 */

//讲座版块目录结构
public class struct_LectureCatalogItem implements Comparable<struct_LectureCatalogItem> {
    String title;          //讲座标题
    String time;           //讲座时间
    String url;            //讲座链接
    String deltaTime;      //几天前、几天后、已过期等（不保存在数据库，仅在getView时载入，避免重复）

    @Override
    public int compareTo(struct_LectureCatalogItem para) {
        return para.time.compareTo(this.time);
    }

    //url一致的话，肯定就是一样了
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        struct_LectureCatalogItem that = (struct_LectureCatalogItem) o;

        return !(url != null ? !url.equals(that.url) : that.url != null);
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }
}
