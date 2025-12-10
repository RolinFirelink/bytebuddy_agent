package org.example.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AgentMain {

    private static ScheduledExecutorService heartbeatExecutor;

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
                // 添加类型匹配监听器，用于调试和日志输出
                .with(AgentBuilder.Listener.StreamWriting.toSystemOut().withTransformationsOnly())
                // 添加错误处理：即使某个类增强失败，也不影响其他类
                .with(new AgentBuilder.Listener.Filtering(
                        ElementMatchers.any(),
                        new AgentBuilder.Listener() {
                            @Override
                            public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
                                // 发现类，不需要处理
                            }

                            @Override
                            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {
                                // 转换成功，不需要处理
                            }

                            @Override
                            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
                                // 类被忽略，不需要处理
                            }

                            @Override
                            public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
                                System.err.println("[Agent] 警告: 类 " + typeName + " 增强失败，跳过该类: " + throwable.getMessage());
                                // 不重新抛出异常，允许其他类继续增强
                            }

                            @Override
                            public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
                                // 完成，不需要处理
                            }
                        }))
                // 只匹配指定包下的 Controller 类，排除 fish 子包
                // 限制范围：只增强 com.rolin.orangesmart.controller 包下的类（排除 fish 子包）
                .type(ElementMatchers.nameStartsWith("com.rolin.orangesmart.controller")
                        .and(ElementMatchers.not(ElementMatchers.nameStartsWith("com.rolin.orangesmart.controller.fish")))
                        .and(ElementMatchers.isAnnotatedWith(
                                ElementMatchers.named("org.springframework.web.bind.annotation.RestController")
                                        .or(ElementMatchers.named("org.springframework.stereotype.Controller"))
                        )))
                // 添加调试信息：当类被匹配到时打印日志，并使用 Advice 进行增强
                .transform((builder, type, classLoader, module, protectionDomain) -> {
                    System.out.println("[Agent] ========== 匹配到类 ==========");
                    System.out.println("[Agent] 类名: " + type.getName());
                    System.out.println("[Agent] 类加载器: " + (classLoader != null ? classLoader.getClass().getName() : "null"));
                    System.out.println("[Agent] 开始增强该类的方法...");
                    
                    // 在类级别使用 visit 方式应用 Advice，这是推荐的方式
                    return builder.visit(Advice.to(org.example.agent.interceptor.AdviceInterceptor.class)
                            .on(ElementMatchers.isPublic()
                                    .and(ElementMatchers.not(ElementMatchers.isStatic()))
                                    .and(ElementMatchers.not(ElementMatchers.isConstructor()))));
                })
                .installOn(inst);
        
        System.out.println("[Agent] Agent 配置完成，等待类加载...");
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    private static void startHeartbeat(String agentArgs) {
        // 创建单线程调度器（守护线程）
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Agent-Heartbeat-Thread");
            t.setDaemon(true); // 设置为守护线程，避免阻止JVM退出
            return t;
        });

        // 解析心跳间隔（默认30秒）
        long interval = 30;
        String heartbeatUrl = null;

        if (agentArgs != null) {
            // 解析参数，例如: heartbeatInterval=10&heartbeatUrl=http://monitor:8080/heartbeat
            String[] pairs = agentArgs.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    if ("heartbeatInterval".equals(kv[0])) {
                        interval = Long.parseLong(kv[1]);
                    } else if ("heartbeatUrl".equals(kv[0])) {
                        heartbeatUrl = kv[1];
                    }
                }
            }
        }

        final String url = heartbeatUrl;
        final long finalInterval = interval;

        // 启动定时心跳任务
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                long timestamp = System.currentTimeMillis();
                String hostname = System.getProperty("user.name", "unknown");
                String javaVersion = System.getProperty("java.version");

                System.out.println("[Agent-Heartbeat] " + timestamp +
                        " | Host: " + hostname +
                        " | Java: " + javaVersion +
                        " | Status: ALIVE");

                // 如果配置了URL，发送HTTP心跳
                if (url != null && !url.isEmpty()) {
                    sendHeartbeat(url, timestamp, hostname, javaVersion);
                }
            } catch (Exception e) {
                // 心跳失败不影响主应用
                System.err.println("[Agent-Heartbeat] 心跳发送失败: " + e.getMessage());
            }
        }, 5, finalInterval, TimeUnit.SECONDS); // 延迟5秒启动，然后每interval秒执行一次

        System.out.println("[Agent] 心跳机制已启动，间隔: " + finalInterval + "秒");
    }

    private static void sendHeartbeat(String url, long timestamp, String hostname, String javaVersion) {
        try {
            // 使用简单的HTTP连接发送心跳
            java.net.URL heartbeatUrl = new java.net.URL(url);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) heartbeatUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setDoOutput(true);

            // 构建心跳数据
            String json = String.format(
                    "{\"timestamp\":%d,\"hostname\":\"%s\",\"javaVersion\":\"%s\",\"agent\":\"bytebuddy_agent\"}",
                    timestamp, hostname, javaVersion
            );

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                System.out.println("[Agent-Heartbeat] 心跳发送成功: " + responseCode);
            } else {
                System.out.println("[Agent-Heartbeat] 心跳发送失败: " + responseCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("发送心跳失败", e);
        }
    }
}
