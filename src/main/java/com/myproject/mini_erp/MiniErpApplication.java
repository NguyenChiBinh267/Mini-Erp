package com.myproject.mini_erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MiniErpApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniErpApplication.class, args);
	}

}
