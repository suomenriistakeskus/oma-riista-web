package fi.riista.feature.account;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
public class AccountShootingTestFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private AccountShootingTestService shootingTestService;

    @Resource
    private PersonRepository personRepository;

    @Transactional(readOnly = true)
    public List<AccountShootingTestDTO> listMyShootingTests(final Long personId) {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        Person person = activeUser.getPerson();

        if (person == null) {
            if (personId != null && activeUser.isModeratorOrAdmin()) {
                person = personRepository.getOne(personId);
            } else {
                return Collections.emptyList();
            }
        }

        return shootingTestService.listQualifiedShootingTests(person, LocaleContextHolder.getLocale());
    }
}
