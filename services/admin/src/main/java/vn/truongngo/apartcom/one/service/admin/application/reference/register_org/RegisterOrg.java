package vn.truongngo.apartcom.one.service.admin.application.reference.register_org;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.reference.OrgReference;
import vn.truongngo.apartcom.one.service.admin.domain.reference.OrgReferenceRepository;

public class RegisterOrg {

    public record Command(String orgId, String name, String orgType) {
        public Command {
            Assert.hasText(orgId, "orgId is required");
            Assert.hasText(name, "name is required");
            Assert.hasText(orgType, "orgType is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final OrgReferenceRepository orgReferenceRepository;

        @Override
        @Transactional
        public Void handle(Command command) {
            if ("BQL".equals(command.orgType())) {
                orgReferenceRepository.upsert(
                        OrgReference.of(command.orgId(), command.name(), command.orgType()));
            }
            return null;
        }
    }
}
