package fi.riista.feature.harvestpermit.report.email;

import com.github.jknack.handlebars.Handlebars;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitContactPerson;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.security.EntityPermission;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Käyttäjä joutuu tiettyjen saaliseläinten osalta tekemään ilmoituksen
 * kaadosta myös (erikseen) omalle poliisilaitokselleen.
 * <p/>
 * Tämän vuoksi (hyväksyttäväksi) lähetetystä saalisilmoituksesta tulee järjestelmän lähettää saalisilmoituksen
 * tekijälle / luvan yhteyshenkilölle sähköpostitse ennalta määritellyn muotoinen sähköpostiviesti
 * jota hän voi käyttää tähän ilmoittamiseen.
 * <p/>
 * Vaikka ilmoitusta poliisille ei tarvitakaan, on sähkösposti tarpeellinen mm. muille tahoille raportointiin.
 */
@Component
public class HarvestReportNotificationFeature {

    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Resource
    private MailService mailService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private OrganisationRepository organisationRepository;

    @Transactional
    public void sendNotification(final Long reportId) {
        final HarvestReport harvestReport = requireEntityService.requireHarvestReport(reportId, EntityPermission.READ);
        final HarvestPermit harvestPermit = harvestReport.getHarvestPermit();
        final Organisation rka = harvestPermit != null ? findRka(harvestPermit) : null;

        Harvest harvest = null;
        HarvestQuota harvestQuota = null;

        if (harvestReport.getHarvestPermit() != null && harvestReport.getHarvestPermit().isHarvestsAsList()) {
            final HarvestReportListNotification message = new HarvestReportListNotification(handlebars, messageSource)
                    .withReport(harvestReport)
                    .withPermit(harvestPermit)
                    .withSummaries(HarvestReportListNotification.SpecimenSummary.create(harvestReport.getHarvests()));

            for (String email : getTargetEmails(harvestReport, harvestPermit)) {
                mailService.send(message.withEmail(email).build());
            }
        } else if (harvestReport.isEndOfHuntingReport()) {
            final EndOfHuntingReportNotification message = new EndOfHuntingReportNotification(handlebars, messageSource)
                    .withReport(harvestReport)
                    .withPermit(harvestPermit)
                    .withSummaries(EndOfHuntingReportNotification.SpecimenSummary.create(harvestPermit.getUndeletedHarvestReports()));

            for (String email : getTargetEmails(harvestReport, harvestPermit)) {
                mailService.send(message.withEmail(email).build());
            }
        }
        else {
            harvest = harvestReport.getHarvests().iterator().next();
            harvestQuota = harvest.getHarvestQuota();

            final HarvestReportNotification message = new HarvestReportNotification(handlebars, messageSource)
                    .withReport(harvestReport, harvest)
                    .withRiistakeskuksenAlue(rka)
                    .withQuota(harvestQuota)
                    .withPermit(harvestPermit);

            for (String email : getTargetEmails(harvestReport, harvestPermit)) {
                mailService.send(message.withEmail(email).build());
            }
        }
    }

    private static Set<String> getTargetEmails(HarvestReport report, HarvestPermit permit) {
        Objects.requireNonNull(report);

        // Collection of target email addresses
        final Set<String> result = new HashSet<>();

        // Author email
        final String authorEmail = report.getAuthor().getEmail();
        if (StringUtils.isNotBlank(authorEmail)) {
            result.add(authorEmail);
        }
        if (permit != null) {
            // Permit contact person email if permit is used
            final String permitContactEmail = permit.getOriginalContactPerson().getEmail();

            if (StringUtils.isNotBlank(permitContactEmail)) {
                result.add(permitContactEmail);
            }

            // Every other contact person available
            for (HarvestPermitContactPerson contactPerson : permit.getContactPersons()) {
                if (!permit.getOriginalContactPerson().equals(contactPerson.getContactPerson())) {
                    final String contactPersonEmail = contactPerson.getContactPerson().getEmail();

                    if (StringUtils.isNotBlank(contactPersonEmail)) {
                        result.add(contactPersonEmail);
                    }
                }
            }
        }

        return result;
    }

    private Organisation findRka(HarvestPermit harvestPermit) {
        Objects.requireNonNull(harvestPermit);

        if (harvestPermit.getPermitNumber().length() == 18) {
            // example: 2013-3-450-00260-2
            final String areaCode = harvestPermit.getPermitNumber().substring(7, 10); // 450

            final Organisation result = organisationRepository.findByTypeAndOfficialCode(OrganisationType.RKA, areaCode);

            if (result != null) {
                return result;
            }
        }

        if (harvestPermit.getRhy() != null) {
            // Fallback to RHY parent RK-alue
            return harvestPermit.getRhy().getClosestAncestorOfType(OrganisationType.RKA).orElse(null);
        }

        return null;
    }
}
