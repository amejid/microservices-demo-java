package com.microservices.demo.elastic.query.service.common.api.error.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ElasticQueryServiceErrorHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ElasticQueryServiceErrorHandler.class);

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<String> handle(AccessDeniedException ex) {
		LOG.error("Access denied exception: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to access this resource");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handle(IllegalArgumentException ex) {
		LOG.error("Illegal argument exception: {}", ex.getMessage(), ex);
		return ResponseEntity.badRequest().body("Illegal argument exception: " + ex.getMessage());
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<String> handle(RuntimeException ex) {
		LOG.error("Service runtime exception: {}", ex.getMessage(), ex);
		return ResponseEntity.badRequest().body("Service runtime exception: " + ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handle(Exception ex) {
		LOG.error("Internal server error: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body("Internal server error: " + ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handle(MethodArgumentNotValidException ex) {
		LOG.error("Method argument validation exception: {}", ex.getMessage(), ex);
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult()
			.getAllErrors()
			.forEach(error -> errors.put(((FieldError) error).getField(), error.getDefaultMessage()));

		return ResponseEntity.badRequest().body(errors);
	}

}
