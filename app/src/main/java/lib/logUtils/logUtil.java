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
 * Created by 彩笔怪盗基德 on 2015/10/5
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package lib.logUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * 每次用，必须new出来
 * 一个函数体内用一个新的类
 * 保证每个类都能控制各自日志的输出
 */
public final class logUtil extends abstract_LogUtil {
    //tagName
    private String tagName;
    //决定这一层是否启用Log
    private boolean logEnable = true;

    /**
     * @param this_obj 传个this进来就行
     */
    public logUtil(Object this_obj) {
        if (!isDebug) return;

        //函数调用深度
        final int depth = 3;

        //获取调用的类、函数
        StackTraceElement stacktrace = Thread.currentThread().getStackTrace()[depth];
        String className = stacktrace.getClassName();
        String methodName = stacktrace.getMethodName();

        //默认结果
        String aliasName = "";
        tagStateEnum tagState = tagStateEnum.showMethodAndAlias;

        //遍历函数获得注解(不允许用在具有重名的函数上)
        Method[] methods = this_obj.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (!method.getName().equals(methodName)) continue;

            //注解1：日志关闭
            boolean logOFF = method.isAnnotationPresent(LogOFF.class);
            if (logOFF) {
                this.logEnable = false;
                return;
            }

            //注解2：Log状态
            LogStatus annotation = method.getAnnotation(LogStatus.class);
            if (annotation != null) {
                aliasName = annotation.aliasName();
                if (aliasName == null)
                    aliasName = "";

                tagState = annotation.tagState();
            }
            break;
        }

        //设置标签名称
        if (aliasName.equals(""))
            tagState = tagStateEnum.showClassAndMethod;

        switch (tagState) {
            case showMethodAndAlias:
                this.tagName = methodName + " - " + aliasName;
                break;
            case showClassAndMethod:
                this.tagName = className + " - " + methodName;
                break;
            case showClassAndAlias:
                this.tagName = className + " - " + aliasName;
                break;
            case showAll:
                this.tagName = className + " - " + methodName + " - " + aliasName;
                break;
            case showOnlyAliasName:
                this.tagName = aliasName;
                break;
            case showOnlyMethod:
                this.tagName = methodName;
                break;
            case showOnlyClass:
                this.tagName = className;
                break;
            default:
                this.tagName = "错误的标签";
        }
    }

    /**
     * 断言
     * android studio里assert不知道怎么就不起作用，自己写个输出日志
     */
    public void Assert(boolean condition, String errorMessage) {
        if (logEnable && !condition) {
            abstract_LogUtil.w(tagName, errorMessage);
        }
    }

    @SuppressWarnings("deprecation")
    public void v(String str) {
        if (!logEnable) return;
        abstract_LogUtil.v(tagName, str);
    }

    @SuppressWarnings("deprecation")
    public void d(String str) {
        if (!logEnable) return;
        abstract_LogUtil.d(tagName, str);
    }

    @SuppressWarnings("deprecation")
    public void i(String str) {
        if (!logEnable) return;
        abstract_LogUtil.i(tagName, str);
    }

    @SuppressWarnings("deprecation")
    public void w(String str) {
        if (!logEnable) return;
        abstract_LogUtil.w(tagName, str);
    }

    @SuppressWarnings("deprecation")
    public void e(String str) {
        if (!logEnable) return;
        abstract_LogUtil.e(tagName, str);
    }

    public enum tagStateEnum {
        //只显示一处标签
        showOnlyClass, showOnlyMethod, showOnlyAliasName,
        //显示两处标签
        showClassAndMethod, showClassAndAlias, showMethodAndAlias,
        //显示三处标签
        showAll
    }

    /**
     * 日志TAG状态
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LogStatus {
        //Log别名标签(一旦这个属性不为空，另两个属性就作废)
        String aliasName();

        //TAG显示的状态
        tagStateEnum tagState() default tagStateEnum.showMethodAndAlias;
    }

    /**
     * 日志开关
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LogOFF {
    }
}
