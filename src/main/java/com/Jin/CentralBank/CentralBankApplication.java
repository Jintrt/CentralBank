package com.Jin.CentralBank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.Jin.CentralBank")
public class CentralBankApplication {
	public static void main(String[] args) {
		SpringApplication.run(CentralBankApplication.class, args);
	}
}
