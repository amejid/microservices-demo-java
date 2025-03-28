package com.microservices.demo.elastic.index.client.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.microservices.demo.elastic.index.client.repository.TwitterElasticsearchIndexRepository;
import com.microservices.demo.elastic.index.client.service.ElasticIndexClient;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "elastic-config.is-repository", havingValue = "true", matchIfMissing = true)
public class TwitterElasticRepositoryIndexClient implements ElasticIndexClient<TwitterIndexModel> {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterElasticRepositoryIndexClient.class);

	private final TwitterElasticsearchIndexRepository twitterElasticsearchIndexRepository;

	public TwitterElasticRepositoryIndexClient(
			TwitterElasticsearchIndexRepository twitterElasticsearchIndexRepository) {
		this.twitterElasticsearchIndexRepository = twitterElasticsearchIndexRepository;
	}

	@Override
	public List<String> save(List<TwitterIndexModel> documents) {
		List<TwitterIndexModel> repositoryResponse = (List<TwitterIndexModel>) this.twitterElasticsearchIndexRepository
			.saveAll(documents);
		List<String> ids = repositoryResponse.stream().map(TwitterIndexModel::getId).collect(Collectors.toList());
		LOG.info("Documents indexed successfully with type: {} and ids: {}", TwitterIndexModel.class.getName(), ids);
		return ids;
	}

}
