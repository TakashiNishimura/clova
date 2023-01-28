package com.example.clova;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ClovaApplication {

	public static void main(String[] args) { SpringApplication.run(ClovaApplication.class, args); }

}
