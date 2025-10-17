package com.itheima.mapper.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper {

    @Select("SELECT p.name " +
            "FROM permissions p " +
            "JOIN role_permissions rp ON p.id = rp.permission_id " +
            "JOIN user_roles ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> getPermissionsByUserId(Long userId);
}
