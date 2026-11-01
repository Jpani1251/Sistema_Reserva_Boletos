package com.example.ms_compras;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class MsComprasApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsComprasApplication.class, args);
    }
}
