package com.microservices.demo.elastic.query.service.model.assembler;

import java.util.List;
import java.util.stream.Collectors;

import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.service.api.ElasticDocumentController;
import com.microservices.demo.elastic.query.service.model.ElasticQueryServiceResponseModel;
import com.microservices.demo.elastic.query.service.transformer.ElasticToResponseModelTransformer;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class ElasticQueryServiceResponseModelAssembler
		extends RepresentationModelAssemblerSupport<TwitterIndexModel, ElasticQueryServiceResponseModel> {

	private final ElasticToResponseModelTransformer elasticToResponseModelTransformer;

	public ElasticQueryServiceResponseModelAssembler(
			ElasticToResponseModelTransformer elasticToResponseModelTransformer) {
		super(ElasticDocumentController.class, ElasticQueryServiceResponseModel.class);
		this.elasticToResponseModelTransformer = elasticToResponseModelTransformer;
	}

	@Override
	public ElasticQueryServiceResponseModel toModel(TwitterIndexModel entity) {
		ElasticQueryServiceResponseModel responseModel = this.elasticToResponseModelTransformer
			.getResponseModel(entity);
		responseModel.add(WebMvcLinkBuilder
			.linkTo(WebMvcLinkBuilder.methodOn(ElasticDocumentController.class).getDocumentById(entity.getId()))
			.withSelfRel());
		responseModel.add(WebMvcLinkBuilder.linkTo(ElasticDocumentController.class).withRel("documents"));

		return responseModel;
	}

	public List<ElasticQueryServiceResponseModel> toModels(List<TwitterIndexModel> entities) {
		return entities.stream().map(this::toModel).collect(Collectors.toList());
	}

}
