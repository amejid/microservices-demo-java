package com.microservices.demo.kafka.to.elastic.service.consumer.impl;

import java.util.List;
import java.util.Objects;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.config.KafkaConsumerConfigData;
import com.microservices.demo.elastic.index.client.service.ElasticIndexClient;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.kafka.admin.client.KafkaAdminClient;
import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import com.microservices.demo.kafka.to.elastic.service.consumer.KafkaConsumer;
import com.microservices.demo.kafka.to.elastic.service.transformer.AvroToElasticModelTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class TwitterKafkaConsumer implements KafkaConsumer<Long, TwitterAvroModel> {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterKafkaConsumer.class);

	private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

	private final KafkaAdminClient kafkaAdminClient;

	private final KafkaConfigData kafkaConfigData;

	private final KafkaConsumerConfigData kafkaConsumerConfigData;

	private final AvroToElasticModelTransformer avroToElasticModelTransformer;

	private final ElasticIndexClient<TwitterIndexModel> elasticIndexClient;

	public TwitterKafkaConsumer(KafkaListenerEndpointRegistry listenerEndpointRegistry,
			KafkaAdminClient kafkaAdminClient, KafkaConfigData kafkaConfigData,
			KafkaConsumerConfigData kafkaConsumerConfigData,
			AvroToElasticModelTransformer avroToElasticModelTransformer,
			ElasticIndexClient<TwitterIndexModel> elasticIndexClient) {
		this.kafkaListenerEndpointRegistry = listenerEndpointRegistry;
		this.kafkaAdminClient = kafkaAdminClient;
		this.kafkaConfigData = kafkaConfigData;
		this.kafkaConsumerConfigData = kafkaConsumerConfigData;
		this.avroToElasticModelTransformer = avroToElasticModelTransformer;
		this.elasticIndexClient = elasticIndexClient;
	}

	@EventListener
	public void onAppStarted(ApplicationStartedEvent event) {
		this.kafkaAdminClient.checkTopicsCreated();
		LOG.info("Topics with name {} are ready for operations",
				this.kafkaConfigData.getTopicNamesToCreate().toArray());
		Objects
			.requireNonNull(this.kafkaListenerEndpointRegistry
				.getListenerContainer(this.kafkaConsumerConfigData.getConsumerGroupId()))
			.start();
	}

	@Override
	@KafkaListener(id = "${kafka-consumer-config.consumer-group-id}", topics = "${kafka-config.topic-name}")
	public void receive(@Payload List<TwitterAvroModel> messages, @Header(KafkaHeaders.RECEIVED_KEY) List<Integer> keys,
			@Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets) {
		LOG.info(
				"{} number of messages received with keys: {} and partitions: {} and offsets: {} sending to elastic: Thread id {}",
				messages.size(), keys, partitions, offsets, Thread.currentThread().getId());
		List<TwitterIndexModel> twitterIndexModels = this.avroToElasticModelTransformer.getElasticModels(messages);
		List<String> documentIds = this.elasticIndexClient.save(twitterIndexModels);
		LOG.info("Documents with ids: {} are saved in elastic search", documentIds.toArray());
	}

}
