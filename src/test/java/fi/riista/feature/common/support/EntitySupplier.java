package fi.riista.feature.common.support;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.announcement.AnnouncementSenderType;
import fi.riista.feature.announcement.AnnouncementSubscriber;
import fi.riista.feature.common.entity.BicEntity;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.IbanEntity;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenOpsForTest;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.metadata.ObservationBaseFields;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextSensitiveFields;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadata;
import fi.riista.feature.gamediary.observation.specimen.GameMarking;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameAge;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameState;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventNameEnum;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.gamediary.srva.SrvaEventTypeEnum;
import fi.riista.feature.gamediary.srva.SrvaResultEnum;
import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.method.SrvaMethodEnum;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitContactPerson;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.allocation.HarvestPermitAllocation;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.feature.harvestpermit.report.state.HarvestReportStateHistory;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.harvestpermit.season.MooselikePrice;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingMethod;
import fi.riista.feature.huntingclub.hunting.rejection.HarvestRejection;
import fi.riista.feature.huntingclub.hunting.rejection.ObservationRejection;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitation;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImport;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.harvestreport.MooseHarvestReport;
import fi.riista.feature.huntingclub.permit.summary.AreaSizeAndRemainingPopulation;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingAreaType;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummary;
import fi.riista.feature.huntingclub.permit.summary.SpeciesEstimatedAppearance;
import fi.riista.feature.huntingclub.permit.summary.TrendOfPopulationGrowth;
import fi.riista.feature.organization.AlueellinenRiistaneuvosto;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.Riistakeskus;
import fi.riista.feature.organization.ValtakunnallinenRiistaneuvosto;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.calendar.Venue;
import fi.riista.feature.organization.jht.nomination.OccupationNomination;
import fi.riista.feature.organization.jht.training.JHTTraining;
import fi.riista.feature.organization.lupahallinta.LHOrganisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.storage.backend.db.PersistentFileContent;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.StorageType;
import fi.riista.integration.common.entity.Integration;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import fi.riista.util.MediaTypeExtras;
import fi.riista.util.NumberGenerator;
import fi.riista.util.ValueGenerator;
import fi.riista.util.ValueGeneratorMixin;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.springframework.data.domain.Persistable;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static fi.riista.util.DateUtil.localDateTime;
import static fi.riista.util.DateUtil.today;
import static java.util.Optional.ofNullable;

/**
 * This class can be used to create @Entity annotated objects or graphs for test code.
 */
public class EntitySupplier implements ValueGeneratorMixin {

    private static final int DEFAULT_PERMIT_AREA_SIZE = 34567;
    private final NumberGenerator numberGenerator;
    private final List<Persistable<?>> transientEntityList;
    private final Supplier<Riistakeskus> riistakeskusSupplier;

    public EntitySupplier(
            final NumberGenerator numberGenerator,
            final List<Persistable<?>> transientEntityList,
            final Supplier<Riistakeskus> riistakeskusSupplier) {

        this.numberGenerator = Objects.requireNonNull(numberGenerator, "numberGenerator is null");
        this.transientEntityList = Objects.requireNonNull(transientEntityList, "transientEntityList is null");
        this.riistakeskusSupplier = Objects.requireNonNull(riistakeskusSupplier, "riistakeskusSupplier is null");
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return numberGenerator;
    }

    public SystemUser newUser(String username, PasswordEncoder passwordEncoder) {
        return newUser(username, SystemUser.Role.ROLE_USER, passwordEncoder);
    }

    public SystemUser newUser(SystemUser.Role role, PasswordEncoder passwordEncoder) {
        return newUser(generateUsername(role), role, passwordEncoder);
    }

    public SystemUser newUser(String username, SystemUser.Role role, PasswordEncoder passwordEncoder) {
        return newUser(username, String.format("%12d", serial()), role, passwordEncoder);
    }

    public SystemUser newUser(String username, String password, SystemUser.Role role, PasswordEncoder passwordEncoder) {
        SystemUser user = new SystemUser();
        user.setUsername(username);
        user.setEmail(username + "@invalid");
        user.setRole(role);
        user.setActive(true);

        if (passwordEncoder != null) {
            user.setPasswordAsPlaintext(password, passwordEncoder);
        }

        return add(user);
    }

    public SystemUser newUser(Person person) {
        SystemUser user = newUser(SystemUser.Role.ROLE_USER, null);
        user.setPerson(person);
        return user;
    }

    public Person newPerson() {
        int nameNum = serial();

        return newPerson("FirstName-" + nameNum, "LastName-" + nameNum, ssn(), hunterNumber());
    }

    public Person newPerson(String firstName, String lastName, String ssn, String hunterNumber) {
        Person person = new Person();
        person.setSsn(ssn);
        person.setFirstName(firstName);
        person.setByName(firstName);
        person.setLastName(lastName);
        person.setFinnishCitizen(true);
        person.setLanguageCode(Locales.FI_LANG);
        person.setEmail(ValueGenerator.email(numberGenerator, person.getByName(), person.getLastName()));
        person.setPhoneNumber(phoneNumber());
        person.setHunterNumber(hunterNumber);
        person.setLhPersonId(lupaHallintaId());

        final LocalDate today = today();
        person.setHunterExamDate(today.minusYears(2).minusDays(10));
        person.setHuntingCardStart(today.minusYears(2));
        person.setHuntingCardEnd(today.plusYears(1));

        return add(person);
    }

    public Address newAddress() {
        int nameNum = serial();
        return newAddress("street " + nameNum, "city " + nameNum, "country" + nameNum);
    }

    public Municipality newMunicipality() {
        int nameNum = serial();
        return add(new Municipality(zeroPaddedNumber(3), "KuntaFI" + nameNum, "KuntaSV" + nameNum));
    }

    public Address newAddress(String street, String city, String country) {
        return newAddress(street, postalCode(), city, country);
    }

    public Address newAddress(String street, String postalCode, String city, String country) {
        return add(new Address(street, postalCode, city, country));
    }

    public ValtakunnallinenRiistaneuvosto newValtakunnallinenRiistaneuvosto(Riistakeskus rk, String nameFi, String nameSv) {
        return add(new ValtakunnallinenRiistaneuvosto(rk, nameFi, nameSv));
    }

    public RiistakeskuksenAlue newRiistakeskuksenAlue() {
        return newRiistakeskuksenAlue(zeroPaddedNumber(3));
    }

    public RiistakeskuksenAlue newRiistakeskuksenAlue(String officialCode) {
        return newRiistakeskuksenAlue(
                riistakeskusSupplier.get(),
                officialCode,
                "RiistakeskuksenAlueFI-" + officialCode,
                "RiistakeskuksenAlueSV-" + officialCode);
    }

    public RiistakeskuksenAlue newRiistakeskuksenAlue(
            Riistakeskus rk, String officialCode, String nameFi, String nameSv) {

        final RiistakeskuksenAlue rka = new RiistakeskuksenAlue(rk, nameFi, nameSv, officialCode);
        rka.setEmail(nameFi + "@invalid");
        return add(rka);
    }

    public AlueellinenRiistaneuvosto newAlueellinenRiistaneuvosto(
            RiistakeskuksenAlue rkAlue, String nameFi, String nameSv) {

        return add(new AlueellinenRiistaneuvosto(rkAlue, nameFi, nameSv));
    }

    public Riistanhoitoyhdistys newRiistanhoitoyhdistys(RiistakeskuksenAlue rka) {
        return newRiistanhoitoyhdistys(rka, zeroPaddedNumber(3));
    }

    public Riistanhoitoyhdistys newRiistanhoitoyhdistys(RiistakeskuksenAlue rka, String officialCode) {
        return newRiistanhoitoyhdistys(
                rka, officialCode, "RiistanhoitoyhdistysFI-" + officialCode, "RiistanhoitoyhdistysSV-" + officialCode);
    }

    public Riistanhoitoyhdistys newRiistanhoitoyhdistys(
            RiistakeskuksenAlue rka, String officialCode, String nameFi, String nameSv) {

        return add(new Riistanhoitoyhdistys(rka, nameFi, nameSv, officialCode));
    }

    public Riistanhoitoyhdistys newRiistanhoitoyhdistys() {
        return newRiistanhoitoyhdistys(newRiistakeskuksenAlue());
    }

    public Occupation newOccupation(Organisation org, Person person, OccupationType type) {
        return newOccupation(org, person, type, null, null);
    }

    public Occupation newOccupation(
            Organisation org, Person person, OccupationType type, LocalDate beginDate, LocalDate endDate) {

        Occupation occupation = new Occupation(person, org, type);
        occupation.setBeginDate(beginDate);
        occupation.setEndDate(endDate);
        return add(occupation);
    }

    public OccupationNomination newOccupationNomination(
            Riistanhoitoyhdistys rhy, OccupationType occupationType, Person person, Person rhyPerson) {
        final OccupationNomination occupationNomination = new OccupationNomination();
        occupationNomination.setNominationStatus(OccupationNomination.NominationStatus.EHDOLLA);
        occupationNomination.setOccupationType(occupationType);
        occupationNomination.setPerson(person);
        occupationNomination.setRhy(rhy);
        occupationNomination.setRhyPerson(rhyPerson);

        return add(occupationNomination);
    }

    public Occupation newDeletedOccupation(Organisation org, Person person, OccupationType type) {
        Occupation occupation = add(new Occupation(person, org, type));
        occupation.softDelete();
        return occupation;
    }

    public GameSpecies newGameSpecies() {
        int num = serial();
        return newGameSpecies(
                num, some(GameCategory.class), "SpeciesNameFi-" + num, "SpeciesNameSv-" + num, "SpeciesNameEn-" + num);
    }

    public GameSpecies newGameSpecies(int officialCode) {
        GameSpecies species = newGameSpecies();
        species.setOfficialCode(officialCode);
        return species;
    }

    public GameSpecies newGameSpecies(boolean multipleSpecimenAllowedOnHarvest) {
        GameSpecies species = newGameSpecies();
        species.setMultipleSpecimenAllowedOnHarvest(multipleSpecimenAllowedOnHarvest);
        return species;
    }

    public GameSpecies newGameSpecies(
            int officialCode, GameCategory category, String nameFi, String nameSv, String nameEn) {

        return add(new GameSpecies(officialCode, category, nameFi, nameSv, nameEn));
    }

    public GameSpecies newGameSpeciesMoose() {
        final GameSpecies species =
                newGameSpecies(GameSpecies.OFFICIAL_CODE_MOOSE, GameCategory.GAME_MAMMAL, "hirvi", "älg", "moose");
        species.setMultipleSpecimenAllowedOnHarvest(false);
        return species;
    }

    public HarvestPermit newHarvestPermit() {
        return newHarvestPermit(newRiistanhoitoyhdistys());
    }

    public HarvestPermit newHarvestPermitWithPermitAreaSize(final Riistanhoitoyhdistys rhy) {
        final HarvestPermit permit = newHarvestPermit(rhy);
        permit.setPermitAreaSize(DEFAULT_PERMIT_AREA_SIZE);
        permit.setPermitTypeCode(HarvestPermit.MOOSELIKE_PERMIT_TYPE);
        return permit;
    }

    public HarvestPermit newHarvestPermit(String permitNumber) {
        return newHarvestPermit(newRiistanhoitoyhdistys(), permitNumber);
    }

    public HarvestPermit newHarvestPermit(Riistanhoitoyhdistys rhy) {
        Organisation rka = rhy.getParentOrganisation();
        return newHarvestPermit(rhy, permitNumber(rka.getOfficialCode()));
    }

    public HarvestPermit newHarvestPermit(Riistanhoitoyhdistys rhy, String permitNumber) {
        HarvestPermit permit = new HarvestPermit();
        permit.setRhy(rhy);
        permit.setPermitNumber(permitNumber);
        permit.setOriginalContactPerson(newPerson());

        permit.setPermitTypeCode("200");
        permit.setPermitType("testPermitType " + permit.getPermitTypeCode());

        return add(permit);
    }

    public HarvestPermit newHarvestPermit(Person contactPerson) {
        return newHarvestPermit(newRiistanhoitoyhdistys(), contactPerson);
    }

    public HarvestPermit newHarvestPermit(Riistanhoitoyhdistys rhy, Person contactPerson) {
        return newHarvestPermit(rhy, contactPerson, false);
    }

    public HarvestPermit newHarvestPermit(Person contactPerson, boolean harvestsAsList) {
        return newHarvestPermit(newRiistanhoitoyhdistys(), contactPerson, harvestsAsList);
    }

    public HarvestPermit newHarvestPermit(Riistanhoitoyhdistys rhy, Person contactPerson, boolean harvestsAsList) {
        HarvestPermit permit = newHarvestPermit(rhy);
        permit.setHarvestsAsList(harvestsAsList);
        add(permit);

        newHarvestPermitContactPerson(permit, contactPerson);

        return permit;
    }

    public HarvestPermit newHarvestPermit(boolean harvestsAsList) {
        return newHarvestPermit(newRiistanhoitoyhdistys(), harvestsAsList);
    }

    public HarvestPermit newHarvestPermit(Riistanhoitoyhdistys rhy, boolean harvestsAsList) {
        HarvestPermit permit = newHarvestPermit(rhy);
        permit.setHarvestsAsList(harvestsAsList);
        return permit;
    }

    public HarvestPermit newHarvestPermit(HarvestPermit originalPermit) {
        HarvestPermit permit = newHarvestPermit(originalPermit.getRhy());
        permit.setOriginalPermit(originalPermit);
        return permit;
    }

    public HarvestPermit newHarvestPermit(HarvestPermit originalPermit, Person contactPerson) {
        return newHarvestPermit(originalPermit, contactPerson, false);
    }

    public HarvestPermit newHarvestPermit(HarvestPermit originalPermit, Person contactPerson, boolean harvestsAsList) {
        HarvestPermit permit = newHarvestPermit(originalPermit.getRhy(), contactPerson);
        permit.setOriginalPermit(originalPermit);
        permit.setPermitTypeCode(HarvestPermit.MOOSELIKE_AMENDMENT_PERMIT_TYPE);
        permit.setHarvestsAsList(harvestsAsList);
        return permit;
    }

    public HarvestPermit newHarvestPermitForHuntingGroup(HuntingClubGroup group) {
        final Organisation rhy = group.getParentOrganisation().getParentOrganisation();
        final HarvestPermit harvestPermit = newHarvestPermit((Riistanhoitoyhdistys) rhy);
        newHarvestPermitSpeciesAmount(harvestPermit, group.getSpecies());
        group.updateHarvestPermit(harvestPermit);
        return harvestPermit;
    }

    public HarvestPermitContactPerson newHarvestPermitContactPerson(HarvestPermit permit, Person person) {
        return add(new HarvestPermitContactPerson(permit, person));
    }

    public HarvestPermitSpeciesAmount newHarvestPermitSpeciesAmount(HarvestPermit permit, GameSpecies species) {
        return newHarvestPermitSpeciesAmount(permit, species, 1f);
    }

    public HarvestPermitSpeciesAmount newHarvestPermitSpeciesAmount(
            HarvestPermit permit, GameSpecies species, int huntingYear) {

        return newHarvestPermitSpeciesAmount(permit, species, huntingYear, 1f);
    }

    public HarvestPermitSpeciesAmount newHarvestPermitSpeciesAmount(
            HarvestPermit permit, GameSpecies species, float amount) {

        return newHarvestPermitSpeciesAmount(
                permit, species, DateUtil.getFirstCalendarYearOfCurrentHuntingYear(), amount);
    }

    public HarvestPermitSpeciesAmount newHarvestPermitSpeciesAmount(
            HarvestPermit permit, GameSpecies species, int huntingYear, float amount) {

        HarvestPermitSpeciesAmount speciesAmount = new HarvestPermitSpeciesAmount();
        speciesAmount.setHarvestPermit(permit);
        speciesAmount.setGameSpecies(species);
        speciesAmount.setBeginDate(DateUtil.huntingYearBeginDate(huntingYear));
        speciesAmount.setEndDate(DateUtil.huntingYearEndDate(huntingYear));
        speciesAmount.setAmount(amount);
        return add(speciesAmount);
    }

    public HarvestPermitAllocation newHarvestPermitAllocation(
            HarvestPermit permit, GameSpecies species, HuntingClub club) {

        HarvestPermitAllocation allocation = new HarvestPermitAllocation();
        allocation.setHarvestPermit(permit);
        allocation.setGameSpecies(species);
        allocation.setHuntingClub(club);
        allocation.setAdultMales(nextPositiveIntAtMost(50));
        allocation.setAdultFemales(nextPositiveIntAtMost(50));
        allocation.setYoung(nextPositiveIntAtMost(50));
        allocation.setTotal(
                allocation.getAdultMales().floatValue()
                        + allocation.getAdultFemales().floatValue()
                        + 0.5f * allocation.getYoung().floatValue());
        return add(allocation);
    }

    /**
     * PERSIST BEFORE CALLING THIS!
     * <p>
     *     If you do not persist before calling this, then persist will fail to:<br>
     *     Referential integrity constraint violation: "FK_MOOSE_HUNTING_SUMMARY_HARVEST_PERMIT_PARTNERS: PUBLIC.MOOSE_HUNTING_SUMMARY FOREIGN KEY(HARVEST_PERMIT_ID, CLUB_ID) REFERENCES PUBLIC.HARVEST_PERMIT_PARTNERS(HARVEST_PERMIT_ID, ORGANISATION_ID)
     * </p>
     * <p>moose_hunting_summary table has foreign key to harvest_permit_partners table.
     * Hibernate has no knowledge of this, therefore it will arrange flushing in a way that moose_hunting_summary is
     * inserted before harvest_permit_partners table is populated.</p>
     * <p>Fix would be to create HarvestPermitPartner entity referencing to permit and club.</p>
     *
     * @param permit
     * @param club
     * @param huntingFinished
     * @return
     */
    public MooseHuntingSummary newMooseHuntingSummary(HarvestPermit permit, HuntingClub club, boolean huntingFinished) {
        final MooseHuntingSummary summary = new MooseHuntingSummary(club, permit);

        summary.setAreaSizeAndPopulation(new AreaSizeAndRemainingPopulation()
                .withTotalHuntingArea(DEFAULT_PERMIT_AREA_SIZE)
                .withEffectiveHuntingArea(23456)
                .withRemainingPopulationInTotalArea(789)
                .withRemainingPopulationInEffectiveArea(456));
        summary.setHuntingAreaType(MooseHuntingAreaType.SUMMER_PASTURE);

        summary.setWhiteTailedDeerAppearance(
                new SpeciesEstimatedAppearance(true, TrendOfPopulationGrowth.DECREASED, 123));
        summary.setRoeDeerAppearance(
                new SpeciesEstimatedAppearance(true, TrendOfPopulationGrowth.UNCHANGED, 456));
        summary.setWildForestReindeerAppearance(
                new SpeciesEstimatedAppearance(true, TrendOfPopulationGrowth.INCREASED, 789));
        summary.setFallowDeerAppearance(new SpeciesEstimatedAppearance(false, null, null));

        summary.setNumberOfDrownedMooses(1);
        summary.setNumberOfMoosesKilledByBear(2);
        summary.setNumberOfMoosesKilledByWolf(3);
        summary.setNumberOfMoosesKilledInTrafficAccident(4);
        summary.setNumberOfMoosesKilledByPoaching(5);
        summary.setNumberOfMoosesKilledInRutFight(6);
        summary.setNumberOfStarvedMooses(7);
        summary.setNumberOfMoosesDeceasedByOtherReason(8);
        summary.setCauseOfDeath("abc");

        LocalDate today = today();

        summary.setMooseHeatBeginDate(today.minusMonths(6));
        summary.setMooseHeatEndDate(today.minusMonths(5));
        summary.setMooseFawnBeginDate(today.minusMonths(4));
        summary.setMooseFawnEndDate(today.minusMonths(3));

        summary.setDeerFliesAppeared(true);
        summary.setDateOfFirstDeerFlySeen(today.minusMonths(4));
        summary.setDateOfFirstDeerFlySeen(today.minusMonths(3));
        summary.setTrendOfDeerFlyPopulationGrowth(TrendOfPopulationGrowth.UNCHANGED);
        summary.setNumberOfAdultMoosesHavingFlies(123);
        summary.setNumberOfAdultMoosesHavingFlies(456);

        summary.setHuntingEndDate(today);
        summary.setHuntingFinished(huntingFinished);

        return add(summary);
    }

    public BasicClubHuntingSummary newBasicHuntingSummary(
            HarvestPermitSpeciesAmount speciesAmount, HuntingClub club, boolean huntingFinished) {

        final BasicClubHuntingSummary summary = new BasicClubHuntingSummary(club, speciesAmount);

        summary.setHuntingFinished(huntingFinished);
        summary.setHuntingEndDate(today());
        summary.setAreaSizeAndPopulation(new AreaSizeAndRemainingPopulation()
                .withTotalHuntingArea(56789)
                .withEffectiveHuntingArea(45678)
                .withRemainingPopulationInTotalArea(678)
                .withRemainingPopulationInEffectiveArea(345));

        return add(summary);
    }

    public BasicClubHuntingSummary newModeratedBasicHuntingSummary(
            HarvestPermitSpeciesAmount speciesAmount, HuntingClub club) {

        BasicClubHuntingSummary summary = newBasicHuntingSummary(speciesAmount, club, true);

        summary.doModeratorOverride(
                speciesAmount.getLastDate(),
                new AreaSizeAndRemainingPopulation()
                        .withTotalHuntingArea(45678)
                        .withEffectiveHuntingArea(34567)
                        .withRemainingPopulationInTotalArea(567)
                        .withRemainingPopulationInEffectiveArea(234),
                HasHarvestCountsForPermit.of(98, 87, 76, 65, 54, 43));

        return summary;
    }

    public MooseHarvestReport newMooseHarvestReport(final HarvestPermitSpeciesAmount speciesAmount) {
        final MooseHarvestReport e = new MooseHarvestReport();
        e.setSpeciesAmount(speciesAmount);
        e.setReceiptFileMetadata(newPersistentFileMetadata(StorageType.LOCAL_FOLDER));
        return add(e);
    }

    public MooseHarvestReport newMooseHarvestReportModeratorOverride(final HarvestPermitSpeciesAmount speciesAmount) {
        final MooseHarvestReport e = new MooseHarvestReport();
        e.setSpeciesAmount(speciesAmount);
        e.setModeratorOverride(true);
        return add(e);
    }

    public Harvest newHarvest() {
        return newHarvest(newPerson());
    }

    public Harvest newHarvest(Person author) {
        return newHarvest(newGameSpecies(), author, author, GeoLocation.Source.MANUAL);
    }

    public Harvest newHarvest(Person author, Person hunter) {
        return newHarvest(newGameSpecies(), author, hunter, GeoLocation.Source.MANUAL);
    }

    public Harvest newHarvest(Person author, GeoLocation location) {
        return newHarvest(newGameSpecies(), author, author, location, localDateTime(), 1);
    }

    public Harvest newHarvest(HarvestPermit permit) {
        Harvest harvest = newHarvest();
        harvest.setHarvestPermit(permit);
        harvest.setRhy(permit.getRhy());
        permit.addHarvest(harvest);
        return harvest;
    }

    public Harvest newHarvest(HarvestPermit permit, GameSpecies species) {
        Harvest harvest = newHarvest(permit);
        harvest.setSpecies(species);
        return harvest;
    }

    public Harvest newHarvest(GameSpecies species) {
        Person hunter = newPerson();
        return newHarvest(species, hunter, hunter);
    }

    public Harvest newHarvest(GameSpecies species, Person hunter) {
        return newHarvest(species, hunter, hunter);
    }

    public Harvest newHarvest(GameSpecies species, Person author, Person hunter) {
        return newHarvest(species, author, hunter, GeoLocation.Source.MANUAL);
    }

    public Harvest newHarvest(Person hunter, GroupHuntingDay huntingDay) {
        return newHarvest(huntingDay.getGroup().getSpecies(), hunter, huntingDay);
    }

    public Harvest newHarvest(GameSpecies species, Person hunter, GroupHuntingDay huntingDay) {
        return newHarvest(species, hunter, huntingDay, null);
    }

    private Harvest newHarvest(GameSpecies species, Person hunter, GroupHuntingDay huntingDay, Person acceptor) {
        final Harvest harvest = newHarvest(species, hunter, huntingDay.getStartDate());
        harvest.updateHuntingDayOfGroup(huntingDay, acceptor);
        return harvest;
    }

    public Harvest newHarvest(Person hunter, LocalDate pointOfTime) {
        return newHarvest(newGameSpecies(), hunter, pointOfTime);
    }

    public Harvest newHarvest(GameSpecies species, Person hunter, LocalDate pointOfTime) {
        Harvest harvest = newHarvest(species, hunter);
        harvest.setPointOfTime(DateUtil.toDateNullSafe(pointOfTime));
        return harvest;
    }

    public Harvest newHarvest(GameSpecies species, Riistanhoitoyhdistys rhy) {
        Person hunter = newPerson();
        Harvest harvest = newHarvest(species, hunter, hunter);
        harvest.setRhy(rhy);
        return harvest;
    }

    public Harvest newHarvest(GameSpecies species, GameAge age, GameGender gender, Riistanhoitoyhdistys rhy) {
        Person person = newPerson();
        Harvest harvest = newHarvest(species, person, person, some(GeoLocation.Source.class));
        harvest.setRhy(rhy);
        newHarvestSpecimen(harvest, age, gender);
        harvest.setAmount(1);
        return harvest;
    }

    public Harvest newMobileHarvest() {
        return newMobileHarvest(newGameSpecies(), newPerson());
    }

    public Harvest newMobileHarvest(GameSpecies species) {
        return newMobileHarvest(species, newPerson());
    }

    public Harvest newMobileHarvest(Person author) {
        return newMobileHarvest(newGameSpecies(), author);
    }

    public Harvest newMobileHarvest(GameSpecies species, Person author) {
        Harvest harvest = newHarvest(species, author, author, GeoLocation.Source.GPS_DEVICE);
        harvest.setFromMobile(true);
        harvest.setMobileClientRefId((long) serial());
        return harvest;
    }

    public Harvest newMobileHarvest(GameSpecies species, Person hunter, LocalDate pointOfTime) {
        Harvest harvest = newMobileHarvest(species, hunter);
        harvest.setPointOfTime(DateUtil.toDateNullSafe(pointOfTime));
        return harvest;
    }

    public Harvest newMobileHarvest(GameSpecies species, Person author, GroupHuntingDay huntingDay) {
        return newMobileHarvest(species, author, huntingDay, null);
    }

    private Harvest newMobileHarvest(GameSpecies species, Person author, GroupHuntingDay huntingDay, Person acceptor) {
        final Harvest harvest = newMobileHarvest(species, author, huntingDay.getStartDate());
        harvest.updateHuntingDayOfGroup(huntingDay, acceptor);
        return harvest;
    }

    public Harvest newHarvest(GameSpecies species, Person author, Person hunter, GeoLocation.Source source) {
        return newHarvest(species, author, hunter, geoLocation(source), localDateTime(), 1);
    }

    public Harvest newHarvest(GameSpecies species, Person author, Person hunter, GeoLocation geoLocation,
                               LocalDateTime pointOfTime, int amount) {

        Harvest harvest = new Harvest(author, geoLocation, pointOfTime, species, amount);
        if (hunter != null) {
            harvest.setActualShooter(hunter);
        }
        harvest.setFromMobile(false);
        harvest.setDescription("description");
        return add(harvest);
    }

    public HarvestSpecimen newHarvestSpecimen(Harvest harvest) {
        return newHarvestSpecimen(harvest, HarvestSpecVersion.MOST_RECENT);
    }

    public HarvestSpecimen newHarvestSpecimen(Harvest harvest, HarvestSpecVersion version) {
        final int gameSpeciesCode = harvest != null && harvest.getSpecies() != null
                ? harvest.getSpecies().getOfficialCode()
                : serial();

        return newHarvestSpecimen(
                harvest, new HarvestSpecimenOpsForTest(gameSpeciesCode, version, getNumberGenerator()));
    }

    public HarvestSpecimen newHarvestSpecimen(Harvest harvest, HarvestSpecimenOpsForTest specimenOps) {
        Optional.ofNullable(harvest).map(Harvest::getSpecies).ifPresent(species -> {
            Preconditions.checkArgument(species.getOfficialCode() == specimenOps.getGameSpeciesCode(),
                    "Game species code mismatch while generating harvest specimen");
        });

        final boolean allowUnknownAgeAndGender = !GameSpecies.isMoose(specimenOps.getGameSpeciesCode());
        final HarvestSpecimen specimen = new HarvestSpecimen(harvest);
        specimenOps.mutateContent(specimen, allowUnknownAgeAndGender);
        return add(specimen);
    }

    public HarvestSpecimen newHarvestSpecimen(Harvest harvest, GameAge age, GameGender gender) {
        return newHarvestSpecimen(harvest, age, gender, weight());
    }

    public HarvestSpecimen newHarvestSpecimen(Harvest harvest, GameAge age, GameGender gender, Double weight) {
        return add(new HarvestSpecimen(harvest, age, gender, weight));
    }

    public ObservationSpecimen newObservationSpecimen(Observation obs) {
        ObservationSpecimen specimen = new ObservationSpecimen(obs);
        specimen.setAge(some(ObservedGameAge.class));
        specimen.setGender(some(GameGender.class));
        specimen.setState(some(ObservedGameState.class));
        specimen.setMarking(some(GameMarking.class));
        return add(specimen);
    }

    public GameDiaryImage newGameDiaryImage(Harvest harvest) {
        return newGameDiaryImage(harvest, newPersistentFileContent().getMetadata());
    }

    public GameDiaryImage newGameDiaryImage(Observation observation) {
        return newGameDiaryImage(observation, newPersistentFileContent().getMetadata());
    }

    public GameDiaryImage newGameDiaryImage(SrvaEvent srvaEvent) {
        return add(new GameDiaryImage(srvaEvent, newPersistentFileContent().getMetadata()));
    }

    public GameDiaryImage newGameDiaryImage(Harvest harvest, PersistentFileMetadata metadata) {
        return add(new GameDiaryImage(harvest, metadata));
    }

    public GameDiaryImage newGameDiaryImage(Observation observation, PersistentFileMetadata metadata) {
        return add(new GameDiaryImage(observation, metadata));
    }

    public PersistentFileContent newPersistentFileContent() {
        final PersistentFileMetadata metadata = newPersistentFileMetadata(StorageType.LOCAL_DATABASE);
        return add(new PersistentFileContent(metadata, "xyzzy".getBytes()));
    }

    public PersistentFileMetadata newPersistentFileMetadata(StorageType storageType) {
        final PersistentFileMetadata pfm = new PersistentFileMetadata();
        pfm.setId(UUID.randomUUID());
        pfm.setStorageType(storageType);
        pfm.setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        return add(pfm);
    }

    public HarvestReportFields newHarvestReportFields(GameSpecies species, boolean usedWithPermit) {
        return newHarvestReportFields("HarvestReportFields-" + serial(), species, usedWithPermit);
    }

    public HarvestReportFields newHarvestReportFields(String name, GameSpecies species, boolean usedWithPermit) {
        return add(new HarvestReportFields(name, species, usedWithPermit));
    }

    public HarvestArea newHarvestArea() {
        return newHarvestArea(newRiistanhoitoyhdistys());
    }

    public HarvestArea newHarvestArea(Riistanhoitoyhdistys rhy) {
        String name = "HarvestArea-" + serial();
        return newHarvestArea(HarvestArea.HarvestAreaType.PORONHOITOALUE, name, name, Sets.newHashSet(rhy));
    }

    public HarvestArea newHarvestArea(
            HarvestArea.HarvestAreaType harvestAreaType, String nameFi, String nameSv, Set<Riistanhoitoyhdistys> rhys) {

        return add(new HarvestArea(harvestAreaType, nameFi, nameSv, rhys));
    }

    public HarvestSeason newHarvestSeason(LocalDate seasonBegin) {
        final LocalDate seasonEnd = seasonBegin.plusYears(1);
        final LocalDate reportingDeadline = seasonEnd.plusMonths(1);

        return newHarvestSeason(seasonBegin, seasonEnd, reportingDeadline);
    }

    public HarvestSeason newHarvestSeason(LocalDate beginDate, LocalDate endDate, LocalDate endOfReportingDate) {
        String name = "HarvestSeason-" + serial();
        return newHarvestSeason(name, name, beginDate, endDate, endOfReportingDate);
    }

    public HarvestSeason newHarvestSeason(
            String nameFi, String nameSv, LocalDate beginDate, LocalDate endDate, LocalDate endOfReportingDate) {

        return newHarvestSeason(
                nameFi, nameSv, newHarvestReportFields(newGameSpecies(), false), beginDate, endDate, endOfReportingDate);
    }

    public HarvestSeason newHarvestSeason(
            String nameFi,
            String nameSv,
            HarvestReportFields fields,
            LocalDate beginDate,
            LocalDate endDate,
            LocalDate endOfReportingDate) {

        return add(new HarvestSeason(nameFi, nameSv, fields, beginDate, endDate, endOfReportingDate));
    }

    public HarvestQuota newHarvestQuota(HarvestSeason harvestSeason, HarvestArea harvestArea, int quota) {
        HarvestQuota q = new HarvestQuota(harvestSeason, harvestArea, quota);
        return add(q);
    }

    public HarvestReport newHarvestReport_endOfHunting(HarvestPermit permit, HarvestReport.State state) {
        return newHarvestReport_endOfHunting(permit, state, newPerson());
    }

    public HarvestReport newHarvestReport_endOfHunting(HarvestPermit permit, HarvestReport.State state, Person author) {
        HarvestReport report = add(new HarvestReport());

        report.setHarvestPermit(permit);
        permit.setEndOfHuntingReport(report);

        report.setState(state);
        report.getStateHistory().add(new HarvestReportStateHistory(report, state));
        report.setAuthor(author);
        return report;
    }

    public HarvestReport newHarvestReport(Harvest harvest, HarvestReport.State state) {
        return newHarvestReport(newHarvestReportFields(harvest.getSpecies(), false), state, harvest);
    }

    public HarvestReport newHarvestReport(Harvest harvest, HarvestReport.State state, HarvestQuota quota) {
        HarvestReport r = newHarvestReport(quota.getHarvestSeason().getFields(), state, harvest);
        harvest.setHarvestSeason(quota.getHarvestSeason());
        harvest.setHarvestQuota(quota);
        return r;
    }

    public HarvestReport newHarvestReport(HarvestReportFields fields, HarvestReport.State state, Harvest harvest) {

        // Do queueing first because a HarvestReportStateHistory object is created later which references
        // the report instance.
        HarvestReport report = add(new HarvestReport());

        harvest.setHarvestReport(report);
        // we add here harvest to report, otherwise all tests should refresh their report entity from database
        report.addHarvest(harvest);

        if (harvest.getHarvestPermit() != null) {
            report.setHarvestPermit(harvest.getHarvestPermit());
        }

        harvest.setHarvestReportFields(fields);

        report.setState(state);
        report.getStateHistory().add(new HarvestReportStateHistory(report, state));

        report.setDescription(harvest.getDescription());
        report.setAuthor(harvest.getAuthor());

        return report;
    }

    public Observation newObservation() {
        return newObservation(newGameSpecies());
    }

    public Observation newObservation(final boolean withinMooseHunting) {
        final Observation observation = newObservation(newGameSpecies());
        observation.setWithinMooseHunting(withinMooseHunting);
        return observation;
    }

    public Observation newObservation(Person observer) {
        return newObservation(newGameSpecies(), observer);
    }

    public Observation newObservation(GameSpecies species) {
        return newObservation(species, newPerson());
    }

    public Observation newObservation(GameSpecies species, Person observer) {
        return newObservation(species, observer, observer);
    }

    public Observation newObservation(GameSpecies species, Person author, Person observer) {
        return newObservation(species, author, observer, geoLocation(GeoLocation.Source.MANUAL), localDateTime(), 1);
    }

    public Observation newObservation(GameSpecies species, Person observer, final boolean withinMooseHunting) {
        final Observation observation = newObservation(species, observer);
        observation.setWithinMooseHunting(withinMooseHunting);
        return observation;
    }

    public Observation newObservation(ObservationMetadata metadata) {
        return newObservation(newPerson(), metadata);
    }

    public Observation newObservation(Person observer, ObservationMetadata metadata) {
        return newObservation(observer, metadata, o -> {});
    }

    public Observation newObservation(Person observer, ObservationMetadata metadata, Integer amount) {
        return newObservation(observer, metadata, o -> o.setAmount(amount));
    }

    public Observation newObservation(Person observer, GroupHuntingDay huntingDay) {
        return newObservation(huntingDay.getGroup().getSpecies(), observer, huntingDay);
    }

    public Observation newObservation(GameSpecies species, Person observer, GroupHuntingDay huntingDay) {
        return newObservation(species, observer, huntingDay, null);
    }

    private Observation newObservation(GameSpecies species, Person observer, GroupHuntingDay huntingDay, Person acceptor) {
        final Observation observation = newObservation(species, observer, huntingDay.getStartDate());
        observation.updateHuntingDayOfGroup(huntingDay, acceptor);
        observation.setWithinMooseHunting(true);
        return observation;
    }

    public Observation newObservation(Person observer, LocalDate date) {
        return newObservation(newGameSpecies(), observer, date);
    }

    public Observation newObservation(GameSpecies species, Person observer, LocalDate date) {
        return newObservation(species, observer, observer, geoLocation(GeoLocation.Source.MANUAL),
                date.toLocalDateTime(new LocalTime(9, 0)), 1);
    }

    private Observation newObservation(
            GameSpecies species, Person author, Person observer, GeoLocation geoLocation, LocalDateTime pointOfTime, int amount) {

        final Observation observation = new Observation(author, geoLocation, pointOfTime, species, amount);

        // The observation type is selected because it is applicable for all
        // species. With this observation type, the amount field is mandatory
        // but any other additional fields are optional.
        observation.setObservationType(ObservationType.NAKO);

        if (author != observer) {
            observation.setObserver(observer);
        }
        observation.setDescription("description");
        observation.setFromMobile(false);

        return add(observation);
    }

    private Observation newObservation(
            Person observer, ObservationMetadata metadata, Consumer<Observation> decorator) {

        Objects.requireNonNull(metadata);

        final Observation observation = newObservation(metadata.getSpecies(), observer);
        observation.setWithinMooseHunting(metadata.getWithinMooseHunting());
        observation.setObservationType(metadata.getObservationType());

        final Required amountReq = metadata.getAmount();
        observation.setAmount(amountReq.nonNullValueRequired() ? 1 : null);

        decorator.accept(observation);

        if (observation.getAmount() != null) {
            Preconditions.checkArgument(observation.getAmount() > 0, "amount must be positive");
            Preconditions.checkArgument(amountReq.isAllowedField(), "amount field not allowed by metadata");
        } else {
            Preconditions.checkArgument(!amountReq.nonNullValueRequired(), "amount field is required by metadata");
        }

        return observation;
    }

    public Observation newMobileObservation(Person observer, ObservationMetadata metadata) {
        return newMobileObservation(observer, metadata, o -> {});
    }

    public Observation newMobileObservation(Person observer, ObservationMetadata metadata, Integer amount) {
        return newMobileObservation(observer, metadata, o -> o.setAmount(amount));
    }

    public Observation newMobileObservation(Person observer, ObservationMetadata metadata, LocalDate date) {
        return newMobileObservation(observer, metadata, o -> {
            o.setPointOfTime(DateUtil.toDateNullSafe(date.toLocalDateTime(new LocalTime(9, 0))));
        });
    }

    public Observation newMobileObservation(
            Person observer, ObservationMetadata metadata, GroupHuntingDay huntingDay) {

        return newMobileObservation(observer, metadata, huntingDay, null);
    }

    private Observation newMobileObservation(
            Person observer, ObservationMetadata metadata, GroupHuntingDay huntingDay, Person acceptor) {

        final Observation observation = newMobileObservation(observer, metadata, huntingDay.getStartDate());
        observation.updateHuntingDayOfGroup(huntingDay, acceptor);
        return observation;
    }

    private Observation newMobileObservation(
            Person observer, ObservationMetadata metadata, Consumer<Observation> decorator) {

        return newObservation(observer, metadata, decorator.andThen(o -> {
            o.setFromMobile(true);
            o.setMobileClientRefId((long) serial());
            o.getGeoLocation().setSource(GeoLocation.Source.GPS_DEVICE);
        }));
    }

    public HuntingClubMemberInvitation newHuntingClubInvitation(HuntingClub club) {
        return newHuntingClubInvitation(newPerson(), club, OccupationType.SEURAN_JASEN);
    }

    public HuntingClubMemberInvitation newHuntingClubInvitation(
            Person person, HuntingClub club, OccupationType occType) {

        return add(new HuntingClubMemberInvitation(person, club, occType));
    }

    public HuntingClub newHuntingClub() {
        return newHuntingClub(newRiistanhoitoyhdistys());
    }

    public HuntingClub newHuntingClub(Riistanhoitoyhdistys rhy) {
        return newHuntingClub(rhy,
                "Club" + zeroPaddedNumber(6),
                "ClubSV" + zeroPaddedNumber(6));
    }

    public HuntingClub newHuntingClub(Riistanhoitoyhdistys rhy, String nameFi, String nameSv) {
        return add(new HuntingClub(rhy, nameFi, nameSv));
    }

    public Occupation newHuntingClubMember(HuntingClub club, OccupationType type) {
        return newOccupation(club, newPerson(), type);
    }

    public HuntingClubGroup newHuntingClubGroup() {
        return newHuntingClubGroup(newHuntingClub(), newGameSpecies());
    }

    public HuntingClubGroup newHuntingClubGroup(HuntingClub club) {
        return newHuntingClubGroup(club, newGameSpecies());
    }

    public HuntingClubGroup newHuntingClubGroup(GameSpecies species) {
        return newHuntingClubGroup(newHuntingClub(), species);
    }

    public HuntingClubGroup newHuntingClubGroup(HuntingClub club, GameSpecies species) {
        return newHuntingClubGroup(club, species, DateUtil.getFirstCalendarYearOfCurrentHuntingYear());
    }

    public HuntingClubGroup newHuntingClubGroup(
            HuntingClub club, GameSpecies species, int firstCalendarYearOfHuntingYear) {

        return newHuntingClubGroup(club, species, firstCalendarYearOfHuntingYear, false);
    }

    public HuntingClubGroup newHuntingClubGroup(
            HuntingClub club, GameSpecies species, int firstCalendarYearOfHuntingYear, boolean fromMooseDataCard) {

        HuntingClubGroup group = newHuntingClubGroup(club,
                "Group" + zeroPaddedNumber(6),
                "GroupSV" + zeroPaddedNumber(6),
                species, firstCalendarYearOfHuntingYear);
        group.setFromMooseDataCard(fromMooseDataCard);
        return group;
    }

    public HuntingClubGroup newHuntingClubGroup(
            HuntingClub club, String nameFi, String nameSv, GameSpecies species, int firstCalendarYearOfHuntingYear) {

        return add(new HuntingClubGroup(club, nameFi, nameSv, species, firstCalendarYearOfHuntingYear));
    }

    public HuntingClubGroup newHuntingClubGroupWithAreaContaining(GeoLocation location) {
        return newHuntingClubGroupWithAreaContaining(location, newHuntingClub(), newGameSpecies());
    }

    public HuntingClubGroup newHuntingClubGroupWithAreaContaining(HuntingClub club, GeoLocation location) {
        return newHuntingClubGroupWithAreaContaining(location, club, newGameSpecies());
    }

    public HuntingClubGroup newHuntingClubGroupWithAreaContaining(GeoLocation location, GameSpecies species) {
        return newHuntingClubGroupWithAreaContaining(location, newHuntingClub(), species);
    }

    public HuntingClubGroup newHuntingClubGroupWithAreaContaining(
            GeoLocation location, HuntingClub club, GameSpecies species) {

        return newHuntingClubGroupWithArea(location, true, club, species);
    }

    public HuntingClubGroup newHuntingClubGroupWithAreaNotContaining(GeoLocation location) {
        return newHuntingClubGroupWithArea(location, false, newHuntingClub(), newGameSpecies());
    }

    public HuntingClubGroup newHuntingClubGroupWithAreaNotContaining(HuntingClub club, GeoLocation location) {
        return newHuntingClubGroupWithArea(location, false, club, newGameSpecies());
    }

    private HuntingClubGroup newHuntingClubGroupWithArea(
            GeoLocation location, boolean areaContainsLocation, HuntingClub club, GameSpecies species) {

        final GISZone zone = areaContainsLocation ? newGISZoneContaining(location) : newGISZoneNotContaining(location);

        final HuntingClubArea area = newHuntingClubArea(club);
        area.setZone(zone);

        final HuntingClubGroup group = newHuntingClubGroup(club, species);
        group.setHuntingArea(area);
        return group;
    }

    public HuntingClubGroup newHuntingClubGroupWithArea(final HuntingClubArea area) {
        final HuntingClubGroup group = newHuntingClubGroup(area.getClub(), newGameSpecies());
        group.setHuntingArea(area);
        return group;
    }

    public Occupation newHuntingClubGroupMember(HuntingClubGroup group, OccupationType type) {
        return newOccupation(group, newPerson(), type);
    }

    public Occupation newHuntingClubGroupMember(Person person, HuntingClubGroup group) {
        return newHuntingClubGroupMember(person, group, OccupationType.RYHMAN_JASEN);
    }

    public Occupation newHuntingClubGroupMember(Person person, HuntingClubGroup group, OccupationType occupationType) {
        return add(new Occupation(person, group, occupationType));
    }

    public HuntingClubArea newHuntingClubArea(HuntingClub club) {
        return newHuntingClubArea(club, "Area", "AreaSV", DateUtil.getFirstCalendarYearOfCurrentHuntingYear());
    }

    public HuntingClubArea newHuntingClubArea(HuntingClub club, GISZone zone) {
        HuntingClubArea area = newHuntingClubArea(club);
        area.setZone(zone);
        return area;
    }

    public HuntingClubArea newHuntingClubArea(HuntingClub club, String fi, String sv, int year) {
        return add(new HuntingClubArea(club, fi, sv, year, year, UUID.randomUUID().toString()));
    }

    public GroupHuntingDay newGroupHuntingDay(HuntingClubGroup group, LocalDate date) {
        final DateTime startTime = DateUtil.toDateTimeNullSafe(date, new LocalTime(6, 0));
        final DateTime endTime = DateUtil.toDateTimeNullSafe(date, new LocalTime(21, 0));

        GroupHuntingDay huntingDay = new GroupHuntingDay(group, startTime, endTime);
        huntingDay.setHuntingMethod(some(GroupHuntingMethod.class));

        if (huntingDay.getHuntingMethod().isWithHound()) {
            huntingDay.setNumberOfHounds(4);
        }

        return add(huntingDay);
    }

    public HarvestRejection newHarvestRejection(HuntingClubGroup group, Harvest harvest) {
        return add(new HarvestRejection(group, harvest));
    }

    public ObservationRejection newObservationRejection(HuntingClubGroup group, Observation observation) {
        return add(new ObservationRejection(group, observation));
    }

    public MooseDataCardImport newMooseDataCardImport(HuntingClubGroup group) {
        MooseDataCardImport imp = new MooseDataCardImport();
        imp.setGroup(group);
        imp.setXmlFileMetadata(newPersistentFileContent().getMetadata());
        imp.setPdfFileMetadata(newPersistentFileContent().getMetadata());
        imp.setFilenameTimestamp(DateUtil.toDateTodayNullSafe(LocalTime.now()));
        return add(imp);
    }

    public GISZone newGISZone() {
        return add(new GISZone());
    }

    public GISZone newGISZone(double computedAreaSize) {
        final GISZone zone = newGISZone();
        zone.setComputedAreaSize(computedAreaSize);
        return zone;
    }

    public GISZone newGISZoneContaining(GeoLocation location) {
        GISZone zone = newGISZone();
        zone.setGeom(ValueGenerator.geometryContaining(location));
        return zone;
    }

    public GISZone newGISZoneNotContaining(GeoLocation location) {
        GISZone zone = newGISZone();
        zone.setGeom(ValueGenerator.geometryNotContaining(location));
        return zone;
    }

    public GISHirvitalousalue newGISHirvitalousalue() {
        final int serial = serial() % 1_000_000;
        final String formattedNumber = htaNumber();

        final GISHirvitalousalue hta =
                new GISHirvitalousalue(formattedNumber,
                        "fooFI" + serial,
                        "foo" + serial,
                        "fooSV" + serial,
                        ValueGenerator.geometryContaining(new GeoLocation(0, 0)));
        hta.setId(serial);
        return add(hta);
    }

    public Venue newVenue() {
        return newVenue(newAddress());
    }

    public Venue newVenue(Address address) {
        return newVenue("Tapahtumapaikka-" + serial(), address);
    }

    public Venue newVenue(String name, Address address) {
        return add(new Venue(address, name));
    }

    public CalendarEvent newCalendarEvent(
            Organisation organisation,
            Venue venue,
            CalendarEventType eventType,
            Date date,
            LocalTime beginTime,
            String name,
            String description) {

        return add(new CalendarEvent(organisation, venue, eventType, date, beginTime, name, description));
    }

    public ObservationBaseFields newObservationBaseFields(GameSpecies species, int metadataVersion) {
        return newObservationBaseFields(species, Required.VOLUNTARY, metadataVersion);
    }

    public ObservationBaseFields newObservationBaseFields(
            GameSpecies species, Required withinMooseHunting, int metadataVersion) {

        ObservationBaseFields baseFields = new ObservationBaseFields(species, metadataVersion);
        baseFields.setWithinMooseHunting(withinMooseHunting);
        return add(baseFields);
    }

    public ObservationContextSensitiveFields newObservationContextSensitiveFields(
            GameSpecies species, boolean withinMooseHunting, ObservationType observationType, int metadataVersion) {

        final ObservationContextSensitiveFields fields =
                new ObservationContextSensitiveFields(species, withinMooseHunting, observationType, metadataVersion);

        fields.setExtendedAgeRange(true);
        fields.setAmount(Required.YES);
        fields.setGender(Required.VOLUNTARY);
        fields.setAge(Required.VOLUNTARY);
        fields.setWounded(Required.VOLUNTARY);
        fields.setDead(Required.VOLUNTARY);
        fields.setOnCarcass(Required.VOLUNTARY);
        fields.setCollarOrRadioTransmitter(Required.VOLUNTARY);
        fields.setLegRingOrWingMark(Required.VOLUNTARY);
        fields.setEarMark(Required.VOLUNTARY);

        return add(fields);
    }

    public SrvaEvent newSrvaEvent() {
        return newSrvaEvent(newPerson());
    }

    public SrvaEvent newSrvaEvent(Riistanhoitoyhdistys rhy) {
        return newSrvaEvent(newPerson(), newGameSpecies(), rhy);
    }

    public SrvaEvent newSrvaEvent(Person person) {
        return newSrvaEvent(person, newRiistanhoitoyhdistys());
    }

    public SrvaEvent newSrvaEvent(Person person, Riistanhoitoyhdistys rhy) {
        return newSrvaEvent(person, newGameSpecies(), rhy);
    }

    public SrvaEvent newSrvaEvent(Person person, GameSpecies species) {
        return newSrvaEvent(person, species, newRiistanhoitoyhdistys());
    }

    public SrvaEvent newSrvaEvent(Person author, GameSpecies species, Riistanhoitoyhdistys rhy) {
        final SrvaEvent srvaEventEntity = new SrvaEvent();
        srvaEventEntity.setEventName(some(SrvaEventNameEnum.class));
        srvaEventEntity.setEventType(some(SrvaEventTypeEnum.class));
        srvaEventEntity.setGeoLocation(geoLocation());
        srvaEventEntity.setPointOfTime(new Date());
        srvaEventEntity.setSpecies(species);
        srvaEventEntity.setTotalSpecimenAmount(1);
        srvaEventEntity.setAuthor(author);
        srvaEventEntity.setDescription("description");
        srvaEventEntity.setPersonCount(nextPositiveIntAtMost(15));
        srvaEventEntity.setTimeSpent(nextPositiveIntAtMost(50));
        srvaEventEntity.setEventResult(some(SrvaResultEnum.class));
        srvaEventEntity.setOtherMethodDescription("other method description");
        srvaEventEntity.setOtherTypeDescription("other type description");
        srvaEventEntity.setRhy(rhy);
        srvaEventEntity.setState(SrvaEventStateEnum.UNFINISHED);

        return add(srvaEventEntity);
    }

    public SrvaSpecimen newSrvaSpecimen(SrvaEvent srvaEvent) {
        return newSrvaSpecimen(srvaEvent, some(GameAge.class), some(GameGender.class));
    }

    public SrvaSpecimen newSrvaSpecimen(SrvaEvent srvaEvent, GameAge age, GameGender gender) {

        SrvaSpecimen specimen = new SrvaSpecimen(srvaEvent);
        specimen.setAge(age);
        specimen.setGender(gender);

        return add(specimen);
    }

    public SrvaMethod newSrvaMethod(SrvaEvent srvaEvent) {
        return newSrvaMethod(srvaEvent, some(SrvaMethodEnum.class), true);
    }

    public SrvaMethod newSrvaMethod(SrvaEvent srvaEvent, SrvaMethodEnum name, boolean isChecked) {

        SrvaMethod method = new SrvaMethod(srvaEvent);
        method.setName(name);
        method.setChecked(isChecked);

        return add(method);
    }

    public MooselikePrice newMooselikePrice(Integer year, GameSpecies species) {
        return newMooselikePrice(year, species, BigDecimal.TEN, BigDecimal.ONE);
    }

    public MooselikePrice newMooselikePrice(Integer year, GameSpecies species, BigDecimal adultPrice, BigDecimal youngPrice) {
        MooselikePrice p = new MooselikePrice();
        p.setHuntingYear(year);
        p.setGameSpecies(species);
        p.setIban(new IbanEntity(Iban.random(CountryCode.FI).toFormattedString()));
        p.setBic(new BicEntity("OKOYFIHH"));
        p.setRecipientName("money or nothing");
        p.setAdultPrice(adultPrice);
        p.setYoungPrice(youngPrice);
        return add(p);
    }

    public Announcement newAnnouncement(final SystemUser fromUser,
                                        final Organisation fromOrganisation,
                                        final AnnouncementSenderType senderType) {
        return add(new Announcement(
                "AnnouncementBody-" + serial(),
                "AnnouncementSubject-" + serial(),
                fromUser,
                fromOrganisation,
                senderType));
    }

    public AnnouncementSubscriber newAnnouncementSubscriber(final Announcement announcement,
                                                            final Organisation organisation,
                                                            final OccupationType occupationType) {
        return add(new AnnouncementSubscriber(announcement, organisation, occupationType));
    }

    public LHOrganisation newLHOrganisation() {
        return newLHOrganisation(newRiistanhoitoyhdistys());
    }

    public LHOrganisation newLHOrganisation(Riistanhoitoyhdistys rhy) {
        return newLHOrganisation(rhy, zeroPaddedNumber(7));
    }

    private LHOrganisation newLHOrganisation(Riistanhoitoyhdistys rhy, String officialCode) {
        LHOrganisation o = new LHOrganisation();
        o.setNameFinnish("LHOrganisation" + officialCode);
        o.setNameSwedish("LHOrganisation" + officialCode);
        o.setOfficialCode(officialCode);
        o.setRhyOfficialCode(rhy.getOfficialCode());
        return add(o);
    }

    public JHTTraining newJHTTraining(OccupationType occupationType, Person person) {
        final JHTTraining training = new JHTTraining();
        training.setOccupationType(occupationType);
        training.setPerson(person);
        training.setTrainingDate(today());
        training.setTrainingType(JHTTraining.TrainingType.SAHKOINEN);
        return add(training);
    }

    public Integration newIntegration(String id) {
        Integration i = new Integration();
        i.setId(id);
        return add(i);
    }

    // Result objects is not a JPA entity but a wrapper class holding two objects actually
    // constituting complete metadata for an observation instance.
    public ObservationMetadata newObservationMetadata(GameSpecies species,
                                                      final int metadataVersion,
                                                      Boolean withinMooseHunting,
                                                      ObservationType observationType) {

        return new ObservationMetadata(
                newObservationBaseFields(
                        species, withinMooseHunting != null ? Required.YES : Required.NO, metadataVersion),
                newObservationContextSensitiveFields(
                        species, ofNullable(withinMooseHunting).orElse(false), observationType, metadataVersion));
    }

    protected <T extends Persistable<?>> T add(@Nonnull final T object) {
        Objects.requireNonNull(object);
        transientEntityList.add(object);
        return object;
    }

    protected int serial() {
        return getNumberGenerator().nextInt();
    }

    protected String lupaHallintaId() {
        return zeroPaddedNumber(8);
    }

    private String generateUsername(SystemUser.Role role) {
        Objects.requireNonNull(role);

        final String basename;

        switch (role) {
            case ROLE_ADMIN:
                basename = "admin";
                break;
            case ROLE_MODERATOR:
                basename = "moderator";
                break;
            case ROLE_REST:
                basename = "api-user";
                break;
            default:
                basename = "user";
        }

        return basename + "-" + serial();
    }

}
