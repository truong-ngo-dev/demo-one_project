package vn.truongngo.apartcom.one.lib.common.application;

public interface CommandHandler<C, R> {
    R handle(C command);
}
