package com.hzlg.flowable.service.impl;

import com.hzlg.flowable.service.ProcessFileService;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.RepositoryService;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessFileServiceIml implements ProcessFileService {

    @Autowired
    private ModelService modelService;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public String ExportProcessFile(String modelId) {
        String type = createFlow(modelId);
        return type;
    }

    public String createFlow(String modelId) {
        String ts = "导入成功";
        try {
            Model modelData = modelService.getModel(modelId);
            byte[] bytes = modelService.getBpmnXML(modelData);
            if (bytes == null) {
                ts = "模型数据为空，请先设计流程并成功保存，再进行发布。";
            }

            BpmnModel model = modelService.getBpmnModel(modelData);
            if (model.getProcesses().size() == 0) {
                ts = "数据模型不符要求，请至少设计一条主线流程。";
            }
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
            String processName = modelData.getName() + ".bpmn20.xml";
            repositoryService.createDeployment()
                    .name(modelData.getName())
                    .addBytes(processName, bpmnBytes)
                    .deploy();
            return ts;
        } catch (Exception ex) {
            ex.printStackTrace();
            return ts = "导入失败";
        }
    }
}
