package me.juhezi.eternal.model;

import io.realm.RealmObject;

/**
 * 用户实体类
 * - id
 * - username 用户名
 * - nickname 昵称
 * - password 密码
 * - ...
 */
public class User extends RealmObject {

    private String id;
    private String username;
    private String password;
    private String nickname;

    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public User setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

}
