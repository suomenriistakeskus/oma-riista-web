package fi.riista.feature.account;

import com.google.common.base.Preconditions;
import fi.riista.feature.account.password.ChangePasswordDTO;
import fi.riista.feature.account.audit.AccountActivityMessage;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.audit.AccountAuditService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.password.ChangePasswordService;
import fi.riista.feature.account.audit.AuditService;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.organization.person.PersonAuthorization;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.security.EntityPermission;
import fi.riista.security.authentication.CustomSpringSessionRememberMeServices;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME;

@Component
@PreAuthorize("hasRole('ROLE_USER')")
public class AccountEditFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ChangePasswordService changePasswordService;

    @Resource
    private AccountAuditService accountAuditService;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private AccountRoleService roleService;

    @Resource
    private AuditService auditService;

    @Resource
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Transactional(readOnly = true)
    public AccountDTO getActiveAccount(final HttpServletRequest request) {
        final SystemUser activeUser = activeUserService.getActiveUser();
        final boolean isRememberMe = CustomSpringSessionRememberMeServices.isRememberMeActive(request);

        activeUserService.assertHasPermission(activeUser, EntityPermission.READ);

        return AccountDTOBuilder.create()
                .withRememberMe(isRememberMe)
                .withUser(activeUser)
                .withRoles(roleService.getRoles(activeUser)).build();
    }

    @Transactional
    public void updateActiveAccount(AccountDTO dto) {
        final SystemUser activeUser = activeUserService.getActiveUser();
        activeUserService.assertHasPermission(activeUser, EntityPermission.UPDATE);

        if (dto.getLocale() != null) {
            activeUser.setLocale(dto.getLocale());
        }

        if (dto.getTimeZone() != null) {
            activeUser.setTimeZone(dto.getTimeZone());
        }
        updatePerson(dto, activeUser.getPerson());
        auditService.log("updateActiveAccount", activeUser.getPerson());
    }

    @Transactional
    public void updateOtherUserAccount(AccountDTO dto) {
        Person person = personRepository.findOne(dto.getPersonId());
        activeUserService.assertHasPermission(person, EntityPermission.UPDATE);
        updatePerson(dto, person);
        auditService.log("updateOtherUserAccount", person);
    }

    private static void updatePerson(AccountDTO dto, Person person) {
        if (person == null) {
            return;
        }

        if (StringUtils.isNotBlank(dto.getByName())) {
            person.setByName(dto.getByName());
        }
        if (!person.isRegistered() && StringUtils.isNotBlank(dto.getEmail())) {
            person.setEmail(dto.getEmail());
        }
        if (StringUtils.isNotBlank(dto.getPhoneNumber())) {
            person.setPhoneNumber(dto.getPhoneNumber());
        }

        if (person.isAddressEditable() && dto.getAddress() != null) {
            if (person.getOtherAddress() == null) {
                person.setOtherAddress(new Address());
            }

            person.getOtherAddress().setStreetAddress(dto.getAddress().getStreetAddress());
            person.getOtherAddress().setCity(dto.getAddress().getCity());
            person.getOtherAddress().setPostalCode(dto.getAddress().getPostalCode());
            person.getOtherAddress().setCountry(dto.getAddress().getCountry());
        }
    }

    @Transactional
    public void changeActiveUserPassword(ChangePasswordDTO dto) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(dto.getPassword()), "No password given");

        final SystemUser user = activeUserService.getActiveUser();

        activeUserService.assertHasPermission(user, EntityPermission.UPDATE);

        changePasswordService.setUserPassword(user, dto.getPassword());

        // Log account activity
        accountAuditService.auditUserEvent(user, activeUserService.getAuthentication(),
                AccountActivityMessage.ActivityType.PASSWORD_CHANGE, null);
    }

    @Transactional
    public void deactivate(final Long personId) {
        final Person person = requireEntityService.requirePerson(personId, PersonAuthorization.Permission.DEACTIVATE);
        person.deactivate();

        deleteSessions(person.listUsernames());

        auditService.log("deactivate", person);
    }

    private void deleteSessions(final List<String> usernames) {
        usernames.stream()
                .flatMap(this::findSessionKeysByUsername)
                .forEach(sessionRepository::delete);
    }

    private Stream<String> findSessionKeysByUsername(final String username) {
        return sessionRepository.findByIndexNameAndIndexValue(PRINCIPAL_NAME_INDEX_NAME, username).keySet().stream();
    }

    @Transactional
    public void updateSrvaEnabled(boolean enableSrva) {
        final SystemUser activeUser = activeUserService.getActiveUser();
        activeUserService.assertHasPermission(activeUser, EntityPermission.UPDATE);

        final Person person = activeUser.getPerson();
        person.setEnableSrva(enableSrva);
        if (enableSrva) {
            auditService.log("SRVA activated", person);
        } else {
            auditService.log("SRVA deativated", person);
        }
    }
}
