package com.microservices.demo.elastic.query.service.business.impl;

import java.util.List;

import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.client.service.ElasticQueryClient;
import com.microservices.demo.elastic.query.service.business.ElasticQueryService;
import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import com.microservices.demo.elastic.query.service.model.assembler.ElasticQueryServiceResponseModelAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service
public class TwitterElasticQueryService implements ElasticQueryService {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterElasticQueryService.class);

	private final ElasticQueryServiceResponseModelAssembler elasticQueryServiceResponseModelAssembler;

	private final ElasticQueryClient<TwitterIndexModel> elasticQueryClient;

	public TwitterElasticQueryService(
			ElasticQueryServiceResponseModelAssembler elasticQueryServiceResponseModelAssembler,
			ElasticQueryClient<TwitterIndexModel> elasticQueryClient) {
		this.elasticQueryServiceResponseModelAssembler = elasticQueryServiceResponseModelAssembler;
		this.elasticQueryClient = elasticQueryClient;
	}

	@Override
	public ElasticQueryServiceResponseModel getDocumentById(String id) {
		LOG.info("Querying elasticsearch for document with id {}", id);
		return this.elasticQueryServiceResponseModelAssembler.toModel(this.elasticQueryClient.getIndexModelById(id));
	}

	@Override
	public List<ElasticQueryServiceResponseModel> getDocumentsByText(String text) {
		LOG.info("Querying elasticsearch for documents with text {}", text);
		return this.elasticQueryServiceResponseModelAssembler
			.toModels(this.elasticQueryClient.getIndexModelByText(text));
	}

	@Override
	public List<ElasticQueryServiceResponseModel> getAllDocuments() {
		LOG.info("Querying elasticsearch for all documents");
		return this.elasticQueryServiceResponseModelAssembler.toModels(this.elasticQueryClient.getAllIndexModels());
	}

}
