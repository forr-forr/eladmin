package com.itheima.mapper.log;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pogo.UserOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserOperationLogMapper extends BaseMapper<UserOperationLog> {

    @Select("<script>" +
            "SELECT user_operation_logs.*, users.username " +
            "FROM user_operation_logs " +
            "LEFT JOIN users ON user_operation_logs.user_id = users.id " +
            "WHERE 1=1 " +
            "<if test='userId != null'> AND user_operation_logs.user_id = #{userId} </if>" +
            "<if test='username != null and username != \"\"'> AND users.username = #{username} </if>" +
            "<if test='category != null and category != \"\"'> AND user_operation_logs.category = #{category} </if>" +
            "ORDER BY user_operation_logs.create_time DESC" +
            "</script>")
    List<UserOperationLog> selectLogsWithConditions(
            @Param("userId") Long userId,
            @Param("username") String username,
            @Param("category") String category
    );

}