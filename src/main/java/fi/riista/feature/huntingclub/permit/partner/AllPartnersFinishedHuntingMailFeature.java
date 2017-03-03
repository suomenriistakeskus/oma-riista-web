package fi.riista.feature.huntingclub.permit.partner;

import fi.riista.feature.gamediary.GameDiaryService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitContactPerson;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitDTO;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitFeature;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitService;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitTotalPaymentDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.LocalisedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.Objects;
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
    private HuntingClubPermitFeature huntingClubPermitFeature;

    @Resource
    private GameDiaryService gameDiaryService;

    @Resource
    private HuntingClubPermitService huntingPermitService;

    // to be used in tests
    public void setMailService(AllPartnersFinishedHuntingMailService mailService) {
        this.mailService = mailService;
    }

    // to be used in tests
    public AllPartnersFinishedHuntingMailService getMailService() {
        return mailService;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void checkAndSend(final HarvestPermit permit, int speciesCode) {
        if (huntingPermitService.allPartnersFinishedHunting(permit, speciesCode)) {
            sendMail(permit, gameDiaryService.getGameSpeciesByOfficialCode(speciesCode));
        }
    }

    private void sendMail(final HarvestPermit permit, final GameSpecies species) {
        final Set<String> emails = findEmails(permit);
        final AllPartnersFinishedHuntingMailService.MailData data = getMailData(permit, species);
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
        final HuntingClub permitHolder = permit.getPermitHolder();
        return Stream.concat(findClubContactPersons(permitHolder), findPermitContactPersons(permit))
                .map(Person::getEmail)
                // Persons not associated with active user may have null e-mail address.
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private Stream<Person> findClubContactPersons(final HuntingClub permitHolder) {
        return occupationRepository
                .findActiveByOrganisationAndOccupationType(permitHolder, OccupationType.SEURAN_YHDYSHENKILO)
                .stream()
                .map(Occupation::getPerson);
    }

    private static Stream<Person> findPermitContactPersons(final HarvestPermit permit) {
        return Stream.concat(
                Stream.of(permit.getOriginalContactPerson()),
                permit.getContactPersons().stream().map(HarvestPermitContactPerson::getContactPerson));
    }

    private AllPartnersFinishedHuntingMailService.MailData getMailData(
            final HarvestPermit permit, final GameSpecies species) {

        final String permitNumber = permit.getPermitNumber();
        final LocalisedString speciesName = species.getNameLocalisation();

        final HuntingClubPermitDTO dto =
                huntingClubPermitFeature.getPermitWithoutAuthorization(permit, species.getOfficialCode(), null);
        final boolean notEdibleOk = dto.isAmendmentPermitsMatchHarvests();
        final HuntingClubPermitTotalPaymentDTO payment = dto.getTotalPayment();
        final long permitHolderId = permit.getPermitHolder().getId();
        final long permitId = permit.getId();
        return new AllPartnersFinishedHuntingMailService.MailData(
                permitHolderId, permitId, permitNumber, speciesName, notEdibleOk, payment);
    }

}
