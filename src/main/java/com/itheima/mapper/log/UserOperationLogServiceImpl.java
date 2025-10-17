package com.itheima.mapper.log;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.pogo.UserOperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserOperationLogServiceImpl implements UserOperationLogService {

    @Autowired
    private UserOperationLogMapper userOperationLogMapper;

    @Override
    public IPage<UserOperationLog> getLogsPage(Integer page, Integer size, Long userId, String username, String category) {

        // 查询所有数据
        List<UserOperationLog> logs = userOperationLogMapper.selectLogsWithConditions(userId, username, category);

        // 手动分页
        Page<UserOperationLog> pageRequest = new Page<>(page, size);
        int total = logs.size();
        pageRequest.setTotal(total);

        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);
        if (fromIndex < total) {
            pageRequest.setRecords(logs.subList(fromIndex, toIndex));
        } else {
            pageRequest.setRecords(Collections.emptyList());
        }

        return pageRequest;
    }

}