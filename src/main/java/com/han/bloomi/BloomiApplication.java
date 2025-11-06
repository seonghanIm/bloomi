package com.han.bloomi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BloomiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BloomiApplication.class, args);
    }
}
