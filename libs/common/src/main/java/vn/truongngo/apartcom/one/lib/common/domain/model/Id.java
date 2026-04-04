package vn.truongngo.apartcom.one.lib.common.domain.model;

public interface Id<ID> extends ValueObject {

    ID getValue();

    default String getValueAsString() {
        return getValue().toString();
    }
}
