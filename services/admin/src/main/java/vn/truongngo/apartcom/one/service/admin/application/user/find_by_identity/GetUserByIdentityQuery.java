package vn.truongngo.apartcom.one.service.admin.application.user.find_by_identity;

import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;

public record GetUserByIdentityQuery(
        String identity
) {
    public GetUserByIdentityQuery {
        Assert.hasText(identity, "identity is required");
    }
}
