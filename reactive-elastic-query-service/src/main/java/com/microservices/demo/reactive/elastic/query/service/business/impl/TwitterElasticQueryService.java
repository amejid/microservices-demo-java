package com.microservices.demo.reactive.elastic.query.service.business.impl;

import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import com.microservices.demo.elastic.query.service.common.transformer.ElasticToResponseModelTransformer;
import com.microservices.demo.reactive.elastic.query.service.business.ElasticQueryService;
import com.microservices.demo.reactive.elastic.query.service.business.ReactiveElasticQueryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.stereotype.Service;

@Service
public class TwitterElasticQueryService implements ElasticQueryService {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterElasticQueryService.class);

	private final ReactiveElasticQueryClient<TwitterIndexModel> reactiveElasticQueryClient;

	private final ElasticToResponseModelTransformer elasticToResponseModelTransformer;

	public TwitterElasticQueryService(ReactiveElasticQueryClient<TwitterIndexModel> reactiveElasticQueryClient,
			ElasticToResponseModelTransformer elasticToResponseModelTransformer) {
		this.reactiveElasticQueryClient = reactiveElasticQueryClient;
		this.elasticToResponseModelTransformer = elasticToResponseModelTransformer;
	}

	@Override
	public Flux<ElasticQueryServiceResponseModel> getDocumentByText(String text) {
		LOG.info("Querying reactive elasticsearch for text: {}", text);
		return this.reactiveElasticQueryClient.getIndexModelByText(text)
			.map(this.elasticToResponseModelTransformer::getResponseModel);
	}

}
