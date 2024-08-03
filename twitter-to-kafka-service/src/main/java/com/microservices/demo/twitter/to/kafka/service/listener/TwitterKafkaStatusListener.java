package com.microservices.demo.twitter.to.kafka.service.listener;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import com.microservices.demo.kafka.producer.config.service.KafkaProducer;
import com.microservices.demo.twitter.to.kafka.service.transformer.TwitterStatusToAvroTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.StatusAdapter;

import org.springframework.stereotype.Component;

@Component
public class TwitterKafkaStatusListener extends StatusAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterKafkaStatusListener.class);

	private final KafkaConfigData kafkaConfigData;

	private final KafkaProducer<Long, TwitterAvroModel> kafkaProducer;

	private final TwitterStatusToAvroTransformer twitterStatusToAvroTransformer;

	public TwitterKafkaStatusListener(KafkaConfigData kafkaConfigData,
			KafkaProducer<Long, TwitterAvroModel> kafkaProducer,
			TwitterStatusToAvroTransformer twitterStatusToAvroTransformer) {
		this.kafkaConfigData = kafkaConfigData;
		this.kafkaProducer = kafkaProducer;
		this.twitterStatusToAvroTransformer = twitterStatusToAvroTransformer;
	}

	@Override
	public void onStatus(Status status) {
		LOG.info("Received status text {} sending to kafka topic {}", status.getText(),
				this.kafkaConfigData.getTopicName());

		TwitterAvroModel twitterAvroModel = this.twitterStatusToAvroTransformer.getTwitterAvroModelFromStatus(status);

		this.kafkaProducer.send(this.kafkaConfigData.getTopicName(), twitterAvroModel.getUserId(), twitterAvroModel);
	}

}
