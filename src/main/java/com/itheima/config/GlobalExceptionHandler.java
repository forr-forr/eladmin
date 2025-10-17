package com.itheima.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.util.SaResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
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

}
