package com.example.kradankanban_backend;

import com.example.kradankanban_backend.common.properties.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({FileStorageProperties.class})
@SpringBootApplication
public class KradanKanbanBackEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(KradanKanbanBackEndApplication.class, args);
    }

}
