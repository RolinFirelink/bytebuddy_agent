package org.example.agent.interceptor;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.AllArguments;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 高级拦截器示例
 * 捕获更多信息：线程信息、调用栈、类加载器、方法签名等
 */
public class AdvancedInterceptor {

    @RuntimeType
    public static Object intercept(
            @Origin Method method,
            @AllArguments Object[] args,
            @SuperCall Callable<?> callable) throws Exception {

        long startTime = System.currentTimeMillis();
        Thread currentThread = Thread.currentThread();
        
        // 获取类信息
        Class<?> clazz = method.getDeclaringClass();
        String className = clazz.getName();
        String simpleClassName = clazz.getSimpleName();
        ClassLoader classLoader = clazz.getClassLoader();
        
        // 获取线程信息
        String threadName = currentThread.getName();
        long threadId = Thread.currentThread().threadId(); // 使用新的API替代已废弃的getId()
        Thread.State threadState = currentThread.getState();
        
        // 获取调用栈信息（限制深度避免过多输出）
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int stackDepth = Math.min(stackTrace.length, 5); // 只取前5层
        
        System.out.println("\n[Advanced] ========================================");
        System.out.println("[Advanced] ========== 类信息 ==========");
        System.out.println("[Advanced] 完整类名: " + className);
        System.out.println("[Advanced] 简单类名: " + simpleClassName);
        System.out.println("[Advanced] 包名: " + (clazz.getPackage() != null ? clazz.getPackage().getName() : "default"));
        System.out.println("[Advanced] 类加载器: " + (classLoader != null ? classLoader.getClass().getName() : "Bootstrap ClassLoader"));
        
        System.out.println("[Advanced] ========== 方法信息 ==========");
        System.out.println("[Advanced] 方法名: " + method.getName());
        System.out.println("[Advanced] 完整方法签名: " + method.toGenericString());
        System.out.println("[Advanced] 返回类型: " + method.getReturnType().getName());
        System.out.println("[Advanced] 参数数量: " + method.getParameterCount());
        
        if (method.getParameterTypes().length > 0) {
            System.out.print("[Advanced] 参数类型: ");
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                System.out.print(method.getParameterTypes()[i].getName());
                if (i < method.getParameterTypes().length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
        
        System.out.println("[Advanced] ========== 线程信息 ==========");
        System.out.println("[Advanced] 线程ID: " + threadId);
        System.out.println("[Advanced] 线程名: " + threadName);
        System.out.println("[Advanced] 线程状态: " + threadState);
        System.out.println("[Advanced] 线程优先级: " + currentThread.getPriority());
        System.out.println("[Advanced] 是否守护线程: " + currentThread.isDaemon());
        
        System.out.println("[Advanced] ========== 调用栈信息 ==========");
        for (int i = 0; i < stackDepth; i++) {
            StackTraceElement element = stackTrace[i];
            System.out.println("[Advanced]   [" + i + "] " + element.getClassName() 
                    + "." + element.getMethodName() 
                    + "(" + element.getFileName() + ":" + element.getLineNumber() + ")");
        }
        if (stackTrace.length > stackDepth) {
            System.out.println("[Advanced]   ... (还有 " + (stackTrace.length - stackDepth) + " 层)");
        }
        
        System.out.println("[Advanced] ========== 方法参数 ==========");
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                System.out.println("[Advanced]   arg[" + i + "] " + 
                        method.getParameterTypes()[i].getSimpleName() + " = " +
                        (arg != null ? arg.toString() : "null") +
                        (arg != null ? " (hashCode: " + arg.hashCode() + ")" : ""));
            }
        } else {
            System.out.println("[Advanced]   无参数");
        }
        
        System.out.println("[Advanced] ========== 执行过程 ==========");
        System.out.println("[Advanced] 开始时间: " + startTime);
        
        Object result = null;
        Exception caughtException = null;
        
        try {
            result = callable.call();
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("[Advanced] 执行成功");
            System.out.println("[Advanced] 执行耗时: " + duration + "ms");
            
            if (result != null) {
                System.out.println("[Advanced] 返回值类型: " + result.getClass().getName());
                String resultStr = result.toString();
                if (resultStr.length() > 200) {
                    resultStr = resultStr.substring(0, 200) + "... (已截断)";
                }
                System.out.println("[Advanced] 返回值: " + resultStr);
                System.out.println("[Advanced] 返回值hashCode: " + result.hashCode());
            } else {
                System.out.println("[Advanced] 返回值: null (void或返回null)");
            }
        } catch (Exception e) {
            caughtException = e;
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("[Advanced] 执行异常");
            System.out.println("[Advanced] 执行耗时: " + duration + "ms");
            System.out.println("[Advanced] 异常类型: " + e.getClass().getName());
            System.out.println("[Advanced] 异常信息: " + e.getMessage());
            System.out.println("[Advanced] 异常堆栈: ");
            StackTraceElement[] exceptionTrace = e.getStackTrace();
            int traceDepth = Math.min(exceptionTrace.length, 3);
            for (int i = 0; i < traceDepth; i++) {
                System.out.println("[Advanced]   " + exceptionTrace[i]);
            }
            if (exceptionTrace.length > traceDepth) {
                System.out.println("[Advanced]   ... (还有 " + (exceptionTrace.length - traceDepth) + " 层)");
            }
        } finally {
            System.out.println("[Advanced] ========================================\n");
        }
        
        if (caughtException != null) {
            throw caughtException;
        }
        
        return result;
    }
}

