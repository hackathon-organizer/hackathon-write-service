package com.hackathonorganizer.hackathonwriteservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class HackathonWriteServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HackathonWriteServiceApplication.class, args);
	}

}
