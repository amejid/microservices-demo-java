package com.microservices.demo.elastic.query.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = "com.microservices.demo")
public class ElasticQueryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ElasticQueryServiceApplication.class, args);
	}

}
