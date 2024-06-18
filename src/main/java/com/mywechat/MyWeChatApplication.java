package com.mywechat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = ("com.mywechat"))
@MapperScan(basePackages = ("com.mywechat.mappers"))
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class MyWeChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyWeChatApplication.class,args);
    }
}
