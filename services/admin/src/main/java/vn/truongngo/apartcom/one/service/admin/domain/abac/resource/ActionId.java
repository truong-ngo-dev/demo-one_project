package vn.truongngo.apartcom.one.service.admin.domain.abac.resource;

public class ActionId {

    private final Long value;

    private ActionId(Long value) {
        this.value = value;
    }

    public static ActionId of(Long value) {
        return new ActionId(value);
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActionId other)) return false;
        return value != null && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
