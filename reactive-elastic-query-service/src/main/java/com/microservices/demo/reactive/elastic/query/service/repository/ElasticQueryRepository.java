package com.microservices.demo.reactive.elastic.query.service.repository;

import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import reactor.core.publisher.Flux;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticQueryRepository extends ReactiveCrudRepository<TwitterIndexModel, String> {

	Flux<TwitterIndexModel> findByText(String text);

}
