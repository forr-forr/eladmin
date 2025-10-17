package com.itheima.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itheima.config.Operation.NotLog;
import com.itheima.mapper.log.UserOperationLogService;
import com.itheima.pogo.UserOperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/operation-logs")
public class UserOperationLogController {

    @Autowired
    private UserOperationLogService userOperationLogService;

    @SaCheckLogin
    @SaCheckPermission("log:query")
    @GetMapping("/list")
    @NotLog
    public SaResult listLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String category) {

        List<String> roles = StpUtil.getRoleList();
        if (!roles.contains("root") && !roles.contains("admin")) {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            if (userId != null && !userId.equals(currentUserId)) {
                return SaResult.error("无权限查看其他用户的日志");
            }
            userId = currentUserId;
            username = null;
        }

        IPage<UserOperationLog> logPage = userOperationLogService.getLogsPage(page, size, userId, username, category);

        Map<String, Object> result = new HashMap<>();
        result.put("data", logPage.getRecords());
        result.put("total", logPage.getTotal());

        return SaResult.ok().setData(result);
    }
}