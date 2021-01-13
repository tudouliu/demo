package com.hzlg.flowable.listen;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * @author liujj
 * @date 2020-07-08 16:36
 */
public class JingLiTaskHandler implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.setAssignee("2");
    }
}
