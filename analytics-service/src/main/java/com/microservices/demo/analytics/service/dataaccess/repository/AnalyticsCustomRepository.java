package com.microservices.demo.analytics.service.dataaccess.repository;

import java.util.Collection;

public interface AnalyticsCustomRepository<T, P> {

	<S extends T> P persist(S entity);

	<S extends T> void batchPersist(Collection<S> entities);

	<S extends T> S merge(S entity);

	<S extends T> void batchMerge(Collection<S> entities);

	void clear();

}
