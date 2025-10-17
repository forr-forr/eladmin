package com.itheima.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.config.Operation.OperationDesc;
import com.itheima.mapper.user.UserMapper;
import com.itheima.pogo.Users;
import com.itheima.utlis.AESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manage/user")
public class UserManageController {

    private static final Logger LogUtils = LoggerFactory.getLogger(UserManageController.class);

    @Autowired
    private UserMapper userMapper;

    /**
     * 查询所有用户（支持分页和搜索）
     */
    @OperationDesc("查询所有用户")
    @SaCheckLogin
    @SaCheckPermission("user:read:all")
    @GetMapping("/list")
    public SaResult getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer roleId) {
        List<String> roles = StpUtil.getRoleList();
        if (!roles.contains("root") && !roles.contains("admin")) {
            LogUtils.warn("无权限访问用户管理接口：userId={}", StpUtil.getLoginId());
            return SaResult.error("无权限访问");
        }

        Page<Users> userPage = new Page<>(page, pageSize);
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();

        if (username != null && !username.trim().isEmpty()) {
            queryWrapper.like("username", username);
        }
        if (email != null && !email.trim().isEmpty()) {
            queryWrapper.like("email", email);
        }
        if (roleId != null) {
            if (roleId == 0) {
                queryWrapper.notExists("SELECT 1 FROM user_roles ur WHERE ur.user_id = users.id");
            } else {
                queryWrapper.exists("SELECT 1 FROM user_roles ur WHERE ur.user_id = users.id AND ur.role_id = " + roleId);
            }
        }

        userMapper.selectPage(userPage, queryWrapper);

        List<Users> users = userPage.getRecords();
        List<Map<String, Object>> userList = new ArrayList<>();

        users.forEach(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("createdAt", user.getCreatedAt());
            userMap.put("lastLoginAt", user.getLastLoginAt());
            userMap.put("password", "*********");
            if (user.getGoogleSecretKey() != null) {
                userMap.put("googleSecretKey", "*********");
            }
            userMap.put("isDisabled", StpUtil.isDisable(user.getId(), "user-freeze") ? 1 : 0);
            List<Map<String, Object>> userRoles = userMapper.getUserRoles(user.getId());
            userMap.put("roles", userRoles);
            userList.add(userMap);
        });

        Map<String, Object> result = new HashMap<>();
        result.put("data", userList);
        result.put("total", userPage.getTotal());
        LogUtils.info("管理员查询所有用户成功，记录数：{}", userList.size());
        return SaResult.ok().setData(result);
    }

    /**
     * 获取所有角色列表
     */
    @OperationDesc("获取所有角色")
    @SaCheckLogin
    @SaCheckPermission("user:read:all")
    @GetMapping("/roles")
    public SaResult getAllRoles() {
        List<Map<String, Object>> roleList = userMapper.getAllRoles();
        LogUtils.info("查询所有角色成功，记录数：{}", roleList.size());
        return SaResult.ok().setData(roleList);
    }

    /**
     * 添加用户
     */
    @OperationDesc("添加用户")
    @SaCheckLogin
    @SaCheckPermission("user:insert")
    @PostMapping("/add")
    public SaResult addUser(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String email = params.get("email");
        String password = params.get("password");
        Integer roleId = params.get("roleId") != null ? Integer.parseInt(params.get("roleId")) : null;

        List<String> currentUserRoles = StpUtil.getRoleList();

        if (username == null || email == null || password == null || roleId == null) {
            LogUtils.warn("添加用户失败，参数不完整：params={}", params);
            return SaResult.error("请填写所有字段");
        }
        if (username.length() < 3 || username.length() > 20) {
            LogUtils.warn("添加用户失败，用户名长度不符合要求：username={}", username);
            return SaResult.error("用户名长度应在 3 到 20 个字符之间");
        }
        if (email.length() > 50) {
            LogUtils.warn("添加用户失败，邮箱长度超出限制：email={}", email);
            return SaResult.error("邮箱长度不能超过 50 个字符");
        }
        if (password.length() < 6 || password.length() > 20) {
            LogUtils.warn("添加用户失败，密码长度不符合要求：username={}", username);
            return SaResult.error("密码长度应在 6 到 20 个字符之间");
        }

        if (!currentUserRoles.contains("root")) {
            if (roleId == 1 || roleId == 2) {
                LogUtils.warn("添加用户失败，admin 无权分配 root 或 admin 角色：roleId={}", roleId);
                return SaResult.error("无权分配超级管理员或管理员角色");
            }
        }

        Users existingUser = userMapper.getUserByUsername(username);
        if (existingUser != null) {
            LogUtils.info("添加用户失败，用户名已存在：username={}", username);
            return SaResult.error("用户名已存在");
        }
        Users existingEmailUser = userMapper.getUserByEmail(email);
        if (existingEmailUser != null) {
            LogUtils.info("添加用户失败，邮箱已被注册：email={}", email);
            return SaResult.error("邮箱已被注册");
        }

        Map<String, Object> role = userMapper.getRoleById(roleId);
        if (role == null) {
            LogUtils.warn("添加用户失败，角色不存在：roleId={}", roleId);
            return SaResult.error("所选角色不存在");
        }

        try {
            String encryptedPassword = AESUtil.encrypt(password);
            Users newUser = new Users();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(encryptedPassword);

            int result = userMapper.insert(newUser);
            if (result > 0) {
                int userId = newUser.getId();
                userMapper.insertUserRole(userId, roleId);
                LogUtils.info("管理员添加用户成功：userId={} username={} roleId={}", userId, username, roleId);
                return SaResult.ok("添加用户成功");
            } else {
                LogUtils.error("添加用户失败，数据库插入失败：username={}", username);
                return SaResult.error("添加用户失败");
            }
        } catch (Exception e) {
            LogUtils.error("添加用户失败，发生异常：username={}, error={}", username, e.getMessage(), e);
            return SaResult.error("添加用户失败，请稍后再试");
        }
    }

    /**
     * 修改用户
     */
    @OperationDesc("修改用户")
    @SaCheckLogin
    @SaCheckPermission("user:update")
    @PostMapping("/update")
    @Transactional
    public SaResult updateUser(@RequestBody Map<String, Object> params) {
        Integer userId = params.get("userId") != null ? Integer.parseInt(params.get("userId").toString()) : null;
        String username = (String) params.get("username");
        String email = (String) params.get("email");
        Integer roleId = params.get("roleId") != null ? Integer.parseInt(params.get("roleId").toString()) : null;

        Integer currentUserId = Integer.parseInt(StpUtil.getLoginId().toString());
        List<String> currentUserRoles = StpUtil.getRoleList();

        // 验证用户 ID
        if (userId == null) {
            LogUtils.warn("修改用户失败，用户ID为空：params={}", params);
            return SaResult.error("用户ID不能为空");
        }
        // 验证用户名
        if (username != null && (username.length() < 3 || username.length() > 20)) {
            LogUtils.warn("修改用户失败，用户名长度不符合要求：username={}", username);
            return SaResult.error("用户名长度应在 3 到 20 个字符之间");
        }
        // 验证邮箱
        if (email != null && email.length() > 50) {
            LogUtils.warn("修改用户失败，邮箱长度超出限制：email={}", email);
            return SaResult.error("邮箱长度不能超过 50 个字符");
        }
        // 禁止修改自己
        if (userId.equals(currentUserId)) {
            LogUtils.warn("修改用户失败，用户不能修改自己的角色：userId={}", userId);
            return SaResult.error("不能修改自己的角色");
        }
        // 非 root 用户不能分配 root 或 admin 角色
        if (!currentUserRoles.contains("root")) {
            if (roleId != null && (roleId == 1 || roleId == 2)) {
                LogUtils.warn("修改用户失败，admin 无权分配 root 或 admin 角色：roleId={}", roleId);
                return SaResult.error("无权分配超级管理员或管理员角色");
            }
        }

        Users user = userMapper.selectById(userId);
        if (user == null) {
            LogUtils.warn("修改用户失败，用户不存在：userId={}", userId);
            return SaResult.error("用户不存在");
        }

        // 验证角色（允许 roleId 为 null 或 0）
        if (roleId != null && roleId != 0) {
            Map<String, Object> role = userMapper.getRoleById(roleId);
            if (role == null) {
                LogUtils.warn("修改用户失败，角色不存在：roleId={}", roleId);
                return SaResult.error("所选角色不存在");
            }
        }

        // 更新用户名
        if (username != null && !username.equals(user.getUsername())) {
            Users existingUser = userMapper.getUserByUsername(username);
            if (existingUser != null) {
                LogUtils.info("修改用户失败，用户名已存在：username={}", username);
                return SaResult.error("用户名已存在");
            }
            user.setUsername(username);
        }
        // 更新邮箱
        if (email != null && !email.equals(user.getEmail())) {
            Users existingEmailUser = userMapper.getUserByEmail(email);
            if (existingEmailUser != null) {
                LogUtils.info("修改用户失败，邮箱已被注册：email={}", email);
                return SaResult.error("邮箱已被注册");
            }
            user.setEmail(email);
        }

        // 更新用户信息
        int result = userMapper.updateById(user);
        if (result > 0) {
            try {
                // 删除现有角色
                userMapper.deleteUserRole(userId);
                // 如果 roleId 不为 null 或 0，分配新角色
                if (roleId != null && roleId != 0) {
                    userMapper.insertUserRole(userId, roleId);
                }
                // 如果用户在线，强制登出
                if (StpUtil.isLogin(userId)) {
                    StpUtil.logout(userId);
                }
                LogUtils.info("管理员修改用户成功：userId={}, roleId={}", userId, roleId);
                return SaResult.ok("修改用户成功");
            } catch (Exception e) {
                LogUtils.error("更新用户角色失败：userId={}, roleId={}, error={}", userId, roleId, e.getMessage(), e);
                throw new RuntimeException("更新用户角色失败", e);
            }
        } else {
            LogUtils.error("修改用户失败，数据库更新失败：userId={}", userId);
            return SaResult.error("修改用户失败");
        }
    }

    /**
     * 删除用户
     */
    @OperationDesc("删除用户")
    @SaCheckLogin
    @SaCheckPermission("user:delete")
    @PostMapping("/delete")
    public SaResult deleteUser(@RequestBody Map<String, Integer> params) {
        Integer userId = params.get("userId");

        if (userId == null) {
            LogUtils.warn("删除用户失败，用户ID为空：params={}", params);
            return SaResult.error("用户ID不能为空");
        }

        Users user = userMapper.selectById(userId);
        if (user == null) {
            LogUtils.warn("删除用户失败，用户不存在：userId={}", userId);
            return SaResult.error("用户不存在");
        }

        //查询冻结的是否是自己
        int loginIdAsInt = StpUtil.getLoginIdAsInt();
        if (user.getId()==loginIdAsInt){
            LogUtils.warn("禁止自己刪除自己 userId={}", userId);
            return SaResult.error("禁止自刪");
        }


        int result = 0;
        try {
            result = userMapper.deleteById(userId);
        } catch (Exception e) {
            return  SaResult.error("禁止删除用户-500");
        }
        if (result > 0) {
            userMapper.deleteUserRole(userId);
            if (StpUtil.isLogin(userId)) {
                StpUtil.logout(userId);
            }
            LogUtils.info("管理员删除用户成功：userId={}", userId);
            return SaResult.ok("删除用户成功");
        } else {
            LogUtils.error("删除用户失败，数据库删除失败：userId={}", userId);
            return SaResult.error("删除用户失败");
        }
    }

    /**
     * 批量删除用户
     */
    @OperationDesc("批量删除用户")
    @SaCheckLogin
    @SaCheckPermission("user:delete")
    @PostMapping("/batchDelete")
    public SaResult batchDelete(@RequestBody List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            LogUtils.warn("批量删除用户失败，用户ID列表为空");
            return SaResult.error("用户ID列表不能为空");
        }

        int successCount = 0;
        for (Integer userId : userIds) {
            Users user = userMapper.selectById(userId);
            if (user == null) continue;
            int result = userMapper.deleteById(userId);
            if (result > 0) {
                userMapper.deleteUserRole(userId);
                if (StpUtil.isLogin(userId)) {
                    StpUtil.logout(userId);
                }
                successCount++;
            }
        }

        LogUtils.info("管理员批量删除用户成功：成功 {} 个，失败 {} 个", successCount, userIds.size() - successCount);
        return SaResult.ok("批量删除成功：" + successCount + " 个，失败：" + (userIds.size() - successCount) + " 个");
    }

    /**
     * 冻结用户
     */
    @OperationDesc("冻结用户")
    @SaCheckLogin
    @SaCheckPermission("user:update")
    @PostMapping("/freeze")
    public SaResult freezeUser(@RequestBody Map<String, Object> params) {

        Integer userId = params.get("userId") != null ? Integer.parseInt(params.get("userId").toString()) : null;
        Integer freezeDays = params.get("freezeDays") != null ? Integer.parseInt(params.get("freezeDays").toString()) : null;
        String reason = (String) params.get("reason");

        if (userId == null) {
            LogUtils.warn("冻结用户失败，用户ID为空：params={}", params);
            return SaResult.error("用户ID不能为空");
        }

        if (freezeDays == null || freezeDays < 0) {
            LogUtils.warn("冻结用户失败，冻结天数无效：freezeDays={}", freezeDays);
            return SaResult.error("冻结天数必须大于等于 0（0 表示永久冻结）");
        }

        Users user = userMapper.selectById(userId);
        if (user == null) {
            LogUtils.warn("冻结用户失败，用户不存在：userId={}", userId);
            return SaResult.error("用户不存在");
        }

        //查询冻结的是否是自己
        int loginIdAsInt = StpUtil.getLoginIdAsInt();
        if (user.getId()==loginIdAsInt){
            LogUtils.warn("禁止凍結自己 userId={}", userId);
            return SaResult.error("禁止凍結自己");
        }


        if (StpUtil.isDisable(userId, "user-freeze")) {
            LogUtils.warn("冻结用户失败，用户已被冻结：userId={}", userId);
            return SaResult.error("用户已被冻结");
        }

        long freezeSeconds = freezeDays == 0 ? -1 : freezeDays * 24 * 60 * 60L;
        StpUtil.disable(userId, "user-freeze", freezeSeconds);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime disableUntil = freezeSeconds == -1 ? null :
                LocalDateTime.ofInstant(Instant.now().plusSeconds(freezeSeconds), ZoneId.systemDefault());
        userMapper.upsertDisableInfo(userId, "user-freeze", reason,
                disableUntil != null ? Timestamp.valueOf(disableUntil) : null,
                Timestamp.valueOf(now), Timestamp.valueOf(now));

        StpUtil.kickout(userId);

        LogUtils.info("管理员冻结用户成功：userId={} freezeDays={} reason={}", userId, freezeDays, reason);
        return SaResult.ok("冻结用户成功");
    }

    /**
     * 批量冻结用户
     */
    @OperationDesc("批量冻结用户")
    @SaCheckLogin
    @SaCheckPermission("user:update")
    @PostMapping("/batchFreeze")
    public SaResult batchFreeze(@RequestBody Map<String, Object> params) {
        List<Integer> userIds = (List<Integer>) params.get("userIds");
        Integer freezeDays = params.get("freezeDays") != null ? Integer.parseInt(params.get("freezeDays").toString()) : null;
        String reason = (String) params.get("reason");

        if (userIds == null || userIds.isEmpty()) {
            LogUtils.warn("批量冻结用户失败，用户ID列表为空");
            return SaResult.error("用户ID列表不能为空");
        }
        if (freezeDays == null || freezeDays < 0) {
            LogUtils.warn("批量冻结用户失败，冻结天数无效：freezeDays={}", freezeDays);
            return SaResult.error("冻结天数必须大于等于 0（0 表示永久冻结）");
        }
        if (reason == null || reason.trim().isEmpty()) {
            LogUtils.warn("批量冻结用户失败，冻结原因为空");
            return SaResult.error("请输入冻结原因");
        }

        int successCount = 0;
        LocalDateTime now = LocalDateTime.now();
        for (Integer userId : userIds) {
            Users user = userMapper.selectById(userId);
            if (user == null || StpUtil.isDisable(userId, "user-freeze")) continue;
            long freezeSeconds = freezeDays == 0 ? -1 : freezeDays * 24 * 60 * 60L;
            StpUtil.disable(userId, "user-freeze", freezeSeconds);
            LocalDateTime disableUntil = freezeSeconds == -1 ? null :
                    LocalDateTime.ofInstant(Instant.now().plusSeconds(freezeSeconds), ZoneId.systemDefault());
            userMapper.upsertDisableInfo(userId, "user-freeze", reason,
                    disableUntil != null ? Timestamp.valueOf(disableUntil) : null,
                    Timestamp.valueOf(now), Timestamp.valueOf(now));
            StpUtil.kickout(userId);
            successCount++;
        }

        LogUtils.info("管理员批量冻结用户成功：成功 {} 个，失败 {} 个", successCount, userIds.size() - successCount);
        return SaResult.ok("批量冻结成功：" + successCount + " 个，失败：" + (userIds.size() - successCount) + " 个");
    }

    /**
     * 解冻用户
     */
    @OperationDesc("解冻用户")
    @SaCheckLogin
    @SaCheckPermission("user:update")
    @PostMapping("/unfreeze")
    public SaResult unfreezeUser(@RequestBody Map<String, Integer> params) {
        Integer userId = params.get("userId");

        if (userId == null) {
            LogUtils.warn("解冻用户失败，用户ID为空：params={}", params);
            return SaResult.error("用户ID不能为空");
        }

        Users user = userMapper.selectById(userId);
        if (user == null) {
            LogUtils.warn("解冻用户失败，用户不存在：userId={}", userId);
            return SaResult.error("用户不存在");
        }

        if (!StpUtil.isDisable(userId, "user-freeze")) {
            LogUtils.warn("解冻用户失败，用户未被冻结：userId={}", userId);
            return SaResult.error("用户未被冻结");
        }

        StpUtil.untieDisable(userId, "user-freeze");
        userMapper.deleteDisableInfo(userId, "user-freeze");

        LogUtils.info("管理员解冻用户成功：userId={}", userId);
        return SaResult.ok("解冻用户成功");
    }

    /**
     * 批量解冻用户
     */
    @OperationDesc("批量解冻用户")
    @SaCheckLogin
    @SaCheckPermission("user:update")
    @PostMapping("/batchUnfreeze")
    public SaResult batchUnfreeze(@RequestBody List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            LogUtils.warn("批量解冻用户失败，用户ID列表为空");
            return SaResult.error("用户ID列表不能为空");
        }

        int successCount = 0;
        for (Integer userId : userIds) {
            Users user = userMapper.selectById(userId);
            if (user == null || !StpUtil.isDisable(userId, "user-freeze")) continue;
            StpUtil.untieDisable(userId, "user-freeze");
            userMapper.deleteDisableInfo(userId, "user-freeze");
            successCount++;
        }

        LogUtils.info("管理员批量解冻用户成功：成功 {} 个，失败 {} 个", successCount, userIds.size() - successCount);
        return SaResult.ok("批量解冻成功：" + successCount + " 个，失败：" + (userIds.size() - successCount) + " 个");
    }
}