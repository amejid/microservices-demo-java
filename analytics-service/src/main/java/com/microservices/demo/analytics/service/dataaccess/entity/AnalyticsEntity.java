package com.microservices.demo.analytics.service.dataaccess.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "twitter-analytics")
public class AnalyticsEntity implements BaseEntity<UUID> {

	@Id
	@NotNull
	@Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	@NotNull
	@Column(name = "word")
	private String word;

	@NotNull
	@Column(name = "word_count")
	private Long wordCount;

	@NotNull
	@Column(name = "record_date")
	private LocalDateTime recordDate;

	@Override
	public final boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AnalyticsEntity)) {
			return false;
		}

		AnalyticsEntity that = (AnalyticsEntity) o;
		return this.id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

}
