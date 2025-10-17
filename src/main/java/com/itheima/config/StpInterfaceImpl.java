package com.itheima.config;

import cn.dev33.satoken.stp.StpInterface;
import com.itheima.mapper.user.PermissionMapper;
import com.itheima.mapper.user.RoleMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Sa-Token 自定义权限验证扩展，使用 MyBatis-Plus 查询数据库
 */
@Component
public class StpInterfaceImpl implements StpInterface {
    @Resource
    private RoleMapper roleMapper; // 角色查询 Mapper

    @Resource
    private PermissionMapper permissionMapper; // 权限查询 Mapper

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return permissionMapper.getPermissionsByUserId(Long.parseLong(loginId.toString()));
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {

        return roleMapper.getRolesByUserId(Long.parseLong(loginId.toString()));
    }
}
