package com.itheima.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.util.SaResult;
import com.itheima.controller.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LogUtils = LoggerFactory.getLogger(UserController.class);

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<SaResult> handlerNotLoginException(NotLoginException nle) {

        String message;
        if (nle.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = "未登录";
        } else if (nle.getType().equals(NotLoginException.INVALID_TOKEN)) {
            message = "未登录";
        } else if (nle.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = "token 已过期,请重新登录";
        } else if (nle.getType().equals(NotLoginException.BE_REPLACED)) {
            message = "token 已被顶下线,请重新登录";
        } else if (nle.getType().equals(NotLoginException.KICK_OUT)) {
            message = "token 已被踢下线";
        } else if (nle.getType().equals(NotLoginException.TOKEN_FREEZE)) {
            message = "token 已被冻结";
        } else if (nle.getType().equals(NotLoginException.NO_PREFIX)) {
            message = "未按照指定前缀提交 token";
        } else {
            message = "当前会话未登录";
        }
        // 设置 HTTP 状态码 401（未授权）
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(SaResult.code(401).setMsg(message));
    }

    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<SaResult> handlerNotPermissionException(NotPermissionException npe) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(SaResult.code(403).setMsg("权限不足"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SaResult> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));

        LogUtils.warn("参数验证失败 | IP={} | URI={} | {}", ip, uri, errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(SaResult.error("参数验证失败: " + errors));
    }

    // --- 兜底异常 ---
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<SaResult> handleOtherExceptions(Exception ex) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(SaResult.error("系统异常: " + ex.getMessage()));
//    }

}
