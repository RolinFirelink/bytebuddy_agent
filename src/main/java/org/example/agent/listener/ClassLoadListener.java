package org.example.agent.listener;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.io.PrintStream;

/**
 * 类加载监听器
 * 监听类的加载、转换、错误等事件
 */
public class ClassLoadListener implements AgentBuilder.Listener {

    private final PrintStream out;
    private final boolean verbose;

    public ClassLoadListener() {
        this(System.out, true);
    }

    public ClassLoadListener(PrintStream out, boolean verbose) {
        this.out = out;
        this.verbose = verbose;
    }

    @Override
    public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        if (verbose) {
            out.println("[Listener] 发现类: " + typeName + 
                    " (ClassLoader: " + (classLoader != null ? classLoader.getClass().getName() : "Bootstrap") + 
                    ", 已加载: " + loaded + ")");
        }
    }

    @Override
    public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {
        out.println("[Listener] ✓ 转换类: " + typeDescription.getName() + 
                " (已加载: " + loaded + ")");
        if (verbose && dynamicType != null) {
            out.println("[Listener]   生成的字节码大小: " + dynamicType.getBytes().length + " bytes");
        }
    }

    @Override
    public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
        if (verbose) {
            out.println("[Listener] ⊗ 忽略类: " + typeDescription.getName() + 
                    " (已加载: " + loaded + ")");
        }
    }

    @Override
    public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
        out.println("[Listener] ✗ 错误 - 类: " + typeName);
        out.println("[Listener]   异常类型: " + throwable.getClass().getName());
        out.println("[Listener]   异常信息: " + throwable.getMessage());
        if (verbose) {
            throwable.printStackTrace(out);
        }
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        if (verbose) {
            out.println("[Listener] 完成处理: " + typeName);
        }
    }
}

