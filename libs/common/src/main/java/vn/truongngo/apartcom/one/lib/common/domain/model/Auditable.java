package vn.truongngo.apartcom.one.lib.common.domain.model;

public class Auditable implements ValueObject {

    private final Long createdAt;
    private final Long updatedAt;
    private final String createdBy;
    private final String updatedBy;

    public Auditable(Long createdAt, Long updatedAt, String createdBy, String updatedBy) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}
