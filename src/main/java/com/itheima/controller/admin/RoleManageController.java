package com.itheima.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.itheima.config.Operation.NotLog;
import com.itheima.config.Operation.OperationDesc;
import com.itheima.mapper.user.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/manage/role")
public class RoleManageController {

    private static final Logger LogUtils = LoggerFactory.getLogger(RoleManageController.class);

    @Autowired
    private UserMapper userMapper;

    @OperationDesc("查询所有角色")
    @SaCheckLogin
    @SaCheckPermission("role:read")
    @GetMapping("/list")
    @NotLog
    public SaResult getAllRoles() {
        List<String> roles = StpUtil.getRoleList();
        if (!roles.contains("root") && !roles.contains("admin")) {
            LogUtils.warn("无权限访问角色管理接口：userId={}", StpUtil.getLoginId());
            return SaResult.error("无权限访问");
        }

        List<Map<String, Object>> roleList = userMapper.getAllRoles();
        LogUtils.info("查询所有角色成功，记录数：{}", roleList.size());
        return SaResult.ok().setData(roleList);
    }

    @OperationDesc("添加角色")
    @SaCheckLogin
    @SaCheckPermission("role:insert")
    @PostMapping("/add")
    public SaResult addRole(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        String description = params.get("description");

        if (name == null || name.trim().isEmpty()) {
            LogUtils.warn("添加角色失败，角色名称为空：params={}", params);
            return SaResult.error("角色名称不能为空");
        }
        if (name.length() > 50) {
            LogUtils.warn("添加角色失败，角色名称长度超出限制：name={}", name);
            return SaResult.error("角色名称长度不能超过 50 个字符");
        }
        if (description != null && description.length() > 255) {
            LogUtils.warn("添加角色失败，描述长度超出限制：description={}", description);
            return SaResult.error("描述长度不能超过 255 个字符");
        }

        Map<String, Object> existingRole = userMapper.getRoleByName(name);
        if (existingRole != null) {
            LogUtils.info("添加角色失败，角色名称已存在：name={}", name);
            return SaResult.error("角色名称已存在");
        }

        int result = userMapper.insertRole(name, description);
        if (result > 0) {
            LogUtils.info("管理员添加角色成功：name={}", name);
            return SaResult.ok("添加角色成功");
        } else {
            LogUtils.error("添加角色失败，数据库插入失败：name={}", name);
            return SaResult.error("添加角色失败");
        }
    }

    @OperationDesc("更新角色信息及权限")
    @SaCheckLogin
    @SaCheckPermission("role:update")
    @PostMapping("/updateWithPermissions")
    @Transactional
    public SaResult updateRoleWithPermissions(@RequestBody Map<String, Object> params) {
        Integer roleId = params.get("roleId") != null ? Integer.parseInt(params.get("roleId").toString()) : null;
        String name = (String) params.get("name");
        String description = (String) params.get("description");
        List<Integer> permissionIds = params.get("permissionIds") != null ? (List<Integer>) params.get("permissionIds") : new ArrayList<>();

        if (roleId == null) {
            LogUtils.warn("更新角色失败，角色ID为空：params={}", params);
            return SaResult.error("角色ID不能为空");
        }
        if (name != null && name.trim().isEmpty()) {
            LogUtils.warn("更新角色失败，角色名称为空：params={}", params);
            return SaResult.error("角色名称不能为空");
        }
        if (name != null && name.length() > 50) {
            LogUtils.warn("更新角色失败，角色名称长度超出限制：name={}", name);
            return SaResult.error("角色名称长度不能超过 50 个字符");
        }
        if (description != null && description.length() > 255) {
            LogUtils.warn("更新角色失败，描述长度超出限制：description={}", description);
            return SaResult.error("描述长度不能超过 255 个字符");
        }

        Map<String, Object> role = userMapper.getRoleById(roleId);
        if (role == null) {
            LogUtils.warn("更新角色失败，角色不存在：roleId={}", roleId);
            return SaResult.error("角色不存在");
        }

        if (name != null && !name.equals(role.get("name"))) {
            Map<String, Object> existingRole = userMapper.getRoleByName(name);
            if (existingRole != null) {
                LogUtils.info("更新角色失败，角色名称已存在：name={}", name);
                return SaResult.error("角色名称已存在");
            }
        }

        int result = userMapper.updateRole(roleId, name, description);
        if (result == 0) {
            LogUtils.error("更新角色失败，数据库更新失败：roleId={}", roleId);
            return SaResult.error("更新角色信息失败");
        }

        List<Map<String, Object>> currentPermissions = userMapper.getRolePermissions(roleId, null);
        List<Integer> currentPermissionIds = currentPermissions.stream()
                .map(p -> (Integer) p.get("permissionId"))
                .filter(id -> id != null)
                .collect(Collectors.toList());

        List<Integer> permissionIdsToAdd = permissionIds.stream()
                .filter(id -> !currentPermissionIds.contains(id))
                .collect(Collectors.toList());
        List<Integer> permissionIdsToRemove = currentPermissionIds.stream()
                .filter(id -> !permissionIds.contains(id))
                .collect(Collectors.toList());

        for (Integer permissionId : permissionIdsToAdd) {
            Map<String, Object> permission = userMapper.getPermissionById(permissionId);
            if (permission == null) {
                LogUtils.warn("添加权限失败，权限不存在：permissionId={}", permissionId);
                continue;
            }
            userMapper.insertRolePermission(roleId, permissionId);
            LogUtils.info("为角色添加权限成功：roleId={}, permissionId={}", roleId, permissionId);
        }

        for (Integer permissionId : permissionIdsToRemove) {
            userMapper.deleteRolePermission(roleId, permissionId);
            LogUtils.info("为角色删除权限成功：roleId={}, permissionId={}", roleId, permissionId);
        }

        LogUtils.info("管理员更新角色及权限成功：roleId={}", roleId);
        return SaResult.ok("更新角色及权限成功");
    }

    @OperationDesc("删除角色")
    @SaCheckLogin
    @SaCheckPermission("role:delete")
    @PostMapping("/delete")
    public SaResult deleteRole(@RequestBody Map<String, Integer> params) {
        Integer roleId = params.get("roleId");

        if (roleId == null) {
            LogUtils.warn("删除角色失败，角色ID为空：params={}", params);
            return SaResult.error("角色ID不能为空");
        }

        Map<String, Object> role = userMapper.getRoleById(roleId);
        if (role == null) {
            LogUtils.warn("删除角色失败，角色不存在：roleId={}", roleId);
            return SaResult.error("角色不存在");
        }

        int deletedPermissions = userMapper.deleteRolePermissions(roleId);
        LogUtils.info("删除角色权限关联：roleId={}, deletedRows={}", roleId, deletedPermissions);

        int deletedUserRoles = userMapper.deleteUserRolesByRoleId(roleId);
        LogUtils.info("删除用户角色关联：roleId={}, deletedRows={}", roleId, deletedUserRoles);

        int result = userMapper.deleteRole(roleId);
        if (result > 0) {
            LogUtils.info("管理员删除角色成功：roleId={}", roleId);
            return SaResult.ok("删除角色成功");
        } else {
            LogUtils.error("删除角色失败，数据库删除失败：roleId={}", roleId);
            return SaResult.error("删除角色失败");
        }
    }

    @OperationDesc("批量删除角色")
    @SaCheckLogin
    @SaCheckPermission("role:delete")
    @PostMapping("/batchDelete")
    public SaResult batchDeleteRoles(@RequestBody List<Integer> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            LogUtils.warn("批量删除角色失败，角色ID列表为空");
            return SaResult.error("角色ID列表不能为空");
        }

        int successCount = 0;
        for (Integer roleId : roleIds) {
            Map<String, Object> role = userMapper.getRoleById(roleId);
            if (role == null) continue;

            userMapper.deleteRolePermissions(roleId);
            userMapper.deleteUserRolesByRoleId(roleId);
            int result = userMapper.deleteRole(roleId);
            if (result > 0) successCount++;
        }

        LogUtils.info("管理员批量删除角色成功：成功 {} 个，失败 {} 个", successCount, roleIds.size() - successCount);
        return SaResult.ok("批量删除成功：" + successCount + " 个，失败：" + (roleIds.size() - successCount) + " 个");
    }

    @OperationDesc("查询角色权限")
    @SaCheckLogin
    @SaCheckPermission("role:read")
    @GetMapping("/permissions")
    public SaResult getRolePermissions(
            @RequestParam(required = false) Integer roleId,
            @RequestParam(required = false) String roleName) {
        List<String> roles = StpUtil.getRoleList();
        if (!roles.contains("root") && !roles.contains("admin")) {
            LogUtils.warn("无权限访问角色权限接口：userId={}", StpUtil.getLoginId());
            return SaResult.error("无权限访问");
        }

        if (roleId == null && (roleName == null || roleName.trim().isEmpty())) {
            return SaResult.error("请输入角色ID或角色名称");
        }

        List<Map<String, Object>> permissions = userMapper.getRolePermissions(roleId, roleName);
        if (permissions.isEmpty()) {
            LogUtils.warn("查询角色权限失败，角色不存在：roleId={}, roleName={}", roleId, roleName);
            return SaResult.error("角色不存在或未分配权限");
        }

        LogUtils.info("查询角色权限成功：roleId={}, roleName={}, permissionsCount={}", roleId, roleName, permissions.size());
        return SaResult.ok().setData(permissions);
    }

    @OperationDesc("查询所有权限")
    @SaCheckLogin
    @SaCheckPermission("role:read")
    @GetMapping("/permission/list")
    public SaResult getAllPermissions() {
        List<Map<String, Object>> permissionList = userMapper.getAllPermissions();
        LogUtils.info("查询所有权限成功，记录数：{}", permissionList.size());
        return SaResult.ok().setData(permissionList);
    }
}