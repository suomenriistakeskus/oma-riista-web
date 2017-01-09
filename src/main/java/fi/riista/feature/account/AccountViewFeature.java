package fi.riista.feature.account;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.security.EntityPermission;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class AccountViewFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private PersonRepository personRepository;

    @Transactional(readOnly = true)
    public AccountDTO getAccount(Long personId) {
        final Person person = personRepository.getOne(personId);

        activeUserService.assertHasPermission(person, EntityPermission.READ);

        return AccountDTOBuilder.create().withPerson(person).build();
    }

}
