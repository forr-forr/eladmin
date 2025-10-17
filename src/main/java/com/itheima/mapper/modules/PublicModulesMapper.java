package com.itheima.mapper.modules;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pogo.modules.PublicModules;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * PublicModules 的 Mapper 接口
 * 继承 BaseMapper 以获得 Mybatis-Plus 提供的 CRUD 方法
 */
@Mapper // Mybatis-Plus 注解，将接口标记为 Mapper
@Repository // Spring 注解，将接口标记为数据访问层组件
public interface PublicModulesMapper extends BaseMapper<PublicModules> {

    // 继承 BaseMapper 即可获得以下方法，通常无需额外编写：
    // insert(entity)
    // deleteById(id)
    // updateById(entity)
    // selectById(id)
    // selectList(wrapper)
    // selectPage(page, wrapper)
    // 等等...
    
    // 如果您需要自定义复杂的 SQL 查询，可以在这里额外添加方法，例如：
    // @Select("SELECT * FROM public_modules WHERE creator = #{creator}")
    // List<PublicModules> findByCreator(String creator);
}