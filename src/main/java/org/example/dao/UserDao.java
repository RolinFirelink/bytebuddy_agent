package org.example.dao;

/**
 * 用户数据访问对象
 * 用于演示高级拦截器
 */
public class UserDao {

    public String queryById(Long id) {
        System.out.println("[UserDao] 数据库查询: id=" + id);
        return "User from DB: " + id;
    }

    public void insert(String name) {
        System.out.println("[UserDao] 数据库插入: name=" + name);
    }

    public void update(Long id, String name) {
        System.out.println("[UserDao] 数据库更新: id=" + id + ", name=" + name);
    }
}

