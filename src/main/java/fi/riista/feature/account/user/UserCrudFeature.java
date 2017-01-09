package fi.riista.feature.account.user;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.SimpleAbstractCrudFeature;
import fi.riista.feature.account.password.ChangePasswordService;
import fi.riista.feature.common.entity.EntityAuditFields;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.security.UserInfo;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static fi.riista.util.jpa.JpaSpecs.inCollection;
import static org.springframework.data.jpa.domain.Specifications.where;

@Component
public class UserCrudFeature extends SimpleAbstractCrudFeature<Long, SystemUser, SystemUserDTO> {
    @Resource
    private UserRepository userRepository;

    @Resource
    private ChangePasswordService changePasswordService;

    @Override
    protected JpaRepository<SystemUser, Long> getRepository() {
        return userRepository;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public Page<SystemUserDTO> list(Pageable page) {
        return toDTO(userRepository.findAll(page), page);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public Page<SystemUserDTO> listHavingAnyOfRole(List<SystemUser.Role> roles, Pageable page) {
        return toDTO(userRepository.listHavingAnyOfRole(roles, page), page);
    }

    @Override
    protected void updateEntity(SystemUser user, SystemUserDTO dto) {
        final UserInfo activeUserInfo = activeUserService.getActiveUserInfo();

        if (activeUserInfo.isAdmin()) {
            if (user.isNew()) {
                user.setUsername(dto.getUsername());
            }

            if (dto.getRole() != null) {
                user.setRole(dto.getRole());
            } else if (user.getRole() == null) {
                user.setRole(SystemUser.Role.ROLE_USER);
            }

            user.setActive(dto.isActive());
            user.setEmail(dto.getEmail());
            user.setPhoneNumber(dto.getPhoneNumber());
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setLocale(dto.getLocale());
            user.setTimeZone(dto.getTimeZone());
            user.setIpWhiteList(dto.getIpWhiteList());

            if (dto.getPassword() != null) {
                changePasswordService.setUserPassword(user, dto.getPassword());
            }

            user.clearPrivileges();
            if (user.getRole() == SystemUser.Role.ROLE_REST) {
                dto.getPrivileges().forEach(user::addPrivilege);
            }
        }
    }

    @Override
    protected Function<SystemUser, SystemUserDTO> entityToDTOFunction() {
        return user -> {
            final SystemUserDTO dto = new SystemUserDTO();

            dto.setId(user.getId());
            dto.setRev(user.getConsistencyVersion());
            dto.setUsername(user.getUsername());
            dto.setActive(user.isActive());
            dto.setRole(user.getRole());
            dto.setEmail(user.getEmail());
            dto.setPhoneNumber(user.getPhoneNumber());
            dto.setLocale(user.getLocale());
            dto.setTimeZone(user.getTimeZone());
            dto.setIpWhiteList(user.getIpWhiteList());

            if (user.getPerson() != null) {
                dto.setFirstName(user.getPerson().getFirstName());
                dto.setLastName(user.getPerson().getLastName());
            } else {
                dto.setFirstName(user.getFirstName());
                dto.setLastName(user.getLastName());
                dto.setNameEditable(true);
            }
            if (user.getRole() == SystemUser.Role.ROLE_REST) {
                dto.setPrivileges(ImmutableSet.copyOf(user.getPrivileges()));
            }
            return dto;
        };
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public Map<Long, SystemUser> getModeratorCreatorsGroupedById(
            final Iterable<? extends LifecycleEntity<? extends Long>> lifecycleEntities) {

        final Set<Long> creatorIds = F.mapNonNullsToSet(lifecycleEntities, entity -> Optional.ofNullable(entity)
                .map(LifecycleEntity::getAuditFields)
                .map(EntityAuditFields::getCreatedByUserId)
                .orElse(null));

        return F.indexById(userRepository.findAll(where(
                inCollection(SystemUser_.id, creatorIds))
                .and(JpaSpecs.inCollection(SystemUser_.role, EnumSet.of(SystemUser.Role.ROLE_ADMIN, SystemUser.Role.ROLE_MODERATOR)))));
    }

}
