package fi.riista.feature.account;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.audit.AccountActivityMessage;
import fi.riista.feature.account.audit.AccountAuditService;
import fi.riista.feature.account.audit.AuditService;
import fi.riista.feature.account.password.ChangePasswordDTO;
import fi.riista.feature.account.password.ChangePasswordService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonAuthorization;
import fi.riista.security.EntityPermission;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
@PreAuthorize("hasRole('ROLE_USER')")
public class AccountEditFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private AccountSessionService accountSessionService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ChangePasswordService changePasswordService;

    @Resource
    private AccountAuditService accountAuditService;

    @Resource
    private AuditService auditService;

    @Transactional
    public void updateAddress(final AccountAddressDTO dto) {
        final SystemUser activeUser = activeUserService.requireActiveUser();

        if (activeUser.getPerson() != null) {
            auditService.log("updateActiveAccount", activeUser.getPerson());

            updateAddress(dto, activeUser.getPerson());
        }
    }

    @Transactional
    public void updateAddress(final AccountAddressDTO dto, final long personId) {
        final Person person = requireEntityService.requirePerson(personId, EntityPermission.UPDATE);

        auditService.log("updateOtherUserAccount", person);

        updateAddress(dto, person);
    }

    private static void updateAddress(final AccountAddressDTO dto, final Person person) {
        if (!person.isAddressEditable()) {
            throw new IllegalStateException("Address is read-only");
        }

        if (person.getOtherAddress() == null) {
            person.setOtherAddress(new Address());
        }

        person.getOtherAddress().setStreetAddress(dto.getStreetAddress());
        person.getOtherAddress().setCity(dto.getCity());
        person.getOtherAddress().setPostalCode(dto.getPostalCode());
        person.getOtherAddress().setCountry(dto.getCountry());
    }

    @Transactional
    public void updateOtherInfo(final AccountOtherInfoDTO dto) {
        final SystemUser activeUser = activeUserService.requireActiveUser();

        if (activeUser.getPerson() != null) {
            auditService.log("updateActiveAccount", activeUser.getPerson());

            updateOtherInfo(dto, activeUser.getPerson());
        }
    }

    @Transactional
    public void updateOtherInfo(final AccountOtherInfoDTO dto, final long personId) {
        final Person person = requireEntityService.requirePerson(personId, EntityPermission.UPDATE);

        auditService.log("updateOtherUserAccount", person);

        updateOtherInfo(dto, person);
    }

    private static void updateOtherInfo(final AccountOtherInfoDTO dto, final Person person) {
        if (StringUtils.isNotBlank(dto.getByName())) {
            person.setByName(dto.getByName());
        }

        if (!person.isRegistered() && StringUtils.isNotBlank(dto.getEmail())) {
            person.setEmail(dto.getEmail());
        }

        if (StringUtils.isNotBlank(dto.getPhoneNumber())) {
            person.setPhoneNumber(dto.getPhoneNumber());
        }
    }

    @Transactional
    public void changeActiveUserPassword(final ChangePasswordDTO dto) {
        final SystemUser user = activeUserService.requireActiveUser();

        if (user.getRole().isNormalUser() || user.getRole().isModeratorOrAdmin()) {
            changePasswordService.setUserPassword(user, dto.getPassword());
        }

        // Log account activity
        accountAuditService.auditActiveUserEvent(AccountActivityMessage.ActivityType.PASSWORD_CHANGE, null);
    }

    @Transactional
    public void deactivate(final Long personId) {
        final Person person = requireEntityService.requirePerson(personId, PersonAuthorization.Permission.DEACTIVATE);
        person.deactivate();

        accountSessionService.deleteSessions(person.listUsernames());

        auditService.log("deactivate", person);
    }

    @Transactional
    public void toggleActivationOfSrvaFeature(final long personId, final boolean enableSrva) {
        final Person person = requireEntityService.requirePerson(personId, EntityPermission.UPDATE);
        person.setEnableSrva(enableSrva);

        if (enableSrva) {
            auditService.log("SRVA feature activated", person);
        } else {
            auditService.log("SRVA feature deactivated", person);
        }
    }

    @Transactional
    public void toggleActivationOfShootingTestFeature(final long personId, final boolean enableShootingTests) {
        final Person person = requireEntityService.requirePerson(personId, EntityPermission.UPDATE);
        person.setEnableShootingTests(enableShootingTests);

        if (enableShootingTests) {
            auditService.log("Shooting test supervisor feature activated", person);
        } else {
            auditService.log("Shooting test supervisor feature deactivated", person);
        }
    }
}
