package com.microservices.demo.elastic.query.client.repository;

import java.util.List;

import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TwitterElasticsearchQueryRepository extends ElasticsearchRepository<TwitterIndexModel, String> {

	List<TwitterIndexModel> findByText(String text);

}
