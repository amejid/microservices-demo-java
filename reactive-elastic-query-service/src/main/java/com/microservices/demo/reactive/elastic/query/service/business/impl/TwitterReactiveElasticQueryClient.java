package com.microservices.demo.reactive.elastic.query.service.business.impl;

import java.time.Duration;

import com.microservices.demo.config.ElasticQueryServiceConfigData;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.reactive.elastic.query.service.business.ReactiveElasticQueryClient;
import com.microservices.demo.reactive.elastic.query.service.repository.ElasticQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.stereotype.Service;

@Service
public class TwitterReactiveElasticQueryClient implements ReactiveElasticQueryClient<TwitterIndexModel> {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterReactiveElasticQueryClient.class);

	private final ElasticQueryRepository elasticQueryRepository;

	private final ElasticQueryServiceConfigData elasticQueryServiceConfigData;

	public TwitterReactiveElasticQueryClient(ElasticQueryRepository elasticQueryRepository,
			ElasticQueryServiceConfigData elasticQueryServiceConfigData) {
		this.elasticQueryRepository = elasticQueryRepository;
		this.elasticQueryServiceConfigData = elasticQueryServiceConfigData;
	}

	@Override
	public Flux<TwitterIndexModel> getIndexModelByText(String text) {
		LOG.info("Getting data from elasticsearch for text: {}", text);
		return this.elasticQueryRepository.findByText(text)
			.delayElements(Duration.ofMillis(this.elasticQueryServiceConfigData.getBackPressureDelayMs()));
	}

}
