package fi.riista.feature.huntingclub.moosedatacard;

import com.google.common.base.Throwables;
import com.google.common.collect.Streams;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.common.entity.EntityPersister;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup_;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayRepository;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay_;
import fi.riista.feature.huntingclub.moosedatacard.converter.MooseDataCardHuntingDayConverter;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardPage1Validation;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummaryRepository;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.lupahallinta.LHOrganisation;
import fi.riista.feature.organization.lupahallinta.LHOrganisationRepository;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.Occupation_;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.Filters;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import fi.riista.util.ValidationUtils;
import fi.riista.util.jpa.JpaSubQuery;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Value;
import io.vavr.control.Either;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportException.contactPersonIsLeaderInMultipleMooseDataCardGroups;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportException.contactPersonMemberOfMultipleMooseDataCardGroupsButNotAsLeader;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportException.contactPersonMemberOfMultipleMooseDataCardGroupsButWithNoActiveOccupations;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportException.couldNotCreateHarvests;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportException.couldNotCreateHuntingClubGroup;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportException.couldNotCreateHuntingDays;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportException.couldNotCreateMooseDataCardImport;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportException.couldNotCreateObservations;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportException.failureOnFileStorage;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportException.huntingClubAlreadyHasGroupNotCreatedWithinMooseDataCardImport;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.clubHuntingFinishedByModeratorOverride;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.contactPersonCouldNotBeFoundByHunterNumberOrSsn;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.harvestPermitSpeciesAmountForMooseNotFound;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.huntingClubNotFoundByCustomerNumber;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.huntingFinishedForPermit;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.huntingYearForHarvestPermitCouldNotBeUnambiguouslyResolved;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.multipleHarvestPermitMooseAmountsFound;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.permitNotFoundByPermitNumber;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.permitNotOfCorrectType;
import static fi.riista.util.DateUtil.createDateInterval;
import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.notEqual;
import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Component
public class MooseDataCardImportHelper {

    private static final Logger LOG = LoggerFactory.getLogger(MooseDataCardImportHelper.class);

    @Resource
    private HarvestPermitRepository permitRepo;

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepo;

    @Resource
    private HuntingClubRepository clubRepo;

    @Resource
    private LHOrganisationRepository lhOrgRepo;

    @Resource
    private HuntingClubGroupRepository groupRepo;

    @Resource
    private OccupationRepository occupationRepo;

    @Resource
    private GroupHuntingDayRepository huntingDayRepo;

    @Resource
    private BasicClubHuntingSummaryRepository basicSummaryRepo;

    @Resource
    private GISQueryService gisQueryService;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private EntityPersister persister;

    @Resource
    private PersonLookupService personLookupService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Validation<List<String>, MooseDataCardEntitySearchResult> resolveEntities(
            @Nonnull final MooseDataCardPage1Validation input) {

        Objects.requireNonNull(input);

        final Validation<String, HarvestPermitSpeciesAmount> hpsaValidation =
                findHarvestPermit(input.permitNumber).flatMap(this::findMoosePermitAmount);

        Validation<String, Either<LHOrganisation, HuntingClub>> clubValidation =
                findHuntingClubOrLHOrganisation(input.clubCode);

        if (hpsaValidation.isValid()) {
            clubValidation = clubValidation.flatMap(clubEither -> {
                final Optional<HuntingClub> moderatedClub = F.toOptional(clubEither)
                        .filter(club -> isClubHuntingFinishedByModeratorOverride(club, hpsaValidation.get()));

                return moderatedClub.isPresent()
                        ? invalid(clubHuntingFinishedByModeratorOverride(input.clubCode))
                        : valid(clubEither);
            });
        }

        return clubValidation
                .combine(hpsaValidation)
                .combine(resolveContactPersonId(input))
                .ap((clubEither, moosePermitAmount, personId) -> new MooseDataCardEntitySearchResult(
                        clubEither, moosePermitAmount, moosePermitAmount.resolveHuntingYear(), personId))
                .mapError(Value::toJavaList);
    }

    private Validation<String, HarvestPermit> findHarvestPermit(final String permitNumber) {
        Objects.requireNonNull(permitNumber);

        return ValidationUtils.toValidation(
                Optional.ofNullable(permitRepo.findByPermitNumber(permitNumber)),
                permit -> {
                    if (!permit.isApplicableForMooseDataCardImport()) {
                        return invalid(permitNotOfCorrectType(permit.getPermitTypeCode()));
                    }

                    final List<HarvestPermitSpeciesAmount> hpsaList = speciesAmountRepo.findMooseAmounts(permit);

                    return hpsaList.size() == 1 &&  hpsaList.get(0).isMooselikeHuntingFinished()
                            ? invalid(huntingFinishedForPermit(permitNumber))
                            : valid(permit);
                },
                () -> invalid(permitNotFoundByPermitNumber(permitNumber)));
    }

    private Validation<String, HarvestPermitSpeciesAmount> findMoosePermitAmount(final HarvestPermit permit) {

        final List<HarvestPermitSpeciesAmount> moosePermitAmounts = speciesAmountRepo.findMooseAmounts(permit);

        if (moosePermitAmounts.isEmpty()) {
            return invalid(harvestPermitSpeciesAmountForMooseNotFound());
        } else if (moosePermitAmounts.size() > 1) {
            return invalid(multipleHarvestPermitMooseAmountsFound());
        }

        final HarvestPermitSpeciesAmount speciesAmount = moosePermitAmounts.iterator().next();

        return speciesAmount.findUnambiguousHuntingYear().isPresent()
                ? valid(speciesAmount)
                : invalid(huntingYearForHarvestPermitCouldNotBeUnambiguouslyResolved(permit.getPermitNumber()));
    }

    private Validation<String, Either<LHOrganisation, HuntingClub>> findHuntingClubOrLHOrganisation(
            final String clubOfficialCode) {

        Objects.requireNonNull(clubOfficialCode);

        final Either<LHOrganisation, HuntingClub> eitherClubOrLhOrg =
                Optional.ofNullable(clubRepo.findByOfficialCode(clubOfficialCode))
                        .map(club -> Either.<LHOrganisation, HuntingClub> right(club))
                        .orElseGet(() -> Optional.ofNullable(lhOrgRepo.findByOfficialCode(clubOfficialCode))
                                .map(lhOrgList -> lhOrgList.isEmpty() ? null : lhOrgList.get(0))
                                .map(lhOrg -> Either.<LHOrganisation, HuntingClub> left(lhOrg))
                                .orElse(null));

        return eitherClubOrLhOrg != null
                ? valid(eitherClubOrLhOrg)
                : invalid(huntingClubNotFoundByCustomerNumber(clubOfficialCode));
    }

    private boolean isClubHuntingFinishedByModeratorOverride(final HuntingClub club,
                                                             final HarvestPermitSpeciesAmount speciesAmount) {

        return basicSummaryRepo.findByClubAndSpeciesAmount(club, speciesAmount, true).isPresent();
    }

    private Validation<String, Long> resolveContactPersonId(final MooseDataCardPage1Validation page1Validation) {
        final Either<String, Person> resolvedPerson = page1Validation.hunterNumberOrDateOfBirth.fold(

                // Foreign person is currently not eligible as a contact person.
                hunterNumber -> F.toEither(personLookupService.findByHunterNumber(hunterNumber, false), () -> {

                    LOG.info("Contact person of moose data card could not be found by hunter number: \"{}\"",
                            hunterNumber);

                    return contactPersonCouldNotBeFoundByHunterNumberOrSsn(hunterNumber, null);
                }),

                ssn -> F.toEither(personLookupService.findBySsnFallbackVtj(ssn), () -> {

                    LOG.info(
                            "Contact person of moose data card could not be found by ssn starting with: \"{}\"",
                            ssn.substring(0, 6));

                    return contactPersonCouldNotBeFoundByHunterNumberOrSsn(null, ssn);
                }));

        return Validation.fromEither(resolvedPerson).map(Person::getId);
    }

    // Returns a tuple of two lists: (1) newly-persisted hunting days and (2) informational messages.
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Try<Tuple2<List<GroupHuntingDay>, List<String>>> persistHuntingDayData(
            final HuntingClubGroup group,
            final List<GroupHuntingDay> transientHuntingDays,
            final List<Tuple2<Harvest, HarvestSpecimen>> transientHarvests,
            final List<Observation> transientObservations) {

        final Stream.Builder<String> messages = Stream.builder();

        final List<GroupHuntingDay> existingHuntingDays = huntingDayRepo.findAll(equal(GroupHuntingDay_.group, group));

        final List<LocalDate> datesOfMissingTransientHuntingDays = getDatesOfMissingHuntingDays(
                F.concat(transientHuntingDays, existingHuntingDays),
                transientHarvests.stream().map(Tuple2::_1).collect(toList()),
                transientObservations);

        final List<GroupHuntingDay> missingTransientHuntingDays =
                MooseDataCardHuntingDayConverter.fabricateFromDates(datesOfMissingTransientHuntingDays);

        F.consumeIfNonEmpty(datesOfMissingTransientHuntingDays, missingDates -> {
            messages.add(MooseDataCardImportMessages.missingHuntingDaysCreated(missingDates.stream()));
        });

        final Function<Collection<GroupHuntingDay>, Map<LocalDate, GroupHuntingDay>> dateGroupingFn =
                coll -> F.index(coll, GroupHuntingDay::getStartDate);

        final Set<LocalDate> datesOfExistingHuntingDays = dateGroupingFn.apply(existingHuntingDays).keySet();

        // Partition transient hunting days based on whether there already exists hunting day for
        // the given start date.
        // Data for existing hunting days (plus associated harvests/observations) must be ignored.
        final Map<Boolean, List<GroupHuntingDay>> transientHuntingDayPartition = F.partition(
                F.concat(transientHuntingDays, missingTransientHuntingDays),
                day -> datesOfExistingHuntingDays.contains(day.getStartDate()));

        F.consumeIfNonEmpty(transientHuntingDayPartition.get(true), ignoredHuntingDays -> {
            messages.add(MooseDataCardImportMessages.alreadyExistingHuntingDaysIgnored(
                    ignoredHuntingDays.stream().map(GroupHuntingDay::getStartDate)));
        });

        return Try.success(transientHuntingDayPartition.get(false)).flatMapTry(newHuntingDays -> {

            if (!newHuntingDays.isEmpty()) {
                newHuntingDays.forEach(huntingDay -> huntingDay.setGroup(group));

                try {
                    persister.saveInCurrentlyOpenTransaction(newHuntingDays);
                } catch (final RuntimeException e) {
                    LOG.error("Persisting hunting days within moose data card import failed: {}", e.getMessage(), e);
                    throw couldNotCreateHuntingDays();
                }
            }

            final Map<LocalDate, GroupHuntingDay> newHuntingDayIndex = dateGroupingFn.apply(newHuntingDays);

            final Try<Stream<String>> harvestResult = filterAndSaveHarvests(
                    transientHarvests, newHuntingDayIndex, datesOfExistingHuntingDays);

            return harvestResult.flatMapTry(harvestMessages -> {

                final Try<Stream<String>> observationResult = filterAndSaveObservations(
                        transientObservations, newHuntingDayIndex, datesOfExistingHuntingDays);

                return observationResult.mapTry(observationMessages -> Tuple.of(
                        newHuntingDays,
                        Streams.concat(messages.build(), harvestMessages, observationMessages).collect(toList())));
            });
        });
    }

    // Returns a list of informational messages.
    private Try<Stream<String>> filterAndSaveHarvests(final List<Tuple2<Harvest, HarvestSpecimen>> transientHarvests,
                                                      final Map<LocalDate, GroupHuntingDay> indexedNewHuntingDays,
                                                      final Set<LocalDate> datesOfExistingHuntingDays) {

        final Stream.Builder<String> messages = Stream.builder();

        final Map<Boolean, List<Tuple2<Harvest, HarvestSpecimen>>> partitionByRelationToExistingHuntingDay =
                F.partition(transientHarvests, tuple -> isEntryDateContained(tuple._1, datesOfExistingHuntingDays));

        F.consumeIfNonEmpty(partitionByRelationToExistingHuntingDay.get(true), ignoredHarvests -> {
            messages.add(MooseDataCardImportMessages.harvestsIgnoredBecauseOfAlreadyExistingHuntingDays(
                    F.countByApplication(
                            ignoredHarvests.stream().map(Tuple2::_1), GameDiaryEntry::getPointOfTimeAsLocalDate)));
        });

        return Try.success(partitionByRelationToExistingHuntingDay.get(false)).mapTry(filteredTuples -> {

            if (!filteredTuples.isEmpty()) {
                // Complete data/associations.
                filteredTuples.stream().map(Tuple2::_1).forEach(harvest -> {
                    // TODO validate coordinates
                    final Riistanhoitoyhdistys rhyByLocation = gisQueryService.findRhyByLocation(harvest.getGeoLocation());
                    final String municipalityCode = Optional.ofNullable(gisQueryService.findMunicipality(harvest.getGeoLocation()))
                            .map(Municipality::getOfficialCode).orElse(null);

                    harvest.setRhy(rhyByLocation);
                    harvest.setMunicipalityCode(municipalityCode);
                    harvest.updateHuntingDayOfGroup(indexedNewHuntingDays.get(harvest.getPointOfTimeAsLocalDate()), null);
                });

                try {
                    persister.saveInCurrentlyOpenTransaction(filteredTuples.stream()
                            .flatMap(tuple -> tuple.apply(Stream::of))
                            .collect(toList()));
                } catch (final RuntimeException e) {
                    LOG.error("Persisting harvests and specimens within moose data card import failed: {}",
                            e.getMessage(), e);
                    throw couldNotCreateHarvests();
                }
            }

            return messages.build();
        });
    }

    // Returns a list of informational messages.
    private Try<Stream<String>> filterAndSaveObservations(final List<Observation> transientObservations,
                                                          final Map<LocalDate, GroupHuntingDay> indexedNewHuntingDays,
                                                          final Set<LocalDate> datesOfExistingHuntingDays) {

        final Stream.Builder<String> messages = Stream.builder();

        final Map<Boolean, List<Observation>> partitionByRelationToExistingHuntingDay =
                F.partition(transientObservations, obs -> isEntryDateContained(obs, datesOfExistingHuntingDays));

        F.consumeIfNonEmpty(partitionByRelationToExistingHuntingDay.get(true), ignoredObservations -> {
            messages.add(MooseDataCardImportMessages.observationsIgnoredBecauseOfAlreadyExistingHuntingDays(
                    F.countByApplication(ignoredObservations.stream(), GameDiaryEntry::getPointOfTimeAsLocalDate)));
        });

        return Try.success(partitionByRelationToExistingHuntingDay.get(false)).mapTry(filteredObservations -> {

            if (!filteredObservations.isEmpty()) {
                // Complete data/associations.
                filteredObservations.forEach(observation -> {
                    // TODO validate coordinates
                    observation.setRhy(observation.getGeoLocation() != null
                            ? gisQueryService.findRhyByLocation(observation.getGeoLocation()) : null);
                    observation
                            .updateHuntingDayOfGroup(indexedNewHuntingDays.get(observation.getPointOfTimeAsLocalDate()), null);
                });

                try {
                    persister.saveInCurrentlyOpenTransaction(filteredObservations);
                } catch (final RuntimeException e) {
                    LOG.error("Persisting observations within moose data card import failed: {}", e.getMessage(), e);
                    throw couldNotCreateObservations();
                }
            }

            return messages.build();
        });
    }

    public static List<LocalDate> getDatesOfMissingHuntingDays(final List<GroupHuntingDay> huntingDays,
                                                               final List<Harvest> harvests,
                                                               final List<Observation> observations) {

        final Set<LocalDate> startDatesOfHuntingDays = huntingDays.stream()
                .map(GroupHuntingDay::getStartDate)
                .distinct()
                .collect(toSet());

        return Stream.concat(harvests.stream(), observations.stream())
                .map(GameDiaryEntry::getPointOfTimeAsLocalDate)
                .distinct()
                .filter(Filters.notIn(startDatesOfHuntingDays))
                .sorted()
                .collect(toList());
    }

    private static boolean isEntryDateContained(final GameDiaryEntry entry, final Set<LocalDate> dates) {
        return dates.contains(entry.getPointOfTimeAsLocalDate());
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Try<Occupation> assignClubMembershipToPerson(@Nonnull final Person person,
                                                        @Nonnull final HuntingClub club,
                                                        @Nullable final LocalDate huntingSeasonBeginDate) {

        final LocalDate today = DateUtil.today();

        // Find first club occupation that is active on or after given reporting period.
        final Optional<Occupation> firstOccupationActiveOnOrAfterGivenPeriod =
                occupationRepo.findNotDeletedByOrganisationAndPerson(club, person)
                        .stream()
                        .filter(occ -> occ.isActiveWithinPeriod(huntingSeasonBeginDate, null))
                        .min(HasBeginAndEndDate.DEFAULT_COMPARATOR)
                        .map(occupation -> {

                            // Set Occupation's beginDate to minimum of (reportingPeriodBeginDate,
                            // current begin date, today).
                            final LocalDate newBeginDate = F.getFirstByNaturalOrderNullsFirst(
                                    occupation.getBeginDate(), huntingSeasonBeginDate, today);
                            occupation.setBeginDate(newBeginDate);

                            return occupation;
                        });

        return firstOccupationActiveOnOrAfterGivenPeriod
                .map(Try::success)
                .orElseGet(() -> Try.of(() -> {
                    final Occupation occupation = new Occupation(person, club, OccupationType.SEURAN_JASEN);
                    occupation.setBeginDate(F.getFirstByNaturalOrderNullsFirst(huntingSeasonBeginDate, today));
                    return occupationRepo.save(occupation);
                }));
    }

    /**
     * <pre>
     *
     * Uusi ryhmä luodaan, jos ei löydy ennestään sellaista ryhmää, jolle
     * pätevät kaikki seuraavat ehdot:
     * (1) Ryhmän seura vastaa hirvitietokortissa ilmoitettua asiakasnumeroa
     * (2) Ryhmä on merkitty luoduksi hirvitietokortin pohjalta
     * (3) Ryhmä on hirvilajikohtainen
     * (4) Ryhmän metsästysvuosi vastaa hirvitietokortille ilmoitetun luvan metsästyskautta
     * (5) Ryhmään liitetty lupa vastaa hirvitietokortissa ilmoitettua lupanumeroa
     * (6) Hirvitietokortin yhteyshenkilö on joko ryhmän voimassa oleva jäsen tai metsästyksenjohtaja
     *
     * Huom! Uuden ryhmän perustaminen epäonnistuu:
     * - Jos seuralle löytyy ennestään yksikin ryhmä, jota ei ole luotu hirvitietokortin importin pohjalta
     * - Jos hirvitietokortin yhteyshenkilö on useammassa samaan seuraan ja lupaan liittyvässä hirvitietokorttiryhmässä
     *   jäsenenä, mutta
     *   (A) ei yhdessäkään metsästyksenjohtajana tai
     *   (B) useammassa kuin yhdessä metsästyksenjohtajana tai
     *   (C) jos kaikki ryhmäjäsenyydet, olivatpa metsästyksenjohtajatasoisia tai ei, ovat päättyneitä. Tällöin
     *       ei voida päätellä, mistä ryhmästä on kyse.
     * </pre>
     */
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Try<HuntingClubGroup> findOrCreateGroupForMooseDataCardImport(@Nonnull final HuntingClub club,
                                                                         @Nonnull final HarvestPermit permit,
                                                                         @Nonnull final GameSpecies mooseSpecies,
                                                                         final int huntingYear,
                                                                         @Nonnull final Person contactPerson) {

        final Try<Optional<HuntingClubGroup>> groupSearchResult =
                findMooseDataCardGroup(club, permit, mooseSpecies, huntingYear, contactPerson);

        // Create group if not existing found.
        return groupSearchResult.flatMapTry(groupOpt -> groupOpt.map(Try::success).orElseGet(() -> {

            final String permitNumber = permit.getPermitNumber();

            final LocalisedString groupName = Optional.ofNullable(contactPerson.getHunterNumber())
                    .map(hunterNumber -> generateNameForMooseDataCardGroupUsingHunterNumber(permitNumber, hunterNumber))
                    .orElseGet(() -> generateNameForMooseDataCardGroupUsingSsn(permitNumber, contactPerson.getSsn()));

            return Try.of(() -> {
                try {
                    final HuntingClubGroup group = new HuntingClubGroup();
                    group.setFromMooseDataCard(true);
                    group.setNameFinnish(groupName.getFinnish());
                    group.setNameSwedish(groupName.getSwedish());
                    group.setParentOrganisation(club);
                    group.setSpecies(mooseSpecies);
                    group.setHuntingYear(huntingYear);
                    group.updateHarvestPermit(permit);
                    groupRepo.save(group);

                    // Create hunting group leader occupation.
                    final Occupation groupLeadership =
                            new Occupation(contactPerson, group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
                    groupLeadership.setCallOrder(0);
                    occupationRepo.save(groupLeadership);

                    return group;

                } catch (final Exception e) {
                    LOG.info("Failure while creating hunting group with " +
                            "club-customer-number = \"{}\", permit-number = \"{}\":",
                            club.getOfficialCode(), permitNumber, e);

                    throw couldNotCreateHuntingClubGroup();
                }
            });
        }));
    }

    private Try<Optional<HuntingClubGroup>> findMooseDataCardGroup(final HuntingClub club,
                                                                   final HarvestPermit permit,
                                                                   final GameSpecies mooseSpecies,
                                                                   final int huntingYear,
                                                                   final Person contactPerson) {

        final Specification<HuntingClubGroup> baseCriteria = Specification
                .where(equal(HuntingClubGroup_.huntingYear, huntingYear))
                .and(equal(HuntingClubGroup_.species, mooseSpecies))
                .and(equal(Organisation_.parentOrganisation, club));

        // Not operator used because "fromMooseDataCard" field is nullable.
        final Specification<HuntingClubGroup> searchNonMooseCardGroupCriteria =
                baseCriteria.and(notEqual(HuntingClubGroup_.fromMooseDataCard, true));

        if (groupRepo.count(searchNonMooseCardGroupCriteria) > 0) {
            LOG.info("Cannot import moose data card because hunting club with customer number "
                    + "\"{}\" already has hunting groups not created within moose data card import",
                    club.getOfficialCode());

            return Try.failure(huntingClubAlreadyHasGroupNotCreatedWithinMooseDataCardImport());
        }

        // Find groups created by import and to which person is linked via occupations.

        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Specification<HuntingClubGroup> mooseCardGroupCriteria = baseCriteria
                .and(equal(HuntingClubGroup_.harvestPermit, permit))
                .and(equal(HuntingClubGroup_.fromMooseDataCard, true))
                .and((root, query, cb) -> JpaSubQuery.of(Organisation_.occupations)
                        .exists((subRoot, cb2) -> cb2.equal(subRoot.get(Occupation_.person), contactPerson))
                        // Need to cast because of inheritance model
                        .toPredicate((Root) root, query, cb));

        final List<HuntingClubGroup> groups = groupRepo.findAll(mooseCardGroupCriteria);
        HuntingClubGroup resolvedGroup = null;

        if (!groups.isEmpty()) {

            // If only one group exists return it whether or not the related occupations are
            // expired or soft-deleted. At the moment, it is not defined how expired
            // occupations should be handled within import so at this point we are just
            // satisfied if any group can be resolved unambiguously.
            if (groups.size() == 1) {
                resolvedGroup = groups.iterator().next();
            } else {

                // In case of multiple groups try to resolve single group by existing active
                // occupations (active during reported period and not soft-deleted).

                final Map<Long, HuntingClubGroup> groupById = F.indexById(groups);
                final HarvestPermitSpeciesAmount speciesAmount =
                        speciesAmountRepo.getOneByHarvestPermitAndSpeciesCode(permit, mooseSpecies.getOfficialCode());
                final Interval period = createDateInterval(speciesAmount.getFirstDate(), speciesAmount.getLastDate());

                final Map<Long, List<Occupation>> groupIdToActiveOccupations =
                        userAuthorizationHelper.findActiveOccupationsInOrganisations(
                                F.getUniqueIds(groups), contactPerson, period.getStart(), period.getEnd());

                if (groupIdToActiveOccupations.isEmpty()) {
                    return Try.failure(contactPersonMemberOfMultipleMooseDataCardGroupsButWithNoActiveOccupations());
                }

                if (groupIdToActiveOccupations.size() == 1) {
                    resolvedGroup = groupById.get(groupIdToActiveOccupations.keySet().iterator().next());
                } else {
                    // Try to find only one group for which person has an active leader occupation.

                    final OccupationType leader = OccupationType.RYHMAN_METSASTYKSENJOHTAJA;

                    final Set<Long> idsOfLedGroups = groupIdToActiveOccupations.entrySet().stream()
                            .filter(e -> e.getValue().stream().anyMatch(occ -> occ.getOccupationType() == leader))
                            .map(Map.Entry::getKey)
                            .collect(toSet());

                    if (idsOfLedGroups.size() != 1) {
                        return idsOfLedGroups.isEmpty()
                                ? Try.failure(contactPersonMemberOfMultipleMooseDataCardGroupsButNotAsLeader())
                                : Try.failure(contactPersonIsLeaderInMultipleMooseDataCardGroups());
                    }

                    resolvedGroup = groupById.get(idsOfLedGroups.iterator().next());
                }
            }
        }

        return Try.success(Optional.ofNullable(resolvedGroup));
    }

    @Nonnull
    public LocalisedString generateNameForMooseDataCardGroupUsingHunterNumber(@Nonnull final String permitNumber,
                                                                              @Nonnull final String hunterNumber) {

        return generateNameForMooseDataCardGroup(permitNumber, hunterNumber);
    }

    @Nonnull
    public LocalisedString generateNameForMooseDataCardGroupUsingSsn(@Nonnull final String permitNumber,
                                                                     @Nonnull final String ssn) {

        return generateNameForMooseDataCardGroup(permitNumber, ssn.substring(0, 6));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Try<MooseDataCardImport> saveImportData(final MultipartFile xmlFile,
                                                   final MultipartFile pdfFile,
                                                   final HuntingClubGroup group,
                                                   final Collection<GroupHuntingDay> huntingDays,
                                                   final DateTime filenameTimestamp,
                                                   final List<String> messages) {

        Objects.requireNonNull(xmlFile, "xmlFile is null");
        Objects.requireNonNull(pdfFile, "pdfFile is null");
        Objects.requireNonNull(group, "group is null");
        Objects.requireNonNull(huntingDays, "huntingDays is null");
        Objects.requireNonNull(messages, "messages is null");

        final MooseDataCardImport imp = new MooseDataCardImport();
        imp.setGroup(group);
        imp.setFilenameTimestamp(filenameTimestamp);
        imp.getMessages().addAll(messages);

        final UUID xmlUuid = UUID.randomUUID();
        final UUID pdfUuid = UUID.randomUUID();

        LOG.info("Associating moose data card XML file {} with UUID={} and PDF file {} with UUID={}",
                xmlFile.getOriginalFilename(), xmlUuid, pdfFile.getOriginalFilename(), pdfUuid);

        try {
            imp.setXmlFileMetadata(fileStorageService.storeFile(
                    xmlUuid,
                    xmlFile.getBytes(),
                    FileType.MOOSE_DATA_CARD,
                    MediaType.APPLICATION_XML_VALUE,
                    xmlFile.getOriginalFilename()));

            imp.setPdfFileMetadata(fileStorageService.storeFile(
                    pdfUuid,
                    pdfFile.getBytes(),
                    FileType.MOOSE_DATA_CARD,
                    MediaTypeExtras.APPLICATION_PDF_VALUE,
                    pdfFile.getOriginalFilename()));

        } catch (final IOException ioe) {
            LOG.error("Storing moose data card files failed:", ioe);
            return Try.failure(failureOnFileStorage());
        }

        try {
            persister.saveInCurrentlyOpenTransaction(imp);
            huntingDays.forEach(huntingDay -> huntingDay.setMooseDataCardImport(imp));
            return Try.success(imp);
        } catch (final Exception e) {
            LOG.error("Persisting moose data card import failed:", e);

            final Throwable rootCause = Throwables.getRootCause(e);
            if (rootCause instanceof BatchUpdateException) {
                LOG.error("Root cause for failure on persisting moose data card import failed:",
                        BatchUpdateException.class.cast(rootCause).getNextException());
            }

            return Try.failure(couldNotCreateMooseDataCardImport());
        }
    }

    private static LocalisedString generateNameForMooseDataCardGroup(final String permitNumber,
                                                                     final String hunterNumberOrDateOfBirth) {

        Objects.requireNonNull(permitNumber, "permitNumber is null");
        Objects.requireNonNull(hunterNumberOrDateOfBirth, "hunterNumberOrDateOfBirth is null");

        return HuntingClubGroup.generateNameForMooseDataCardGroup(prefix -> {
            final String capitalizedPrefix = prefix == null ? "" : StringUtils.capitalize(prefix) + " ";
            return String.format("%s%s %s", capitalizedPrefix, permitNumber, hunterNumberOrDateOfBirth);
        });
    }
}
