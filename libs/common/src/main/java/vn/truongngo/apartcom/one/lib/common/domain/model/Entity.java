package vn.truongngo.apartcom.one.lib.common.domain.model;

public interface Entity<T extends Id<?>> {
    T getId();
}
