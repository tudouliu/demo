package com.hzlg.flowable.vo;

import lombok.Data;
import org.flowable.engine.history.HistoricActivityInstance;

import java.util.Date;

@Data
public class ActivityVo {
    private String activityName;
    private String assignee;
    private Date startTime;
    private Date endTime;

    public static ActivityVo create(HistoricActivityInstance aci){
        ActivityVo vo = new ActivityVo();
        vo.setActivityName(aci.getActivityName());
        vo.setAssignee(aci.getAssignee());
        vo.setStartTime(aci.getStartTime());
        vo.setEndTime(aci.getEndTime());
        return vo;
    }
}
