package com.itheima.serice.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.itheima.mapper.ProjectMapper;
import com.itheima.pogo.Project;
import com.itheima.serice.ProjectService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 项目服务实现类，实现项目相关的所有业务逻辑
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    private static final Pattern PATH_PATTERN = Pattern.compile("^[a-zA-Z0-9]{1,5}$");

    /**
     * 创建一个新项目
     *
     * @param project 包含项目名称、路径等信息的项目实体
     * @return 操作结果
     */
    @Override
    public SaResult createProject(Project project) {
        if (project.getName() == null || project.getName().isEmpty()) {
            return SaResult.error("项目名称不能为空");
        }
        if (project.getPath() == null || project.getPath().isEmpty()) {
            return SaResult.error("项目路径不能为空");
        }

        // 验证路径格式
        if (!PATH_PATTERN.matcher(project.getPath()).matches()) {
            return SaResult.error("项目路径必须由1-5位小写字母、大写字母或数字组成");
        }

        // 检查项目名称是否已存在（针对当前用户）
        QueryWrapper<Project> nameQueryWrapper = new QueryWrapper<>();
        nameQueryWrapper.eq("name", project.getName()).eq("user_id", StpUtil.getLoginIdAsInt());
        if (this.count(nameQueryWrapper) > 0) {
            return SaResult.error("项目名称已存在");
        }

        // 检查项目路径是否已存在（全局唯一）
        QueryWrapper<Project> pathQueryWrapper = new QueryWrapper<>();
        pathQueryWrapper.eq("path", project.getPath());
        if (this.count(pathQueryWrapper) > 0) {
            return SaResult.error("此路径不可用");
        }

        // 从 Sa-Token 获取当前登录用户ID
        project.setUserId(StpUtil.getLoginIdAsInt());

        // 手动设置 created_at，以兼容旧版MySQL
        project.setCreatedAt(new Date());

        // 使用 ServiceImpl 的 save 方法进行插入
        boolean success = this.save(project);
        if (success) {
            return SaResult.ok("项目创建成功").set("path", project.getPath());
        }
        return SaResult.error("项目创建失败");
    }

    // ... 其他服务方法保持不变
    @Override
    public SaResult getProjectById(Long projectId) {
        Project project = this.getById(projectId);
        if (project == null) {
            return SaResult.error("项目不存在");
        }
        if (!project.getUserId().equals(StpUtil.getLoginIdAsInt())) {
            return SaResult.error("无权访问该项目");
        }
        return SaResult.ok("获取成功").set("data", project);
    }

    @Override
    public SaResult getProjectsByCurrentUser() {
        QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", StpUtil.getLoginIdAsInt());
        List<Project> projectList = this.list(queryWrapper);
        return SaResult.ok("获取成功").set("data", projectList);
    }

    @Override
    public SaResult updateProject(Project project) {
        if (project.getId() == null) {
            return SaResult.error("项目ID不能为空");
        }

        Project existingProject = this.getById(project.getId());
        if (existingProject == null || !existingProject.getUserId().equals(StpUtil.getLoginIdAsInt())) {
            return SaResult.error("无权修改该项目");
        }

        // 业务逻辑：不允许修改 path 和 user_id
        project.setPath(null);
        project.setUserId(null);

        boolean success = this.updateById(project);
        if (success) {
            return SaResult.ok("项目更新成功");
        }
        return SaResult.error("项目更新失败");
    }

    @Override
    public SaResult deleteProjectById(Long projectId) {
        Project project = this.getById(projectId);
        if (project == null) {
            return SaResult.error("项目不存在");
        }
        if (!project.getUserId().equals(StpUtil.getLoginIdAsInt())) {
            return SaResult.error("无权删除该项目");
        }

        boolean success = this.removeById(projectId);
        if (success) {
            return SaResult.ok("项目删除成功");
        }
        return SaResult.error("项目删除失败");
    }

    @Override
    public SaResult batchDeleteProjects(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return SaResult.error("删除列表不能为空");
        }

        for (Long projectId : projectIds) {
            Project project = this.getById(projectId);
            if (project == null || !project.getUserId().equals(StpUtil.getLoginIdAsInt())) {
                return SaResult.error("无权删除列表中的部分项目");
            }
        }

        boolean success = this.removeByIds(projectIds);
        if (success) {
            return SaResult.ok("项目批量删除成功");
        }
        return SaResult.error("项目批量删除失败");
    }
}