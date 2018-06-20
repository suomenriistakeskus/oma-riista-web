package fi.riista.feature.harvestpermit.report.email;

import com.github.jknack.handlebars.Handlebars;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitContactPerson;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.statistics.HarvestPermitSpecimenSummary;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.security.EntityPermission;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class HarvestReportNotificationService {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestReportNotificationService.class);

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

    @Resource
    private MunicipalityRepository municipalityRepository;

    @Transactional
    public void sendNotificationForHarvest(final Long harvestId) {
        final Harvest harvest = requireEntityService.requireHarvest(harvestId, EntityPermission.READ);
        final HarvestPermit harvestPermit = harvest.getHarvestPermit();
        final Organisation rka = harvestPermit != null ? findRka(harvestPermit) : null;
        final Set<String> targetEmails = getTargetEmails(harvest, harvestPermit);

        if (targetEmails.isEmpty()) {
            LOG.warn("Empty recipient list for harvestReportId={}", harvestId);
            return;
        }

        final HarvestQuota harvestQuota = harvest.getHarvestQuota();
        final Municipality municipality = StringUtils.isNotBlank(harvest.getMunicipalityCode())
                ? municipalityRepository.findOne(harvest.getMunicipalityCode()) : null;
        mailService.send(new HarvestReportNotification(handlebars, messageSource)
                .withHarvest(harvest)
                .withRiistakeskuksenAlue(rka)
                .withQuota(harvestQuota)
                .withMunicipality(municipality)
                .withPermit(harvestPermit)
                .withRecipients(targetEmails)
                .build(mailService.getDefaultFromAddress()));

    }

    @Transactional
    public void sendNotificationForPermit(final Long harvestPermitId) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.NONE);

        final Set<String> targetEmails = getTargetEmailsForPermit(harvestPermit);
        if (targetEmails.isEmpty()) {
            LOG.warn("Empty recipient list for harvestPermitId={}", harvestPermitId);
            return;
        }
        mailService.send(new EndOfHuntingReportNotification(handlebars, messageSource)
                .withPermit(harvestPermit)
                .withSummaries(HarvestPermitSpecimenSummary.create(harvestPermit.getAcceptedHarvestForEndOfHuntingReport()))
                .withRecipients(targetEmails)
                .build(mailService.getDefaultFromAddress()));
    }

    private static Set<String> getTargetEmails(Harvest harvest, HarvestPermit permit) {
        Objects.requireNonNull(harvest);

        // Collection of target email addresses
        final Set<String> result = new HashSet<>();

        // Author email
        final String authorEmail = harvest.getHarvestReportAuthor().getEmail();
        if (StringUtils.isNotBlank(authorEmail)) {
            result.add(authorEmail);
        }
        if (permit != null) {
            result.addAll(getTargetEmailsForPermit(permit));

        }

        return result;
    }

    private static Set<String> getTargetEmailsForPermit(final HarvestPermit permit) {
        final Set<String> result = new HashSet<>();

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
