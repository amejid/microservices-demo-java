package com.microservices.demo.kafka.admin.config;

import java.util.Map;

import com.microservices.demo.config.KafkaConfigData;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@Configuration
public class KafkaAdminConfig {

	private final KafkaConfigData kafkaConfigData;

	public KafkaAdminConfig(KafkaConfigData kafkaConfigData) {
		this.kafkaConfigData = kafkaConfigData;
	}

	@Bean
	public AdminClient adminClient() {
		return AdminClient
			.create(Map.of(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, this.kafkaConfigData.getBootstrapServers()));
	}

}
