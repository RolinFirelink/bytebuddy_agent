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
 * 可以通过 agentArgs 参数控制使用哪种拦截方式和包路径：
 * 参数格式：模式:包路径
 * 
 * 支持的模式：
 * - 默认（无参数）: 拦截 @RestController/@Controller 注解的类
 * - package:包路径 - 使用包路径匹配拦截器，例如: package:com.example.service
 * - advice:包路径 - 使用 Advice 方式拦截，例如: advice:com.example.service
 * - advanced:包路径 - 使用高级拦截器，例如: advanced:com.example.service
 * - all:包路径1,包路径2 - 启用所有拦截方式，支持多个包（逗号分隔），例如: all:com.example.service,com.example.dao
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
        System.out.println("支持的参数格式：");
        System.out.println("  格式: 模式:包路径");
        System.out.println("  - 默认（无参数）: 拦截 @RestController/@Controller 注解的类");
        System.out.println("  - package:包路径 - 包路径匹配拦截器，例如: package:com.example.service");
        System.out.println("  - advice:包路径 - Advice 方式拦截，例如: advice:com.example.service");
        System.out.println("  - advanced:包路径 - 高级拦截器，例如: advanced:com.example.service");
        System.out.println("  - all:包路径1,包路径2 - 所有拦截方式，例如: all:com.example.service,com.example.dao");
        System.out.println("========================================");

        AgentBuilder agentBuilder = new AgentBuilder.Default()
                // 忽略系统类和 ByteBuddy 自身，提升性能
                .ignore(CustomMatchers.excludeSystemClasses())
                // 添加监听器，监听类加载和转换事件
                .with(new ClassLoadListener());

        // 解析参数：支持 "模式:包路径" 格式
        String mode = "default";
        String[] packageNames = null;
        
        if (agentArgs != null && !agentArgs.isEmpty()) {
            String[] parts = agentArgs.split(":", 2);
            mode = parts[0].toLowerCase();
            if (parts.length > 1 && !parts[1].isEmpty()) {
                // 支持多个包路径，用逗号分隔
                packageNames = parts[1].split(",");
                for (int i = 0; i < packageNames.length; i++) {
                    packageNames[i] = packageNames[i].trim();
                }
            }
        }

        System.out.println("[配置] 拦截模式: " + mode);
        if (packageNames != null && packageNames.length > 0) {
            System.out.println("[配置] 拦截包路径: " + String.join(", ", packageNames));
        }

        switch (mode) {
            case "package":
                if (packageNames != null && packageNames.length > 0) {
                    setupPackageMatcher(agentBuilder, packageNames[0]);
                } else {
                    System.err.println("[错误] package 模式需要指定包路径，格式: package:包路径");
                    System.err.println("[错误] 示例: package:com.example.service");
                }
                break;
            case "advice":
                if (packageNames != null && packageNames.length > 0) {
                    setupAdviceInterceptor(agentBuilder, packageNames[0]);
                } else {
                    System.err.println("[错误] advice 模式需要指定包路径，格式: advice:包路径");
                    System.err.println("[错误] 示例: advice:com.example.service");
                }
                break;
            case "advanced":
                if (packageNames != null && packageNames.length > 0) {
                    setupAdvancedInterceptor(agentBuilder, packageNames[0]);
                } else {
                    System.err.println("[错误] advanced 模式需要指定包路径，格式: advanced:包路径");
                    System.err.println("[错误] 示例: advanced:com.example.service");
                }
                break;
            case "all":
                if (packageNames != null && packageNames.length > 0) {
                    setupAllInterceptors(agentBuilder, packageNames);
                } else {
                    System.err.println("[错误] all 模式需要指定包路径，格式: all:包路径1,包路径2");
                    System.err.println("[错误] 示例: all:com.example.service,com.example.dao");
                }
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
     * @param packageName 要拦截的包路径
     */
    private static void setupPackageMatcher(AgentBuilder agentBuilder, String packageName) {
        System.out.println("[配置] 使用包路径匹配拦截器：拦截 " + packageName + " 包下的类");
        agentBuilder
                .type(CustomMatchers.packageStartsWith(packageName))
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
     * @param packageName 要拦截的包路径
     */
    private static void setupAdviceInterceptor(AgentBuilder agentBuilder, String packageName) {
        System.out.println("[配置] 使用 Advice 拦截器：拦截 " + packageName + " 包下的类");
        agentBuilder
                .type(CustomMatchers.packageStartsWith(packageName))
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
     * @param packageName 要拦截的包路径
     */
    private static void setupAdvancedInterceptor(AgentBuilder agentBuilder, String packageName) {
        System.out.println("[配置] 使用高级拦截器：拦截 " + packageName + " 包下的类");
        agentBuilder
                .type(CustomMatchers.packageStartsWith(packageName))
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
     * @param packageNames 要拦截的包路径数组
     */
    private static void setupAllInterceptors(AgentBuilder agentBuilder, String[] packageNames) {
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

        // 2. 为每个指定的包路径设置拦截器
        if (packageNames != null && packageNames.length > 0) {
            for (int i = 0; i < packageNames.length; i++) {
                String packageName = packageNames[i];
                if (packageName != null && !packageName.isEmpty()) {
                    // 使用包路径匹配拦截器
                    agentBuilder
                            .type(CustomMatchers.packageStartsWith(packageName))
                            .transform((builder, type, classLoader, module, protectionDomain) ->
                                    builder.method(isPublic()
                                                    .and(not(isStatic()))
                                                    .and(not(isConstructor()))
                                            )
                                            .intercept(MethodDelegation.to(PackageMatcherInterceptor.class))
                            );
                    
                    // 如果包名包含 dao，使用高级拦截器
                    if (packageName.toLowerCase().contains("dao")) {
                        agentBuilder
                                .type(CustomMatchers.packageStartsWith(packageName))
                                .transform((builder, type, classLoader, module, protectionDomain) ->
                                        builder.method(isPublic()
                                                        .and(not(isStatic()))
                                                        .and(not(isConstructor()))
                                                )
                                                .intercept(MethodDelegation.to(AdvancedInterceptor.class))
                                );
                    }
                }
            }
        }
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }
}
