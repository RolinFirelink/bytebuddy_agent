package org.example.service;

/**
 * 订单服务类
 * 用于演示包路径匹配拦截器
 */
public class OrderService {

    public String createOrder(String productName, Double price) {
        System.out.println("[OrderService] 创建订单: product=" + productName + ", price=" + price);
        return "Order-" + System.currentTimeMillis();
    }

    public void cancelOrder(Long orderId) {
        System.out.println("[OrderService] 取消订单: " + orderId);
    }

    public String getOrderStatus(Long orderId) {
        System.out.println("[OrderService] 查询订单状态: " + orderId);
        return "PROCESSING";
    }
}

