package com.microservices.demo.elastic.query.web.client.config;

import java.util.List;
import java.util.stream.Collectors;

import com.microservices.demo.config.ElasticQueryWebClientConfigData;
import reactor.core.publisher.Flux;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Primary
public class ElasticQueryServiceInstanceListSupplierConfig implements ServiceInstanceListSupplier {

	private final ElasticQueryWebClientConfigData.WebClient webClientConfig;

	public ElasticQueryServiceInstanceListSupplierConfig(ElasticQueryWebClientConfigData webClientConfigData) {
		this.webClientConfig = webClientConfigData.getWebclient();
	}

	@Override
	public String getServiceId() {
		return this.webClientConfig.getServiceId();
	}

	@Override
	public Flux<List<ServiceInstance>> get() {
		return Flux.just(this.webClientConfig.getInstances()
			.stream()
			.map(instance -> new DefaultServiceInstance(instance.getId(), getServiceId(), instance.getHost(),
					instance.getPort(), false))
			.collect(Collectors.toList()));
	}

}
