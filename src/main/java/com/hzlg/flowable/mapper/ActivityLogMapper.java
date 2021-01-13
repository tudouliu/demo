package com.hzlg.flowable.mapper;

import com.hzlg.flowable.po.ActivityLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivityLogMapper {
    void insert(ActivityLog activityLog);
}
