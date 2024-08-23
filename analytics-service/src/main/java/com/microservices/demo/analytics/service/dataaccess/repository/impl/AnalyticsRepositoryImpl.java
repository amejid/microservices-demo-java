package com.microservices.demo.analytics.service.dataaccess.repository.impl;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import com.microservices.demo.analytics.service.dataaccess.entity.BaseEntity;
import com.microservices.demo.analytics.service.dataaccess.repository.AnalyticsCustomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class AnalyticsRepositoryImpl<T extends BaseEntity<P>, P> implements AnalyticsCustomRepository<T, P> {

	private static final Logger LOG = LoggerFactory.getLogger(AnalyticsRepositoryImpl.class);

	@PersistenceContext
	protected EntityManager entityManager;

	@Value("${spring.jpa.properties.hibernate.jdbc.batch_size:50}")
	protected int batchSize;

	@Override
	@Transactional
	public <S extends T> P persist(S entity) {
		this.entityManager.persist(entity);
		return entity.getId();
	}

	@Override
	@Transactional
	public <S extends T> void batchPersist(Collection<S> entities) {
		if (entities.isEmpty()) {
			LOG.info("No entities to persist");
			return;
		}
		int batchCount = 0;
		for (S entity : entities) {
			LOG.info("Persisting entity with id: {}", entity.getId());
			this.entityManager.persist(entity);
			batchCount++;
			if (batchCount % this.batchSize == 0) {
				this.entityManager.flush();
				this.entityManager.clear();
			}
		}
		if (batchCount % this.batchSize != 0) {
			this.entityManager.flush();
			this.entityManager.clear();
		}
	}

	@Override
	@Transactional
	public <S extends T> S merge(S entity) {
		return this.entityManager.merge(entity);
	}

	@Override
	public <S extends T> void batchMerge(Collection<S> entities) {
		if (entities.isEmpty()) {
			LOG.info("No entities to merge");
			return;
		}
		int batchCount = 0;
		for (S entity : entities) {
			LOG.info("Merging entity with id: {}", entity.getId());
			this.entityManager.merge(entity);
			batchCount++;
			if (batchCount % this.batchSize == 0) {
				this.entityManager.flush();
				this.entityManager.clear();
			}
		}
		if (batchCount % this.batchSize != 0) {
			this.entityManager.flush();
			this.entityManager.clear();
		}
	}

	@Override
	public void clear() {
		this.entityManager.clear();
	}

}
