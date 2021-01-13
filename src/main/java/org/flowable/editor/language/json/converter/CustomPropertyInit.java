package org.flowable.editor.language.json.converter;

import com.hzlg.flowable.converter.LGUserTaskJsonConverter;
import org.flowable.bpmn.model.BaseElement;
import org.flowable.editor.language.json.converter.BaseBpmnJsonConverter;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;

import java.util.Map;

public class CustomPropertyInit {
    public void init() {
        Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap = BpmnJsonConverter.convertersToJsonMap;
        Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap = BpmnJsonConverter.convertersToBpmnMap;
        //添加自定义的任务json转化器
        LGUserTaskJsonConverter.setCustomTypes(convertersToBpmnMap, convertersToJsonMap);
    }
}
