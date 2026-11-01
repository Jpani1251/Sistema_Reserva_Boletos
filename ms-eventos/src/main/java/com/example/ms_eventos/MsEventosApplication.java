package com.example.ms_eventos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class MsEventosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsEventosApplication.class, args);
    }
}
