package com.microservices.demo.elastic.query.client.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.microservices.demo.config.ElasticConfigData;
import com.microservices.demo.config.ElasticQueryConfigData;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.client.exception.ElasticQueryClientException;
import com.microservices.demo.elastic.query.client.service.ElasticQueryClient;
import com.microservices.demo.elastic.query.client.util.ElasticQueryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class TwitterElasticQueryClient implements ElasticQueryClient<TwitterIndexModel> {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterElasticQueryClient.class);

	private final ElasticConfigData elasticConfigData;

	private final ElasticQueryConfigData elasticQueryConfigData;

	private final ElasticsearchOperations elasticsearchOperations;

	private final ElasticQueryUtil<TwitterIndexModel> elasticQueryUtil;

	public TwitterElasticQueryClient(ElasticConfigData elasticConfigData, ElasticQueryConfigData elasticQueryConfigData,
			ElasticsearchOperations elasticsearchOperations, ElasticQueryUtil<TwitterIndexModel> elasticQueryUtil) {
		this.elasticConfigData = elasticConfigData;
		this.elasticQueryConfigData = elasticQueryConfigData;
		this.elasticsearchOperations = elasticsearchOperations;
		this.elasticQueryUtil = elasticQueryUtil;
	}

	@Override
	public TwitterIndexModel getIndexModelById(String id) {
		Query query = this.elasticQueryUtil.getSearchQueryById(id);
		SearchHit<TwitterIndexModel> searchResult = this.elasticsearchOperations.searchOne(query,
				TwitterIndexModel.class, IndexCoordinates.of(this.elasticConfigData.getIndexName()));
		if (searchResult == null) {
			LOG.error("No document found at elasticsearch with id: {}", id);
			throw new ElasticQueryClientException("No document found at elasticsearch with id: " + id);
		}
		LOG.info("Document with id: {} received successfully", id);
		return searchResult.getContent();
	}

	@Override
	public List<TwitterIndexModel> getIndexModelByText(String text) {
		Query query = this.elasticQueryUtil.getSearchQueryByFieldText(this.elasticQueryConfigData.getTextField(), text);
		return search(query, "{} documents found at elasticsearch with text: {}", text);
	}

	@Override
	public List<TwitterIndexModel> getAllIndexModels() {
		Query query = this.elasticQueryUtil.getSearchQueryForAll();
		return search(query, "{} documents found at elasticsearch");
	}

	private List<TwitterIndexModel> search(Query query, String logMessage, Object... logParams) {
		SearchHits<TwitterIndexModel> searchResult = this.elasticsearchOperations.search(query, TwitterIndexModel.class,
				IndexCoordinates.of(this.elasticConfigData.getIndexName()));
		LOG.info(logMessage, searchResult.getTotalHits(), logParams);
		return searchResult.get().map(SearchHit::getContent).collect(Collectors.toList());
	}

}
