package fi.riista.feature.account.certificate;

import fi.riista.feature.account.AccountShootingTestDTO;
import fi.riista.feature.account.AccountShootingTestService;
import fi.riista.feature.account.audit.AccountActivityMessage;
import fi.riista.feature.account.audit.AccountAuditService;
import fi.riista.feature.account.certificate.HuntingCardDTO.OccupationDTO;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationSort;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonNotFoundException;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
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
    private AccountShootingTestService shootingTestService;

    @Resource
    private EnumLocaliser enumLocaliser;

    @Resource
    private HuntingCardQRCodeKeyHolder keyHolder;

    private void auditHunterCard(final String hunterNumber) {
        accountAuditService.auditActiveUserEvent(AccountActivityMessage.ActivityType.PDF_HUNTER_CARD, hunterNumber);
    }

    private void auditForeignCertificate(final String hunterNumber) {
        accountAuditService.auditActiveUserEvent(AccountActivityMessage.ActivityType.PDF_FOREIGN_CERTIFICATE, hunterNumber);
    }

    @Transactional
    public HunterForeignCertificateDTO foreignHuntingCertificateModel(final String hunterNumber) {
        auditForeignCertificate(hunterNumber);

        final Person person = requireHunter(hunterNumber);
        final LocalDate paymentDate = requirePaymentDate(person);

        return HunterForeignCertificateDTO.create(person, paymentDate);
    }

    @Transactional
    public HuntingCardDTO huntingCard(final String hunterNumber, final String lang) {
        auditHunterCard(hunterNumber);

        final Person person = requireHunter(hunterNumber);
        final LocalDate paymentDate = requirePaymentDate(person);

        final List<HuntingCardDTO.OccupationDTO> validOccupations = getRhyOccupations(person, lang);

        final List<AccountShootingTestDTO> shootingTests = shootingTestService
                .listQualifiedShootingTests(person, Locales.getLocaleByLanguageCode(lang))
                .stream()
                .filter(test -> !test.isExpired())
                .collect(toList());

        return HuntingCardDTO.create(
                person, validOccupations, paymentDate, shootingTests, lang, keyHolder.getPrivateKey());
    }

    private static LocalDate requirePaymentDate(final Person person) {
        return person.getHuntingPaymentDateForNextOrCurrentSeason()
                .orElseThrow(() -> new IllegalStateException("Payment date is missing"));
    }

    private List<OccupationDTO> getRhyOccupations(final Person person, final String lang) {
        return occupationRepository.findActiveByPerson(person)
                .stream()
                .filter(occ -> occ.getOrganisation().getOrganisationType() == OrganisationType.RHY)
                .sorted(OccupationSort.BY_TYPE.thenComparing(OccupationSort.BY_CALL_ORDER_ONLY_FOR_APPLICABLE_TYPES))
                .map(occupation -> {
                    final OccupationDTO dto = new OccupationDTO();

                    final LocalisedString occupationName =
                            enumLocaliser.getLocalisedString(occupation.getOccupationType());

                    if (occupationName != null) {
                        dto.setOccupationName(occupationName.getAnyTranslation(lang));
                    }
                    dto.setOrganisationOfficialCode(occupation.getOrganisation().getOfficialCode());
                    dto.setOrganisationName(occupation.getOrganisation().getNameLocalisation().getTranslation(lang));
                    dto.setBeginDate(occupation.getBeginDate());
                    dto.setEndDate(occupation.getEndDate());

                    return dto;
                })
                .collect(toList());
    }

    private Person requireHunter(final String hunterNumber) {
        return personRepository.findByHunterNumber(hunterNumber).map(person -> {
            activeUserService.assertHasPermission(person, EntityPermission.READ);

            if (!person.canPrintCertificate()) {
                throw new IllegalStateException("Cannot print certificate for personId=" + person.getId());
            }

            return person;
        }).orElseThrow(() -> PersonNotFoundException.byHunterNumber(hunterNumber));
    }
}
