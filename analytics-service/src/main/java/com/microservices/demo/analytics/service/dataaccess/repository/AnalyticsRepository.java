package com.microservices.demo.analytics.service.dataaccess.repository;

import java.util.List;
import java.util.UUID;

import com.microservices.demo.analytics.service.dataaccess.entity.AnalyticsEntity;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AnalyticsRepository
		extends JpaRepository<AnalyticsEntity, UUID>, AnalyticsCustomRepository<AnalyticsEntity, UUID> {

	@Query("SELECT e FROM AnalyticsEntity e WHERE e.word = :word ORDER BY e.recordDate DESC")
	List<AnalyticsEntity> getAnalyticsEntitiesByWord(String word, Pageable pageable);

}
