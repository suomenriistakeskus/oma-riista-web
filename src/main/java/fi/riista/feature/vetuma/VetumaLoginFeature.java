package fi.riista.feature.vetuma;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.account.password.ChangePasswordService;
import fi.riista.feature.account.audit.AuditService;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonIsDeceasedException;
import fi.riista.feature.organization.address.AddressRepository;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.vetuma.dto.VetumaLoginResponseDTO;
import fi.riista.feature.vetuma.entity.VetumaTransaction;
import fi.riista.feature.vetuma.entity.VetumaTransactionStatus;
import fi.riista.feature.vetuma.repository.VetumaTransactionRepository;
import fi.riista.feature.vetuma.support.VtjData;
import fi.riista.util.Locales;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

@Component
public class VetumaLoginFeature {

    private static final Logger LOG = LoggerFactory.getLogger(VetumaLoginFeature.class);

    @Resource
    private PersonRepository personRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private AddressRepository addressRepository;

    @Resource
    private MunicipalityRepository municipalityRepository;

    @Resource
    private VetumaTransactionRepository vetumaTransactionRepository;

    @Resource
    private VetumaConfig vetumaConfig;

    @Resource
    private ChangePasswordService changePasswordService;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Resource
    private AuditService auditService;

    private boolean checkRequestMAC(VetumaLoginResponseDTO response) {
        final String computedMac = response.computeMac(vetumaConfig.getShareSecretKey());

        if (computedMac.equals(response.getMAC())) {
            LOG.debug("Vetuma login response: MAC verification passed");

            return true;
        }

        LOG.error("Vetuma login response: Incorrect MAC: expected {} but was {}", computedMac, response.getMAC());

        return false;
    }

    @Transactional
    public void handleCancel(final VetumaLoginResponseDTO response) {
        final VetumaTransaction vetumaTransaction = vetumaTransactionRepository.getOne(response.getTRID());

        checkRequestMAC(response);

        vetumaTransaction.setStatusCancel(response.getSO());
    }

    @Transactional
    public void handleError(final VetumaLoginResponseDTO response) {
        final VetumaTransaction vetumaTransaction = vetumaTransactionRepository.getOne(response.getTRID());

        checkRequestMAC(response);

        vetumaTransaction.setStatusError(response.getSO());
    }

    @Transactional
    public boolean handleSuccess(final VetumaLoginResponseDTO response) {
        final VetumaTransaction vetumaTransaction = vetumaTransactionRepository.findOne(response.getTRID());

        if (vetumaTransaction == null) {
            LOG.error("Vetuma login response: Could not commit Vetuma transaction with id={}", response.getTRID());

            return false;
        }

        if (vetumaTransaction.getStatus() != VetumaTransactionStatus.INIT) {
            LOG.error("Vetuma transaction {} status was {}", vetumaTransaction.getId(), vetumaTransaction.getStatus());

            return false;
        }

        if (!checkRequestMAC(response)) {
            vetumaTransaction.setStatusMACError(response.getSO());

            return false;

        } else if (response.isExpiredNow(vetumaConfig.getVetumaResponseExpirationSeconds())) {
            LOG.warn("Vetuma login response expired: " + response.getTimestamp());

            vetumaTransaction.setStatusTimeout(response.getSO());

            return false;

        } else if (!VetumaLoginResponseDTO.Status.SUCCESSFUL.equals(response.getSTATUS())) {
            LOG.error("Vetuma login response:\nSTATUS = {}", response.getSTATUS());

            vetumaTransaction.setStatusError(response.getSO());

            return false;
        }

        if (vetumaTransaction.isExpiredNow(vetumaConfig.getVetumaTransactionTimeout())) {
            LOG.error("Vetuma transaction {} has expired", vetumaTransaction.getId());

            vetumaTransaction.setStatusTimeout(response.getSO());

            return false;
        }

        final VtjData vtjData = response.getVtjData();

        if (vtjData == null) {
            LOG.error("VTJ data was not found in response");

            return false;

        } else if (vtjData.isKuollut()) {
            LOG.error("According to VETUMA/VTJ the person is dead.");

            return false;
        }

        // Found it!
        LOG.info("Vetuma login OK, SO=" + response.getSO());

        final SystemUser user = handleSuccess(vetumaTransaction.getEmail(), response.getSsn(), vtjData);
        vetumaTransaction.setStatusSuccess(response.getSO(), user);

        return true;
    }

    // For unit-testing
    SystemUser handleSuccess(final String email, final String ssn, final VtjData vtjData) {
        LOG.debug("Received person data from Vetuma: E-mail: {}", email);

        final Person person = registerPerson(email, ssn, vtjData);

        if (person.isDeceased()) {
            // Vetuma should make this check anyway
            throw new PersonIsDeceasedException("Cannot register deceased person");
        }

        // Disable all previous credentials for same person
        List<SystemUser> systemUsers = userRepository.findByPerson(person);
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

        final SystemUser user = registerUser(email, vtjData);

        // Attach user account to person identified by SSN
        user.setPerson(person);

        // Set account inactive until before registration is complete
        user.setActive(false);

        // Randomize user password before user has provided a new value
        changePasswordService.setUserPassword(user, RandomStringUtils.random(64));

        return user;
    }

    private SystemUser registerUser(String email, VtjData vtjData) {
        final SystemUser user = userRepository.findByUsernameIgnoreCase(email);

        if (user != null) {
            LOG.debug("Handling registration of existing user.");

            user.setLastName(vtjData.getSukunimi());
            user.setFirstName(vtjData.getEtunimet());

            return user;
        }

        return registerNewUser(email, vtjData);
    }

    private Person registerPerson(String email, String ssn, VtjData vtjData) {
        return personRepository.findBySsn(ssn)
                .map(existingPerson -> {
                    LOG.debug("Handling registration of existing person.");
                    writePersonData(email, vtjData, existingPerson);
                    return existingPerson;
                })
                .orElseGet(() -> registerNewPerson(email, ssn, vtjData));
    }

    private Person registerNewPerson(String registrationEmail, String ssn, VtjData vtjData) {
        LOG.debug("Registering a new person.");
        final Person person = new Person();
        person.setSsn(ssn);
        writePersonData(registrationEmail, vtjData, person);
        return personRepository.save(person);
    }

    private void writePersonData(String email, VtjData vtjData, Person person) {
        person.setEmail(email);
        person.setLastName(vtjData.getSukunimi());
        person.setFirstName(vtjData.getEtunimet());
        // OR-549 Because of MR import database contains invalid byNames. If byName is not overwritten
        // here to a valid value, findBySsn will flush and byName validation fails, and user is not able to register.
        person.setByName(vtjData.getEtunimet());
        person.setFinnishCitizen(vtjData.isSuomenKansalainen());
        person.setLanguageCode(vtjData.getKielikoodi());

        person.setHomeMunicipalityCode(vtjData.getKuntanumero());

        if (vtjData.getKuntanumero() != null) {
            person.setHomeMunicipality(municipalityRepository.findOne(vtjData.getKuntanumero()));
        } else {
            person.setHomeMunicipality(null);
        }
    }

    private SystemUser registerNewUser(String registrationEmail, VtjData vtjData) {
        LOG.debug("Registering a new user.");

        final SystemUser user = new SystemUser();

        user.setUsername(registrationEmail);
        user.setEmail(registrationEmail);
        user.setLastName(vtjData.getSukunimi());
        user.setFirstName(vtjData.getEtunimet());

        user.setRole(SystemUser.Role.ROLE_USER);

        if (vtjData.getKielikoodi() != null) {
            Locale locale = createLocale(vtjData.getKielikoodi());
            user.setLocale(locale);
        }

        return userRepository.save(user);
    }

    private Locale createLocale(String languageCode) {
        return Locales.getLocaleByLanguageCode(languageCode, runtimeEnvironmentUtil.getDefaultLocale());
    }
}
