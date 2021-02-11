package fi.riista.feature.account;

import fi.riista.feature.account.pilot.DeerPilotService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.security.EntityPermission;
import fi.riista.security.authentication.CustomSpringSessionRememberMeServices;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Component
public class AccountViewFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private AccountRoleService roleService;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private DeerPilotService deerPilotService;

    @Transactional(readOnly = true)
    public AccountDTO getActiveAccount(final HttpServletRequest request) {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final boolean isRememberMe = CustomSpringSessionRememberMeServices.isRememberMeActive(request);
        final boolean isDeerPilotUser = deerPilotService.isPilotUser();

        return AccountDTOBuilder.create()
                .withRememberMe(isRememberMe)
                .withUser(activeUser)
                .withRoles(roleService.getRoles(activeUser))
                .withDeerPilot(isDeerPilotUser)
                .build();
    }

    @Transactional(readOnly = true)
    public AccountDTO getAccount(final Long personId) {
        final Person person = personRepository.getOne(personId);

        activeUserService.assertHasPermission(person, EntityPermission.READ);

        return AccountDTOBuilder.create().withPerson(person).build();
    }
}
