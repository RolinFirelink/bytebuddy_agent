# ByteBuddy Agent 功能扩展说明文档

## 📋 目录
1. [项目概述](#项目概述)
2. [新增功能列表](#新增功能列表)
3. [文件结构说明](#文件结构说明)
4. [功能详细说明](#功能详细说明)
5. [使用方法](#使用方法)
6. [测试指南](#测试指南)
7. [技术要点总结](#技术要点总结)

---

## 项目概述

本项目是一个基于 ByteBuddy 的 Java Agent 探针学习项目，用于学习和演示 ByteBuddy 的各种功能。本次扩展增加了多种拦截方式、匹配器、监听器等高级功能。

### 原始功能
- ✅ 使用 `MethodDelegation` 拦截方法
- ✅ 基于注解的类匹配（`@RestController`、`@Controller`）
- ✅ 捕获方法执行时间、参数、返回值、异常

### 新增功能
- ✅ 包路径匹配拦截器
- ✅ Advice 方式拦截器（性能更好）
- ✅ 高级拦截器（捕获更多信息）
- ✅ 自定义匹配器工具类
- ✅ 类加载监听器
- ✅ 多种拦截模式配置
- ✅ 测试类示例

---

## 新增功能列表

### 1. 拦截器（Interceptors）

#### 1.1 PackageMatcherInterceptor（包路径匹配拦截器）
- **文件位置**: `src/main/java/org/example/agent/interceptor/PackageMatcherInterceptor.java`
- **功能**: 拦截指定包路径下的所有类的方法
- **特点**: 
  - 显示完整的包路径和类名
  - 显示完整的方法签名
  - 显示参数类型和值

#### 1.2 AdviceInterceptor（Advice 方式拦截器）
- **文件位置**: `src/main/java/org/example/agent/interceptor/AdviceInterceptor.java`
- **功能**: 使用 ByteBuddy 的 Advice API 进行拦截
- **特点**:
  - 性能比 MethodDelegation 更好
  - 使用 `@Advice.OnMethodEnter` 和 `@Advice.OnMethodExit` 注解
  - 支持方法进入和退出时的处理

#### 1.3 AdvancedInterceptor（高级拦截器）
- **文件位置**: `src/main/java/org/example/agent/interceptor/AdvancedInterceptor.java`
- **功能**: 捕获更详细的方法执行信息
- **捕获的信息**:
  - 类信息（完整类名、包名、类加载器）
  - 方法信息（完整方法签名、参数类型、返回类型）
  - 线程信息（线程ID、线程名、线程状态、优先级、是否守护线程）
  - 调用栈信息（前5层调用栈）
  - 方法参数（类型、值、hashCode）
  - 返回值（类型、值、hashCode）
  - 异常信息（类型、消息、堆栈）

### 2. 匹配器（Matchers）

#### 2.1 CustomMatchers（自定义匹配器工具类）
- **文件位置**: `src/main/java/org/example/agent/matcher/CustomMatchers.java`
- **功能**: 提供各种常用的匹配器组合方法
- **提供的匹配器**:
  - `packageStartsWith(String)`: 匹配包路径前缀
  - `packageMatches(String)`: 使用正则表达式匹配包路径
  - `classNameEndsWith(String)`: 匹配类名后缀
  - `classNameContains(String)`: 匹配类名包含的字符串
  - `packageAndClass(String, String)`: 组合匹配包路径和类名
  - `methodNameStartsWith(String)`: 匹配方法名前缀
  - `methodNameEndsWith(String)`: 匹配方法名后缀
  - `methodNameMatches(String)`: 使用正则表达式匹配方法名
  - `returnsType(Class<?>)`: 匹配返回类型
  - `takesArguments(Class<?>...)`: 匹配参数类型
  - `excludeSystemClasses()`: 排除系统类（性能优化）

### 3. 监听器（Listener）

#### 3.1 ClassLoadListener（类加载监听器）
- **文件位置**: `src/main/java/org/example/agent/listener/ClassLoadListener.java`
- **功能**: 监听类的加载、转换、错误等事件
- **监听的事件**:
  - `onDiscovery`: 类被发现时
  - `onTransformation`: 类被转换时
  - `onIgnored`: 类被忽略时
  - `onError`: 发生错误时
  - `onComplete`: 处理完成时

### 4. AgentMain 扩展

#### 4.1 多模式支持
- **文件位置**: `src/main/java/org/example/agent/AgentMain.java`
- **功能**: 通过 agentArgs 参数控制使用哪种拦截方式
- **支持的模式**:
  - `default`: 默认模式，拦截 `@RestController`/`@Controller` 注解的类
  - `package`: 使用包路径匹配拦截器
  - `advice`: 使用 Advice 方式拦截器
  - `advanced`: 使用高级拦截器
  - `all`: 启用所有拦截方式

### 5. 测试类

#### 5.1 UserService
- **文件位置**: `src/main/java/org/example/service/UserService.java`
- **用途**: 演示包路径匹配拦截器

#### 5.2 OrderService
- **文件位置**: `src/main/java/org/example/service/OrderService.java`
- **用途**: 演示包路径匹配拦截器

#### 5.3 UserDao
- **文件位置**: `src/main/java/org/example/dao/UserDao.java`
- **用途**: 演示高级拦截器

#### 5.4 TestApplication
- **文件位置**: `src/main/java/org/example/TestApplication.java`
- **用途**: 主测试类，调用所有服务方法进行测试

---

## 文件结构说明

```
src/main/java/org/example/
├── agent/
│   ├── AgentMain.java                    # Agent 主入口（已扩展）
│   ├── interceptor/
│   │   ├── ControllerInterceptor.java    # 原有：Controller 拦截器
│   │   ├── PackageMatcherInterceptor.java # 新增：包路径匹配拦截器
│   │   ├── AdviceInterceptor.java        # 新增：Advice 方式拦截器
│   │   └── AdvancedInterceptor.java      # 新增：高级拦截器
│   ├── matcher/
│   │   └── CustomMatchers.java           # 新增：自定义匹配器工具类
│   └── listener/
│       └── ClassLoadListener.java        # 新增：类加载监听器
├── service/
│   ├── UserService.java                  # 新增：测试服务类
│   └── OrderService.java                 # 新增：测试服务类
├── dao/
│   └── UserDao.java                      # 新增：测试 DAO 类
├── Main.java                             # 原有：简单主类
└── TestApplication.java                  # 新增：测试应用程序
```

---

## 功能详细说明

### 1. 包路径匹配拦截器

**使用场景**: 拦截特定包路径下的所有类的方法

**示例代码**:
```java
.type(CustomMatchers.packageStartsWith("org.example.service"))
.transform((builder, type, classLoader, module, protectionDomain) ->
    builder.method(isPublic()
            .and(not(isStatic()))
            .and(not(isConstructor()))
    )
    .intercept(MethodDelegation.to(PackageMatcherInterceptor.class))
)
```

**输出示例**:
```
[PackageMatcher] ========================================
[PackageMatcher] 包路径: org.example.service
[PackageMatcher] 完整类名: org.example.service.UserService
[PackageMatcher] 方法名: getUserById
[PackageMatcher] 方法签名: public java.lang.String org.example.service.UserService.getUserById(java.lang.Long)
[PackageMatcher] 参数: [Long] 1
[PackageMatcher] 执行成功，耗时: 2ms
[PackageMatcher] 返回值类型: String
[PackageMatcher] 返回值: User-1
[PackageMatcher] ========================================
```

### 2. Advice 方式拦截器

**使用场景**: 需要更高性能的拦截方式

**特点**:
- 比 MethodDelegation 性能更好
- 使用字节码增强，而不是方法委托
- 支持方法进入和退出时的处理

**示例代码**:
```java
.type(CustomMatchers.packageStartsWith("org.example.service"))
.transform((builder, type, classLoader, module, protectionDomain) ->
    builder.visit(Advice.to(AdviceInterceptor.class)
        .on(isPublic()
            .and(not(isStatic()))
            .and(not(isConstructor()))
        ))
)
```

**输出示例**:
```
[Advice] ========================================
[Advice] 方法进入: getUserById
[Advice] 进入时间: 1234567890123
[Advice] 参数值: arg0=1
[Advice] 方法正常退出: getUserById
[Advice] 返回值: User-1
[Advice] 执行耗时: 1ms
[Advice] ========================================
```

### 3. 高级拦截器

**使用场景**: 需要捕获详细的执行信息，用于调试或监控

**捕获的信息**:
- 类信息：完整类名、包名、类加载器
- 方法信息：完整方法签名、参数类型、返回类型
- 线程信息：线程ID、线程名、线程状态、优先级、是否守护线程
- 调用栈信息：前5层调用栈
- 方法参数：类型、值、hashCode
- 返回值：类型、值、hashCode
- 异常信息：类型、消息、堆栈

**输出示例**:
```
[Advanced] ========================================
[Advanced] ========== 类信息 ==========
[Advanced] 完整类名: org.example.service.UserService
[Advanced] 简单类名: UserService
[Advanced] 包名: org.example.service
[Advanced] 类加载器: jdk.internal.loader.ClassLoaders$AppClassLoader
[Advanced] ========== 方法信息 ==========
[Advanced] 方法名: getUserById
[Advanced] 完整方法签名: public java.lang.String org.example.service.UserService.getUserById(java.lang.Long)
[Advanced] 返回类型: java.lang.String
[Advanced] 参数数量: 1
[Advanced] 参数类型: java.lang.Long
[Advanced] ========== 线程信息 ==========
[Advanced] 线程ID: 1
[Advanced] 线程名: main
[Advanced] 线程状态: RUNNABLE
[Advanced] 线程优先级: 5
[Advanced] 是否守护线程: false
[Advanced] ========== 调用栈信息 ==========
[Advanced]   [0] org.example.service.UserService.getUserById(UserService.java:8)
[Advanced]   [1] org.example.TestApplication.main(TestApplication.java:15)
...
[Advanced] ========== 方法参数 ==========
[Advanced]   arg[0] Long = 1 (hashCode: 1)
[Advanced] ========== 执行过程 ==========
[Advanced] 开始时间: 1234567890123
[Advanced] 执行成功
[Advanced] 执行耗时: 2ms
[Advanced] 返回值类型: java.lang.String
[Advanced] 返回值: User-1
[Advanced] 返回值hashCode: 123456789
[Advanced] ========================================
```

### 4. 自定义匹配器

**使用示例**:

```java
// 匹配包路径
.type(CustomMatchers.packageStartsWith("org.example.service"))

// 匹配类名后缀
.type(CustomMatchers.classNameEndsWith("Service"))

// 组合匹配
.type(CustomMatchers.packageAndClass("org.example", "Service"))

// 匹配方法名
.method(CustomMatchers.methodNameStartsWith("get"))

// 匹配返回类型
.method(CustomMatchers.returnsType(String.class))

// 匹配参数类型
.method(CustomMatchers.takesArguments(Long.class))

// 排除系统类
.ignore(CustomMatchers.excludeSystemClasses())
```

### 5. 类加载监听器

**功能**: 监听类的加载和转换过程

**输出示例**:
```
[Listener] 发现类: org.example.service.UserService (ClassLoader: jdk.internal.loader.ClassLoaders$AppClassLoader, 已加载: false)
[Listener] ✓ 转换类: org.example.service.UserService (已加载: false)
[Listener]   生成的字节码大小: 1234 bytes
[Listener] 完成处理: org.example.service.UserService
```

---

## 使用方法

### 1. 编译项目

```bash
mvn clean package
```

编译完成后，会在 `target` 目录下生成 `bytebuddy_agent-1.0-SNAPSHOT.jar`

### 2. 运行测试

#### 方式一：使用默认模式（拦截 Controller）

```bash
java -javaagent:target/bytebuddy_agent-1.0-SNAPSHOT.jar -cp target/classes org.example.TestApplication
```

#### 方式二：使用包路径匹配模式

```bash
java -javaagent:target/bytebuddy_agent-1.0-SNAPSHOT.jar=package -cp target/classes org.example.TestApplication
```

#### 方式三：使用 Advice 模式

```bash
java -javaagent:target/bytebuddy_agent-1.0-SNAPSHOT.jar=advice -cp target/classes org.example.TestApplication
```

#### 方式四：使用高级拦截器模式

```bash
java -javaagent:target/bytebuddy_agent-1.0-SNAPSHOT.jar=advanced -cp target/classes org.example.TestApplication
```

#### 方式五：启用所有拦截方式

```bash
java -javaagent:target/bytebuddy_agent-1.0-SNAPSHOT.jar=all -cp target/classes org.example.TestApplication
```

### 3. 参数说明

- **无参数（默认）**: 拦截 `@RestController`/`@Controller` 注解的类
- **package**: 拦截 `org.example.service` 包下的类
- **advice**: 使用 Advice 方式拦截 `org.example.service` 包下的类
- **advanced**: 使用高级拦截器拦截 `org.example.service` 包下的类
- **all**: 启用所有拦截方式（Controller、service包、dao包）

---

## 测试指南

### 测试步骤

1. **编译项目**
   ```bash
   mvn clean package
   ```

2. **运行测试（使用不同模式）**

   - 测试包路径匹配：
     ```bash
     java -javaagent:target/bytebuddy_agent-1.0-SNAPSHOT.jar=package -cp target/classes org.example.TestApplication
     ```
     预期：看到 `[PackageMatcher]` 开头的日志

   - 测试 Advice 方式：
     ```bash
     java -javaagent:target/bytebuddy_agent-1.0-SNAPSHOT.jar=advice -cp target/classes org.example.TestApplication
     ```
     预期：看到 `[Advice]` 开头的日志

   - 测试高级拦截器：
     ```bash
     java -javaagent:target/bytebuddy_agent-1.0-SNAPSHOT.jar=advanced -cp target/classes org.example.TestApplication
     ```
     预期：看到 `[Advanced]` 开头的详细日志，包括线程信息、调用栈等

   - 测试所有模式：
     ```bash
     java -javaagent:target/bytebuddy_agent-1.0-SNAPSHOT.jar=all -cp target/classes org.example.TestApplication
     ```
     预期：看到多种拦截器的日志

3. **观察输出**

   每种模式都会输出不同的日志格式，可以对比：
   - 日志详细程度
   - 性能影响（执行时间）
   - 捕获的信息类型

### 测试要点

1. **包路径匹配测试**
   - ✅ 验证 `org.example.service` 包下的类被拦截
   - ✅ 验证其他包下的类不被拦截

2. **Advice 性能测试**
   - ✅ 对比 Advice 和 MethodDelegation 的性能差异
   - ✅ 验证方法进入和退出时的处理

3. **高级拦截器测试**
   - ✅ 验证线程信息捕获
   - ✅ 验证调用栈信息捕获
   - ✅ 验证类加载器信息捕获

4. **监听器测试**
   - ✅ 验证类加载事件被监听
   - ✅ 验证转换事件被记录

---

## 技术要点总结

### 1. MethodDelegation vs Advice

| 特性 | MethodDelegation | Advice |
|------|------------------|--------|
| 性能 | 较慢（方法委托） | 较快（字节码增强） |
| 灵活性 | 高（可以访问所有方法信息） | 中（受限于 Advice API） |
| 使用场景 | 需要复杂逻辑处理 | 需要高性能拦截 |
| 代码示例 | `MethodDelegation.to(Interceptor.class)` | `Advice.to(AdviceClass.class)` |

### 2. 匹配器组合

ByteBuddy 的匹配器支持链式组合：

```java
// AND 组合
isPublic().and(not(isStatic())).and(not(isConstructor()))

// OR 组合
named("method1").or(named("method2"))

// NOT 组合
not(nameStartsWith("java."))
```

### 3. 性能优化建议

1. **忽略系统类**: 使用 `ignore()` 排除不需要处理的类
2. **使用 Advice**: 对于简单拦截，使用 Advice 比 MethodDelegation 更快
3. **限制匹配范围**: 尽量精确匹配，避免匹配过多类
4. **避免在拦截器中做耗时操作**: 拦截器代码会在每次方法调用时执行

### 4. 常见匹配器

| 匹配器 | 说明 | 示例 |
|--------|------|------|
| `nameStartsWith(String)` | 名称以指定字符串开头 | `nameStartsWith("org.example")` |
| `nameEndsWith(String)` | 名称以指定字符串结尾 | `nameEndsWith("Service")` |
| `nameMatches(String)` | 名称匹配正则表达式 | `nameMatches(".*Service.*")` |
| `isAnnotatedWith(ElementMatcher)` | 匹配注解 | `isAnnotatedWith(named("..."))` |
| `isPublic()` | 匹配 public 方法 | `isPublic()` |
| `isStatic()` | 匹配静态方法 | `isStatic()` |
| `returns(Class)` | 匹配返回类型 | `returns(String.class)` |
| `takesArguments(Class...)` | 匹配参数类型 | `takesArguments(Long.class)` |

### 5. 监听器使用

监听器可以用于：
- 调试：查看哪些类被处理
- 监控：统计类加载和转换情况
- 错误追踪：捕获转换过程中的错误

### 6. 注意事项

1. **类加载时机**: Agent 只能拦截在 Agent 启动后加载的类
2. **系统类限制**: 某些系统类可能无法被转换
3. **性能影响**: 拦截会增加方法调用开销，生产环境需谨慎使用
4. **异常处理**: 拦截器中的异常会影响原方法的执行

---

## 总结

本次扩展为 ByteBuddy Agent 项目增加了：

✅ **3 种新的拦截器**（包路径匹配、Advice、高级拦截器）
✅ **1 个匹配器工具类**（提供常用匹配器组合）
✅ **1 个类加载监听器**（监听类加载事件）
✅ **多模式配置支持**（通过参数控制拦截方式）
✅ **完整的测试类**（演示各种功能）

这些功能覆盖了 ByteBuddy 的核心用法，包括：
- 多种拦截方式（MethodDelegation、Advice）
- 灵活的匹配器（包路径、类名、方法名、参数类型等）
- 监听器机制
- 性能优化技巧

通过这些示例，可以全面学习 ByteBuddy 的各种功能和最佳实践。

---

## 后续扩展建议

如果还想继续学习，可以考虑添加：

1. **字段访问拦截**: 使用 `@FieldValue` 访问和修改字段
2. **方法调用拦截**: 使用 `MethodCall` 拦截方法调用
3. **固定返回值**: 使用 `FixedValue` 返回固定值
4. **动态添加方法**: 在运行时添加新方法
5. **动态添加字段**: 在运行时添加新字段
6. **配置化**: 通过配置文件控制拦截规则
7. **采样率控制**: 只拦截部分调用（性能优化）
8. **异步处理**: 在拦截器中使用异步处理（避免阻塞）

---

**文档版本**: 1.0  
**最后更新**: 2024年  
**作者**: ByteBuddy 学习项目

