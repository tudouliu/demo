package com.hzlg.flowable;

import com.hzlg.flowable.config.ApplicationConfiguration;
import com.hzlg.flowable.servlet.AppDispatcherServletConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Import({
        ApplicationConfiguration.class,
        AppDispatcherServletConfiguration.class
})
@ComponentScan(basePackages = {"com.hzlg.flowable"})
@EnableTransactionManagement
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
