package vn.truongngo.apartcom.one.lib.common.domain.service;

import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.Id;

import java.util.Optional;

public interface Repository<T extends AggregateRoot<ID>, ID extends Id<?>> {
    Optional<T> findById(ID id);
    void save(T aggregate);
    void delete(ID id);
}
