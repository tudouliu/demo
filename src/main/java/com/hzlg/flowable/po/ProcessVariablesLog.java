package com.hzlg.flowable.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author liujj
 * @since 2020-08-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="LgProcessVariablesLog对象", description="")
public class ProcessVariablesLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "添加人登录名")
    private String creater;

    @ApiModelProperty(value = "添加事件")
    private Date createTime;

    @ApiModelProperty(value = "添加人")
    private String createrName;

    @ApiModelProperty(value = "表单标题")
    private String title;

    @ApiModelProperty(value = "流程类型")
    private String processType;

    @ApiModelProperty(value = "表名")
    private String tableName;

    @ApiModelProperty(value = "表单id")
    private String formId;

    @ApiModelProperty(value = "流程id")
    private String processInstanceId;

    @ApiModelProperty(value = "流程名称")
    private String processInstanceName;


}
