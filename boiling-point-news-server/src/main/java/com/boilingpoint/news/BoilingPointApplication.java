package com.boilingpoint.news;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BoilingPointApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoilingPointApplication.class, args);
    }
}
