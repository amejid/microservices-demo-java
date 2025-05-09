package com.microservices.demo.kafka.streams.service.init.impl;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.kafka.admin.client.KafkaAdminClient;
import com.microservices.demo.kafka.streams.service.init.StreamsInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
public class KafkaStreamsInitializer implements StreamsInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(KafkaStreamsInitializer.class);

	private final KafkaConfigData kafkaConfigData;

	private final KafkaAdminClient kafkaAdminClient;

	public KafkaStreamsInitializer(KafkaConfigData configData, KafkaAdminClient adminClient) {
		this.kafkaConfigData = configData;
		this.kafkaAdminClient = adminClient;
	}

	@Override
	public void init() {
		this.kafkaAdminClient.checkTopicsCreated();
		this.kafkaAdminClient.checkSchemaRegistry();
		LOG.info("Topics with name {} is ready for operations!",
				this.kafkaConfigData.getTopicNamesToCreate().toArray());
	}

}
