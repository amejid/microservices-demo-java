package com.microservices.demo.kafka.producer.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.config.KafkaProducerConfigData;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig<K extends Serializable, V extends SpecificRecordBase> {

	private final KafkaConfigData kafkaConfigData;

	private final KafkaProducerConfigData kafkaProducerConfigData;

	public KafkaProducerConfig(KafkaConfigData kafkaConfigData, KafkaProducerConfigData kafkaProducerConfigData) {
		this.kafkaConfigData = kafkaConfigData;
		this.kafkaProducerConfigData = kafkaProducerConfigData;
	}

	@Bean
	public Map<String, Object> producerConfig() {
		Map<String, Object> producerConfig = new HashMap<>();
		producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaConfigData.getBootstrapServers());
		producerConfig.put(this.kafkaConfigData.getSchemaRegistryUrlKey(), this.kafkaConfigData.getSchemaRegistryUrl());
		producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				this.kafkaProducerConfigData.getKeySerializerClass());
		producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				this.kafkaProducerConfigData.getValueSerializerClass());
		producerConfig.put(ProducerConfig.BATCH_SIZE_CONFIG,
				this.kafkaProducerConfigData.getBatchSize() * this.kafkaProducerConfigData.getBatchSizeBoostFactor());
		producerConfig.put(ProducerConfig.LINGER_MS_CONFIG, this.kafkaProducerConfigData.getLingerMs());
		producerConfig.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, this.kafkaProducerConfigData.getCompressionType());
		producerConfig.put(ProducerConfig.ACKS_CONFIG, this.kafkaProducerConfigData.getAcks());
		producerConfig.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
				this.kafkaProducerConfigData.getRequestTimeoutMs());
		producerConfig.put(ProducerConfig.RETRIES_CONFIG, this.kafkaProducerConfigData.getRetryCount());
		return producerConfig;
	}

	@Bean
	public ProducerFactory<K, V> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfig());
	}

	@Bean
	public KafkaTemplate<K, V> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

}
