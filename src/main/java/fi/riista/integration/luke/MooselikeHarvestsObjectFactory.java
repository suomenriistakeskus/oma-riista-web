package fi.riista.integration.luke;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAndRemainingPopulation;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.ModeratedHarvestCounts;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.BeaverAppearance;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.SpeciesEstimatedAppearance;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.SpeciesEstimatedAppearanceWithPiglets;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Address;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Amount;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_BeaverAppearance;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Club;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_DataSource;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_EstimatedAppearance;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_FemaleAndCalfs;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_GameAge;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_GameAntlersType;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_GameFitnessClass;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_GameGender;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_GameMarking;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_GeoLocation;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Group;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Harvest;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_HuntingDay;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_HuntingMethod;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_HuntingSummary;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_MooseHuntingAreaType;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Observation;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_ObservationSpecimen;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_ObservationType;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_ObservedGameAge;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_ObservedGameState;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Overrides;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Permit;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Person;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_RestrictionType;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Source;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_TrendOfPopulationGrowth;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_WildBoarEstimatedAppearance;
import fi.riista.util.F;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

public class MooselikeHarvestsObjectFactory {

    private static final Logger LOG = LoggerFactory.getLogger(MooselikeHarvestsObjectFactory.class);

    public static LEM_Permit createPermit(final @Nonnull String permitNumber,
                                          final @Nonnull String rhyOfficialCode,
                                          final Person contactPerson,
                                          final @Nonnull HarvestPermitSpeciesAmount spa,
                                          final List<HarvestPermitSpeciesAmount> amendmentSpas,
                                          final @Nonnull List<LEM_Club> huntingClubs) {

        LEM_Permit dto = new LEM_Permit();
        dto.setPermitNumber(permitNumber);
        dto.setRhyOfficialCode(rhyOfficialCode);
        dto.setContactPerson(createPerson(contactPerson));
        dto.setMooseAmount(createAmount(spa));

        if (amendmentSpas != null && amendmentSpas.size() > 0) {
            createAmounts(amendmentSpas).forEach(dto.getAmendmentPermits()::add);
        }
        dto.setHuntingClubs(huntingClubs);
        return dto;
    }

    private static Stream<LEM_Amount> createAmounts(List<HarvestPermitSpeciesAmount> spas) {
        return spas.stream().map(MooselikeHarvestsObjectFactory::createAmount);
    }

    private static LEM_Amount createAmount(HarvestPermitSpeciesAmount spa) {
        LEM_Amount a = new LEM_Amount();
        a.setAmount(spa.getSpecimenAmount());
        if (spa.getRestrictionType() != null) {
            a.setRestrictedAmount(spa.getRestrictionAmount());
            a.setRestriction(convert(LEM_RestrictionType.class, spa.getRestrictionType()));
        }
        return a;
    }

    private static LEM_Person createPerson(Person contact) {
        if (contact == null) {
            return null;
        }
        LEM_Person dto = new LEM_Person();
        dto.setFirstName(contact.getFirstName());
        dto.setLastName(contact.getLastName());
        dto.setPhoneNumber(contact.getPhoneNumber());
        dto.setEmail(contact.getEmail());

        dto.setAddress(createAddress(contact.getAddress()));
        return dto;
    }

    private static LEM_Address createAddress(Address address) {
        if (address == null) {
            return null;
        }
        final LEM_Address dto = new LEM_Address();
        dto.setStreetAddress(address.getStreetAddress());
        dto.setCity(address.getCity());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        return dto;
    }

    public static LEM_Club createClub(final @Nonnull HuntingClub club,
                                      final @Nonnull HarvestPermit moosePermit,
                                      final @Nonnull Map<Long, Occupation> clubContacts,
                                      final @Nonnull Map<Long, List<HuntingClubGroup>> groups,
                                      final @Nonnull Map<Long, List<GroupHuntingDay>> groupDays,
                                      final @Nonnull Map<Long, List<Harvest>> dayHarvests,
                                      final @Nonnull Map<Long, List<Observation>> dayObservations,
                                      final @Nonnull Map<Long, List<HarvestSpecimen>> harvestSpecimens,
                                      final @Nonnull Map<Long, List<ObservationSpecimen>> observationSpecimens,
                                      final @Nonnull Map<HarvestPermit, Map<HuntingClub, MooseHuntingSummary>> permitToClubToSummary,
                                      final @Nonnull Map<Long, BasicClubHuntingSummary> clubOverrides) {
        final LEM_Club dto = new LEM_Club();

        dto.setClubOfficialCode(club.getOfficialCode());
        dto.setNameFinnish(club.getNameFinnish());
        dto.setRhyOfficialCode(club.getParentOrganisation().getOfficialCode());
        dto.setGeoLocation(createGeoLocation(club.getGeoLocation()));
        dto.setContactPerson(Optional.of(club.getId())
                .map(clubContacts::get)
                .map(Occupation::getPerson)
                .map(MooselikeHarvestsObjectFactory::createPerson)
                .orElse(null));

        final boolean hasOverride = clubOverrides.containsKey(club.getId());

        final List<HuntingClubGroup> clubGroups = groups.getOrDefault(club.getId(), emptyList());

        dto.setGroups(clubGroups.stream()
                .filter(g -> g.getHarvestPermit().equals(moosePermit))
                .filter(g -> CollectionUtils.isNotEmpty(groupDays.getOrDefault(g.getId(), emptyList())))
                .map(g -> {
                    if (hasOverride) {
                        return createGroup(g, emptyList(), emptyMap(), emptyMap(), emptyMap(), emptyMap());
                    }
                    final List<GroupHuntingDay> days = groupDays.get(g.getId());
                    return createGroup(g, days, dayHarvests, dayObservations, harvestSpecimens, observationSpecimens);
                })
                .collect(toList()));

        if (hasOverride) {
            dto.setOverrides(createOverrides(club, clubOverrides));

        } else {
            dto.setHuntingSummary(Optional.of(moosePermit)
                    .map(permitToClubToSummary::get)
                    .map(summaries -> summaries.get(club))
                    .map(MooselikeHarvestsObjectFactory::createSummary)
                    .orElse(null));
        }
        return dto;
    }

    private static LEM_Overrides createOverrides(HuntingClub club, Map<Long, BasicClubHuntingSummary> clubOverrides) {
        final BasicClubHuntingSummary override = clubOverrides.get(club.getId());
        final LEM_Overrides o = new LEM_Overrides();

        final ModeratedHarvestCounts moderatedHarvestCounts = override.getModeratedHarvestCounts();
        o.setAdultMales(moderatedHarvestCounts.getNumberOfAdultMales());
        o.setAdultFemales(moderatedHarvestCounts.getNumberOfAdultFemales());
        o.setYoungMales(moderatedHarvestCounts.getNumberOfYoungMales());
        o.setYoungFemales(moderatedHarvestCounts.getNumberOfYoungFemales());
        o.setNonEdibleAdults(moderatedHarvestCounts.getNumberOfNonEdibleAdults());
        o.setNonEdibleYoung(moderatedHarvestCounts.getNumberOfNonEdibleYoungs());

        final AreaSizeAndRemainingPopulation a = override.getAreaSizeAndPopulation();
        o.setTotalHuntingArea(a.getTotalHuntingArea());
        o.setEffectiveHuntingArea(a.getEffectiveHuntingArea());
        o.setRemainingPopulationInTotalArea(a.getRemainingPopulationInTotalArea());
        o.setRemainingPopulationInEffectiveArea(a.getRemainingPopulationInEffectiveArea());
        return o;
    }

    private static LEM_Group createGroup(final HuntingClubGroup group,
                                         final List<GroupHuntingDay> days,
                                         final Map<Long, List<Harvest>> dayHarvests,
                                         final Map<Long, List<Observation>> dayObservations,
                                         final Map<Long, List<HarvestSpecimen>> harvestSpecimens,
                                         final Map<Long, List<ObservationSpecimen>> observationSpecimens) {

        final LEM_Group dto = new LEM_Group();

        dto.setDataSource(group.isFromMooseDataCard() ? LEM_DataSource.MOOSE_DATA_CARD : LEM_DataSource.WEB);
        dto.setNameFinnish(group.getNameFinnish());

        days.stream().map(d -> {
            final List<Harvest> harvests = F.firstNonNull(dayHarvests.get(d.getId()), Collections.<Harvest>emptyList());
            final List<Observation> observations =
                    F.firstNonNull(dayObservations.get(d.getId()), Collections.<Observation>emptyList());
            return createHuntingDay(d, harvests, observations, harvestSpecimens, observationSpecimens);
        }).forEach(dto.getHuntingDays()::add);
        return dto;
    }

    private static LEM_HuntingDay createHuntingDay(final GroupHuntingDay day,
                                                   final List<Harvest> harvests,
                                                   final List<Observation> observations,
                                                   final Map<Long, List<HarvestSpecimen>> harvestSpecimens,
                                                   final Map<Long, List<ObservationSpecimen>> observationSpecimens) {
        final LEM_HuntingDay dto = new LEM_HuntingDay();

        dto.setStartDate(day.getStartDate());
        dto.setStartTime(day.getStartTime());
        dto.setEndDate(day.getEndDate());
        dto.setEndTime(day.getEndTime());
        dto.setBreakDurationInMinutes(F.coalesceAsInt(day.getBreakDurationInMinutes(), 0));

        dto.setDurationInMinutes(day.calculateHuntingDayDurationInMinutes());

        dto.setSnowDepth(day.getSnowDepth());
        if (day.getHuntingMethod() != null) {
            dto.setHuntingMethod(convertByEnumName(LEM_HuntingMethod.class, day.getHuntingMethod().name()));
        }
        dto.setNumberOfHunters(day.getNumberOfHunters());
        dto.setNumberOfHounds(day.getNumberOfHounds());

        harvests.stream().map(h -> createHarvest(h, nullToEmpty(harvestSpecimens.get(h.getId())))).forEach(dto.getMooseHarvests()::add);
        observations.stream().map(o -> createObservation(o, nullToEmpty(observationSpecimens.get(o.getId())))).forEach(dto.getObservations()::add);
        return dto;
    }

    private static <T> List<T> nullToEmpty(List<T> list) {
        return list != null ? list : emptyList();
    }

    private static LEM_Harvest createHarvest(final Harvest harvest, final List<HarvestSpecimen> harvestSpecimens) {
        final LEM_Harvest dto = new LEM_Harvest();
        dto.setGeoLocation(createGeoLocation(harvest.getGeoLocation()));
        dto.setPointOfTime(harvest.getPointOfTime().toLocalDateTime());

        if (CollectionUtils.isEmpty(harvestSpecimens)) {
            return dto;
        }

        if (harvestSpecimens.size() > 1) {
            LOG.warn("Harvest has too many specimen, should have only 1 but has {}, using only the first one.", harvestSpecimens.size());
        }

        final HarvestSpecimen specimen = harvestSpecimens.get(0);

        dto.setGender(convert(LEM_GameGender.class, specimen.getGender()));
        dto.setAge(convert(LEM_GameAge.class, specimen.getAge()));
        dto.setAlone(specimen.getAlone());
        dto.setWeightEstimated(specimen.getWeightEstimated());
        dto.setWeightMeasured(specimen.getWeightMeasured());
        dto.setFitnessClass(convert(LEM_GameFitnessClass.class, specimen.getFitnessClass()));
        dto.setAntlersLost(specimen.getAntlersLost());
        dto.setAntlersType(convert(LEM_GameAntlersType.class, specimen.getAntlersType()));
        dto.setAntlersWidth(specimen.getAntlersWidth());
        dto.setAntlerPointsLeft(specimen.getAntlerPointsLeft());
        dto.setAntlerPointsRight(specimen.getAntlerPointsRight());
        dto.setAntlersGirth(specimen.getAntlersGirth());
        dto.setNotEdible(specimen.getNotEdible());
        dto.setAdditionalInfo(specimen.getAdditionalInfo());

        return dto;
    }

    private static LEM_Observation createObservation(final Observation observation,
                                                     final List<ObservationSpecimen> observationSpecimens) {
        final LEM_Observation dto = new LEM_Observation();
        dto.setGeoLocation(createGeoLocation(observation.getGeoLocation()));
        dto.setPointOfTime(observation.getPointOfTime().toLocalDateTime());
        dto.setObservationType(convert(LEM_ObservationType.class, observation.getObservationType()));
        dto.setGameSpeciesCode(observation.getSpecies().getOfficialCode());
        dto.setGameSpeciesNameFinnish(observation.getSpecies().getNameFinnish());

        dto.setMooselikeMaleAmount(observation.getMooselikeMaleAmount());
        dto.setMooselikeSolitaryCalfAmount(observation.getMooselikeCalfAmount());
        dto.setMooselikeUnknownSpecimenAmount(observation.getMooselikeUnknownSpecimenAmount());

        Stream.of(
                createFemaleAndCalfs(observation.getMooselikeFemaleAmount(), 0),
                createFemaleAndCalfs(observation.getMooselikeFemale1CalfAmount(), 1),
                createFemaleAndCalfs(observation.getMooselikeFemale2CalfsAmount(), 2),
                createFemaleAndCalfs(observation.getMooselikeFemale3CalfsAmount(), 3),
                createFemaleAndCalfs(observation.getMooselikeFemale4CalfsAmount(), 4)
        ).filter(Objects::nonNull)
                .forEach(dto.getMooseLikeFemaleAndCalfs()::add);

        observationSpecimens.stream().map(MooselikeHarvestsObjectFactory::createSpecimen).forEach(dto.getSpecimens()::add);
        return dto;
    }

    private static LEM_GeoLocation createGeoLocation(GeoLocation geoLocation) {
        if (geoLocation == null) {
            return null;
        }
        LEM_GeoLocation dto = new LEM_GeoLocation();
        dto.setLatitude(geoLocation.getLatitude());
        dto.setLongitude(geoLocation.getLongitude());
        dto.setAccuracy(geoLocation.getAccuracy());
        dto.setAltitude(geoLocation.getAltitude());
        dto.setAltitudeAccuracy(geoLocation.getAltitudeAccuracy());
        dto.setSource(convert(LEM_Source.class, geoLocation.getSource()));
        return dto;
    }

    private static LEM_ObservationSpecimen createSpecimen(ObservationSpecimen specimen) {
        final LEM_ObservationSpecimen dto = new LEM_ObservationSpecimen();
        dto.setGender(convert(LEM_GameGender.class, specimen.getGender()));
        dto.setAge(convert(LEM_ObservedGameAge.class, specimen.getAge()));
        dto.setState(convert(LEM_ObservedGameState.class, specimen.getState()));
        dto.setMarking(convert(LEM_GameMarking.class, specimen.getMarking()));
        return dto;
    }

    private static LEM_FemaleAndCalfs createFemaleAndCalfs(Integer amount, int calfs) {
        if (amount == null || amount == 0) {
            return null;
        }
        LEM_FemaleAndCalfs dto = new LEM_FemaleAndCalfs();
        dto.setAmount(amount);
        dto.setCalfs(calfs);
        return dto;
    }

    private static LEM_HuntingSummary createSummary(MooseHuntingSummary summary) {
        if (summary == null) {
            return null;
        }

        final LEM_HuntingSummary dto = new LEM_HuntingSummary();

        dto.setHuntingFinished(summary.isHuntingFinished());
        dto.setHuntingEndDate(summary.getHuntingEndDate());

        final AreaSizeAndRemainingPopulation areaAndPopulation = summary.getAreaSizeAndPopulation();
        dto.setTotalHuntingArea(areaAndPopulation.getTotalHuntingArea());
        dto.setEffectiveHuntingArea(areaAndPopulation.getEffectiveHuntingArea());
        dto.setMoosesRemainingInTotalHuntingArea(areaAndPopulation.getRemainingPopulationInTotalArea());
        dto.setMoosesRemainingInEffectiveHuntingArea(areaAndPopulation.getRemainingPopulationInEffectiveArea());
        dto.setEffectiveHuntingAreaPercentage(summary.getEffectiveHuntingAreaPercentage());
        dto.setHuntingAreaType(convert(LEM_MooseHuntingAreaType.class, summary.getHuntingAreaType()));

        dto.setNumberOfDrownedMooses(summary.getNumberOfDrownedMooses());
        dto.setNumberOfMoosesKilledByBear(summary.getNumberOfMoosesKilledByBear());
        dto.setNumberOfMoosesKilledByWolf(summary.getNumberOfMoosesKilledByWolf());
        dto.setNumberOfMoosesKilledInTrafficAccident(summary.getNumberOfMoosesKilledInTrafficAccident());
        dto.setNumberOfMoosesKilledByPoaching(summary.getNumberOfMoosesKilledByPoaching());
        dto.setNumberOfMoosesKilledInRutFight(summary.getNumberOfMoosesKilledInRutFight());
        dto.setNumberOfStarvedMooses(summary.getNumberOfStarvedMooses());

        dto.setNumberOfMoosesDeceasedByOtherReason(summary.getNumberOfMoosesDeceasedByOtherReason());
        dto.setCauseOfDeath(summary.getCauseOfDeath());

        dto.setWhiteTailedDeerAppearance(createEstimatedAppearance(summary.getWhiteTailedDeerAppearance()));
        dto.setRoeDeerAppearance(createEstimatedAppearance(summary.getRoeDeerAppearance()));
        dto.setWildForestReindeerAppearance(createEstimatedAppearance(summary.getWildForestReindeerAppearance()));
        dto.setFallowDeerAppearance(createEstimatedAppearance(summary.getFallowDeerAppearance()));
        dto.setWildBoarAppearance(createWildBoarEstimatedAppearance(summary.getWildBoarAppearance()));
        dto.setBeaverAppearance(createBeaverAppearance(summary.getBeaverAppearance()));

        dto.setMooseHeatBeginDate(summary.getMooseHeatBeginDate());
        dto.setMooseHeatEndDate(summary.getMooseHeatEndDate());
        dto.setMooseFawnBeginDate(summary.getMooseFawnBeginDate());
        dto.setMooseFawnEndDate(summary.getMooseFawnEndDate());

        dto.setDateOfFirstDeerFlySeen(summary.getDateOfFirstDeerFlySeen());
        dto.setDateOfLastDeerFlySeen(summary.getDateOfLastDeerFlySeen());
        dto.setNumberOfAdultMoosesHavingFlies(summary.getNumberOfAdultMoosesHavingFlies());
        dto.setNumberOfYoungMoosesHavingFlies(summary.getNumberOfYoungMoosesHavingFlies());
        dto.setDeerFliesAppeared(summary.getDeerFliesAppeared());
        dto.setTrendOfDeerFlyPopulationGrowth(convert(LEM_TrendOfPopulationGrowth.class, summary.getTrendOfDeerFlyPopulationGrowth()));

        dto.setObservationPolicyAdhered(summary.getObservationPolicyAdhered());

        return dto;
    }

    private static LEM_EstimatedAppearance createEstimatedAppearance(SpeciesEstimatedAppearance appearance) {
        if (appearance == null || !Boolean.TRUE.equals(appearance.getAppeared())) {
            return null;
        }
        LEM_EstimatedAppearance dto = new LEM_EstimatedAppearance();
        dto.setTrendOfPopulationGrowth(convert(LEM_TrendOfPopulationGrowth.class, appearance.getTrendOfPopulationGrowth()));
        dto.setEstimatedAmountOfSpecimens(appearance.getEstimatedAmountOfSpecimens());
        return dto;
    }

    private static LEM_WildBoarEstimatedAppearance createWildBoarEstimatedAppearance(SpeciesEstimatedAppearanceWithPiglets appearance) {
        if (appearance == null || !Boolean.TRUE.equals(appearance.getAppeared())) {
            return null;
        }
        LEM_WildBoarEstimatedAppearance dto = new LEM_WildBoarEstimatedAppearance();
        dto.setTrendOfPopulationGrowth(convert(LEM_TrendOfPopulationGrowth.class, appearance.getTrendOfPopulationGrowth()));
        dto.setEstimatedAmountOfSpecimens(appearance.getEstimatedAmountOfSpecimens());
        dto.setEstimatedAmountOfSowWithPiglets(appearance.getEstimatedAmountOfSowWithPiglets());
        return dto;
    }

    private static LEM_BeaverAppearance createBeaverAppearance(BeaverAppearance appearance) {
        if (appearance == null || !Boolean.TRUE.equals(appearance.getAppeared())) {
            return null;
        }
        LEM_BeaverAppearance dto = new LEM_BeaverAppearance();
        dto.setTrendOfPopulationGrowth(convert(LEM_TrendOfPopulationGrowth.class, appearance.getTrendOfPopulationGrowth()));
        dto.setAmountOfInhabitedWinterNests(appearance.getAmountOfInhabitedWinterNests());
        dto.setHarvestAmount(appearance.getHarvestAmount());
        dto.setAreaOfDamage(appearance.getAreaOfDamage());
        dto.setAreaOccupiedByWater(appearance.getAreaOccupiedByWater());
        dto.setAdditionalInfo(appearance.getAdditionalInfo());
        return dto;
    }

    public static <A extends Enum<A>, B extends Enum<B>> B convert(Class<B> clazz, A value) {
        return value == null ? null : convertByEnumName(clazz, value.name());
    }

    static <A extends Enum<A>, B extends Enum<B>> B convertByEnumName(Class<B> clazz, String name) {
        return name == null ? null : Enum.valueOf(clazz, name);
    }
}
