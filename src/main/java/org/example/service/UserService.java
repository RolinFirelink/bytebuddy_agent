package org.example.service;

/**
 * 用户服务类
 * 用于演示包路径匹配拦截器
 */
public class UserService {

    public String getUserById(Long id) {
        System.out.println("[UserService] 查询用户: " + id);
        return "User-" + id;
    }

    public String findUserByName(String name) {
        System.out.println("[UserService] 根据名称查找用户: " + name);
        return "User: " + name;
    }

    public void saveUser(String name, Integer age) {
        System.out.println("[UserService] 保存用户: name=" + name + ", age=" + age);
    }

    public Integer getUserCount() {
        System.out.println("[UserService] 获取用户数量");
        return 100;
    }
}

