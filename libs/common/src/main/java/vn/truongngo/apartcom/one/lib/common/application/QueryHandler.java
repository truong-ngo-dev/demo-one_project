package vn.truongngo.apartcom.one.lib.common.application;

public interface QueryHandler<Q, R> {
    R handle(Q query);
}
