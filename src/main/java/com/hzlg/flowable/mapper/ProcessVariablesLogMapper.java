package com.hzlg.flowable.mapper;

import com.hzlg.flowable.po.ActivityLog;
import com.hzlg.flowable.po.ProcessVariablesLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProcessVariablesLogMapper {

    void insert(ProcessVariablesLog processVariablesLog);

    List<Map<String,Object>> upcomeList(Map<String, Object> map);
}
