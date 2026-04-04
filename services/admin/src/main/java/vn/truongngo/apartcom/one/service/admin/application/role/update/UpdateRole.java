package vn.truongngo.apartcom.one.service.admin.application.role.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.role.Role;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleException;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleRepository;

import java.time.Instant;

public class UpdateRole {

    public record Command(String id, String name, String description) {
        public Command {
            Assert.hasText(id, "id is required");
        }
    }

    public record RoleDetail(String id, String name, String description, Instant createdAt) {}

    static class Mapper {
        static RoleDetail toDetail(Role role) {
            Instant createdAt = role.getAuditable() != null && role.getAuditable().getCreatedAt() != null
                    ? Instant.ofEpochMilli(role.getAuditable().getCreatedAt())
                    : null;
            return new RoleDetail(role.getId().getValue(), role.getName(), role.getDescription(), createdAt);
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, RoleDetail> {

        private final RoleRepository roleRepository;

        @Override
        @Transactional
        public RoleDetail handle(Command command) {
            if (command.name() != null) {
                throw RoleException.nameImmutable();
            }

            Role role = roleRepository.findById(RoleId.of(command.id()))
                    .orElseThrow(RoleException::notFound);

            Role updated = role.updateDescription(command.description());
            roleRepository.save(updated);

            return Mapper.toDetail(updated);
        }
    }
}
