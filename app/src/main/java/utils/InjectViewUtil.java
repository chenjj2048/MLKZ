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
 * Created by 彩笔怪盗基德 on 2015/10/6
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package utils;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import utils.logUtils.logUtil;

/**
 * 动态加载布局
 */
public class InjectViewUtil {

    /**
     * findViewById
     *
     * @param obj        当前对象
     * @param parentView 父视图
     */
    public static void inject(Object obj, View parentView) {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            InjectView annotation = field.getAnnotation(InjectView.class);
            if (annotation == null) continue;
            //找到加载的对象
            View view = parentView.findViewById(annotation.value());

            try {
                field.setAccessible(true);
                field.set(obj, view);
            } catch (Exception e) {
                logUtil.printExceptionLog(InjectViewUtil.class, e);
            }
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface InjectView {
        int value();
    }
}
