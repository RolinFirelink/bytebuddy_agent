package org.example;

import org.example.service.OrderService;
import org.example.service.UserService;
import org.example.dao.UserDao;

/**
 * 测试应用程序
 * 用于演示各种拦截器功能
 */
public class TestApplication {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("开始测试 ByteBuddy Agent 功能");
        System.out.println("========================================");

        // 测试 UserService（会被包路径匹配拦截器拦截）
        System.out.println("\n--- 测试 UserService ---");
        UserService userService = new UserService();
        userService.getUserById(1L);
        userService.findUserByName("张三");
        userService.saveUser("李四", 25);
        userService.getUserCount();

        // 测试 OrderService（会被包路径匹配拦截器拦截）
        System.out.println("\n--- 测试 OrderService ---");
        OrderService orderService = new OrderService();
        orderService.createOrder("商品A", 99.99);
        orderService.cancelOrder(123L);
        orderService.getOrderStatus(123L);

        // 测试 UserDao（会被高级拦截器拦截，如果使用 advanced 模式）
        System.out.println("\n--- 测试 UserDao ---");
        UserDao userDao = new UserDao();
        userDao.queryById(1L);
        userDao.insert("新用户");
        userDao.update(1L, "更新后的名字");

        System.out.println("\n========================================");
        System.out.println("测试完成");
        System.out.println("========================================");
    }
}

