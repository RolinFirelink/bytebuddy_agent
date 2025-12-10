package org.example.agent.interceptor;

import net.bytebuddy.asm.Advice;

/**
 * Advice方式拦截器示例
 * Advice是ByteBuddy中性能更好的拦截方式，比MethodDelegation更轻量
 * 
 * 注意：Advice使用静态方法，方法名可以是任意的
 * Advice很难做到对返回值的通用处理,但是他的性能更高,而MethodDelegation则可以做到通用处理,但相应的他的效率更低
 */
public class AdviceInterceptor {

    /**
     * 方法进入时调用
     * @param methodName 方法名（通过Advice注入）
     * @param args 方法参数
     * @return 进入时间，用于计算耗时
     */
    @Advice.OnMethodEnter
    public static long onEnter(
            @Advice.Origin("#m") String methodName,
            @Advice.AllArguments Object[] args) {
        System.out.println("\n[Advice] ========================================");
        System.out.println("[Advice] 方法进入: " + methodName);
        long enterTime = System.currentTimeMillis();
        System.out.println("[Advice] 进入时间: " + enterTime);
        
        if (args != null && args.length > 0) {
            System.out.print("[Advice] 参数值: ");
            for (int i = 0; i < args.length; i++) {
                System.out.print("arg" + i + "=" + (args[i] != null ? args[i].toString() : "null"));
                if (i < args.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
        
        return enterTime;
    }

    /**
     * 方法退出时调用（正常返回或异常都会调用）
     * 注意：不使用 @Advice.Return 避免类型转换问题（如泛型、void、byte[]等）
     * @param methodName 方法名
     * @param throwable 异常（如果有）
     * @param enterTime 进入时间（通过@Advice.Enter注入）
     */
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(
            @Advice.Origin("#m") String methodName,
            @Advice.Thrown Throwable throwable,
            @Advice.Enter long enterTime) {
        
        long duration = System.currentTimeMillis() - enterTime;
        
        if (throwable != null) {
            System.out.println("[Advice] 方法异常退出: " + methodName);
            System.out.println("[Advice] 异常类型: " + throwable.getClass().getSimpleName());
            System.out.println("[Advice] 异常信息: " + throwable.getMessage());
        } else {
            System.out.println("[Advice] 方法正常退出: " + methodName);
            // 不再读取返回值，避免类型转换问题
        }
        
        System.out.println("[Advice] 执行耗时: " + duration + "ms");
        System.out.println("[Advice] ========================================\n");
    }
}

