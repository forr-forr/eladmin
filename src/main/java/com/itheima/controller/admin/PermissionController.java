package com.itheima.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.itheima.config.Operation.OperationDesc;
import com.itheima.mapper.user.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manage/permission")
public class PermissionController {

    private static final Logger LogUtils = LoggerFactory.getLogger(PermissionController.class);

    @Autowired
    private UserMapper userMapper;

    /**
     * 查询所有权限
     */
    @OperationDesc("查询所有权限")
    @SaCheckLogin
    @SaCheckPermission("permission:read")
    @GetMapping("/list")
    public SaResult getAllPermissions() {
        // 权限检查（确保用户角色为 root 或 admin）
        List<String> roles = StpUtil.getRoleList();
        if (!roles.contains("root") && !roles.contains("admin")) {
            LogUtils.warn("无权限访问权限管理接口：userId={}", StpUtil.getLoginId());
            return SaResult.error("无权限访问");
        }

        List<Map<String, Object>> permissionList = userMapper.getAllPermissions();
        LogUtils.info("查询所有权限成功，记录数：{}", permissionList.size());
        return SaResult.ok().setData(permissionList);
    }

    /**
     * 添加权限
     */
    @OperationDesc("添加权限")
    @SaCheckLogin
    @SaCheckPermission("permission:insert")
    @PostMapping("/add")
    public SaResult addPermission(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        String description = params.get("description");

        // 校验参数
        if (name == null || name.trim().isEmpty()) {
            LogUtils.warn("添加权限失败，权限名称为空：params={}", params);
            return SaResult.error("权限名称不能为空");
        }
        if (name.length() > 50) {
            LogUtils.warn("添加权限失败，权限名称长度超出限制：name={}", name);
            return SaResult.error("权限名称长度不能超过 50 个字符");
        }
        if (description != null && description.length() > 255) {
            LogUtils.warn("添加权限失败，描述长度超出限制：description={}", description);
            return SaResult.error("描述长度不能超过 255 个字符");
        }

        // 检查权限名称是否已存在
        Map<String, Object> existingPermission = userMapper.getPermissionByName(name);
        if (existingPermission != null) {
            LogUtils.info("添加权限失败，权限名称已存在：name={}", name);
            return SaResult.error("权限名称已存在");
        }

        // 插入权限
        int result = userMapper.insertPermission(name, description);
        if (result > 0) {
            LogUtils.info("管理员添加权限成功：name={}", name);
            return SaResult.ok("添加权限成功");
        } else {
            LogUtils.error("添加权限失败，数据库插入失败：name={}", name);
            return SaResult.error("添加权限失败");
        }
    }

    /**
     * 修改权限
     */
    @OperationDesc("修改权限")
    @SaCheckLogin
    @SaCheckPermission("permission:update")
    @PostMapping("/update")
    public SaResult updatePermission(@RequestBody Map<String, Object> params) {
        Integer permissionId = params.get("id") != null ? Integer.parseInt(params.get("id").toString()) : null;
        String name = (String) params.get("name");
        String description = (String) params.get("description");

        // 校验参数
        if (permissionId == null) {
            LogUtils.warn("修改权限失败，权限ID为空：params={}", params);
            return SaResult.error("权限ID不能为空");
        }
        if (name != null && name.trim().isEmpty()) {
            LogUtils.warn("修改权限失败，权限名称为空：params={}", params);
            return SaResult.error("权限名称不能为空");
        }
        if (name != null && name.length() > 50) {
            LogUtils.warn("修改权限失败，权限名称长度超出限制：name={}", name);
            return SaResult.error("权限名称长度不能超过 50 个字符");
        }
        if (description != null && description.length() > 255) {
            LogUtils.warn("修改权限失败，描述长度超出限制：description={}", description);
            return SaResult.error("描述长度不能超过 255 个字符");
        }

        // 检查权限是否存在
        Map<String, Object> permission = userMapper.getPermissionById(permissionId);
        if (permission == null) {
            LogUtils.warn("修改权限失败，权限不存在：permissionId={}", permissionId);
            return SaResult.error("权限不存在");
        }

        // 检查权限名称是否被占用（排除当前权限）
        if (name != null && !name.equals(permission.get("name"))) {
            Map<String, Object> existingPermission = userMapper.getPermissionByName(name);
            if (existingPermission != null) {
                LogUtils.info("修改权限失败，权限名称已存在：name={}", name);
                return SaResult.error("权限名称已存在");
            }
        }

        // 更新权限
        int result = userMapper.updatePermission(permissionId, name, description);
        if (result > 0) {
            LogUtils.info("管理员修改权限成功：permissionId={}", permissionId);
            return SaResult.ok("修改权限成功");
        } else {
            LogUtils.error("修改权限失败，数据库更新失败：permissionId={}", permissionId);
            return SaResult.error("修改权限失败");
        }
    }

    /**
     * 删除权限
     */
    @OperationDesc("删除权限")
    @SaCheckLogin
    @SaCheckPermission("permission:delete")
    @PostMapping("/delete")
    public SaResult deletePermission(@RequestBody Map<String, Integer> params) {
        Integer permissionId = params.get("permissionId");

        // 校验参数
        if (permissionId == null) {
            LogUtils.warn("删除权限失败，权限ID为空：params={}", params);
            return SaResult.error("权限ID不能为空");
        }

        // 检查权限是否存在
        Map<String, Object> permission = userMapper.getPermissionById(permissionId);
        if (permission == null) {
            LogUtils.warn("删除权限失败，权限不存在：permissionId={}", permissionId);
            return SaResult.error("权限不存在");
        }

        // 删除角色-权限关联
        int deletedRolePermissions = userMapper.deleteRolePermissionsByPermissionId(permissionId);
        LogUtils.info("删除角色权限关联：permissionId={}, deletedRows={}", permissionId, deletedRolePermissions);

        // 删除权限
        int result = userMapper.deletePermission(permissionId);
        if (result > 0) {
            LogUtils.info("管理员删除权限成功：permissionId={}", permissionId);
            return SaResult.ok("删除权限成功");
        } else {
            LogUtils.error("删除权限失败，数据库删除失败：permissionId={}", permissionId);
            return SaResult.error("删除权限失败");
        }
    }

    /**
     * 批量删除权限
     */
    @OperationDesc("批量删除权限")
    @SaCheckLogin
    @SaCheckPermission("permission:delete")
    @PostMapping("/batchDelete")
    public SaResult batchDeletePermissions(@RequestBody List<Integer> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            LogUtils.warn("批量删除权限失败，权限ID列表为空");
            return SaResult.error("权限ID列表不能为空");
        }

        int successCount = 0;
        for (Integer permissionId : permissionIds) {
            // 检查权限是否存在
            Map<String, Object> permission = userMapper.getPermissionById(permissionId);
            if (permission == null) continue;

            // 删除角色-权限关联
            userMapper.deleteRolePermissionsByPermissionId(permissionId);

            // 删除权限
            int result = userMapper.deletePermission(permissionId);
            if (result > 0) {
                successCount++;
            }
        }

        LogUtils.info("管理员批量删除权限成功：成功 {} 个，失败 {} 个", successCount, permissionIds.size() - successCount);
        return SaResult.ok("批量删除成功：" + successCount + " 个，失败：" + (permissionIds.size() - successCount) + " 个");
    }
}