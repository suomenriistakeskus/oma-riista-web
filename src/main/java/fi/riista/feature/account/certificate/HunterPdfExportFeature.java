package fi.riista.feature.account.certificate;

import fi.riista.feature.account.audit.AccountActivityMessage;
import fi.riista.feature.account.audit.AccountAuditService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationSort;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.security.EntityPermission;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class HunterPdfExportFeature {

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

    @Resource
    private HuntingCardQRCodeKeyHolder keyHolder;

    private void auditHunterCard(String hunterNumber) {
        accountAuditService.auditActiveUserEvent(AccountActivityMessage.ActivityType.PDF_HUNTER_CARD, hunterNumber);
    }

    private void auditForeignCertificate(String hunterNumber) {
        accountAuditService.auditActiveUserEvent(AccountActivityMessage.ActivityType.PDF_FOREIGN_CERTIFICATE, hunterNumber);
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

        return HuntingCardDTO.create(person, validOccupations, paymentDate, lang, keyHolder.getPrivateKey(), enumLocaliser);
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
