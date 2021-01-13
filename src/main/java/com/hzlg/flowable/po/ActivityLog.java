package com.hzlg.flowable.po;

import lombok.Data;

import java.util.Date;

@Data
public class ActivityLog {
    private String creater;//执行人登录名
    private String createrName;     //执行人姓名
    private Date createTime; //执行时间
    private String opinion; //意见
    private String outcome; //结果
    private String activityName; //流程名称
    private String processInstanceId; //流程实例ID
}
