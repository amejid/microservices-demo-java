package com.microservices.demo.elastic.query.service.business;

import java.util.List;

import com.microservices.demo.elastic.query.service.model.ElasticQueryServiceResponseModel;

public interface ElasticQueryService {

	ElasticQueryServiceResponseModel getDocumentById(String id);

	List<ElasticQueryServiceResponseModel> getDocumentsByText(String text);

	List<ElasticQueryServiceResponseModel> getAllDocuments();

}
