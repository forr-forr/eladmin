package com.itheima.controller.modules;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.config.Operation.OperationDesc;
import com.itheima.mapper.modules.PublicModulesMapper;
import com.itheima.mapper.user.UserMapper;
import com.itheima.pogo.Users;
import com.itheima.pogo.modules.PublicModules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/modules/")
public class PublicModulesController {

    private static final Logger LogUtils = LoggerFactory.getLogger(PublicModulesController.class);

    @Autowired
    private PublicModulesMapper publicModulesMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 分页查询所有公共模块
     * 只有拥有 "modules" 权限的用户可以访问
     */
    @OperationDesc("查询公共模块列表")
    @SaCheckPermission("modules")
    @GetMapping("list")
    public SaResult getModules(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {

        try {
            IPage<PublicModules> modulePage = new Page<>(page, size);
            QueryWrapper<PublicModules> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("create_time");

            publicModulesMapper.selectPage(modulePage, queryWrapper);

            LogUtils.info("公共模块列表查询成功, 页码: {}, 每页大小: {}", page, size);
            return SaResult.ok("查询成功").setData(modulePage);
        } catch (Exception e) {
            LogUtils.error("公共模块列表查询失败: {}", e.getMessage(), e);
            return SaResult.error("查询失败，请稍后重试");
        }
    }

    /**
     * 根据ID查询单个公共模块
     */
    @OperationDesc("查询单个公共模块")
    @SaCheckPermission("modules")
    @GetMapping("{id}")
    public SaResult getModuleById(@PathVariable Integer id) {
        try {
            PublicModules module = publicModulesMapper.selectById(id);
            if (module != null) {
                LogUtils.info("查询单个公共模块成功, ID: {}", id);
                return SaResult.ok("查询成功").setData(module);
            } else {
                LogUtils.warn("查询公共模块失败，模块ID不存在: {}", id);
                return SaResult.error("模块不存在");
            }
        } catch (Exception e) {
            LogUtils.error("查询单个公共模块失败: {}", e.getMessage(), e);
            return SaResult.error("查询失败，请稍后重试");
        }
    }

    /**
     * 新增公共模块
     * 1. 必须保证登录
     * 2. 必须有 "modules.add" 权限
     * 3. 模块添加者从 Sa-Token 自动获取
     */
    @OperationDesc("新增公共模块")
    @SaCheckPermission("modules.add")
    @PostMapping("add")
    public SaResult addModule(@RequestBody PublicModules module) {
        try {
            if (module.getModuleName() == null || module.getModuleContent() == null) {
                return SaResult.error("模块名称和模块内容不能为空");
            }

            // 从 Sa-Token 获取当前登录用户的ID
            Long userId = StpUtil.getLoginIdAsLong();

            // 根据用户ID查询用户信息，获取用户名
            Users user = userMapper.selectById(userId);
            if (user == null) {
                LogUtils.error("新增公共模块失败，无法获取当前登录用户信息，用户ID: {}", userId);
                return SaResult.error("新增失败，用户信息异常");
            }

            // 将当前登录用户的用户名设置为模块的创建者
            module.setCreator(user.getUsername());

            int result = publicModulesMapper.insert(module);

            if (result > 0) {
                LogUtils.info("新增公共模块成功: {}", module.getModuleName());
                return SaResult.ok("新增成功").setData(module.getId());
            } else {
                LogUtils.error("新增公共模块失败，数据库插入失败: {}", module.getModuleName());
                return SaResult.error("新增失败");
            }
        } catch (Exception e) {
            LogUtils.error("新增公共模块失败: {}", e.getMessage(), e);
            return SaResult.error("新增失败，请稍后重试");
        }
    }

    /**
     * 更新公共模块
     * 需要 "modules.update" 权限
     */
    @OperationDesc("更新公共模块")
    @SaCheckPermission("modules.update")
    @PostMapping("update")
    public SaResult updateModule(@RequestBody PublicModules module) {
        try {
            if (module.getId() == null) {
                return SaResult.error("模块ID不能为空");
            }
            // 确保创建者字段不被更新
            module.setCreator(null);
            int result = publicModulesMapper.updateById(module);
            if (result > 0) {
                LogUtils.info("更新公共模块成功, ID: {}", module.getId());
                return SaResult.ok("更新成功");
            } else {
                LogUtils.warn("更新公共模块失败，模块不存在或数据未改变, ID: {}", module.getId());
                return SaResult.error("更新失败，模块不存在或数据未改变");
            }
        } catch (Exception e) {
            LogUtils.error("更新公共模块失败, ID: {}: {}", module.getId(), e.getMessage(), e);
            return SaResult.error("更新失败，请稍后重试");
        }
    }

    /**
     * 删除公共模块
     * 支持批量删除，需要 "modules.delete" 权限
     */
    @OperationDesc("删除公共模块")
    @SaCheckPermission("modules.delete")
    @PostMapping("delete")
    public SaResult deleteModules(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return SaResult.error("删除失败，ID列表不能为空");
        }
        try {
            int result = publicModulesMapper.deleteBatchIds(ids);
            LogUtils.info("删除公共模块成功，共删除 {} 条记录", result);
            return SaResult.ok("成功删除 " + result + " 条记录");
        } catch (Exception e) {
            LogUtils.error("删除公共模块失败: {}", e.getMessage(), e);
            return SaResult.error("删除失败，请稍后重试");
        }
    }
}