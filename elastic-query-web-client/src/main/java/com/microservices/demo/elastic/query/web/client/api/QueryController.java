package com.microservices.demo.elastic.query.web.client.api;

import com.microservices.demo.elastic.query.web.client.common.model.ElasticQueryWebClientAnalyticsResponseModel;
import com.microservices.demo.elastic.query.web.client.common.model.ElasticQueryWebClientRequestModel;
import com.microservices.demo.elastic.query.web.client.service.ElasticQueryWebClient;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class QueryController {

	private static final Logger LOG = LoggerFactory.getLogger(QueryController.class);

	private final ElasticQueryWebClient elasticQueryWebClient;

	public QueryController(ElasticQueryWebClient elasticQueryWebClient) {
		this.elasticQueryWebClient = elasticQueryWebClient;
	}

	@GetMapping
	public String index() {
		return "index";
	}

	@GetMapping("/error")
	public String error() {
		return "error";
	}

	@GetMapping("/home")
	public String home(Model model) {
		model.addAttribute("elasticQueryWebClientRequestModel", ElasticQueryWebClientRequestModel.builder().build());
		return "home";
	}

	@PostMapping("/query-by-text")
	public String queryByText(@Valid ElasticQueryWebClientRequestModel requestModel, Model model) {
		LOG.info("Query by text : {}", requestModel.getText());
		ElasticQueryWebClientAnalyticsResponseModel responseModel = this.elasticQueryWebClient
			.getDataByText(requestModel);
		model.addAttribute("elasticQueryWebClientResponseModels", responseModel.getQueryResponseModels());
		model.addAttribute("wordCount", responseModel.getWordCount());
		model.addAttribute("searchText", requestModel.getText());
		model.addAttribute("elasticQueryWebClientRequestModel", ElasticQueryWebClientRequestModel.builder().build());
		return "home";
	}

}
