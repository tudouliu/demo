package com.hzlg.flowable.vo;

import lombok.Data;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.task.api.Task;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class TaskVo {
    private String id;
    private String name;
    private List<SequenceFlowVo> outgoingFlows = new ArrayList<>();
    private Map<String, Object> customAttrs = null;

    public static TaskVo create(Task task, List<SequenceFlow> flows, Map<String, Object> customAttrs){
        TaskVo vo = new TaskVo();
        vo.id = task.getId();
        vo.name = task.getName();
        for (SequenceFlow flow : flows){
            vo.outgoingFlows.add(SequenceFlowVo.create(flow));
        }
        vo.customAttrs = customAttrs;
        return vo;
    }
}
