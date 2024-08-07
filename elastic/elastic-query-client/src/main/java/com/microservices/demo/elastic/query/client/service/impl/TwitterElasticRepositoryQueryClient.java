package com.microservices.demo.elastic.query.client.service.impl;

import java.util.List;
import java.util.Optional;

import com.microservices.demo.common.util.CollectionsUtil;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.client.exception.ElasticQueryClientException;
import com.microservices.demo.elastic.query.client.repository.TwitterElasticsearchQueryRepository;
import com.microservices.demo.elastic.query.client.service.ElasticQueryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class TwitterElasticRepositoryQueryClient implements ElasticQueryClient<TwitterIndexModel> {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterElasticRepositoryQueryClient.class);

	private final TwitterElasticsearchQueryRepository twitterElasticsearchQueryRepository;

	public TwitterElasticRepositoryQueryClient(
			TwitterElasticsearchQueryRepository twitterElasticsearchQueryRepository) {
		this.twitterElasticsearchQueryRepository = twitterElasticsearchQueryRepository;
	}

	@Override
	public TwitterIndexModel getIndexModelById(String id) {
		Optional<TwitterIndexModel> searchResult = this.twitterElasticsearchQueryRepository.findById(id);
		LOG.info("Document with id: {} retrieved successfully",
				searchResult
					.orElseThrow(
							() -> new ElasticQueryClientException("No document found at elasticsearch with id: " + id))
					.getId());
		return searchResult.get();
	}

	@Override
	public List<TwitterIndexModel> getIndexModelByText(String text) {
		List<TwitterIndexModel> searchResult = this.twitterElasticsearchQueryRepository.findByText(text);
		LOG.info("{} documents found at elasticsearch with text: {}", searchResult.size(), text);
		return searchResult;
	}

	@Override
	public List<TwitterIndexModel> getAllIndexModels() {
		List<TwitterIndexModel> searchResult = CollectionsUtil.getInstance()
			.getListFromIterable(this.twitterElasticsearchQueryRepository.findAll());
		LOG.info("{} documents found at elasticsearch", searchResult.size());
		return searchResult;
	}

}
