package com.itheima.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.util.SaResult;
import com.itheima.config.Operation.OperationDesc;
import com.itheima.pogo.Project;
import com.itheima.serice.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 项目管理控制器
 * 负责处理与项目相关的 HTTP 请求
 */
@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * 创建一个新项目
     * 权限：project.add
     */
    @OperationDesc("创建项目")
    @SaCheckLogin
    @SaCheckPermission("project.add")
    @PostMapping("/create")
    public SaResult createProject(@RequestBody Project project) {
        return projectService.createProject(project);
    }

    /**
     * 获取当前用户的所有项目列表
     * 权限：project.read
     */
    @OperationDesc("获取项目列表")
    @SaCheckLogin
    @SaCheckPermission("project.read")
    @GetMapping("/list")
    public SaResult getProjectsByCurrentUser() {
        return projectService.getProjectsByCurrentUser();
    }
    
    /**
     * 根据ID获取单个项目详情
     * 权限：project.read
     */
    @OperationDesc("获取单个项目详情")
    @SaCheckLogin
    @SaCheckPermission("project.read")
    @GetMapping("/{id}")
    public SaResult getProjectById(@PathVariable("id") Long id) {
        return projectService.getProjectById(id);
    }

    /**
     * 更新一个现有项目
     * 权限：project.update
     */
    @OperationDesc("更新项目")
    @SaCheckLogin
    @SaCheckPermission("project.update")
    @PostMapping("/update")
    public SaResult updateProject(@RequestBody Project project) {
        return projectService.updateProject(project);
    }

    /**
     * 根据ID删除单个项目
     * 权限：project.delete
     */
    @OperationDesc("删除单个项目")
    @SaCheckLogin
    @SaCheckPermission("project.delete")
    @PostMapping("/delete")
    public SaResult deleteProjectById(@RequestBody Long id) {
        return projectService.deleteProjectById(id);
    }

    /**
     * 批量删除多个项目
     * 权限：project.delete
     */
    @OperationDesc("批量删除项目")
    @SaCheckLogin
    @SaCheckPermission("project.delete")
    @PostMapping("/batchDelete")
    public SaResult batchDeleteProjects(@RequestBody List<Long> ids) {
        return projectService.batchDeleteProjects(ids);
    }
}