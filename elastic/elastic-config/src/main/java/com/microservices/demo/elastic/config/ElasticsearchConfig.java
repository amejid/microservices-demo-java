package com.microservices.demo.elastic.config;

import com.microservices.demo.config.ElasticConfigData;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.microservices.demo.elastic")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

	private final ElasticConfigData elasticConfigData;

	public ElasticsearchConfig(ElasticConfigData elasticConfigData) {
		this.elasticConfigData = elasticConfigData;
	}

	@Override
	public ClientConfiguration clientConfiguration() {
		return ClientConfiguration.builder()
			.connectedTo(this.elasticConfigData.getConnectionUrl())
			.withConnectTimeout(this.elasticConfigData.getConnectTimeoutMs())
			.withSocketTimeout(this.elasticConfigData.getSocketTimeoutMs())
			.build();
	}

}
