package com.microservices.demo.elastic.query.service.model;

import java.util.List;

import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElasticQueryServiceWordCountResponseModel {

	private List<ElasticQueryServiceResponseModel> elasticQueryServiceResponseModels;

	private Long wordCount;

}
