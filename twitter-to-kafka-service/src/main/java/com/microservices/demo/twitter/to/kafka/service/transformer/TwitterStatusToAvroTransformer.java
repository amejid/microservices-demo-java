package com.microservices.demo.twitter.to.kafka.service.transformer;

import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import twitter4j.Status;

import org.springframework.stereotype.Component;

@Component
public class TwitterStatusToAvroTransformer {

	public TwitterAvroModel getTwitterAvroModelFromStatus(Status status) {
		return TwitterAvroModel.newBuilder()
			.setId(status.getId())
			.setUserId(status.getUser().getId())
			.setText(status.getText())
			.setCreatedAt(status.getCreatedAt().getTime())
			.build();
	}

}
