package fi.riista.feature.harvest;

import com.google.common.base.Preconditions;
import fi.riista.config.Constants;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.HarvestChangeHistory;
import fi.riista.feature.gamediary.HarvestChangeHistoryRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.joda.time.DateTime;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static fi.riista.util.DateUtil.now;
import static fi.riista.util.DateUtil.today;
import static java.util.Objects.requireNonNull;

@Component
public class LegalHarvestCertificatePdfFeature {

    public static String createFileName(final Locale locale) {
        return String.format(LegalHarvestCertificatePdfFeature.FILENAME.getTranslation(locale),
                Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private static final String JSP_HARVEST_CERTIFICATE_FI = "pdf/legal-harvest-certificate-fi";
    private static final String JSP_HARVEST_CERTIFICATE_SV = "pdf/legal-harvest-certificate-sv";
    private static final LocalisedString FILENAME = LocalisedString.of("todistus-%s.pdf", "intyg-%s.pdf");

    public static class PdfModel {
        private final String view;
        private final Object model;

        PdfModel(final String view, final Object model) {
            this.view = requireNonNull(view);
            this.model = requireNonNull(model);
        }

        public String getView() {
            return view;
        }

        public Object getModel() {
            return model;
        }

    }

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private MessageSource messageSource;

    @Resource
    private HarvestChangeHistoryRepository harvestChangeHistoryRepository;

    @Resource
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public LegalHarvestCertificatePdfFeature.PdfModel getPdfModel(final long harvestId, final Locale locale) {

        final Harvest harvest = requireEntityService.requireHarvest(harvestId, EntityPermission.READ);
        final List<HarvestChangeHistory> changeHistoryList = harvestChangeHistoryRepository.findByHarvest(harvest);

        final HarvestChangeHistory historyItem = changeHistoryList.stream()
                .sorted(Comparator.comparing(HarvestChangeHistory::getPointOfTime).reversed())
                .filter(hch -> hch.getHarvestReportState() == HarvestReportState.APPROVED)
                .filter(hch -> hch.getUserId() != null)
                .filter(hch -> hch.getUserId() > 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Approver not present."));
        final DateTime approvedDate = historyItem.getPointOfTime();
        final String approver = userRepository.findOne(historyItem.getUserId()).getFullName();
        final GameSpecies species = harvest.getSpecies();
        final List<HarvestSpecimen> specimens = harvest.getSortedSpecimens();
        Preconditions.checkState(specimens.size() == 1, "Must have exactly one specimen");
        Preconditions.checkState(species.isLargeCarnivore());

        final EnumLocaliser localiser = new EnumLocaliser(messageSource, locale);
        final Person shooter = harvest.getActualShooter();
        final HarvestSpecimen specimen = specimens.get(0);
        final LegalHarvestCertificateDTO.DerogationDTO derogationDTO =
                Optional.ofNullable(harvest.getHarvestPermit())
                        .map(p -> new LegalHarvestCertificateDTO.DerogationDTO(p.getPermitHolder().getName(),
                                DateUtil.toLocalDateNullSafe(p.getPermitDecision().getPublishDate().toDate()),
                                p.getPermitNumber()))
                        .orElse(null);

        Preconditions.checkState(derogationDTO != null || species.isBear());
        final LegalHarvestCertificateDTO dto = LegalHarvestCertificateDTO.Builder.builder()
                .withCurrentDate(today())
                .withShooterName(shooter.getFullName())
                .withHunterNumber(shooter.getHunterNumber())
                .withLatitude(harvest.getGeoLocation().getLatitude())
                .withLongitude(harvest.getGeoLocation().getLongitude())
                .withPointOfTime(DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTime()))
                .withRhy(localiser.getTranslation(harvest.getRhy().getNameLocalisation()))
                .withSpecies(localiser.getTranslation(species.getNameLocalisation()))
                .withGender(localiser.getTranslation(specimen.getGender()))
                .withWeight(specimen.getWeight())
                .withApprovedDate(approvedDate.toLocalDate())
                .withApprover(approver)
                .withDerogation(derogationDTO)
                .build();

        return new PdfModel(Locales.isSwedish(locale) ? JSP_HARVEST_CERTIFICATE_SV : JSP_HARVEST_CERTIFICATE_FI, dto);
    }

}
