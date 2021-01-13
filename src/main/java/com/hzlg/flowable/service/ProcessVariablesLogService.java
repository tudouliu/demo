package com.hzlg.flowable.service;

import com.hzlg.flowable.po.ProcessVariablesLog;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author liujj
 * @since 2020-08-05
 */
public interface ProcessVariablesLogService {

    void insert(Map<String ,Object> map);

    List<Map<String,Object>> upcomeList(Map<String, Object> map);
}
