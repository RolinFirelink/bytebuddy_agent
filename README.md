# ByteBuddy Agent 示例项目

**建议使用 JDK 21**

本项目演示 ByteBuddy Agent 的核心功能，包括 MethodDelegation、Advice、Listener 等常用模式。

## 快速开始

### 构建
```bash
mvn clean package
```

### 使用
```bash
java -javaagent:target/bytebuddy_agent-1.0-SNAPSHOT.jar=heartbeatInterval=30&heartbeatUrl=http://monitor:8080/heartbeat your-app.jar
```

## 示例说明

### AgentMain
使用 `MethodDelegation` + `ControllerInterceptor`，基础拦截示例。

**核心 API：**
- `AgentBuilder.Default()` - 构建 Agent
- `.type(ElementMatcher)` - 类型匹配
- `.method(ElementMatcher)` - 方法匹配
- `.intercept(MethodDelegation.to(Class))` - 方法拦截
- `.installOn(Instrumentation)` - 安装到 JVM

**拦截器注解：**
- `@RuntimeType` - 运行时类型处理
- `@Origin` - 原始方法
- `@AllArguments` - 所有参数
- `@SuperCall` - 调用原方法

### AgentMain2
使用 `MethodDelegation` + `AdvancedInterceptor`，捕获线程、调用栈、类加载器等详细信息。

### AgentMain3
使用 `Advice` 方式，性能优于 MethodDelegation，但类型处理更严格。

**核心 API：**
- `Advice.to(Class)` - Advice 拦截器
- `.visit(Advice)` - 应用 Advice
- `@Advice.OnMethodEnter` - 方法进入
- `@Advice.OnMethodExit` - 方法退出
- `@Advice.Origin` - 方法信息
- `@Advice.Enter` - 注入进入时返回值

**注意：** Advice 对返回值类型敏感，建议使用 `@Advice.Thrown` 处理异常，避免读取返回值。

### AgentMain4
演示 `Listener` 使用，监听类加载、转换、错误等事件。

**核心 API：**
- `.with(AgentBuilder.Listener)` - 添加监听器
- `Listener.onDiscovery()` - 类发现
- `Listener.onTransformation()` - 类转换
- `Listener.onError()` - 转换错误

## ElementMatcher 常用方法

**类型匹配：**
- `ElementMatchers.named(String)` - 精确匹配类名
- `ElementMatchers.nameStartsWith(String)` - 包路径前缀
- `ElementMatchers.isAnnotatedWith(ElementMatcher)` - 注解匹配
- `.and()` / `.or()` / `.not()` - 逻辑组合

**方法匹配：**
- `ElementMatchers.isPublic()` - public 方法
- `ElementMatchers.isStatic()` - 静态方法
- `ElementMatchers.isConstructor()` - 构造方法
- `ElementMatchers.returns(Class)` - 返回类型
- `ElementMatchers.takesArguments(Class...)` - 参数类型

## 拦截方式对比

| 方式 | 性能 | 灵活性 | 适用场景 |
|------|------|--------|----------|
| MethodDelegation | 较低 | 高 | 需要复杂逻辑、通用处理 |
| Advice | 高 | 较低 | 简单拦截、性能敏感 |

## 官方文档

- [ByteBuddy 官方文档](https://bytebuddy.net/)
- [ByteBuddy GitHub](https://github.com/raphw/byte-buddy)
- [Java Agent 规范](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html)

