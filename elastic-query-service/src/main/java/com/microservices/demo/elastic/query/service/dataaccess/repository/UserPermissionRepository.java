package com.microservices.demo.elastic.query.service.dataaccess.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.microservices.demo.elastic.query.service.dataaccess.entity.UserPermission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserPermissionRepository extends JpaRepository<UserPermission, UUID> {

	@Query(nativeQuery = true,
			value = "SELECT p.user_permission_id as id, u.username, d.document_id, p.permission_type "
					+ "FROM users u, user_permissions p, documents d " + "WHERE u.id = p.user_id "
					+ "AND d.id = p.document_id " + "AND u.username = :username")
	Optional<List<UserPermission>> findPermissionByUsername(@Param("username") String username);

}
