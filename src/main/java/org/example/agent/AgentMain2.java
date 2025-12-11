package org.example.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 使用高级拦截器
 */
public class AgentMain2 {

    private static ScheduledExecutorService heartbeatExecutor;

    //  premain 方法 会在探针被启动时就被调用
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
                // 匹配所有 Controller 类，但排除指定包路径。
                // todo 要注意在你的使用的项目,下面的匹配规则要做适配修改
                .type(ElementMatchers.isAnnotatedWith(
                        ElementMatchers.named("org.springframework.web.bind.annotation.RestController")
                                .or(ElementMatchers.named("org.springframework.stereotype.Controller"))
                ).and(ElementMatchers.not(ElementMatchers.nameStartsWith("com.rolin.orangesmart.controller.fish"))))
                // 匹配所有 public 方法
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.method(ElementMatchers.isPublic()
                                        .and(ElementMatchers.not(ElementMatchers.isStatic()))
                                        .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                                )
                                // 使用 MethodDelegation 将拦截到的方法调用委托给指定的类,该类会拦截指定的方法并在方法前后执行代码
                                .intercept(MethodDelegation.to(org.example.agent.interceptor.AdvancedInterceptor.class))
                )
                .installOn(inst);

        // 启动探针心跳机制，默认每30秒发送一次心跳
        startHeartbeat(agentArgs);
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
