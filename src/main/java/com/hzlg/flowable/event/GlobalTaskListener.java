package com.hzlg.flowable.event;

import cn.hutool.core.convert.Convert;
import com.hzlg.flowable.mapper.ActivityLogMapper;
import com.hzlg.flowable.po.ActivityLog;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.impl.FlowableEntityWithVariablesEventImpl;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component(value="globalTaskListener")
public class GlobalTaskListener extends AbstractFlowableEngineEventListener {
    @Autowired
    private ActivityLogMapper activityLogMapper;
    protected void taskCompleted(FlowableEngineEntityEvent event)		    {
        FlowableEntityWithVariablesEventImpl fEvent = (FlowableEntityWithVariablesEventImpl)event;
        if (!fEvent.getVariables().containsKey("operator") || !fEvent.getVariables().containsKey("operatorName")){
            throw new RuntimeException("variables must include operator and operatorName");
        }
        ActivityLog activityLog = new ActivityLog();
        activityLog.setCreater(Convert.toStr(fEvent.getVariables().get("operator")));
        activityLog.setCreaterName(Convert.toStr(fEvent.getVariables().get("operatorName")));
        activityLog.setCreateTime(new Date());
        activityLog.setOpinion(Convert.toStr(fEvent.getVariables().get("opinion")));
        activityLog.setOutcome(Convert.toStr(fEvent.getVariables().get("outcome")));
        activityLog.setActivityName(((TaskEntityImpl)fEvent.getEntity()).getName());
        activityLog.setProcessInstanceId(fEvent.getProcessInstanceId());
        activityLogMapper.insert(activityLog);
    }
}
