package com.itheima.pogo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class Users {
    @TableId(type = IdType.AUTO) // 自增 ID
    private int id;
    private String username;
    private String password;
    private String email;
    private String googleSecretKey;  // Google 认证密钥

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;  // 创建时间

    @TableField(value = "last_login_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginAt;  // 最后登录时间

    // 添加字段：注册时的 IP 地址
    @TableField(value = "registration_ip")
    private String registrationIp;  // 注册时 IP 地址

    // 添加字段：最后登录的 IP 地址
    @TableField(value = "last_login_ip")
    private String lastLoginIp;  // 最后登录 IP 地址

    // 临时字段，用于存储封禁状态
    @TableField(exist = false)
    private Integer isDisabled;

    public String getRegistrationIp() {
        return registrationIp;
    }

    public void setRegistrationIp(String registrationIp) {
        this.registrationIp = registrationIp;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public Integer getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(Integer isDisabled) {
        this.isDisabled = isDisabled;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGoogleSecretKey() {
        return googleSecretKey;
    }

    public void setGoogleSecretKey(String googleSecretKey) {
        this.googleSecretKey = googleSecretKey;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Date lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
