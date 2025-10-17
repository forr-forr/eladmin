package com.itheima.config.Operation;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.mapper.log.UserOperationLogMapper;
import com.itheima.pogo.UserOperationLog;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserOperationLogMapper userOperationLogMapper;

    private ThreadLocal<Instant> startTime = new ThreadLocal<>();

    @Before("execution(* com.itheima.controller..*.*(..))")
    public void beforeMethod(JoinPoint joinPoint) {
        startTime.set(Instant.now());
    }

    @AfterReturning(pointcut = "execution(* com.itheima.controller..*.*(..))", returning = "result")
    public void logOperation(JoinPoint joinPoint, Object result) {

        String methodName = joinPoint.getSignature().getName();

        // 检查方法上是否有 @NotLog 注解
        if (isNotLogMethod(joinPoint)) {
            return; // 跳过日志记录
        }

        // 现有跳过逻辑（可选保留）
        if ("getDashboardData".equals(methodName) || "getInfo".equals(methodName)) {
            return; // 跳过 getDashboardData 和 getInfo
        }

        Object[] args = joinPoint.getArgs();
        String operationDesc = getOperationDescription(joinPoint);
        String ipAddress = getIpAddress();
        String category = determineCategory(methodName);
        Long userId = extractUserId(args, result);
        String operationParams = buildOperationDetail(args);

        Integer status = 1;
        String resultMessage = "操作成功";
        if (result instanceof SaResult) {
            SaResult saResult = (SaResult) result;
            status = saResult.getCode();
            resultMessage = saResult.getMsg();
            if (resultMessage != null && resultMessage.length() > 1000) {
                resultMessage = resultMessage.substring(0, 500) + "...";
            }
        }

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs != null ? attrs.getRequest() : null;

        Instant endTime = Instant.now();
        Long executionTimeMs = startTime.get() != null ? Duration.between(startTime.get(), endTime).toMillis() : -1L;
        startTime.remove();

        LocalDateTime requestTime = LocalDateTime.now();

        UserOperationLog log = new UserOperationLog();
        log.setUserId(userId);
        log.setCategory(category);
        log.setMethodName(methodName);
        log.setOperationDesc(operationDesc != null ? operationDesc : "");
        log.setRequestPath(request != null ? request.getRequestURI() : "");
        log.setRequestMethod(request != null ? request.getMethod() : "");
        log.setClientInfo(request != null ? request.getHeader("User-Agent") : "");
        log.setRequestTime(requestTime);
        log.setIpAddress(ipAddress);
        log.setOperationParams(operationParams);
        log.setStatusCode(status);
        log.setResultMessage(resultMessage);
        log.setExecutionTimeMs(executionTimeMs);

        userOperationLogMapper.insert(log);

        StringBuilder logBuilder = new StringBuilder("\n==== 操作日志 ====\n");
        logBuilder.append("用户ID: ").append(userId).append("\n");
        logBuilder.append("分类: ").append(category).append("\n");
        logBuilder.append("操作方法: ").append(methodName).append("\n");
        logBuilder.append("操作描述: ").append(operationDesc != null ? operationDesc : "").append("\n");
        if (request != null) {
            logBuilder.append("请求路径: ").append(request.getRequestURI()).append("\n");
            logBuilder.append("请求方式: ").append(request.getMethod()).append("\n");
            logBuilder.append("客户端信息: ").append(request.getHeader("User-Agent")).append("\n");
        }
        logBuilder.append("请求时间: ").append(requestTime).append("\n");
        logBuilder.append("IP地址: ").append(ipAddress).append("\n");
        logBuilder.append("操作参数: ").append(operationParams).append("\n");
        logBuilder.append("状态码: ").append(status).append("\n");
        logBuilder.append("结果信息: ").append(resultMessage).append("\n");
        logBuilder.append("执行耗时: ").append(executionTimeMs).append("ms\n");
        System.out.println(logBuilder.toString());
    }

    // 新增方法：检查方法上是否有 @NotLog 注解
    private boolean isNotLogMethod(JoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            Object target = joinPoint.getTarget();
            Class<?> targetClass = target.getClass();
            for (Method method : targetClass.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return method.isAnnotationPresent(NotLog.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getIpAddress() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            String ip = req.getHeader("X-Forwarded-For");
            if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = req.getRemoteAddr();
            }
            if ("0:0:0:0:0:0:0:1".equals(ip)) {
                ip = "127.0.0.1";
            }
            return ip;
        }
        return "unknown";
    }

    private Long extractUserId(Object[] args, Object result) {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception ignored) {}

        if (result instanceof SaResult) {
            SaResult saResult = (SaResult) result;
            Object userIdFromResult = saResult.get("userId");
            if (userIdFromResult instanceof Number) {
                return ((Number) userIdFromResult).longValue();
            } else if (userIdFromResult instanceof String) {
                try {
                    return Long.parseLong((String) userIdFromResult);
                } catch (NumberFormatException ignored) {}
            }
        }

        for (Object arg : args) {
            if (arg == null) continue;
            if (arg instanceof Integer) return ((Integer) arg).longValue();
            if (arg instanceof Long) return (Long) arg;
            if (arg instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) arg;
                Object userId = map.get("userId");
                if (userId instanceof Number) return ((Number) userId).longValue();
                else if (userId instanceof String) {
                    try {
                        return Long.parseLong((String) userId);
                    } catch (NumberFormatException ignored) {}
                }
            }
            try {
                java.lang.reflect.Field field = arg.getClass().getDeclaredField("userId");
                field.setAccessible(true);
                Object userId = field.get(arg);
                if (userId instanceof Number) return ((Number) userId).longValue();
                else if (userId instanceof String) return Long.parseLong((String) userId);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String buildOperationDetail(Object[] args) {
        try {
            if (args.length == 0) return "{}";
            for (Object arg : args) {
                if (arg instanceof Map) {
                    Map<?, ?> params = (Map<?, ?>) arg;
                    Map<String, Object> safeParams = new HashMap<>();
                    for (Map.Entry<?, ?> entry : params.entrySet()) {
                        String key = String.valueOf(entry.getKey());
                        Object value = entry.getValue();
                        if (key.equalsIgnoreCase("password") || key.toLowerCase().contains("key") || key.equalsIgnoreCase("googleCode")) {
                            safeParams.put(key, "[masked]");
                        } else {
                            safeParams.put(key, value);
                        }
                    }
                    return objectMapper.writeValueAsString(safeParams);
                }
            }
            return objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            return "参数序列化失败: " + e.getMessage();
        }
    }

    private String determineCategory(String methodName) {
        if (methodName.contains("getWallets") || methodName.contains("deleteWallet") ||
                methodName.contains("addWallet") || methodName.contains("refresh") ||
                methodName.contains("transfer") || methodName.contains("check") ||
                methodName.contains("Importwalle") || methodName.contains("dashboard")) {
            return "wallet";
        } else if (methodName.contains("Login") || methodName.contains("Register") ||
                methodName.contains("getinfo") || methodName.contains("logout") ||
                methodName.contains("BindingToken")) {
            return "user";
        }
        return "other";
    }

    private String getOperationDescription(JoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            Object target = joinPoint.getTarget();
            Class<?> targetClass = target.getClass();
            for (Method method : targetClass.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    OperationDesc annotation = method.getAnnotation(OperationDesc.class);
                    if (annotation != null) return annotation.value();
                    return "";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}