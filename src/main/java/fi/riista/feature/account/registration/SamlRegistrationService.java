package fi.riista.feature.account.registration;

import fi.riista.feature.account.audit.AuditService;
import fi.riista.feature.account.password.ChangePasswordService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonIsDeceasedException;
import fi.riista.feature.organization.person.PersonRepository;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
public class SamlRegistrationService {
    private static final Logger LOG = LoggerFactory.getLogger(SamlRegistrationService.class);

    @Resource
    private AuditService auditService;

    @Resource
    private ChangePasswordService changePasswordService;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private UserRepository userRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public SystemUser registerUserAndPerson(final String email, final SamlUserAttributes samlUser) {
        LOG.debug("Received person data from SAML: E-mail: {}", email);

        final Person person = registerPerson(email, samlUser);

        // Disable all previous credentials for same person
        final List<SystemUser> systemUsers = userRepository.findByPerson(person);
        for (final SystemUser otherAccount : systemUsers) {
            if (otherAccount.getRole() == SystemUser.Role.ROLE_USER) {
                otherAccount.setActive(false);
            }
        }

        if (systemUsers.isEmpty()) {
            auditService.log("new-registration", person, auditService.extra("email", email));
        } else {
            auditService.log("re-registration", person, auditService.extra("email", email));
        }

        final SystemUser user = registerUser(email, samlUser);

        if (user.getPerson() != null && !user.getPerson().equals(person)) {
            LOG.warn("Attaching existing userId={} to different person", user.getId());
        }

        // Attach user account to person identified by SSN
        user.setPerson(person);

        // Set account inactive until before registration is complete
        user.setActive(false);

        // Randomize user password before user has provided a new value
        changePasswordService.setUserPassword(user, RandomStringUtils.random(64));

        return user;
    }

    private Person registerPerson(final String email, final SamlUserAttributes samlUser) {
        return personRepository.findBySsn(samlUser.getSsn())
                .map(existingPerson -> {
                    if (existingPerson.isDeceased()) {
                        throw new PersonIsDeceasedException("Cannot register deceased person");
                    }

                    LOG.debug("Handling registration of existing person.");

                    writePersonData(email, samlUser, existingPerson);

                    return existingPerson;
                })
                .orElseGet(() -> {
                    LOG.debug("Registering a new person.");

                    final Person person = new Person();
                    person.setSsn(samlUser.getSsn());
                    writePersonData(email, samlUser, person);

                    return personRepository.save(person);
                });
    }

    private void writePersonData(final String email, final SamlUserAttributes samlUser, final Person person) {
        person.setEmail(email);
        person.setLastName(samlUser.getLastName());
        person.setFirstName(samlUser.getFirstNames());

        // OR-549 Because of MR import database contains invalid byNames. If byName is not overwritten
        // here to a valid value, findBySsn will flush and byName validation fails, and user is not able to register.
        if (StringUtils.hasText(samlUser.getByName())) {
            person.setByName(samlUser.getByName());
        } else {
            person.setByName(samlUser.getFirstNames());
        }
    }

    private SystemUser registerUser(final String email, final SamlUserAttributes samlUser) {
        return Optional.ofNullable(userRepository.findByUsernameIgnoreCase(email))
                .map(existingUser -> {
                    LOG.debug("Handling registration of existing user.");

                    existingUser.setLastName(samlUser.getLastName());
                    existingUser.setFirstName(samlUser.getFirstNames());

                    return existingUser;
                })
                .orElseGet(() -> {
                    LOG.debug("Registering a new user.");

                    final SystemUser user = new SystemUser();
                    user.setUsername(email);
                    user.setEmail(email);
                    user.setLastName(samlUser.getLastName());
                    user.setFirstName(samlUser.getFirstNames());
                    user.setRole(SystemUser.Role.ROLE_USER);

                    return userRepository.save(user);
                });
    }
}
