package fi.riista.integration.vtj;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import fi.riista.feature.account.audit.AccountActivityMessage;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.account.audit.AccountAuditService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.validation.FinnishSocialSecurityNumberValidator;
import fi.vrk.xml.schema.vtjkysely.VTJHenkiloVastaussanoma;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class VtjSearchService {

    private static final Logger LOG = LoggerFactory.getLogger(VtjSearchService.class);

    private static final FinnishSocialSecurityNumberValidator SSN_VALIDATOR =
            new FinnishSocialSecurityNumberValidator();

    @Resource
    private VtjRemoteService vtjRemoteService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private AccountAuditService accountAuditService;

    @Resource
    private UserRepository userRepository;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private MunicipalityRepository municipalityRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Long> searchAndAdd(final String ssn) {
        Preconditions.checkArgument(SSN_VALIDATOR.isValid(ssn, null), "Invalid Finnish SSN");

        final Authentication authentication = activeUserService.getAuthentication();
        final SystemUser activeUser = activeUserService.getActiveUser();

        try {
            LOG.info("User id:{} executing VTJ query", activeUser.getId());

            final String endUser = "activeUserId:" + activeUser.getId();
            final VTJHenkiloVastaussanoma.Henkilo henkilo = vtjRemoteService.search(endUser, ssn);

            if (henkilo == null) {
                accountAuditService.auditUserEvent(activeUser, authentication,
                        AccountActivityMessage.ActivityType.VTJ, "Not found");

                return Optional.empty();
            }

            final Person person = createPerson(henkilo);

            LOG.info("User id:{} executed VTJ query, person id:{} created", activeUser.getId(), person.getId());

            accountAuditService.auditUserEvent(activeUser, authentication,
                    AccountActivityMessage.ActivityType.VTJ, "Person created id:" + person.getId());

            return Optional.of(person.getId());

        } catch (final Exception e) {
            LOG.warn("Problem with VTJ search", e);

            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    private Person createPerson(final VTJHenkiloVastaussanoma.Henkilo result) {
        return personRepository.save(createPersonEntity(result));
    }

    private Person createPersonEntity(final VTJHenkiloVastaussanoma.Henkilo henkilo) {
        final Person person = new Person();

        person.setSsn(henkilo.getHenkilotunnus().getValue());
        person.setLastName(henkilo.getNykyinenSukunimi().getSukunimi());
        person.setLanguageCode(henkilo.getAidinkieli().getKielikoodi());

        final String currentFirstNames = henkilo.getNykyisetEtunimet().getEtunimet();
        person.setFirstName(currentFirstNames);
        person.setByName(currentFirstNames);

        final String municipalityCode = henkilo.getKotikunta().getKuntanumero();
        person.setHomeMunicipalityCode(municipalityCode);

        final Municipality municipality =
                Optional.ofNullable(municipalityCode).map(municipalityRepository::findOne).orElse(null);
        person.setHomeMunicipality(municipality);

        if (StringUtils.isNotBlank(henkilo.getKuolintiedot().getKuolinpvm())) {
            LOG.info("SSN is found but person is deceased, decease date: " + henkilo.getKuolintiedot().getKuolinpvm());
            person.setDeletionCode(Person.DeletionCode.D);
        }

        return person;
    }

}
