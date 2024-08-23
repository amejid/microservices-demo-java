package com.microservices.demo.mdc.interceptor;

import com.microservices.demo.mdc.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.IdGenerator;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MDCHandlerInterceptor implements HandlerInterceptor {

	private final IdGenerator idGenerator;

	public MDCHandlerInterceptor(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
			throws Exception {
		String correlationId = request.getHeader(Constants.CORRELATION_ID_HEADER);
		if (StringUtils.hasLength(correlationId)) {
			MDC.put(Constants.CORRELATION_ID_KEY, correlationId);
		}
		else {
			MDC.put(Constants.CORRELATION_ID_KEY, getNewCorrelationId());
		}
		return true;
	}

	@Override
	public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull Object handler, Exception ex) throws Exception {
		MDC.remove(Constants.CORRELATION_ID_KEY);
	}

	private String getNewCorrelationId() {
		return this.idGenerator.generateId().toString();
	}

}
