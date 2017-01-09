package fi.riista.feature.account.certificate;

import fi.riista.feature.account.audit.AccountActivityMessage;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.audit.AccountAuditService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.occupation.OccupationSort;
import fi.riista.security.EntityPermission;
import fi.riista.util.JCEUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.PrivateKey;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class HunterPdfExportFeature {
    private static final Logger LOG = LoggerFactory.getLogger(HunterPdfExportFeature.class);

    @Resource
    private PersonRepository personRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private AccountAuditService accountAuditService;

    @Resource
    private EnumLocaliser enumLocaliser;

    @Value("${hunting.card.signature.private.key}")
    private String privateKeyText;

    private PrivateKey privateKey;

    public void decodePrivateKey() {
        if (StringUtils.isBlank(privateKeyText)) {
            return;
        }
        try {
            this.privateKey = JCEUtil.loadEllipticCurvePkcs8PrivateKey(this.privateKeyText);
            this.privateKeyText = null;
        } catch (Exception e) {
            LOG.error("Could not load private key", e);
        }
    }

    private void audit(AccountActivityMessage.ActivityType type, String hunterNumber) {
        final SystemUser activeUser = activeUserService.getActiveUser();
        final Authentication authentication = activeUserService.getAuthentication();

        accountAuditService.auditUserEvent(activeUser, authentication, type, hunterNumber);
    }

    private void auditHunterCard(String hunterNumber) {
        audit(AccountActivityMessage.ActivityType.PDF_HUNTER_CARD, hunterNumber);
    }

    private void auditForeignCertificate(String hunterNumber) {
        audit(AccountActivityMessage.ActivityType.PDF_FOREIGN_CERTIFICATE, hunterNumber);
    }

    @Transactional
    public HunterForeignCertificateDTO foreignHuntingCertificateModel(String hunterNumber) {
        auditForeignCertificate(hunterNumber);

        final Person person = requireHunter(hunterNumber);
        final LocalDate paymentDate = requirePaymentDate(person);

        return HunterForeignCertificateDTO.create(person, paymentDate);
    }

    @Transactional
    public HuntingCardDTO huntingCard(String hunterNumber, String lang) {
        auditHunterCard(hunterNumber);

        final Person person = requireHunter(hunterNumber);
        final LocalDate paymentDate = requirePaymentDate(person);
        final List<Occupation> validOccupations = getRhyOccupations(person);

        return HuntingCardDTO.create(person, validOccupations, paymentDate, lang, privateKey, enumLocaliser);
    }

    private static LocalDate requirePaymentDate(final Person person) {
        return person.getHuntingPaymentDateForNextOrCurrentSeason()
                .orElseThrow(() -> new IllegalStateException("Payment date is missing"));
    }

    private List<Occupation> getRhyOccupations(final Person person) {
        return occupationRepository.findActiveByPerson(person).stream()
                .filter(occ -> occ != null && occ.getOrganisation().getOrganisationType() == OrganisationType.RHY)
                .sorted(OccupationSort.BY_TYPE.thenComparing(OccupationSort.BY_CALL_ORDER_ONLY_FOR_APPLICABLE_TYPES))
                .collect(toList());
    }

    private Person requireHunter(String hunterNumber) {
        return personRepository.findByHunterNumber(hunterNumber).map(person -> {
            activeUserService.assertHasPermission(person, EntityPermission.READ);

            if (!person.canPrintCertificate()) {
                throw new IllegalStateException("Cannot print certificate for personId=" + person.getId());
            }

            return person;
        }).orElseThrow(NotFoundException::new);
    }

}
