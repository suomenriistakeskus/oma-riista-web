package fi.riista.feature.gamediary.observation;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUser.Role;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gis.GISPoint;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.DefaultEntitySupplierProvider;
import fi.riista.util.DateUtil;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class ObservationUpdateService_UpdateMutableFieldsTest
        implements DefaultEntitySupplierProvider, ValueGeneratorMixin {

    private static final int DEFAULT_DISTANCE_TO_RESIDENCE = 99;

    @InjectMocks
    private ObservationUpdateService service;

    @Mock
    private GameSpeciesService gameSpeciesService;

    @Mock
    private GISQueryService gisQueryService;

    private GameSpecies species;
    private GameSpecies largeCarnivoreSpecies;

    private Riistanhoitoyhdistys rhy1;
    private Riistanhoitoyhdistys rhy2;

    private GeoLocation rhy1Location;
    private GeoLocation rhy2Location;

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    @Before
    public void setUp() {
        species = getEntitySupplier().newGameSpecies();

        largeCarnivoreSpecies = getEntitySupplier().newGameSpecies();
        largeCarnivoreSpecies.setOfficialCode(GameSpecies.OFFICIAL_CODE_WOLF);

        final Map<Integer, GameSpecies> speciesByCode = new HashMap<>();
        speciesByCode.put(species.getOfficialCode(), species);
        speciesByCode.put(largeCarnivoreSpecies.getOfficialCode(), largeCarnivoreSpecies);

        doAnswer(invocation -> speciesByCode.get(invocation.getArgument(0)))
                .when(gameSpeciesService).requireByOfficialCode(anyInt());

        rhy1 = getEntitySupplier().newRiistanhoitoyhdistys();
        rhy2 = getEntitySupplier().newRiistanhoitoyhdistys();

        rhy1Location = geoLocation();
        rhy2Location = geoLocation();

        final Map<GeoLocation, Riistanhoitoyhdistys> rhyByGeoLocation = new HashMap<>();
        rhyByGeoLocation.put(rhy1Location, rhy1);
        rhyByGeoLocation.put(rhy2Location, rhy2);

        doAnswer(invocation -> rhyByGeoLocation.get(invocation.getArgument(0)))
                .when(gisQueryService).findRhyByLocation(any(GeoLocation.class));

        doAnswer(invocation -> OptionalInt.of(DEFAULT_DISTANCE_TO_RESIDENCE))
                .when(gisQueryService).findInhabitedBuildingDistance(any(GISPoint.class), anyInt());
    }

    @Test
    public void testFieldsIndependentOnMofidierInfo_whenNotLockedByHuntingConditions() {
        final Observation observation = new Observation();

        final ObservationLockInfo lockInfo = createLockInfoForRandomPerson(observation, false);

        final ObservationDTO dto = createDtoForTest();
        dto.setGeoLocation(rhy2Location);
        dto.setPointOfTime(DateUtil.now().toLocalDateTime());
        dto.setGameSpeciesCode(largeCarnivoreSpecies.getOfficialCode());
        dto.setObservationType(some(ObservationType.class));
        dto.setDeerHuntingType(some(DeerHuntingType.class));
        dto.setDeerHuntingTypeDescription("DeerHuntingTypeDescription");

        dto.setMooselikeMaleAmount(1);
        dto.setMooselikeFemaleAmount(2);
        dto.setMooselikeCalfAmount(3);
        dto.setMooselikeFemale1CalfAmount(4);
        dto.setMooselikeFemale2CalfsAmount(5);
        dto.setMooselikeFemale3CalfsAmount(6);
        dto.setMooselikeFemale4CalfsAmount(7);
        dto.setMooselikeUnknownSpecimenAmount(8);

        service.updateMutableFields(observation, dto, lockInfo);

        assertThat(observation.getGeoLocation(), equalTo(rhy2Location));
        assertThat(observation.getRhy(), equalTo(rhy2));

        assertThat(observation.getPointOfTime(), equalTo(DateUtil.toDateTimeNullSafe(dto.getPointOfTime())));

        assertThat(observation.getSpecies(), equalTo(largeCarnivoreSpecies));
        assertThat(observation.getObservationCategory(), equalTo(dto.getObservationCategory()));
        assertThat(observation.getObservationType(), equalTo(dto.getObservationType()));

        assertThat(observation.getDeerHuntingType(), equalTo(dto.getDeerHuntingType()));
        assertThat(observation.getDeerHuntingTypeDescription(), equalTo(dto.getDeerHuntingTypeDescription()));

        assertThat(observation.getAmount(), is(notNullValue()));

        assertThat(observation.getMooselikeMaleAmount(), equalTo(1));
        assertThat(observation.getMooselikeFemaleAmount(), equalTo(2));
        assertThat(observation.getMooselikeCalfAmount(), equalTo(3));
        assertThat(observation.getMooselikeFemale1CalfAmount(), equalTo(4));
        assertThat(observation.getMooselikeFemale2CalfsAmount(), equalTo(5));
        assertThat(observation.getMooselikeFemale3CalfsAmount(), equalTo(6));
        assertThat(observation.getMooselikeFemale4CalfsAmount(), equalTo(7));
        assertThat(observation.getMooselikeUnknownSpecimenAmount(), equalTo(8));
    }

    @Test
    public void testFieldsIndependentOnMofidierInfo_whenLockedByHuntingConditions() {
        final DateTime originalPointOfTime = DateUtil.now();

        final Observation observation = new Observation();
        observation.setGeoLocation(rhy1Location);
        observation.setRhy(rhy1);
        observation.setPointOfTime(originalPointOfTime);
        observation.setSpecies(species);
        observation.setObservationCategory(ObservationCategory.NORMAL);
        observation.setObservationType(ObservationType.JALKI);

        final ObservationLockInfo lockInfo = createLockInfoForRandomPerson(observation, true);

        final ObservationDTO dto = createDtoForTest();
        dto.setGeoLocation(rhy2Location);
        dto.setPointOfTime(originalPointOfTime.toLocalDateTime().minusDays(1));
        dto.setGameSpeciesCode(largeCarnivoreSpecies.getOfficialCode());
        dto.setObservationCategory(ObservationCategory.DEER_HUNTING);
        dto.setObservationType(ObservationType.NAKO);
        dto.setDeerHuntingType(DeerHuntingType.OTHER);
        dto.setDeerHuntingTypeDescription("DeerHuntingTypeDescription");

        dto.setMooselikeMaleAmount(1);
        dto.setMooselikeFemaleAmount(2);
        dto.setMooselikeCalfAmount(3);
        dto.setMooselikeFemale1CalfAmount(4);
        dto.setMooselikeFemale2CalfsAmount(5);
        dto.setMooselikeFemale3CalfsAmount(6);
        dto.setMooselikeFemale4CalfsAmount(7);
        dto.setMooselikeUnknownSpecimenAmount(8);

        service.updateMutableFields(observation, dto, lockInfo);

        assertThat(observation.getGeoLocation(), equalTo(rhy1Location));
        assertThat(observation.getRhy(), equalTo(rhy1));

        assertThat(observation.getPointOfTime(), equalTo(originalPointOfTime));

        assertThat(observation.getSpecies(), equalTo(species));
        assertThat(observation.getObservationCategory(), equalTo(ObservationCategory.NORMAL));
        assertThat(observation.getObservationType(), equalTo(ObservationType.JALKI));

        assertThat(observation.getDeerHuntingType(), is(nullValue()));
        assertThat(observation.getDeerHuntingTypeDescription(), is(nullValue()));

        assertThat(observation.getAmount(), is(nullValue()));

        assertThat(observation.getMooselikeMaleAmount(), is(nullValue()));
        assertThat(observation.getMooselikeFemaleAmount(), is(nullValue()));
        assertThat(observation.getMooselikeCalfAmount(), is(nullValue()));
        assertThat(observation.getMooselikeFemale1CalfAmount(), is(nullValue()));
        assertThat(observation.getMooselikeFemale2CalfsAmount(), is(nullValue()));
        assertThat(observation.getMooselikeFemale3CalfsAmount(), is(nullValue()));
        assertThat(observation.getMooselikeFemale4CalfsAmount(), is(nullValue()));
        assertThat(observation.getMooselikeUnknownSpecimenAmount(), is(nullValue()));
    }

    @Test
    public void testUpdatingDescription_author() {
        final Observation observation = new Observation();
        observation.setDescription("description");

        final ObservationLockInfo lockInfo = createLockInfoForAuthor(observation, false);

        final ObservationDTO dto = createDtoForTest();
        dto.setDescription("changed");

        service.updateMutableFields(observation, dto, lockInfo);

        assertThat(observation.getDescription(), equalTo("changed"));
    }

    @Test
    public void testUpdatingDescription_notAuthorOrObserver() {
        final Observation observation = new Observation();
        observation.setDescription("original");

        final ObservationLockInfo lockInfo = createLockInfoForRandomPerson(observation, false);

        final ObservationDTO dto = createDtoForTest();
        dto.setDescription("changed");

        service.updateMutableFields(observation, dto, lockInfo);

        assertThat(observation.getDescription(), equalTo("original"));
    }

    @Test
    public void testUpdatingDescription_moderator() {
        final Observation observation = new Observation();
        observation.setDescription("original");

        final ObservationLockInfo lockInfo = createLockInfoForModerator(observation, false);

        final ObservationDTO dto = createDtoForTest();
        dto.setDescription("changed");

        service.updateMutableFields(observation, dto, lockInfo);

        assertThat(observation.getDescription(), equalTo("original"));
    }

    @Test
    public void testUpdatingAmount_whenMooselikeAmountFieldsSet() {
        final Observation observation = new Observation();

        final ObservationLockInfo lockInfo = createLockInfoForRandomPerson(observation, false);

        final ObservationDTO dto = createDtoForTest();

        // Bogus value, should actually be not set. It is tested that this is not effective.
        dto.setAmount(999);

        dto.setMooselikeMaleAmount(1);
        dto.setMooselikeFemaleAmount(2);
        dto.setMooselikeCalfAmount(3);
        dto.setMooselikeFemale1CalfAmount(4);
        dto.setMooselikeFemale2CalfsAmount(5);
        dto.setMooselikeFemale3CalfsAmount(6);
        dto.setMooselikeFemale4CalfsAmount(7);
        dto.setMooselikeUnknownSpecimenAmount(8);

        service.updateMutableFields(observation, dto, lockInfo);

        assertThat(observation.getAmount(), equalTo(96));
    }

    @Test
    public void testUpdatingAmount_whenMooselikeAmountFieldsNotSet() {
        final Observation observation = new Observation();

        final ObservationLockInfo lockInfo = createLockInfoForRandomPerson(observation, false);

        final ObservationDTO dto = createDtoForTest();
        dto.setAmount(20);

        service.updateMutableFields(observation, dto, lockInfo);

        assertThat(observation.getAmount(), equalTo(20));
    }

    @Test
    public void testUpdatingLargeCarnivoreFields_authorNotCarnivoreAuthority_whenNotLockedByHuntingConditions() {
        final Observation observation = new Observation();
        observation.setInYardDistanceToResidence(10);

        final ObservationLockInfo lockInfo = createLockInfoForAuthor(observation, false);

        final ObservationDTO dto = createDtoForTest();
        dto.setGameSpeciesCode(largeCarnivoreSpecies.getOfficialCode());
        dto.setVerifiedByCarnivoreAuthority(true);
        dto.setObserverName("test person");
        dto.setObserverPhoneNumber("123456789");
        dto.setOfficialAdditionalInfo("OfficialAdditionalInfo");

        service.updateMutableFields(observation, dto, lockInfo);

        assertThat(observation.getInYardDistanceToResidence(), is(nullValue()));
        assertThat(observation.getVerifiedByCarnivoreAuthority(), is(nullValue()));
        assertThat(observation.getObserverName(), is(nullValue()));
        assertThat(observation.getObserverPhoneNumber(), is(nullValue()));
        assertThat(observation.getOfficialAdditionalInfo(), is(nullValue()));
    }

    @Test
    public void testUpdatingLargeCarnivoreFields_authorWithCarnivoreAuthority_whenNotLockedByHuntingConditions() {
        final Observation observation = new Observation();
        observation.setInYardDistanceToResidence(10);

        final ObservationLockInfo lockInfo = createLockInfoForAuthorWithCarnivoreAuthority(observation, false);

        final ObservationDTO dto = createDtoForTest();
        dto.setGameSpeciesCode(largeCarnivoreSpecies.getOfficialCode());
        dto.setVerifiedByCarnivoreAuthority(true);
        dto.setObserverName("carnivore person");
        dto.setObserverPhoneNumber("123456789");
        dto.setOfficialAdditionalInfo("OfficialAdditionalInfo");

        service.updateMutableFields(observation, dto, lockInfo);

        assertThat(observation.getInYardDistanceToResidence(), equalTo(DEFAULT_DISTANCE_TO_RESIDENCE));
        assertThat(observation.getVerifiedByCarnivoreAuthority(), equalTo(true));
        assertThat(observation.getObserverName(), equalTo("carnivore person"));
        assertThat(observation.getObserverPhoneNumber(), equalTo("123456789"));
        assertThat(observation.getOfficialAdditionalInfo(), equalTo("OfficialAdditionalInfo"));
    }

    @Test
    public void testUpdatingLargeCarnivoreFields_authorWithCarnivoreAuthority_whenLockedByHuntingConditions() {
        final Observation observation = new Observation();
        observation.setSpecies(largeCarnivoreSpecies);
        observation.setInYardDistanceToResidence(10);
        observation.setVerifiedByCarnivoreAuthority(true);
        observation.setObserverName("carnivore person");
        observation.setObserverPhoneNumber("123456789");
        observation.setOfficialAdditionalInfo("original");

        final ObservationLockInfo lockInfo = createLockInfoForAuthorWithCarnivoreAuthority(observation, true);

        final ObservationDTO dto = createDtoForTest();
        dto.setGameSpeciesCode(largeCarnivoreSpecies.getOfficialCode());
        dto.setVerifiedByCarnivoreAuthority(false);
        dto.setObserverName("another person");
        dto.setObserverPhoneNumber("987654321");
        dto.setOfficialAdditionalInfo("changed");

        service.updateMutableFields(observation, dto, lockInfo);

        assertThat(observation.getInYardDistanceToResidence(), equalTo(10));
        assertThat(observation.getVerifiedByCarnivoreAuthority(), equalTo(true));
        assertThat(observation.getObserverName(), equalTo("carnivore person"));
        assertThat(observation.getObserverPhoneNumber(), equalTo("123456789"));
        assertThat(observation.getOfficialAdditionalInfo(), equalTo("original"));
    }

    @Test
    public void testUpdatingLargeCarnivoreFields_nonAuthorWithCarnivoreAuthority_whenNotLockedByHuntingConditions() {
        final Observation observation = new Observation();
        observation.setSpecies(largeCarnivoreSpecies);
        observation.setInYardDistanceToResidence(10);
        observation.setVerifiedByCarnivoreAuthority(true);
        observation.setObserverName("carnivore person");
        observation.setObserverPhoneNumber("123456789");
        observation.setOfficialAdditionalInfo("original");

        final ObservationLockInfo lockInfo = createLockInfoForRandomPersonWithCarnivoreAuthority(observation, false);

        final ObservationDTO dto = createDtoForTest();
        dto.setGameSpeciesCode(largeCarnivoreSpecies.getOfficialCode());
        dto.setVerifiedByCarnivoreAuthority(false);
        dto.setObserverName("another person");
        dto.setObserverPhoneNumber("987654321");
        dto.setOfficialAdditionalInfo("changed");

        service.updateMutableFields(observation, dto, lockInfo);

        assertThat(observation.getInYardDistanceToResidence(), equalTo(10));
        assertThat(observation.getVerifiedByCarnivoreAuthority(), equalTo(true));
        assertThat(observation.getObserverName(), equalTo("carnivore person"));
        assertThat(observation.getObserverPhoneNumber(), equalTo("123456789"));
        assertThat(observation.getOfficialAdditionalInfo(), equalTo("original"));
    }

    @Test
    public void testUpdatingLargeCarnivoreFields_moderator_whenNotLockedByHuntingConditions() {
        final Observation observation = new Observation();
        observation.setSpecies(largeCarnivoreSpecies);
        observation.setGeoLocation(rhy1Location);
        observation.setInYardDistanceToResidence(DEFAULT_DISTANCE_TO_RESIDENCE);
        observation.setVerifiedByCarnivoreAuthority(true);
        observation.setObserverName("carnivore person");
        observation.setObserverPhoneNumber("123456789");
        observation.setOfficialAdditionalInfo("original");

        final ObservationLockInfo lockInfo = createLockInfoForModerator(observation, false);

        final ObservationDTO dto = createDtoForTest();
        dto.setGameSpeciesCode(largeCarnivoreSpecies.getOfficialCode());
        dto.setGeoLocation(rhy2Location);
        dto.setVerifiedByCarnivoreAuthority(false);
        dto.setObserverName("another person");
        dto.setObserverPhoneNumber("987654321");
        dto.setOfficialAdditionalInfo("changed");

        service.updateMutableFields(observation, dto, lockInfo);

        assertThat(observation.getInYardDistanceToResidence(), equalTo(DEFAULT_DISTANCE_TO_RESIDENCE));
        assertThat(observation.getVerifiedByCarnivoreAuthority(), equalTo(true));
        assertThat(observation.getObserverName(), equalTo("carnivore person"));
        assertThat(observation.getObserverPhoneNumber(), equalTo("123456789"));
        assertThat(observation.getOfficialAdditionalInfo(), equalTo("original"));
    }

    // Mandatory fields are populated.
    private ObservationDTO createDtoForTest() {
        final ObservationDTO dto = new ObservationDTO();
        dto.setGeoLocation(rhy1Location);
        dto.setGameSpeciesCode(species.getOfficialCode());
        dto.setObservationCategory(some(ObservationCategory.class));
        return dto;
    }

    private static ObservationLockInfo createLockInfoForAuthor(final Observation observation,
                                                               final boolean lockedByHuntingConditions) {

        return createLockInfoForPerson(observation, true, false, lockedByHuntingConditions);
    }

    private static ObservationLockInfo createLockInfoForAuthorWithCarnivoreAuthority(final Observation observation,
                                                                                     final boolean lockedByHuntingConditions) {

        return createLockInfoForPerson(observation, true, true, lockedByHuntingConditions);
    }

    // "Random person" means not author or observer
    private static ObservationLockInfo createLockInfoForRandomPerson(final Observation observation,
                                                                     final boolean lockedByHuntingConditions) {

        return createLockInfoForPerson(observation, false, false, lockedByHuntingConditions);
    }

    // "Random person" means not author or observer
    private static ObservationLockInfo createLockInfoForRandomPersonWithCarnivoreAuthority(final Observation observation,
                                                                                           final boolean lockedByHuntingConditions) {

        return createLockInfoForPerson(observation, false, true, lockedByHuntingConditions);
    }

    private static ObservationLockInfo createLockInfoForPerson(final Observation observation,
                                                               final boolean authorOrObserver,
                                                               final boolean carnivoreAuthorityInSomeRhy,
                                                               final boolean lockedByHuntingConditions) {

        final ObservationModifierInfo modifierInfo = ObservationModifierInfo.builder()
                .withActiveUser(newUser(Role.ROLE_USER))
                .withAuthorOrObserver(authorOrObserver)
                .withCarnivoreAuthorityInAnyRhyAtObservationDate(carnivoreAuthorityInSomeRhy)
                .build();

        return createLockInfo(observation, modifierInfo, lockedByHuntingConditions);
    }

    private static ObservationLockInfo createLockInfoForModerator(final Observation observation,
                                                                  final boolean lockedByHuntingConditions) {

        final ObservationModifierInfo modifierInfo = ObservationModifierInfo.builder()
                .withActiveUser(newUser(Role.ROLE_MODERATOR))
                .withAuthorOrObserver(false)
                .withCarnivoreAuthorityInAnyRhyAtObservationDate(false)
                .build();

        return createLockInfo(observation, modifierInfo, lockedByHuntingConditions);
    }

    private static ObservationLockInfo createLockInfo(final Observation observation,
                                                      final ObservationModifierInfo modifierInfo,
                                                      final boolean lockedByHuntingConditions) {

        // Locking logic done in ObservationLockChecker is simulated here.

        final boolean lockedByLargeCarnivoreConditions =
                observation.isAnyLargeCarnivoreFieldPresent() && !modifierInfo.canUpdateCarnivoreFields();

        final boolean lockedByAllConditions = lockedByHuntingConditions || lockedByLargeCarnivoreConditions;

        // Last "false" argument is irrelevant here.
        return new ObservationLockInfo(modifierInfo, lockedByAllConditions, false);
    }

    private static SystemUser newUser(final Role role) {
        final SystemUser user = new SystemUser();
        user.setRole(role);
        return user;
    }
}
