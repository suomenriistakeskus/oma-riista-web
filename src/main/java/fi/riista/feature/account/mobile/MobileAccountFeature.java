package fi.riista.feature.account.mobile;

import fi.riista.feature.account.AccountShootingTestDTO;
import fi.riista.feature.account.AccountShootingTestFeature;
import fi.riista.feature.account.certificate.HuntingCardQRCodeGenerator;
import fi.riista.feature.account.certificate.HuntingCardQRCodeKeyHolder;
import fi.riista.feature.account.pilot.DeerPilotService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.Locales;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toCollection;

@Service
public class MobileAccountFeature {

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private ObservationRepository observationRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private DeerPilotService deerPilotService;

    @Resource
    private AccountShootingTestFeature accountShootingTestFeature;

    @Resource
    private MobileOccupationDTOFactory mobileOccupationDTOFactory;

    @Resource
    private HuntingCardQRCodeKeyHolder keyHolder;

    @Transactional(readOnly = true)
    public MobileAccountDTO getMobileAccount() {
        final Person person = activeUserService.requireActivePerson();
        final String username = activeUserService.getActiveUsernameOrNull();

        final SortedSet<Integer> harvestYears = getBeginningCalendarYearsOfHuntingYearsContainingHarvests(person);
        final SortedSet<Integer> observationYears =
                getBeginningCalendarYearsOfHuntingYearsContainingObservations(person);

        final List<Occupation> occupations =
                occupationRepository.findActiveByPersonAndOrganisationTypes(person, singleton(OrganisationType.RHY));

        final boolean shootingTestsEnabled = person.isShootingTestsEnabled()
                && occupations.stream().anyMatch(occ -> occ.getOccupationType() == AMPUMAKOKEEN_VASTAANOTTAJA);

        final String qrCode = person.canPrintCertificate()
                ? HuntingCardQRCodeGenerator.forPerson(person).build(keyHolder.getPrivateKey(), Locales.FI_LANG)
                : null;

        final List<AccountShootingTestDTO> shootingTests = accountShootingTestFeature.listMyShootingTests(person.getId());

        return MobileAccountDTO.create(
                username,
                person,
                person.getAddress(),
                person.getRhyMembership(),
                harvestYears,
                observationYears,
                mobileOccupationDTOFactory.create(occupations),
                shootingTestsEnabled,
                deerPilotService.isPilotUser(),
                qrCode,
                shootingTests);
    }

    private SortedSet<Integer> getBeginningCalendarYearsOfHuntingYearsContainingHarvests(final Person person) {
        return getBeginningCalendarYearsOfHuntingYears(harvestRepository.findByActualShooter(person));
    }

    private SortedSet<Integer> getBeginningCalendarYearsOfHuntingYearsContainingObservations(final Person person) {
        return getBeginningCalendarYearsOfHuntingYears(observationRepository.findByObserver(person));
    }

    private static <T extends GameDiaryEntry> SortedSet<Integer> getBeginningCalendarYearsOfHuntingYears(
            final Iterable<T> diaryEntries) {

        return F.stream(diaryEntries)
                .map(GameDiaryEntry::getPointOfTime)
                .map(DateTime::toLocalDate)
                .map(DateUtil::huntingYearContaining)
                .collect(toCollection(TreeSet::new));
    }
}
