package com.hzlg.flowable;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

import java.util.*;

/**
 * @author liujj
 * @date 2020-08-06 17:27
 */
public class Test {
    public static void main(String[] args) {

        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("username", "123456");
        paramMap.put("groups", Arrays.asList("dept.19102.manager","3456","666"));
        String taskId = HttpUtil.post("http://localhost:8081/flow/lc/upcomeList", paramMap);
        System.out.println(taskId);



//        Map<String, Object> mpProcessParams = new HashMap<>();
//        mpProcessParams.put("taskUser", "123");
//        mpProcessParams.put("operator", "123");
//        mpProcessParams.put("operatorName", "张三");
//        mpProcessParams.put("id", "1");
//        mpProcessParams.put("recordCreater", "1");
//        mpProcessParams.put("title", "沃德玛。烈凯勒");
//        mpProcessParams.put("formId", "999");
//        mpProcessParams.put("tableName", "lg_test");
//
//        HashMap<String, Object> paramMap = new HashMap<>();
//        paramMap.put("key", "test");
//        paramMap.put("jsonMap", JSONUtil.toJsonStr(mpProcessParams));
//
//        String returnFlag = HttpUtil.post(EXPENSE_URL + "/startProcessInstanceByKey", paramMap);
//        System.out.println(returnFlag);


//        paramMap.clear();
//        paramMap.put("processInstanceId", "914283c4-d230-11ea-83c4-b0359fd6eb52");
//        paramMap.put("userId", "123");
//        String taskId = HttpUtil.post(EXPENSE_URL+"/start", paramMap);
//        System.out.println(taskId);
//        paramMap.clear();
//        paramMap.put("processInstanceId", "914283c4-d230-11ea-83c4-b0359fd6eb52");
//        paramMap.put("candidateGroups", "");
//        paramMap.put("assignee", "5");
//        paramMap.put("flowHasName", "");
//        String jsonStr= HttpUtil.post(EXPENSE_URL+"/activeTasks",paramMap);
//        System.out.println(jsonStr);
    }
}
