package org.example.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.MethodDelegation;
import org.example.agent.interceptor.*;
import org.example.agent.listener.ClassLoadListener;
import org.example.agent.matcher.CustomMatchers;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * ByteBuddy Agent 主入口
 * 支持多种拦截方式和匹配器
 * 
 * 可以通过 agentArgs 参数控制使用哪种拦截方式：
 * - 默认：使用 ControllerInterceptor（注解匹配）
 * - package: 使用包路径匹配
 * - advice: 使用 Advice 方式拦截
 * - advanced: 使用高级拦截器（捕获更多信息）
 * - all: 启用所有拦截方式
 */
public class AgentMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("========================================");
        System.out.println("ByteBuddy Agent 已启动！");
        System.out.println("Agent 参数: " + (agentArgs != null ? agentArgs : "无（使用默认配置）"));
        System.out.println("Java 运行时版本: " + System.getProperty("java.version"));
        System.out.println("Java 运行时架构: " + System.getProperty("os.arch"));
        System.out.println("Java 运行时操作系统: " + System.getProperty("os.name"));
        System.out.println("========================================");
        System.out.println("支持的参数：");
        System.out.println("  - 默认（无参数）: 拦截 @RestController/@Controller 注解的类");
        System.out.println("  - package: 拦截 org.example.service 包下的类");
        System.out.println("  - advice: 使用 Advice 方式拦截（性能更好）");
        System.out.println("  - advanced: 使用高级拦截器（捕获更多信息）");
        System.out.println("  - all: 启用所有拦截方式");
        System.out.println("========================================");

        AgentBuilder agentBuilder = new AgentBuilder.Default()
                // 忽略系统类和 ByteBuddy 自身，提升性能
                .ignore(CustomMatchers.excludeSystemClasses())
                // 添加监听器，监听类加载和转换事件
                .with(new ClassLoadListener());

        String mode = agentArgs != null ? agentArgs.toLowerCase() : "default";

        switch (mode) {
            case "package":
                setupPackageMatcher(agentBuilder);
                break;
            case "advice":
                setupAdviceInterceptor(agentBuilder);
                break;
            case "advanced":
                setupAdvancedInterceptor(agentBuilder);
                break;
            case "all":
                setupAllInterceptors(agentBuilder);
                break;
            default:
                setupDefaultInterceptor(agentBuilder);
                break;
        }

        agentBuilder.installOn(inst);
        System.out.println("Agent 安装完成！");
    }

    /**
     * 默认拦截器：拦截 Spring Controller 注解的类
     */
    private static void setupDefaultInterceptor(AgentBuilder agentBuilder) {
        System.out.println("[配置] 使用默认拦截器：拦截 @RestController/@Controller 注解的类");
        agentBuilder
                .type(isAnnotatedWith(
                        named("org.springframework.web.bind.annotation.RestController")
                                .or(named("org.springframework.stereotype.Controller"))
                ))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(isPublic()
                                        .and(not(isStatic()))
                                        .and(not(isConstructor()))
                                )
                                .intercept(MethodDelegation.to(ControllerInterceptor.class))
                );
    }

    /**
     * 包路径匹配拦截器：拦截指定包下的类
     */
    private static void setupPackageMatcher(AgentBuilder agentBuilder) {
        System.out.println("[配置] 使用包路径匹配拦截器：拦截 org.example.service 包下的类");
        agentBuilder
                .type(CustomMatchers.packageStartsWith("org.example.service"))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(isPublic()
                                        .and(not(isStatic()))
                                        .and(not(isConstructor()))
                                )
                                .intercept(MethodDelegation.to(PackageMatcherInterceptor.class))
                );
    }

    /**
     * Advice 方式拦截器：性能更好的拦截方式
     */
    private static void setupAdviceInterceptor(AgentBuilder agentBuilder) {
        System.out.println("[配置] 使用 Advice 拦截器：拦截 org.example.service 包下的类");
        agentBuilder
                .type(CustomMatchers.packageStartsWith("org.example.service"))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(AdviceInterceptor.class)
                                .on(isPublic()
                                        .and(not(isStatic()))
                                        .and(not(isConstructor()))
                                ))
                );
    }

    /**
     * 高级拦截器：捕获更多信息（线程、调用栈等）
     */
    private static void setupAdvancedInterceptor(AgentBuilder agentBuilder) {
        System.out.println("[配置] 使用高级拦截器：拦截 org.example.service 包下的类");
        agentBuilder
                .type(CustomMatchers.packageStartsWith("org.example.service"))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(isPublic()
                                        .and(not(isStatic()))
                                        .and(not(isConstructor()))
                                )
                                .intercept(MethodDelegation.to(AdvancedInterceptor.class))
                );
    }

    /**
     * 启用所有拦截方式（用于演示）
     */
    private static void setupAllInterceptors(AgentBuilder agentBuilder) {
        System.out.println("[配置] 启用所有拦截方式");
        
        // 1. 默认拦截器：拦截 Controller
        agentBuilder
                .type(isAnnotatedWith(
                        named("org.springframework.web.bind.annotation.RestController")
                                .or(named("org.springframework.stereotype.Controller"))
                ))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(isPublic()
                                        .and(not(isStatic()))
                                        .and(not(isConstructor()))
                                )
                                .intercept(MethodDelegation.to(ControllerInterceptor.class))
                );

        // 2. 包路径匹配：拦截 service 包
        agentBuilder
                .type(CustomMatchers.packageStartsWith("org.example.service"))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(isPublic()
                                        .and(not(isStatic()))
                                        .and(not(isConstructor()))
                                )
                                .intercept(MethodDelegation.to(PackageMatcherInterceptor.class))
                );

        // 3. 高级拦截器：拦截 dao 包（如果存在）
        agentBuilder
                .type(CustomMatchers.packageStartsWith("org.example.dao"))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(isPublic()
                                        .and(not(isStatic()))
                                        .and(not(isConstructor()))
                                )
                                .intercept(MethodDelegation.to(AdvancedInterceptor.class))
                );
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }
}
