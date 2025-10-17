package com.itheima.serice;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.dev33.satoken.util.SaResult;
import com.itheima.pogo.Project;

import java.util.List;

/**
 * 项目服务接口
 * 继承 IService 即可拥有 Mybatis-Plus 提供的基础 CRUD 功能
 */
public interface ProjectService extends IService<Project> {

    /**
     * 创建一个新项目
     *
     * @param project 包含项目名称等信息的项目实体
     * @return 操作结果
     */
    SaResult createProject(Project project);

    /**
     * 根据项目ID获取单个项目详情
     *
     * @param projectId 项目ID
     * @return 包含项目数据的操作结果
     */
    SaResult getProjectById(Long projectId);

    /**
     * 获取当前用户的所有项目列表
     *
     * @return 包含项目列表数据的操作结果
     */
    SaResult getProjectsByCurrentUser();

    /**
     * 更新一个现有项目
     *
     * @param project 包含要更新的字段的项目实体
     * @return 操作结果
     */
    SaResult updateProject(Project project);

    /**
     * 根据项目ID删除项目
     *
     * @param projectId 要删除的项目ID
     * @return 操作结果
     */
    SaResult deleteProjectById(Long projectId);

    /**
     * 批量删除多个项目
     *
     * @param projectIds 包含要删除的项目ID列表
     * @return 操作结果
     */
    SaResult batchDeleteProjects(List<Long> projectIds);
}