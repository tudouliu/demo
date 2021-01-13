package com.hzlg.flowable.listen;

import com.hzlg.flowable.mapper.JdbcMapper;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.impl.el.JuelExpression;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


/**
 * @author liujj
 * @date 2020-07-08 16:35
 */
@Component(value="dbTaskHandler")
public class DBTaskHandler implements TaskListener {
    @Autowired
    private JdbcMapper jdbcMapper;

    private JuelExpression sql;

    @Override
    public void notify(DelegateTask delegateTask) {
        List<Map<String, Object>> datas = jdbcMapper.findList(sql.getValue(delegateTask).toString());
        if (datas.size() == 0){
            throw new RuntimeException("流程错误，请指定流程接收人");
        }
        String value = null;
        String type = null;
        boolean hasAssigned = false;
        for (Map<String, Object> data : datas){
            value = (String)data.get("value");
            type = (String)data.get("type");
            if ("candidateGroup".equals(type)){
                delegateTask.addCandidateGroup(value);
                hasAssigned = true;
            } else if ("assignee".equals(type)){
                delegateTask.setAssignee(value);
                hasAssigned = true;
            } else if ("candidateUser".equals(type)){
                delegateTask.addCandidateUser(value);
                hasAssigned = true;
            }
        }
        if (!hasAssigned){
            throw new RuntimeException("流程错误，请指定流程接收人");
        }
    }

    public JuelExpression getSql() {
        return sql;
    }

    public void setSql(JuelExpression sql) {
        this.sql = sql;
    }
}
