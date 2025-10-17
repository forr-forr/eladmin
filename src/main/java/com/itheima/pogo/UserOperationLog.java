package com.itheima.pogo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户操作日志实体类
 */
@Data
@TableName("user_operation_logs")  // 对应数据库中的表名
public class UserOperationLog {

    /**
     * 记录的唯一标识，主键自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户的ID，可为空（如注册时无用户ID）
     */
    @TableField("user_id")
    private Long userId;  // 改为 Long，与 BIGINT 匹配

    /**
     * 分类：user（用户操作）、wallet（钱包操作）、remark（备注操作）
     */
    @TableField("category")
    private String category;  // 与表字段名一致

    /**
     * 具体操作类型，如doLogin、transferUSDT、addRemark等
     */
    @TableField("method_name")
    private String methodName;  // 与表字段名一致

    /**
     * 操作描述
     */
    @TableField("operation_desc")
    private String operationDesc;  // 新增，与表字段一致

    /**
     * 请求路径
     */
    @TableField("request_path")
    private String requestPath;  // 新增，与表字段一致

    /**
     * 请求方式
     */
    @TableField("request_method")
    private String requestMethod;  // 新增，与表字段一致

    /**
     * 客户端信息
     */
    @TableField("client_info")
    private String clientInfo;  // 新增，与表字段一致

    /**
     * 操作发生的时间
     */
    @TableField("request_time")
    private LocalDateTime requestTime;  // 与表字段名一致

    /**
     * 客户端IP地址，支持IPv4和IPv6
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 操作详情，文本格式存储参数，如{"username":"user1"}或{"sender":"T123","amount":50.5}
     */
    @TableField("operation_params")
    private String operationParams;  // 与表字段名一致

    /**
     * 操作状态：1表示成功，0表示失败
     */
    @TableField("status_code")
    private Integer statusCode;  // 与表字段名一致，改为 Integer 支持 null

    /**
     * 操作结果或错误信息，如"登录成功"或"USDT不足"
     */
    @TableField("result_message")
    private String resultMessage;

    /**
     * 执行耗时（毫秒）
     */
    @TableField("execution_time_ms")
    private Long executionTimeMs;  // 新增，与表字段一致

    /**
     * 记录创建时间，自动生成
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 与表字段名一致

    // 添加 username 字段，标记为非数据库字段
    @TableField(exist = false)
    private String username;
}