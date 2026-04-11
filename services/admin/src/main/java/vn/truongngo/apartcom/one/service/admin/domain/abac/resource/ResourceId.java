package vn.truongngo.apartcom.one.service.admin.domain.abac.resource;

public class ResourceId {

    private final Long value;

    private ResourceId(Long value) {
        this.value = value;
    }

    public static ResourceId of(Long value) {
        return new ResourceId(value);
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceId other)) return false;
        return value != null && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
