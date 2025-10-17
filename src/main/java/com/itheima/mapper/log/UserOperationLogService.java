package com.itheima.mapper.log;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itheima.pogo.UserOperationLog;

public interface UserOperationLogService {
    IPage<UserOperationLog> getLogsPage(Integer page, Integer size, Long userId, String username, String category);
}