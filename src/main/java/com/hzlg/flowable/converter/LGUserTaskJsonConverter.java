package com.hzlg.flowable.converter;

import cn.hutool.core.codec.Base64;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.support.json.JSONUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.*;
import org.flowable.editor.constants.StencilConstants;
import org.flowable.editor.language.json.converter.ActivityProcessor;
import org.flowable.editor.language.json.converter.BaseBpmnJsonConverter;
import org.flowable.editor.language.json.converter.BpmnJsonConverterUtil;
import org.flowable.editor.language.json.converter.UserTaskJsonConverter;

import java.util.*;

public class LGUserTaskJsonConverter extends UserTaskJsonConverter {

    public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
                                 Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }

    public static void setCustomTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
                                      Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        removeTypes(convertersToBpmnMap, convertersToJsonMap);
        fillTypes(convertersToBpmnMap, convertersToJsonMap);
    }

    public static void removeTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
                                   Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.remove(UserTask.class);
        convertersToBpmnMap.remove(StencilConstants.STENCIL_TASK_USER);
    }

    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put(STENCIL_TASK_USER, LGUserTaskJsonConverter.class);
    }

    public static void fillBpmnTypes(
            Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(UserTask.class, LGUserTaskJsonConverter.class);
    }

    @SneakyThrows
    @Override
    public void convertToJson(BaseElement baseElement, ActivityProcessor processor, BpmnModel model, FlowElementsContainer container, ArrayNode shapesArrayNode, double subProcessX, double subProcessY) {
        super.convertToJson(baseElement, processor, model, container, shapesArrayNode, subProcessX, subProcessY);

        // 转换自定义属性
        if (baseElement instanceof FlowElement) {
            // 获取所有的properties
            ObjectNode propertiesNode = (ObjectNode) this.flowElementNode.get("properties");
            // 获取当前Element的属性
            Map<String, List<ExtensionElement>> extensionElement = baseElement.getExtensionElements();

            List<ExtensionElement> elements = null;
            for (String key : extensionElement.keySet()) {
                elements = extensionElement.get(key);
                if (elements != null && elements.size() == 1) {
                    // 获取保存的值，转换成JsonNode
                    String elementText = Base64.decodeStr(elements.get(0).getElementText());
                    JsonNode jsonNode = new ObjectMapper().readTree(elementText);

                    if ("reviewConditions".equals(key)) {
                        ObjectNode listenersNode = objectMapper.createObjectNode();
                        // 先存放到listenersNode
                        listenersNode.set(key, jsonNode);
                        // 再存放到propertiesNode，key小写
                        propertiesNode.set("reviewconditions", listenersNode);
                    } else {
                        propertiesNode.set(key, jsonNode);
                    }
                }
            }
            // 修改properties的值
            this.flowElementNode.set("properties", propertiesNode);
        }
    }

    @Override
    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode,
                                               Map<String, JsonNode> shapeMap) {
        UserTask flowElement = (UserTask) super.convertJsonToElement(elementNode, modelNode, shapeMap);
        List<CustomProperty> customProperties = new ArrayList<>();
        // 自定义属性扩展 节点可编辑
        String isEditdata = getPropertyValueAsString("isrecordeditable", elementNode);
        if (StringUtils.isNotBlank(isEditdata)) {
            CustomProperty editData = this.createProperty("isrecordeditable", isEditdata);
            customProperties.add(editData);
        }

        // 自定义属性扩展 审核条件
        JsonNode reviewconditions = getProperty("reviewconditions", elementNode);
        if (reviewconditions != null) {
            JsonNode reviewconditionsJjsonNode = reviewconditions.get("reviewConditions");
            if (reviewconditionsJjsonNode != null) {
                CustomProperty customProperty = this.createProperty("reviewConditions", reviewconditionsJjsonNode.toString());
                customProperties.add(customProperty);
            }
        }

        if (CollectionUtils.isNotEmpty(customProperties)) {
            flowElement.setCustomProperties(customProperties);
        }
        return flowElement;
    }

    /**
     * 创建自定义属性
     *
     * @param propertyName  属性名称
     * @param propertyValue 属性值
     */
    private CustomProperty createProperty(String propertyName, String propertyValue) {
        CustomProperty customProperty = new CustomProperty();
        customProperty.setId(propertyName);
        customProperty.setName(propertyName);
        customProperty.setSimpleValue(Base64.encode(propertyValue));
        return customProperty;
    }
}
