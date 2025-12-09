package org.example.agent.interceptor;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.AllArguments;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 包路径匹配拦截器示例
 * 演示如何拦截特定包路径下的方法
 */
public class PackageMatcherInterceptor {

    @RuntimeType
    public static Object intercept(
            @Origin Method method,
            @AllArguments Object[] args,
            @SuperCall Callable<?> callable) throws Exception {

        long startTime = System.currentTimeMillis();
        String className = method.getDeclaringClass().getName();
        String packageName = method.getDeclaringClass().getPackage() != null 
                ? method.getDeclaringClass().getPackage().getName() 
                : "default";
        String methodName = method.getName();

        System.out.println("\n[PackageMatcher] ========================================");
        System.out.println("[PackageMatcher] 包路径: " + packageName);
        System.out.println("[PackageMatcher] 完整类名: " + className);
        System.out.println("[PackageMatcher] 方法名: " + methodName);
        System.out.println("[PackageMatcher] 方法签名: " + method.toGenericString());

        if (args != null && args.length > 0) {
            System.out.print("[PackageMatcher] 参数: ");
            for (int i = 0; i < args.length; i++) {
                System.out.print("[" + method.getParameterTypes()[i].getSimpleName() + "] " + 
                        (args[i] != null ? args[i].toString() : "null"));
                if (i < args.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }

        Object result = null;
        try {
            result = callable.call();
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("[PackageMatcher] 执行成功，耗时: " + duration + "ms");
            
            if (result != null) {
                String resultStr = result.toString();
                if (resultStr.length() > 100) {
                    resultStr = resultStr.substring(0, 100) + "...";
                }
                System.out.println("[PackageMatcher] 返回值类型: " + method.getReturnType().getSimpleName());
                System.out.println("[PackageMatcher] 返回值: " + resultStr);
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("[PackageMatcher] 执行异常，耗时: " + duration + "ms");
            System.out.println("[PackageMatcher] 异常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            throw e;
        } finally {
            System.out.println("[PackageMatcher] ========================================\n");
        }

        return result;
    }
}

