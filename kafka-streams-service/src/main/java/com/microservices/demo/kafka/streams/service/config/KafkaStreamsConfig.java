package com.microservices.demo.kafka.streams.service.config;

import java.util.Properties;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.config.KafkaStreamsConfigData;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaStreamsConfig {

	private final KafkaConfigData kafkaConfigData;

	private final KafkaStreamsConfigData kafkaStreamsConfigData;

	public KafkaStreamsConfig(KafkaConfigData kafkaConfigData, KafkaStreamsConfigData kafkaStreamsConfigData) {
		this.kafkaConfigData = kafkaConfigData;
		this.kafkaStreamsConfigData = kafkaStreamsConfigData;
	}

	@Bean("streamConfiguration")
	public Properties streamsConfiguration() {
		Properties streamsConfiguration = new Properties();
		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, this.kafkaStreamsConfigData.getApplicationID());
		streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaConfigData.getBootstrapServers());
		streamsConfiguration.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
				this.kafkaConfigData.getSchemaRegistryUrl());
		streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, this.kafkaStreamsConfigData.getStateFileLocation());
		return streamsConfiguration;
	}

}
