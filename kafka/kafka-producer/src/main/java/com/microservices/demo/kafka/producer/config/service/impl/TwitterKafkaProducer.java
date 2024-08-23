package com.microservices.demo.kafka.producer.config.service.impl;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import com.microservices.demo.kafka.producer.config.service.KafkaProducer;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class TwitterKafkaProducer implements KafkaProducer<Long, TwitterAvroModel> {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterKafkaProducer.class);

	private final KafkaTemplate<Long, TwitterAvroModel> kafkaTemplate;

	public TwitterKafkaProducer(KafkaTemplate<Long, TwitterAvroModel> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public void send(String topicName, Long key, TwitterAvroModel message) {
		LOG.info("Sending message='{}' to topic='{}'", message, topicName);

		CompletableFuture<SendResult<Long, TwitterAvroModel>> kafkaResultFuture = this.kafkaTemplate.send(topicName,
				key, message);
		kafkaResultFuture.whenComplete(getCallback(topicName, message));
	}

	@PreDestroy
	public void close() {
		if (this.kafkaTemplate != null) {
			LOG.info("Closing kafka producer");
			this.kafkaTemplate.destroy();
		}
	}

	private BiConsumer<SendResult<Long, TwitterAvroModel>, Throwable> getCallback(String topicName,
			TwitterAvroModel message) {
		return (result, ex) -> {
			if (ex == null) {
				RecordMetadata metadata = result.getRecordMetadata();
				LOG.debug("Received new metadata. Topic: {}; Partition: {}; Offset: {}; Timestamp: {}, at time: {}",
						metadata.topic(), metadata.partition(), metadata.offset(), metadata.timestamp(),
						System.nanoTime());
			}
			else {
				LOG.error("Error while sending message: {} to topic {}", message, topicName, ex);
			}
		};
	}

}
