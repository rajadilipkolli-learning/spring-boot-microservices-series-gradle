package com.example.catalog;

import com.example.catalog.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(Application::main).with(ContainersConfig.class).run(args);
    }
}
