package com.hzlg.flowable.vo;

import lombok.Data;
import org.flowable.bpmn.model.SequenceFlow;

import java.util.ArrayList;
import java.util.List;

@Data
public class SequenceFlowVo {
    private String name;

    public static SequenceFlowVo create(SequenceFlow flow){
        SequenceFlowVo vo = new SequenceFlowVo();
        vo.name = flow.getName();
        return vo;
    }
    public static List<SequenceFlowVo> create(List<SequenceFlow> flows){
        List<SequenceFlowVo> vos = new ArrayList<>();
        for (SequenceFlow flow : flows){
            vos.add(SequenceFlowVo.create(flow));
        }
        return vos;
    }
}
