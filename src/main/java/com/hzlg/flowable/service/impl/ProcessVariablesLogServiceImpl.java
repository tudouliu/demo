package com.hzlg.flowable.service.impl;

import cn.hutool.core.convert.Convert;
import com.hzlg.flowable.mapper.ProcessVariablesLogMapper;
import com.hzlg.flowable.po.ProcessVariablesLog;
import com.hzlg.flowable.service.ProcessVariablesLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author liujj
 * @since 2020-08-05
 */
@Service
public class ProcessVariablesLogServiceImpl implements ProcessVariablesLogService {
    @Autowired
    private ProcessVariablesLogMapper processVariablesLogMapper;

    @Override
    public void insert(Map<String, Object> map) {
        ProcessVariablesLog processVariablesLog = new ProcessVariablesLog();
        processVariablesLog.setCreater(Convert.toStr(map.get("operator")));
        processVariablesLog.setCreaterName(Convert.toStr(map.get("operatorName")));
        processVariablesLog.setCreateTime(new Date());
        processVariablesLog.setTitle(Convert.toStr(map.get("title")));
        processVariablesLog.setProcessType(Convert.toStr(map.get("processType")));
        processVariablesLog.setTableName(Convert.toStr(map.get("tableName")));
        processVariablesLog.setFormId(Convert.toStr(map.get("id")));
        processVariablesLog.setProcessInstanceId(Convert.toStr(map.get("processInstanceId")));
        processVariablesLog.setProcessInstanceName(Convert.toStr(map.get("processDefinitionName")));
        processVariablesLogMapper.insert(processVariablesLog);
    }

    @Override
    public List<Map<String, Object>> upcomeList(Map<String, Object> map) {
        return processVariablesLogMapper.upcomeList(map);
    }
}
