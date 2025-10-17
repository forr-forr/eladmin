package com.itheima.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pogo.Users;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 用户相关的数据库操作接口，继承 MyBatis-Plus 的 BaseMapper，提供基本的 CRUD 操作。
 * 同时定义了用户、角色、权限、封禁日志等相关的自定义查询和操作方法。
 */
@Mapper
public interface UserMapper extends BaseMapper<Users> {

    /**
     * 根据用户名查询用户信息。
     *
     * @param username 用户名
     * @return Users 用户对象，如果用户不存在则返回 null
     * @usage 用于用户登录、注册时检查用户名是否存在
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    Users getUserByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户信息。
     *
     * @param email 邮箱地址
     * @return Users 用户对象，如果用户不存在则返回 null
     * @usage 用于用户注册时检查邮箱是否已被使用
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    Users getUserByEmail(@Param("email") String email);

    /**
     * 为用户分配角色，插入用户-角色关联记录。
     *
     * @param userId 用户 ID
     * @param roleId 角色 ID
     * @usage 在添加用户或修改用户角色时调用，例如在用户注册时分配默认角色
     */
    @Insert("INSERT INTO user_roles (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insertUserRole(@Param("userId") int userId, @Param("roleId") int roleId);

    /**
     * 删除指定用户的所有角色关联记录。
     *
     * @param userId 用户 ID
     * @usage 在删除用户或重置用户角色时调用
     */
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId}")
    void deleteUserRole(@Param("userId") Integer userId);

    /**
     * 插入或更新用户的封禁记录。
     * 如果记录已存在（根据 user_id 和 service 的唯一索引），则更新 reason 和 disable_until 字段。
     *
     * @param userId 用户 ID
     * @param service 封禁服务标识（例如 "user-freeze"）
     * @param reason 封禁原因
     * @param disableUntil 封禁截止时间（NULL 表示永久封禁）
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     * @usage 在冻结用户时调用，记录封禁信息
     */
    @Insert("INSERT INTO user_disable_logs (user_id, service, reason, disable_until, created_at, updated_at) " +
            "VALUES (#{userId}, #{service}, #{reason}, #{disableUntil}, #{createdAt}, #{updatedAt}) " +
            "ON DUPLICATE KEY UPDATE reason = #{reason}, disable_until = #{disableUntil}, updated_at = #{updatedAt}")
    void upsertDisableInfo(@Param("userId") Integer userId, @Param("service") String service,
                           @Param("reason") String reason, @Param("disableUntil") Timestamp disableUntil,
                           @Param("createdAt") Timestamp createdAt, @Param("updatedAt") Timestamp updatedAt);

    /**
     * 删除指定用户的封禁记录。
     *
     * @param userId 用户 ID
     * @param service 封禁服务标识（例如 "user-freeze"）
     * @usage 在解冻用户时调用，删除封禁记录
     */
    @Delete("DELETE FROM user_disable_logs WHERE user_id = #{userId} AND service = #{service}")
    void deleteDisableInfo(@Param("userId") Integer userId, @Param("service") String service);

    /**
     * 查询指定用户的封禁原因。
     *
     * @param userId 用户 ID
     * @param service 封禁服务标识（例如 "user-freeze"）
     * @return String 封禁原因，如果记录不存在则返回 null
     * @usage 在用户登录时检查封禁状态并显示封禁原因
     */
    @Select("SELECT reason FROM user_disable_logs WHERE user_id = #{userId} AND service = #{service}")
    String getDisableReason(@Param("userId") Integer userId, @Param("service") String service);

    /**
     * 查询指定用户的权限信息，包括用户所属的角色和角色关联的权限。
     *
     * @param userId 用户 ID
     * @return List<Map<String, Object>> 权限信息列表，每个 Map 包含 userId, username, roleId, roleName, permissionId, permissionName, permissionDescription
     * @usage 在用户登录后获取其权限列表，用于权限控制
     */
    @Select("SELECT " +
            "    u.id AS userId, " +
            "    u.username AS username, " +
            "    r.id AS roleId, " +
            "    r.name AS roleName, " +
            "    p.id AS permissionId, " +
            "    p.name AS permissionName, " +
            "    p.description AS permissionDescription " +
            "FROM " +
            "    users u " +
            "    LEFT JOIN user_roles ur ON u.id = ur.user_id " +
            "    LEFT JOIN roles r ON ur.role_id = r.id " +
            "    LEFT JOIN role_permissions rp ON r.id = rp.role_id " +
            "    LEFT JOIN permissions p ON rp.permission_id = p.id " +
            "WHERE " +
            "    u.id = #{userId}")
    List<Map<String, Object>> getUserPermissions(@Param("userId") Integer userId);

    /**
     * 查询所有角色信息。
     *
     * @return List<Map<String, Object>> 角色列表，每个 Map 包含 id, name, description, createdAt
     * @usage 在角色管理页面显示所有角色
     */
    @Select("SELECT " +
            "    id, " +
            "    name, " +
            "    description, " +
            "    created_at AS createdAt " +
            "FROM roles")
    List<Map<String, Object>> getAllRoles();

    /**
     * 根据角色 ID 查询角色信息。
     *
     * @param roleId 角色 ID
     * @return Map<String, Object> 角色信息，包含 id, name, description, createdAt，如果角色不存在则返回 null
     * @usage 在修改角色或删除角色时检查角色是否存在
     */
    @Select("SELECT " +
            "    id, " +
            "    name, " +
            "    description, " +
            "    created_at AS createdAt " +
            "FROM roles " +
            "WHERE id = #{roleId}")
    Map<String, Object> getRoleById(@Param("roleId") Integer roleId);

    /**
     * 根据角色名称查询角色信息。
     *
     * @param name 角色名称
     * @return Map<String, Object> 角色信息，包含 id, name, description, createdAt，如果角色不存在则返回 null
     * @usage 在添加角色时检查角色名称是否已存在
     */
    @Select("SELECT " +
            "    id, " +
            "    name, " +
            "    description, " +
            "    created_at AS createdAt " +
            "FROM roles " +
            "WHERE name = #{name}")
    Map<String, Object> getRoleByName(@Param("name") String name);

    /**
     * 插入新角色。
     *
     * @param name 角色名称
     * @param description 角色描述
     * @return int 受影响的行数（成功插入返回 1）
     * @usage 在角色管理页面添加新角色
     */
    @Insert("INSERT INTO roles (name, description, created_at) " +
            "VALUES (#{name}, #{description}, NOW())")
    int insertRole(@Param("name") String name, @Param("description") String description);

    /**
     * 更新角色信息。
     * 使用 COALESCE 确保只更新非 null 的字段。
     *
     * @param roleId 角色 ID
     * @param name 角色名称（可为 null）
     * @param description 角色描述（可为 null）
     * @return int 受影响的行数（成功更新返回 1）
     * @usage 在角色管理页面修改角色信息
     */
    @Update("UPDATE roles " +
            "SET " +
            "    name = COALESCE(#{name}, name), " +
            "    description = COALESCE(#{description}, description) " +
            "WHERE id = #{roleId}")
    int updateRole(@Param("roleId") Integer roleId, @Param("name") String name, @Param("description") String description);

    /**
     * 删除指定角色。
     *
     * @param roleId 角色 ID
     * @return int 受影响的行数（成功删除返回 1）
     * @usage 在角色管理页面删除角色
     */
    @Delete("DELETE FROM roles WHERE id = #{roleId}")
    int deleteRole(@Param("roleId") Integer roleId);

    /**
     * 删除指定角色的所有权限关联记录。
     *
     * @param roleId 角色 ID
     * @return int 受影响的行数
     * @usage 在删除角色时清理其权限关联记录
     */
    @Delete("DELETE FROM role_permissions WHERE role_id = #{roleId}")
    int deleteRolePermissions(@Param("roleId") Integer roleId);

    /**
     * 删除指定角色的所有用户-角色关联记录。
     *
     * @param roleId 角色 ID
     * @return int 受影响的行数
     * @usage 在删除角色时清理其用户-角色关联记录
     */
    @Delete("DELETE FROM user_roles WHERE role_id = #{roleId}")
    int deleteUserRolesByRoleId(@Param("roleId") Integer roleId);

    /**
     * 查询指定角色的权限信息。
     * 支持通过角色 ID 或角色名称查询。
     *
     * @param roleId 角色 ID（可选）
     * @param roleName 角色名称（可选）
     * @return List<Map<String, Object>> 权限信息列表，每个 Map 包含 roleId, roleName, roleDescription, permissionId, permissionName, permissionDescription
     * @usage 在角色管理页面查看角色的权限列表
     */
    @Select("<script>" +
            "SELECT " +
            "    r.id AS roleId, " +
            "    r.name AS roleName, " +
            "    r.description AS roleDescription, " +
            "    p.id AS permissionId, " +
            "    p.name AS permissionName, " +
            "    p.description AS permissionDescription " +
            "FROM " +
            "    roles r " +
            "    LEFT JOIN role_permissions rp ON r.id = rp.role_id " +
            "    LEFT JOIN permissions p ON rp.permission_id = p.id " +
            "WHERE " +
            "    1=1 " +
            "<if test='roleId != null'>" +
            "    AND r.id = #{roleId} " +
            "</if>" +
            "<if test='roleName != null and roleName != \"\"'>" +
            "    AND r.name = #{roleName} " +
            "</if>" +
            "</script>")
    List<Map<String, Object>> getRolePermissions(@Param("roleId") Integer roleId, @Param("roleName") String roleName);

    /**
     * 根据权限 ID 查询权限信息。
     *
     * @param permissionId 权限 ID
     * @return Map<String, Object> 权限信息，包含 id, name, description, createdAt，如果权限不存在则返回 null
     * @usage 在为角色添加权限或修改权限时检查权限是否存在
     */
    @Select("SELECT " +
            "    id, " +
            "    name, " +
            "    description, " +
            "    created_at AS createdAt " +
            "FROM permissions " +
            "WHERE id = #{permissionId}")
    Map<String, Object> getPermissionById(@Param("permissionId") Integer permissionId);

    /**
     * 根据权限名称查询权限信息。
     *
     * @param name 权限名称
     * @return Map<String, Object> 权限信息，包含 id, name, description, createdAt，如果权限不存在则返回 null
     * @usage 在添加权限时检查权限名称是否已存在
     */
    @Select("SELECT " +
            "    id, " +
            "    name, " +
            "    description, " +
            "    created_at AS createdAt " +
            "FROM permissions " +
            "WHERE name = #{name}")
    Map<String, Object> getPermissionByName(@Param("name") String name);

    /**
     * 查询指定角色是否已拥有某权限。
     *
     * @param roleId 角色 ID
     * @param permissionId 权限 ID
     * @return Map<String, Object> 角色-权限关联记录，如果不存在则返回 null
     * @usage 在为角色添加权限时检查是否已存在该权限
     */
    @Select("SELECT * FROM role_permissions " +
            "WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    Map<String, Object> getRolePermission(@Param("roleId") Integer roleId, @Param("permissionId") Integer permissionId);

    /**
     * 为角色分配权限，插入角色-权限关联记录。
     *
     * @param roleId 角色 ID
     * @param permissionId 权限 ID
     * @return int 受影响的行数（成功插入返回 1）
     * @usage 在角色管理页面为角色添加权限
     */
    @Insert("INSERT INTO role_permissions (role_id, permission_id, created_at) " +
            "VALUES (#{roleId}, #{permissionId}, NOW())")
    int insertRolePermission(@Param("roleId") Integer roleId, @Param("permissionId") Integer permissionId);

    /**
     * 删除指定角色的指定权限。
     *
     * @param roleId 角色 ID
     * @param permissionId 权限 ID
     * @return int 受影响的行数（成功删除返回 1）
     * @usage 在角色管理页面删除角色的权限
     */
    @Delete("DELETE FROM role_permissions " +
            "WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int deleteRolePermission(@Param("roleId") Integer roleId, @Param("permissionId") Integer permissionId);

    /**
     * 查询所有权限信息。
     *
     * @return List<Map<String, Object>> 权限列表，每个 Map 包含 id, name, description, createdAt
     * @usage 在权限管理页面显示所有权限
     */
    @Select("SELECT " +
            "    id, " +
            "    name, " +
            "    description, " +
            "    created_at AS createdAt " +
            "FROM permissions")
    List<Map<String, Object>> getAllPermissions();

    /**
     * 插入新权限。
     *
     * @param name 权限名称
     * @param description 权限描述
     * @return int 受影响的行数（成功插入返回 1）
     * @usage 在权限管理页面添加新权限
     */
    @Insert("INSERT INTO permissions (name, description, created_at) " +
            "VALUES (#{name}, #{description}, NOW())")
    int insertPermission(@Param("name") String name, @Param("description") String description);

    /**
     * 更新权限信息。
     * 使用 COALESCE 确保只更新非 null 的字段。
     *
     * @param permissionId 权限 ID
     * @param name 权限名称（可为 null）
     * @param description 权限描述（可为 null）
     * @return int 受影响的行数（成功更新返回 1）
     * @usage 在权限管理页面修改权限信息
     */
    @Update("UPDATE permissions " +
            "SET " +
            "    name = COALESCE(#{name}, name), " +
            "    description = COALESCE(#{description}, description) " +
            "WHERE id = #{permissionId}")
    int updatePermission(@Param("permissionId") Integer permissionId, @Param("name") String name, @Param("description") String description);

    /**
     * 删除指定权限。
     *
     * @param permissionId 权限 ID
     * @return int 受影响的行数（成功删除返回 1）
     * @usage 在权限管理页面删除权限
     */
    @Delete("DELETE FROM permissions WHERE id = #{permissionId}")
    int deletePermission(@Param("permissionId") Integer permissionId);

    /**
     * 删除指定权限的所有角色-权限关联记录。
     *
     * @param permissionId 权限 ID
     * @return int 受影响的行数
     * @usage 在删除权限时清理其角色-权限关联记录
     */
    @Delete("DELETE FROM role_permissions WHERE permission_id = #{permissionId}")
    int deleteRolePermissionsByPermissionId(@Param("permissionId") Integer permissionId);


    // 新增方法：查询用户的角色
    @Select("SELECT " +
            "    r.id AS roleId, " +
            "    r.name AS roleName " +
            "FROM roles r " +
            "JOIN user_roles ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<Map<String, Object>> getUserRoles(@Param("userId") Integer userId);

}