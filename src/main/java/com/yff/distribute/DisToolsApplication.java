package com.yff.distribute;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yff.distribute.mapper")
public class DisToolsApplication {
    public static void main(String[] args) {
        SpringApplication.run(DisToolsApplication.class,args);
    }
}
