package com.hzlg.flowable.config;

import com.hzlg.flowable.event.GlobalTaskListener;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.editor.language.json.converter.CustomPropertyInit;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: fumin
 * @Description:
 * @Date: Create in 2019/5/20 10:26
 */
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {
    @Autowired
    private GlobalTaskListener globalTaskListener;
    @Override
    public void configure(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
        springProcessEngineConfiguration.setActivityFontName("宋体");
        springProcessEngineConfiguration.setLabelFontName("宋体");
        springProcessEngineConfiguration.setAnnotationFontName("宋体");
        List<FlowableEventListener> listeners = new ArrayList<>();
        listeners.add(globalTaskListener);
        springProcessEngineConfiguration.setEventListeners(listeners);

        /**
         * 自定义节点属性初始化
         */
        createCustomPropertyInit().init();
    }

    @Bean
    public CustomPropertyInit createCustomPropertyInit() {
        CustomPropertyInit customPropertyInit = new CustomPropertyInit();
        return customPropertyInit;
    }
}
