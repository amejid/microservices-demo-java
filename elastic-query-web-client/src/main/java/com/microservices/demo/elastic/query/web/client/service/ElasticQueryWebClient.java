package com.microservices.demo.elastic.query.web.client.service;

import com.microservices.demo.elastic.query.web.client.common.model.ElasticQueryWebClientAnalyticsResponseModel;
import com.microservices.demo.elastic.query.web.client.common.model.ElasticQueryWebClientRequestModel;

public interface ElasticQueryWebClient {

	ElasticQueryWebClientAnalyticsResponseModel getDataByText(ElasticQueryWebClientRequestModel requestModel);

}
