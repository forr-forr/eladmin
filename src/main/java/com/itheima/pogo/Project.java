package com.itheima.pogo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * XSS项目实体类，对应数据库中的 `projects` 表
 */
@Data
@TableName("projects")
public class Project {

    /**
     * 项目的唯一主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 自动生成的唯一短链标识符
     */
    @TableField("path")
    private String path;

    /**
     * 创建该项目的用户ID，关联users表
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 项目创建的时间
     * 注意：由于旧版MySQL限制，该字段需要由应用层手动设置
     */
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 项目最后更新的时间
     * 数据库会自动在更新时填充此字段
     */
    @TableField(value = "updated_at", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}