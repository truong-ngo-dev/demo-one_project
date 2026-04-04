package vn.truongngo.apartcom.one.service.oauth2.infrastructure.persistence.activity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginActivityJpaRepository extends JpaRepository<LoginActivityJpaEntity, String> {
}
