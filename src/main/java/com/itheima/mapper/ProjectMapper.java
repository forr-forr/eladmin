package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pogo.Project;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目表的数据访问接口 (Mapper)
 * 继承 BaseMapper 即可获得对 projects 表的基础操作
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
    // 继承 BaseMapper<Project> 后，以下方法会自动生成：
    // insert(Project entity)
    // deleteById(Serializable id)
    // updateById(Project entity)
    // selectById(Serializable id)
    // selectList(Wrapper<T> queryWrapper)
    // 等等...
    // 你可以在这里定义自己需要的复杂查询方法，例如：
    // List<Project> selectProjectsByUserId(@Param("userId") Integer userId);
}