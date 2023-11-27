package com.wisemapping.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
@EnableJpaRepositories("com.wisemapping.model")
@ImportResource("classpath:spring/wisemapping-common.xml")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
