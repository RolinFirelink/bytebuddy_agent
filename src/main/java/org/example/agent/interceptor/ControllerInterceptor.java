package org.example.agent.interceptor;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.AllArguments;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * MethodDelegation普通拦截器示例
 * 会在方法运行时拦截并记录方法调用信息
 * 包括方法名、参数、执行时间、返回值等详细信息
 */
public class ControllerInterceptor {

    @RuntimeType
    public static Object intercept(
            @Origin Method method,
            @AllArguments Object[] args,
            @SuperCall Callable<?> callable) throws Exception {

        long startTime = System.currentTimeMillis();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        System.out.println("\n[Agent] ========================================");
        System.out.println("[Agent] 当前时间: " + System.currentTimeMillis());
        System.out.println("[Agent] 当前类: " + className);
        System.out.println("[Agent] 当前方法: " + methodName);
        System.out.println("[Agent] 当前方法参数: " + method.getParameterCount());
        System.out.println("[Agent] 当前方法参数类型: " + method.getParameterTypes().length);
        System.out.println("[Agent] 当前方法参数值: " + (args != null ? args.length : 0));
        System.out.println("[Agent] 调用方法: " + className + "." + methodName);
        System.out.println("[Agent] 参数数量: " + (args != null ? args.length : 0));

        if (args != null && args.length > 0) {
            System.out.print("[Agent] 参数值: ");
            for (int i = 0; i < args.length; i++) {
                System.out.print("arg" + i + "=" +
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
            System.out.println("[Agent] 执行成功，耗时: " + duration + "ms");

            if (result != null) {
                String resultStr = result.toString();
                if (resultStr.length() > 200) {
                    resultStr = resultStr.substring(0, 200) + "...";
                }
                System.out.println("[Agent] 返回值: " + resultStr);
            } else {
                System.out.println("[Agent] 返回值: null");
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("[Agent] 执行异常，耗时: " + duration + "ms");
            System.out.println("[Agent] 异常类型: " + e.getClass().getName());
            System.out.println("[Agent] 异常信息: " + e.getMessage());
            throw e;
        } finally {
            System.out.println("[Agent] 当你看到这条信息，说明执行已经成功了");
            System.out.println("[Agent] ========================================\n");
        }

        return result;
    }
}
