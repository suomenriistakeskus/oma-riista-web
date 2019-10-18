package fi.riista.feature.huntingclub.permit.endofhunting;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitContactPerson;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.PermitClientUriFactory;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.util.Locales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Component
public class AllPartnersFinishedHuntingMailFeature {

    private static final Logger LOG = LoggerFactory.getLogger(AllPartnersFinishedHuntingMailFeature.class);

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private AllPartnersFinishedHuntingMailService mailService;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HuntingFinishingService huntingFinishingService;

    @Resource
    private PermitClientUriFactory permitClientUriFactory;

    // to be used in tests
    public void setMailService(AllPartnersFinishedHuntingMailService mailService) {
        this.mailService = mailService;
    }

    // to be used in tests
    public AllPartnersFinishedHuntingMailService getMailService() {
        return mailService;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void checkAndSend(final HarvestPermit harvestPermit, int speciesCode) {
        if (!huntingFinishingService.allPartnersFinishedHunting(harvestPermit, speciesCode)) {
            return;
        }

        final Locale permitHolderLocale = Optional.ofNullable(harvestPermit.getPermitDecision())
                .map(PermitDecision::getLocale)
                .orElse(Locales.FI);
        final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(speciesCode);
        final URI dashboardUri = permitClientUriFactory.getAbsolutePermitDashboardUri(harvestPermit.getId());
        final Set<String> emails = findEmails(harvestPermit);

        final AllPartnersFinishedHuntingDTO data = AllPartnersFinishedHuntingDTO.create(
                harvestPermit, gameSpecies, dashboardUri, permitHolderLocale);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                try {
                    // NOTE: Must invoke through proxy to make sure new transaction is started
                    mailService.sendEmailAsync(emails, data);
                } catch (RuntimeException ex) {
                    // Exception should be handled, so that HTTP status code is not altered
                    LOG.error("Error occurred while sending emails", ex);
                }
            }
        });
    }

    private Set<String> findEmails(final HarvestPermit permit) {
        return Stream.concat(findClubContactPersons(permit.getHuntingClub()), findPermitContactPersons(permit))
                .map(Person::getEmail)
                .filter(Objects::nonNull).collect(toSet());
    }

    private Stream<Person> findClubContactPersons(final HuntingClub permitHolder) {
        return Optional.ofNullable(permitHolder).map(ph -> occupationRepository
                .findActiveByOrganisationAndOccupationType(ph, OccupationType.SEURAN_YHDYSHENKILO)
                .stream()
                .map(Occupation::getPerson))
                .orElse(Stream.empty());
    }

    private static Stream<Person> findPermitContactPersons(final HarvestPermit permit) {
        return Stream.concat(
                Stream.of(permit.getOriginalContactPerson()),
                permit.getContactPersons().stream().map(HarvestPermitContactPerson::getContactPerson));
    }

}
