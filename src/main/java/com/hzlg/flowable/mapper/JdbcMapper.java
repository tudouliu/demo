package com.hzlg.flowable.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface JdbcMapper {
    List<Map<String, Object>> findList(@Param(value="sql") String sql);
}
