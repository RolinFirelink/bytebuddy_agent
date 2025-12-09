package org.example.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

public class AgentMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("========================================");
        System.out.println("ByteBuddy Agent 已启动！");
        System.out.println("Agent 参数: " + (agentArgs != null ? agentArgs : "无"));
        System.out.println("Java 运行时版本: " + System.getProperty("java.version"));
        System.out.println("Java 运行时架构: " + System.getProperty("os.arch"));
        System.out.println("Java 运行时操作系统: " + System.getProperty("os.name"));
        System.out.println("Java 运行时路径: " + System.getProperty("java.home"));
        System.out.println("目前该方法仅为测试存在,若看见本条日志则说明该示例已成功");
        System.out.println("========================================");

        new AgentBuilder.Default()
                // 匹配所有 Controller 类，且必须在指定包路径下
                .type(ElementMatchers.isAnnotatedWith(
                        ElementMatchers.named("org.springframework.web.bind.annotation.RestController")
                                .or(ElementMatchers.named("org.springframework.stereotype.Controller"))
                ).and(ElementMatchers.nameStartsWith("com.rolin.orangesmart.controller.fish")))
                // 匹配所有 public 方法
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(ElementMatchers.isPublic()
                                        .and(ElementMatchers.not(ElementMatchers.isStatic()))
                                        .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                                )
                                .intercept(MethodDelegation.to(org.example.agent.interceptor.ControllerInterceptor.class))
                )
                .installOn(inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }
}
