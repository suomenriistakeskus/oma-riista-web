package fi.riista.feature.account.user;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.account.password.ChangePasswordService;
import fi.riista.security.UserInfo;
import fi.riista.util.DtoUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;

@Component
public class UserCrudFeature extends AbstractCrudFeature<Long, SystemUser, SystemUserDTO> {
    @Resource
    private UserRepository userRepository;

    @Resource
    private ChangePasswordService changePasswordService;

    @Override
    protected JpaRepository<SystemUser, Long> getRepository() {
        return userRepository;
    }

    @Override
    protected SystemUserDTO toDTO(@Nonnull final SystemUser user) {
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
        dto.setTwoFactorAuthentication(user.getTwoFactorAuthentication());

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
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public Page<SystemUserDTO> list(Pageable page) {
        return DtoUtil.toDTO(userRepository.findAll(page), page, this::toDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public Page<SystemUserDTO> listHavingAnyOfRole(List<SystemUser.Role> roles, Pageable page) {
        return DtoUtil.toDTO(userRepository.listHavingAnyOfRole(roles, page), page, this::toDTO);
    }

    @Override
    protected void updateEntity(SystemUser user, SystemUserDTO dto) {
        final UserInfo activeUserInfo = activeUserService.getActiveUserInfoOrNull();

        // FIXME Potential NPE
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
            user.setTwoFactorAuthentication(dto.getTwoFactorAuthentication());

            if (dto.getPassword() != null) {
                changePasswordService.setUserPassword(user, dto.getPassword());
            }

            user.clearPrivileges();
            if (user.getRole() == SystemUser.Role.ROLE_REST) {
                dto.getPrivileges().forEach(user::addPrivilege);
            }
        }
    }
}
