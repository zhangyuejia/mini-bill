package com.minibill.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.minibill.api")
@ComponentScan(basePackages = {"com.minibill.common", "com.minibill.system"})
@MapperScan("com.minibill.system.mapper")
public class MiniBillSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniBillSystemApplication.class, args);
    }
}
