package com.hzlg.flowable.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.hzlg.flowable.common.Result;
import com.hzlg.flowable.common.ResultCode;
import com.hzlg.flowable.config.MyDefaultProcessDiagramGenerator;
import com.hzlg.flowable.mapper.ActivityLogMapper;
import com.hzlg.flowable.po.ActivityLog;
import com.hzlg.flowable.service.ProcessFileService;
import com.hzlg.flowable.service.ProcessVariablesLogService;
import com.hzlg.flowable.vo.ActivityVo;
import com.hzlg.flowable.vo.SequenceFlowVo;
import com.hzlg.flowable.vo.TaskVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.idm.api.User;
import org.flowable.idm.engine.impl.persistence.entity.UserEntity;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: fumin
 * @Description:
 * @Date: Create in 2019/5/20 9:45
 */
@RestController
@RequestMapping(value = "lc")
@Slf4j
public class ProcessController {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ProcessFileService processFileService;

    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private ProcessVariablesLogService processVariablesLogService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ActivityLogMapper activityLogMapper;
    @Autowired
    private IdentityService identityService;

    /**
     * 创建流程实例，获取流程实例ID
     *
     * @param key
     * @param jsonMap
     * @return
     */
    @RequestMapping(value = "startProcessInstanceByKey")
    @ResponseBody
    public Result startProcessInstanceByKey(String key, String jsonMap) {
        Map<String, Object> map = JSONUtil.toBean(jsonMap, HashMap.class);

        //启动流程
        ProcessInstance processInstance = startProcessInstanceByKey(key, map);
        if (processInstance.getId() != null) {
            return new Result(processInstance.getId());
        }
        return new Result("此流程不存在", 201);
    }

    /**
     * 创建流程实例，获取流程实例ID, 并且开始第一步提交(第一步提交人为单据创建人，
     * 如果未获取到Task，则表明流程图未配置第一步提交，则忽略
     *
     * @param key
     * @param jsonMap
     * @return
     */
    @RequestMapping(value = "startProcessInstanceByKeyAndStart")
    @ResponseBody
    public Result startProcessInstanceByKeyAndStart(String key, String jsonMap) {
        Map<String, Object> map = JSONUtil.toBean(jsonMap, HashMap.class);

        //启动流程
        ProcessInstance processInstance = startProcessInstanceByKey(key, map);
        if (processInstance.getId() != null) {
            String operator = (String) map.get("operator");
            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskAssignee(operator).singleResult();
            if (task != null) {
                taskService.complete(task.getId(), map);
            }
            return new Result(processInstance.getId());
        }
        return new Result("此流程不存在", 201);
    }

    /**
     * 启动流程，参数添加日志表中
     *
     * @param key 流程配置的id
     * @param map 参数
     * @return ProcessInstance
     */
    private ProcessInstance startProcessInstanceByKey(String key, Map<String, Object> map) {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, map);
        map.put("processInstanceId", processInstance.getProcessInstanceId());
        map.put("processDefinitionName", processInstance.getProcessDefinitionName());
        processVariablesLogService.insert(map);
        return processInstance;
    }

    /**
     * 待办任务列表
     *
     * @param username 登录用户名
     * @param groups   登录用户组
     * @return
     */
    @RequestMapping(value = "/upcomeList")
    @ResponseBody
    public Result upcomeList(String username,String processInstanceName, @RequestParam(value = "groups") List<String> groups) {
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("processInstanceName", processInstanceName);
        map.put("groups", groups);
        List<Map<String, Object>> maps = processVariablesLogService.upcomeList(map);
        for (Map<String, Object> stringObjectMap : maps) {
            Date dateTimeStr = DateUtil.parse(StrUtil.toString(stringObjectMap.get("startTime")));
            stringObjectMap.put("startTime", DateUtil.formatDateTime(dateTimeStr));
        }
        return new Result(maps);
    }

    /**
     * 开始流程
     */
    @RequestMapping(value = "/start")
    @ResponseBody
    public Result start(String userId, String processInstanceId) {
        if (StrUtil.isEmpty(userId) || StrUtil.isEmpty(processInstanceId)) {
            return new Result("参数错误", ResultCode.PARAM_EXCEPTION);
        }

        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).taskAssignee(userId).singleResult();
        if (task == null) {
            return new Result("流程不存在", ResultCode.TASK_NOT_EXIST);
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("outcome", "通过");
        map.put("operator", "123");
        map.put("operatorName", "123");
        taskService.complete(task.getId(), map);

        HashMap taskMap = Convert.convert(HashMap.class, task);
        return new Result(taskMap);
    }

    private void flowBackBeforeNode(String taskId, String hisActivityId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        ExecutionEntity execution = (ExecutionEntity) processEngine.getRuntimeService().createExecutionQuery()
                .executionId(task.getExecutionId()).singleResult();

        // 当前节点
        String activityId = execution.getActivityId();

        /*
         * 获取指定节点节点
         */
        if (StrUtil.isEmpty(hisActivityId)) {
            List<HistoricActivityInstance> historyProcess = getHistoryProcess(task.getProcessInstanceId());
            List<HistoricActivityInstance> collectors = historyProcess
                    .stream().filter(his -> his.getActivityType().equals("userTask")).collect(Collectors.toList());
            hisActivityId = collectors.get(0).getActivityId();
            historyService.deleteHistoricTaskInstance(collectors.get(0).getTaskId());
        }

        // 流程回退到指定节点，审批人继续审批
        runtimeService.createChangeActivityStateBuilder().processInstanceId(task.getProcessInstanceId())
                .moveActivityIdTo(activityId, hisActivityId).changeState();
    }

    /**
     * 获取待处理任务
     *
     * @param processInstanceId
     * @param candidateGroups
     * @param assignee
     * @param flowHasName
     * @return
     */
    @RequestMapping(value = "/activeTasks")
    @ResponseBody
    public Result activeTasks(String processInstanceId, String candidateGroups, String assignee, Boolean flowHasName) {
        List<String> lstCandidateGroups = new ArrayList<>();
        if (StringUtils.isNotBlank(candidateGroups)) {
            String[] ary = candidateGroups.split(",");
            for (String c : ary) {
                lstCandidateGroups.add(c);
            }
        }
        TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstanceId).active();
        if (lstCandidateGroups.size() > 0 && StringUtils.isNotBlank(assignee)) {
            taskQuery = taskQuery.or()
                    .taskCandidateGroupIn(lstCandidateGroups)
                    .taskAssignee(assignee)
                    .endOr();
        } else if (lstCandidateGroups.size() > 0) {
            taskQuery = taskQuery.taskCandidateGroupIn(lstCandidateGroups);
        } else if (StringUtils.isNotBlank(assignee)) {
            taskQuery = taskQuery.taskAssignee(assignee);
        } else {
            return new Result("参数错误", ResultCode.PARAM_EXCEPTION);
        }
        List<Task> tasks = taskQuery.list();
        List<TaskVo> taskVos = new ArrayList<>();
        Map<String, Object> mpCustomAttrs = null;
        for (Task task : tasks) {
            mpCustomAttrs = getCustomAttrsOfTask(task);
            List<SequenceFlow> outFlows = getOutgoningFlows(task, flowHasName);
            taskVos.add(TaskVo.create(task, outFlows, mpCustomAttrs));
        }
        return new Result(taskVos);
    }

    private Map<String, Object> getCustomAttrsOfTask(Task task) {
        Map<String, Object> result = new HashMap<>();
        Map<String, List<ExtensionElement>> extensionElement = null;
        String processDefId = task.getProcessDefinitionId();
        FlowNode flowNode = findFlowNodeByActivityId(processDefId, task.getTaskDefinitionKey());
        if (flowNode != null && flowNode instanceof UserTask) {
            UserTask userTask = (UserTask) flowNode;
            extensionElement = userTask.getExtensionElements();
        }
        List<ExtensionElement> elements = null;
        for (String key : extensionElement.keySet()) {
            elements = extensionElement.get(key);
            if (elements != null && elements.size() == 1) {
                result.put(key, Base64.decodeStr(elements.get(0).getElementText()));
            }
        }
        return result;
    }

    public FlowNode findFlowNodeByActivityId(String processDefId, String activityId) {
        FlowNode activity = null;
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefId);
        List<Process> processes = bpmnModel.getProcesses();
        for (Process process : processes) {
            FlowElement flowElement = process.getFlowElementMap().get(activityId);
            if (flowElement != null) {
                activity = (FlowNode) flowElement;
                break;
            }
        }
        return activity;
    }

    /***
     * 获取输出连线
     * @param taskId
     * @return
     */
    @RequestMapping(value = "/outgoingFlows")
    @ResponseBody
    public Result outgoingFlows(String taskId, Boolean flowHasName) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        List<SequenceFlow> outFlows = getOutgoningFlows(task, flowHasName);
        return new Result(SequenceFlowVo.create(outFlows));
    }

    private List<SequenceFlow> getOutgoningFlows(Task task, Boolean flowHasName) {
        ExecutionEntity ee = (ExecutionEntity) processEngine.getRuntimeService().createExecutionQuery()
                .executionId(task.getExecutionId()).singleResult();
// 当前审批节点
        String crruentActivityId = ee.getActivityId();
        BpmnModel bpmnModel = processEngine.getRepositoryService().getBpmnModel(task.getProcessDefinitionId());
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(crruentActivityId);
// 输出连线
        List<SequenceFlow> resultFlows = new ArrayList<>();
        List<SequenceFlow> flows = flowNode.getOutgoingFlows();
        if (flowHasName != null && flowHasName) {
            for (SequenceFlow flow : flows) {
                if (StringUtils.isNotBlank(flow.getName())) {
                    resultFlows.add(flow);
                }
            }
        }
        return resultFlows;
    }

    /**
     * 审核
     *
     * @param taskId
     * @param chkIsEnd: 执行当前任务后，检查流程是否已经结束
     * @param jsonMap
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "complete")
    public Result complete(String taskId, Boolean chkIsEnd, String jsonMap) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return new Result("任务不存在", ResultCode.TASK_NOT_EXIST);
        }
        Map<String, Object> mp = new HashMap<>();
        HashMap map = JSONUtil.toBean(jsonMap, HashMap.class);

        String outcomeType = StrUtil.toString(map.get("outcomeType"));

        // 如果是"0"：为多选，传过来的outcome是个字符串集合
        if ("0".equals(outcomeType)) {
            Object outcome1 = map.get("outcome");
            List<ArrayList> arrayLists = JSONUtil.toList(JSONUtil.parseArray(outcome1), ArrayList.class);
            map.put("outcome", arrayLists);
        }

        if ("1".equals(outcomeType) || "null".equals(outcomeType) || "0".equals(outcomeType)) {
            taskService.complete(taskId, map);
        } else {
            String outcome = StrUtil.toString(map.get("outcome"));
            flowBackBeforeNode(taskId, outcome);

            ActivityLog activityLog = new ActivityLog();
            activityLog.setCreater(StrUtil.toString(map.get("operator")));
            activityLog.setCreaterName(StrUtil.toString(map.get("operatorName")));
            activityLog.setCreateTime(new Date());
            activityLog.setOpinion(StrUtil.toString(map.get("opinion")));
            activityLog.setOutcome("3".equals(outcomeType) ? "退回到指定位置" : "退回上一步");
            activityLog.setActivityName(StrUtil.toString(map.get("activityName")));
            activityLog.setProcessInstanceId(task.getProcessInstanceId());
            activityLogMapper.insert(activityLog);
        }

        if (chkIsEnd) {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).active().list();
            mp.put("isEnd", tasks.size() == 0); //如果没有活动的任务，表示流程已经结束
        }
        return new Result(mp);
    }

    /**
     * 删除流程
     */
    @ResponseBody
    @RequestMapping(value = "deleteProcess")
    public Result deleteProcessInstance(String processInstanceId, String delReason) {
        if (StrUtil.isEmpty(processInstanceId)) {
            return new Result("参数错误", ResultCode.PARAM_EXCEPTION);
        }
        runtimeService.deleteProcessInstance(processInstanceId, delReason);
        return new Result();
    }

    /***
     * 获取已审核流程步骤
     * @param processInstanceId
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "activities")
    public Result activities(String processInstanceId) throws Exception {
        List<ActivityVo> vos = new ArrayList<>();
        List<HistoricActivityInstance> historyProcess = getHistoryProcess(processInstanceId);
//        List<HistoricActivityInstance> collectors = historyProcess
//                .stream().filter(his -> his.getActivityType().equals("userTask")).collect(Collectors.toList());
        for (HistoricActivityInstance hi : historyProcess) {
            if ("startEvent".equals(hi.getActivityType())) {
                continue;
            }
            vos.add(ActivityVo.create(hi));
        }
        return new Result(vos);
    }

    /**
     * 生成流程图
     *
     * @param processId 任务ID
     */
    @RequestMapping(value = "processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
        String processDefinitionId = null;
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        if (pi != null) {
            processDefinitionId = pi.getProcessDefinitionId();
        } else {
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId = hpi.getProcessDefinitionId();
        }

        List<HistoricActivityInstance> historyProcess = getHistoryProcess(processId);
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        for (HistoricActivityInstance hi : historyProcess) {
            String activityType = hi.getActivityType();
            if (activityType.equals("sequenceFlow") || activityType.equals("exclusiveGateway")) {
                flows.add(hi.getActivityId());
            } else if (activityType.equals("userTask") || activityType.equals("startEvent")) {
                activityIds.add(hi.getActivityId());
            }
        }
//        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processId).list();
//        for (Task task : tasks) {
////            activityIds.add(task.getTaskDefinitionKey());
//        }
        ProcessEngineConfiguration engConf = processEngine.getProcessEngineConfiguration();
        //定义流程画布生成器
        ProcessDiagramGenerator processDiagramGenerator = engConf.getProcessDiagramGenerator();
        InputStream in = processDiagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engConf.getActivityFontName(), engConf.getLabelFontName(), engConf.getAnnotationFontName(), engConf.getClassLoader(), 1.0, true);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * @param processId 提交流程的进程实例id
     */
    @RequestMapping(value = "processDiagram3")
    public void genProcessDiagram3(HttpServletResponse httpServletResponse, String processId) {
        String processDefinitionKey = "start";
        ProcessInstance pi1 = runtimeService.createProcessInstanceQuery()//与正在执行的流程实例和执行对象相关的Service
                .processInstanceId(processId)
                .singleResult();
        /**
         * 获得当前活动的节点
         */
        String processDefinitionId = "";
        if (pi1 == null) {// 如果流程已经结束，则得到结束节点
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();

            processDefinitionId = pi.getProcessDefinitionId();
        } else {// 如果流程没有结束，则取当前活动节点
            // 根据流程实例ID获得当前处于活动状态的ActivityId合集
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId = pi.getProcessDefinitionId();
        }
        List<String> highLightedActivitis = new ArrayList<String>();
        highLightedActivitis.add(processId);
        /**
         * 获得活动的节点
         */
        List<HistoricActivityInstance> highLightedActivitList = historyService.createHistoricActivityInstanceQuery().processInstanceId(processId).orderByHistoricActivityInstanceStartTime().asc().list();
        //高亮节点
        for (HistoricActivityInstance tempActivity : highLightedActivitList) {
            String activityId = tempActivity.getActivityId();
            highLightedActivitis.add(activityId);
        }

        List<String> highLightedflows = new ArrayList<>();
        //高亮线
        for (HistoricActivityInstance tempActivity : highLightedActivitList) {
            if ("sequenceFlow".equals(tempActivity.getActivityType())) {
                highLightedflows.add(tempActivity.getActivityId());
            }
        }


        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);


        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();

        // ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        //获取自定义图片生成器
        ProcessDiagramGenerator diagramGenerator = new MyDefaultProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "bmp", highLightedActivitis, highLightedflows, engconf.getActivityFontName(),
                engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0, true);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } catch (IOException e) {

        } finally {
            IoUtil.close(out);
            IoUtil.close(in);
        }
    }

    /**
     * 生成流程图
     *
     * @param processId 任务ID
     */
    @RequestMapping(value = "processDiagram2")
    public void genProcessDiagram2(HttpServletResponse httpServletResponse, String processId) throws Exception {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();

        //流程走完的不显示图
        if (pi == null) {
            return;
        }
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        //使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        String InstanceId = task.getProcessInstanceId();
        List<Execution> executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(InstanceId)
                .list();

        //得到正在执行的Activity的Id
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        for (Execution exe : executions) {
            List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engconf.getActivityFontName(), engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0, true);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /***
     * 将流程文件导入processes文件夹并部署进数据库
     * @param httpServletResponse
     * @param url
     * @param modelId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "Processfile")
    @ResponseBody
    public Result Processfile(HttpServletResponse httpServletResponse,String modelId) throws Exception {
        String s = processFileService.ExportProcessFile(modelId);
        return new Result(s);
    }

    /**
     * 任务历史
     *
     * @param processId 部署id
     */
    public List<HistoricActivityInstance> getHistoryProcess(String processId) {
        List<HistoricActivityInstance> list = historyService // 历史相关Service
                .createHistoricActivityInstanceQuery() // 创建历史活动实例查询
                .processInstanceId(processId) // 执行流程实例id
                .finished()
                .orderByHistoricActivityInstanceStartTime().desc()
                .list();
        return list;
    }


    @RequestMapping(value = "syncUser")
    @ResponseBody
    public void syncUser() {
        identityService.deleteUser("123456");
        identityService.newUser("123456");
    }
}
