package com.hrgenius;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Main entry point for the HRGenius application.
// Spring Boot auto-configures all beans from sub-packages.
@SpringBootApplication
public class HrGeniusApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrGeniusApplication.class, args);
    }
}
