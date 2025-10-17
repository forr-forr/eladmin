package com.itheima.controller;

import cn.dev33.satoken.annotation.*;
import cn.dev33.satoken.exception.DisableServiceException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.itheima.config.Operation.OperationDesc;
import com.itheima.mapper.user.UserMapper;
import com.itheima.pogo.Users;
import com.itheima.utlis.AESUtil;
import com.itheima.utlis.GoogleAuthe.GoogleAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/user/")
public class UserController {

    private static final Logger LogUtils = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserMapper userMapper;
    @Autowired
    HttpServletRequest httpServletRequest;

    @OperationDesc("用户登录")
    @RequestMapping(value = "doLogin", method = RequestMethod.POST)
    public SaResult doLogin(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        Users user = userMapper.getUserByUsername(username);
        if (user == null) {
            LogUtils.info("登录失败，用户不存在: username={}", username);
            return SaResult.error("登录失败，用户不存在");
        }

        String clientIp = httpServletRequest.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(clientIp)) {
            clientIp = "127.0.0.1";
        }

        try {
            String decryptedPassword = AESUtil.decrypt(user.getPassword());
            if (!password.equals(decryptedPassword)) {
                LogUtils.info("登录失败，账号或密码错误: username={}", username);
                return SaResult.error("登录失败, 账号或密码错误");
            }
        } catch (Exception e) {
            LogUtils.error("登录失败，密码解密异常: username={}, error={}", username, e.getMessage(), e);
            return SaResult.error("登录失败，请稍后重试");
        }

        // 检查封禁状态
        try {
            StpUtil.checkDisable(user.getId(), "user-freeze");
        } catch (DisableServiceException e) {
            // 从 user_disable_logs 表中查询封禁原因
            String reason = userMapper.getDisableReason(user.getId(), "user-freeze");
            if (reason == null || reason.trim().isEmpty()) {
                reason = "原因未知";
            }
            LogUtils.info("登录失败，用户已被封禁: userId={}, username={}, reason={}", user.getId(), username, reason);
            return SaResult.error("账号已被封禁，原因：" + reason + "，请联系管理员");
        } catch (Exception e) {
            LogUtils.error("登录失败，无法访问封禁信息（Redis 不可用）：userId={}, username={}, error={}", user.getId(), username, e.getMessage());
            return SaResult.error("系统维护中，请稍后再试");
        }

        StpUtil.login(user.getId());
        user.setLastLoginAt(new Date());
        user.setLastLoginIp(clientIp);
        userMapper.update(user, new UpdateWrapper<Users>().eq("id", user.getId()));
        LogUtils.info("用户登录成功: userId={} userName={} ip={}", user.getId(), user.getUsername(), clientIp);

        return SaResult.ok("登录成功");
    }

    @OperationDesc("用户注册")
    @RequestMapping(value = "doRegister", method = RequestMethod.POST)
    public SaResult doRegister(@Valid @RequestBody Users newUser) {
        String username = newUser.getUsername();
        String email = newUser.getEmail();
        String password = newUser.getPassword();
        String confirmPassword = newUser.getConfirmPassword();

        if (!password.equals(confirmPassword)) {
            LogUtils.warn("注册失败，密码和确认密码不匹配: username={}", username);
            return SaResult.error("密码和确认密码不匹配");
        }
        if (username.length() < 3 || username.length() > 20) {
            LogUtils.warn("注册失败，用户名长度不符合要求: username={}", username);
            return SaResult.error("用户名长度应在 3 到 20 个字符之间");
        }
        if (email.length() > 50) {
            LogUtils.warn("注册失败，邮箱长度超出限制: email={}", email);
            return SaResult.error("邮箱长度不能超过 50 个字符");
        }
        if (password.length() < 6 || password.length() > 20) {
            LogUtils.warn("注册失败，密码长度不符合要求: username={}", username);
            return SaResult.error("密码长度应在 6 到 20 个字符之间");
        }
        if (confirmPassword.length() < 6 || confirmPassword.length() > 20) {
            LogUtils.warn("注册失败，确认密码长度不符合要求: username={}", username);
            return SaResult.error("确认密码长度应在 6 到 20 个字符之间");
        }

        Users existingUser = userMapper.getUserByUsername(username);
        if (existingUser != null) {
            LogUtils.info("注册失败，用户名已存在: username={}", username);
            return SaResult.error("用户名已存在");
        }

        Users existingEmailUser = userMapper.getUserByEmail(email);
        if (existingEmailUser != null) {
            LogUtils.info("注册失败，邮箱已被注册: email={}", email);
            return SaResult.error("邮箱已被注册");
        }

        try {
            String clientIp = httpServletRequest.getRemoteAddr();
            if ("0:0:0:0:0:0:0:1".equals(clientIp)) {
                clientIp = "127.0.0.1";
            }

            String encryptedPassword = AESUtil.encrypt(password);

            newUser.setEmail(email);
            newUser.setPassword(encryptedPassword);
            newUser.setRegistrationIp(clientIp);

            int result = userMapper.insert(newUser);
            if (result > 0) {
                int userId = newUser.getId();
                int roleId = 3;
                userMapper.insertUserRole(userId, roleId);
                LogUtils.info("用户注册成功: userId={} userName={}", userId, username);
                // 返回结果中携带 userId
                return SaResult.ok("注册成功").set("userId", userId);
            } else {
                LogUtils.error("注册失败，数据库插入失败: username={}", username);
                return SaResult.error("注册失败");
            }
        } catch (Exception e) {
            LogUtils.error("注册失败，发生异常: username={}, error={}", username, e.getMessage(), e);
            return SaResult.error("注册失败 请稍后再试");
        }
    }

    @OperationDesc("获取用户信息")
    @SaCheckLogin
    @RequestMapping("getinfo")
    public SaResult getInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        Users user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword("*********");
            Optional.ofNullable(user.getGoogleSecretKey()).ifPresent(k -> user.setGoogleSecretKey("*********"));
        }
        if (user != null) {
            List<String> roleList = StpUtil.getRoleList();
            List<String> permissionList = StpUtil.getPermissionList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("user", user);
            userInfo.put("roles", roleList);
            userInfo.put("permissions", permissionList);
            LogUtils.info("获取用户信息成功: userId={} userName={}", user.getId(), user.getUsername());
            return SaResult.ok("获取成功").setData(userInfo);
        } else {
            LogUtils.warn("用户信息不存在: userId={}", userId);
            return SaResult.error("用户信息不存在");
        }
    }

    @OperationDesc("退出登录")
    @GetMapping("/logout")
    public SaResult logout() {
        try {
            Long userId = StpUtil.getLoginIdAsLong(); // 获取当前登录用户ID
            StpUtil.logout(); // 执行退出登录
            return SaResult.ok("退出登录成功").set("userId", userId); // 返回成功响应，携带 userId
        } catch (NotLoginException e) {
            return SaResult.error("未登录，无法退出").setCode(401); // 未登录时的错误响应
        } catch (Exception e) {
            return SaResult.error("退出登录失败，请稍后再试"); // 其他异常处理
        }
    }

    @OperationDesc("获取谷歌令牌")
    @RequestMapping("getBindingToken")
    public SaResult secureData() {
        Long userId = StpUtil.getLoginIdAsLong();
        Users user = userMapper.selectById(userId);

        if (StringUtils.isBlank(user.getGoogleSecretKey())) {
            String secretKey = GoogleAuthenticator.getSecretKey();
            String qrCodeText = GoogleAuthenticator.getQrCodeText(secretKey, user.getUsername()+"【】", null);
            Map<String, String> data = new HashMap<>();
            data.put("secretKey", secretKey);
            data.put("qrCodeText", qrCodeText);
            LogUtils.info("生成谷歌令牌成功: userId={}", userId);
            return SaResult.ok().setData(data);
        } else {
            LogUtils.warn("用户已绑定谷歌令牌，无法重复申请: userId={}", userId);
            return SaResult.error("已绑定,请勿重复申请");
        }
    }

    @OperationDesc("绑定谷歌令牌")
    @RequestMapping(value = "verifyAndBindToken", method = RequestMethod.POST)
    public SaResult verifyAndBindToken(@RequestBody Map<String, Object> params) {
        String secretKey = (String) params.get("secretKey");
        Object codeObj = params.get("code");

        if (StringUtils.isBlank(secretKey) || codeObj == null) {
            LogUtils.warn("绑定谷歌令牌失败，参数错误: params={}", params);
            return SaResult.error("参数错误，请检查后重试");
        }

        int code;
        try {
            if (codeObj instanceof Integer) {
                code = (Integer) codeObj;
            } else if (codeObj instanceof String) {
                code = Integer.parseInt((String) codeObj);
            } else {
                LogUtils.warn("绑定谷歌令牌失败，验证码格式错误: code={}", codeObj);
                return SaResult.error("验证码格式错误");
            }
        } catch (NumberFormatException e) {
            LogUtils.warn("绑定谷歌令牌失败，验证码格式错误: code={}", codeObj);
            return SaResult.error("验证码格式错误，请输入正确的数字");
        }

        if (!isValidBase32(secretKey)) {
            LogUtils.warn("绑定谷歌令牌失败，无效的谷歌验证密钥: secretKey={}", secretKey);
            return SaResult.error("无效的谷歌验证密钥");
        }

        LogUtils.info("接收到的谷歌密钥: secretKey={}", secretKey);
        LogUtils.info("接收到的验证码: code={}", code);

        Long userId = StpUtil.getLoginIdAsLong();
        Users user = userMapper.selectById(userId);
        if (StringUtils.isNotBlank(user.getGoogleSecretKey())) {
            LogUtils.warn("用户已绑定谷歌令牌，无法重复操作: userId={}", userId);
            return SaResult.error("用户已绑定，请勿重复操作");
        }

        boolean isValid = GoogleAuthenticator.checkCode(secretKey, code, System.currentTimeMillis());
        if (isValid) {
            user.setGoogleSecretKey(secretKey);
            userMapper.updateById(user);
            LogUtils.info("谷歌令牌绑定成功: userId={}", userId);
            return SaResult.ok("谷歌令牌绑定成功");
        } else {
            LogUtils.warn("绑定谷歌令牌失败，验证码错误: userId={}", userId);
            return SaResult.error("验证码错误，请重新输入");
        }
    }

    private boolean isValidBase32(String secretKey) {
        try {
            java.util.Base64.getDecoder().decode(secretKey);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}