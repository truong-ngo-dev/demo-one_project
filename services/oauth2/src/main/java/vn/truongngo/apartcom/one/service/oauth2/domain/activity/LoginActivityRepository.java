package vn.truongngo.apartcom.one.service.oauth2.domain.activity;

import vn.truongngo.apartcom.one.lib.common.domain.service.Repository;

public interface LoginActivityRepository extends Repository<LoginActivity, LoginActivityId> {

    default void delete(LoginActivityId id) {
        throw new UnsupportedOperationException("LoginActivity is append-only, cannot be delete");
    }
}
