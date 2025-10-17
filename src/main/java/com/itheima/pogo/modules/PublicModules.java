package com.itheima.pogo.modules;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("public_modules") // 映射数据库表名
public class PublicModules {

    @TableId(type = IdType.AUTO) // 设置主键，并使用数据库自增策略
    private Integer id;

    @TableField("module_name") // 映射数据库字段名
    private String moduleName;

    private String remark;

    @TableField("module_content")
    private String moduleContent;

    private String creator;

    @TableField("is_public")
    private Integer isPublic; // 0: 公开, 1: 私有

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    // 以下是完整的 getter 和 setter 方法，以防您的项目未使用 Lombok
    // 您可以根据需要选择保留或删除

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getModuleContent() {
        return moduleContent;
    }

    public void setModuleContent(String moduleContent) {
        this.moduleContent = moduleContent;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Integer getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Integer isPublic) {
        this.isPublic = isPublic;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}