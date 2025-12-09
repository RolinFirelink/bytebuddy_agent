package org.example.agent.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * 自定义匹配器工具类
 * 提供各种常用的匹配器组合
 */
public class CustomMatchers {

    /**
     * 匹配指定包路径下的所有类
     * @param packagePrefix 包路径前缀，如 "org.example.service"
     * @return 匹配器
     */
    public static ElementMatcher.Junction<TypeDescription> packageStartsWith(String packagePrefix) {
        return nameStartsWith(packagePrefix);
    }

    /**
     * 匹配指定包路径下的所有类（使用正则表达式）
     * @param packagePattern 包路径正则表达式，如 ".*\\.service\\..*"
     * @return 匹配器
     */
    public static ElementMatcher.Junction<TypeDescription> packageMatches(String packagePattern) {
        return nameMatches(packagePattern);
    }

    /**
     * 匹配类名以指定后缀结尾的类
     * @param suffix 后缀，如 "Service", "Controller"
     * @return 匹配器
     */
    public static ElementMatcher.Junction<TypeDescription> classNameEndsWith(String suffix) {
        return nameEndsWith(suffix);
    }

    /**
     * 匹配类名包含指定字符串的类
     * @param contains 包含的字符串
     * @return 匹配器
     */
    public static ElementMatcher.Junction<TypeDescription> classNameContains(String contains) {
        return nameContains(contains);
    }

    /**
     * 组合匹配器：匹配指定包路径下且类名以指定后缀结尾的类
     * @param packagePrefix 包路径前缀
     * @param classNameSuffix 类名后缀
     * @return 匹配器
     */
    public static ElementMatcher.Junction<TypeDescription> packageAndClass(
            String packagePrefix, String classNameSuffix) {
        return nameStartsWith(packagePrefix).and(nameEndsWith(classNameSuffix));
    }

    /**
     * 匹配方法名以指定前缀开头的方法
     * @param prefix 前缀，如 "get", "set", "find"
     * @return 匹配器
     */
    public static ElementMatcher.Junction<net.bytebuddy.description.method.MethodDescription> methodNameStartsWith(String prefix) {
        return net.bytebuddy.matcher.ElementMatchers.nameStartsWith(prefix);
    }

    /**
     * 匹配方法名以指定后缀结尾的方法
     * @param suffix 后缀，如 "ById", "ByName"
     * @return 匹配器
     */
    public static ElementMatcher.Junction<net.bytebuddy.description.method.MethodDescription> methodNameEndsWith(String suffix) {
        return net.bytebuddy.matcher.ElementMatchers.nameEndsWith(suffix);
    }

    /**
     * 匹配方法名匹配正则表达式的方法
     * @param pattern 正则表达式
     * @return 匹配器
     */
    public static ElementMatcher.Junction<net.bytebuddy.description.method.MethodDescription> methodNameMatches(String pattern) {
        return net.bytebuddy.matcher.ElementMatchers.nameMatches(pattern);
    }

    /**
     * 匹配返回指定类型的方法
     * @param returnType 返回类型
     * @return 匹配器
     */
    public static ElementMatcher.Junction<net.bytebuddy.description.method.MethodDescription> returnsType(Class<?> returnType) {
        return net.bytebuddy.matcher.ElementMatchers.returns(returnType);
    }

    /**
     * 匹配接受指定参数类型的方法
     * @param parameterTypes 参数类型数组
     * @return 匹配器
     */
    @SafeVarargs
    public static ElementMatcher.Junction<net.bytebuddy.description.method.MethodDescription> takesArguments(Class<?>... parameterTypes) {
        return net.bytebuddy.matcher.ElementMatchers.takesArguments(parameterTypes);
    }

    /**
     * 排除系统类和第三方库类（性能优化）
     * @return 匹配器
     */
    public static ElementMatcher.Junction<TypeDescription> excludeSystemClasses() {
        return not(nameStartsWith("java.")
                .or(nameStartsWith("javax."))
                .or(nameStartsWith("sun."))
                .or(nameStartsWith("com.sun."))
                .or(nameStartsWith("jdk."))
                .or(nameStartsWith("org.apache."))
                .or(nameStartsWith("org.springframework."))
                .or(nameStartsWith("net.bytebuddy.")));
    }
}

