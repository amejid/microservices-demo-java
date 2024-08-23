package com.microservices.demo.analytics.service.transformer;

import java.util.Optional;

import com.microservices.demo.analytics.service.dataaccess.entity.AnalyticsEntity;
import com.microservices.demo.analytics.service.model.AnalyticsResponseModel;

import org.springframework.stereotype.Component;

@Component
public class EntityToResponseModelTransformer {

	public Optional<AnalyticsResponseModel> getResponseModel(AnalyticsEntity analyticsEntity) {
		if (analyticsEntity == null) {
			return Optional.empty();
		}

		return Optional.ofNullable(AnalyticsResponseModel.builder()
			.id(analyticsEntity.getId())
			.word(analyticsEntity.getWord())
			.wordCount(analyticsEntity.getWordCount())
			.build());
	}

}
