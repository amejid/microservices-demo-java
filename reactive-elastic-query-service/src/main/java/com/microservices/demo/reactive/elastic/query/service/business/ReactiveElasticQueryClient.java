package com.microservices.demo.reactive.elastic.query.service.business;

import com.microservices.demo.elastic.model.index.IndexModel;
import reactor.core.publisher.Flux;

public interface ReactiveElasticQueryClient<T extends IndexModel> {

	Flux<T> getIndexModelByText(String text);

}
