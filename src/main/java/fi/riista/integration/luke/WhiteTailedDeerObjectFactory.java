package fi.riista.integration.luke;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoByPermitAndClub;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountByPermitAndClub;
import fi.riista.integration.luke_export.deerharvests.LED_Amount;
import fi.riista.integration.luke_export.deerharvests.LED_Club;
import fi.riista.integration.luke_export.deerharvests.LED_DeerHuntingType;
import fi.riista.integration.luke_export.deerharvests.LED_FemaleAndCalfs;
import fi.riista.integration.luke_export.deerharvests.LED_GameAge;
import fi.riista.integration.luke_export.deerharvests.LED_GameAntlersType;
import fi.riista.integration.luke_export.deerharvests.LED_GameGender;
import fi.riista.integration.luke_export.deerharvests.LED_GeoLocation;
import fi.riista.integration.luke_export.deerharvests.LED_Group;
import fi.riista.integration.luke_export.deerharvests.LED_Harvest;
import fi.riista.integration.luke_export.deerharvests.LED_HuntingMethod;
import fi.riista.integration.luke_export.deerharvests.LED_HuntingSummary;
import fi.riista.integration.luke_export.deerharvests.LED_Observation;
import fi.riista.integration.luke_export.deerharvests.LED_ObservationType;
import fi.riista.integration.luke_export.deerharvests.LED_Overrides;
import fi.riista.integration.luke_export.deerharvests.LED_Permit;
import fi.riista.integration.luke_export.deerharvests.LED_Permits;
import fi.riista.integration.luke_export.deerharvests.LED_RestrictionType;
import fi.riista.integration.luke_export.deerharvests.LED_Source;
import fi.riista.util.DateUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fi.riista.integration.luke_export.deerharvests.LED_DataSource.MOOSE_DATA_CARD;
import static fi.riista.integration.luke_export.deerharvests.LED_DataSource.WEB;
import static java.util.stream.Collectors.toList;

public class WhiteTailedDeerObjectFactory {

    private final List<HarvestPermit> permits;
    private final Map<Long, HarvestPermitSpeciesAmount> amountsByPermitId;
    private final List<HuntingClubGroup> groups;
    private final Map<Long, List<HarvestPermitSpeciesAmount>> amendmentAmountsBySpeciesAmountId;
    private final List<Harvest> harvests;
    private final Map<Long, HarvestSpecimen> harvestSpecimensByHarvestId;
    private final List<Observation> observations;
    private final ClubHuntingSummaryBasicInfoByPermitAndClub summaries;
    private final HarvestCountByPermitAndClub moderatedHarvestCounts;
    private final Map<Long, String> rhyOfficialCodesByRhyId;
    private final Map<Long, HuntingClub> clubIndex;


    public static WhiteTailedDeerObjectFactory create(final List<HarvestPermit> permits,
                                                      final Map<Long, HarvestPermitSpeciesAmount> amountsByPermitId,
                                                      final List<HuntingClubGroup> groups,
                                                      final Map<Long, List<HarvestPermitSpeciesAmount>> amendmentAmountsBySpeciesAmountId,
                                                      final List<Harvest> harvests,
                                                      final Map<Long, HarvestSpecimen> harvestSpecimensByHarvestId,
                                                      final List<Observation> observations,
                                                      final ClubHuntingSummaryBasicInfoByPermitAndClub summaries,
                                                      final HarvestCountByPermitAndClub moderatedHarvestCounts,
                                                      final Map<Long, String> rhyOfficialCodesByRhyId,
                                                      final Map<Long, HuntingClub> clubIndex) {

        return new WhiteTailedDeerObjectFactory(permits, amountsByPermitId, groups, amendmentAmountsBySpeciesAmountId,
                                                harvests, harvestSpecimensByHarvestId, observations, summaries,
                                                moderatedHarvestCounts, rhyOfficialCodesByRhyId, clubIndex);
    }

    public WhiteTailedDeerObjectFactory(final List<HarvestPermit> permits,
                                        final Map<Long, HarvestPermitSpeciesAmount> amountsByPermitId,
                                        final List<HuntingClubGroup> groups,
                                        final Map<Long, List<HarvestPermitSpeciesAmount>> amendmentAmountsBySpeciesAmountId,
                                        final List<Harvest> harvests,
                                        final Map<Long, HarvestSpecimen> harvestSpecimensByHarvestId,
                                        final List<Observation> observations,
                                        final ClubHuntingSummaryBasicInfoByPermitAndClub summaries,
                                        final HarvestCountByPermitAndClub moderatedHarvestCounts,
                                        final Map<Long, String> rhyOfficialCodesByRhyId,
                                        final Map<Long, HuntingClub> clubIndex) {
        this.permits = permits;
        this.amountsByPermitId = amountsByPermitId;
        this.groups = groups;
        this.amendmentAmountsBySpeciesAmountId = amendmentAmountsBySpeciesAmountId;
        this.harvests = harvests;
        this.harvestSpecimensByHarvestId = harvestSpecimensByHarvestId;
        this.observations = observations;
        this.summaries = summaries;
        this.moderatedHarvestCounts = moderatedHarvestCounts;
        this.rhyOfficialCodesByRhyId = rhyOfficialCodesByRhyId;
        this.clubIndex = clubIndex;
    }

    public LED_Permits generate() {
        final LED_Permits permitsDto = new LED_Permits();
        permits.stream().forEach(permit -> permitsDto.getPermits().add(createPermitDto(permit)));
        return permitsDto;
    }

    private LED_Permit createPermitDto(final HarvestPermit permit) {
        final LED_Permit permitDto = new LED_Permit();
        permitDto.setPermitNumber(permit.getPermitNumber());
        permitDto.setRhyOfficialCode(rhyOfficialCodesByRhyId.get(permit.getRhy().getId()));

        Optional.ofNullable(amountsByPermitId.get(permit.getId()))
                .ifPresent(amount -> permitDto.setAmount(createAmountDto(amount)));

        Optional.ofNullable(amendmentAmountsBySpeciesAmountId.get(permit.getId()))
                .ifPresent(amendments -> permitDto.getAmendmentPermits().addAll(
                        amendments.stream()
                                .map(this::createAmountDto)
                                .collect(toList())));

        summaries.indexByClubId(permit).forEach((clubId, summary) -> permitDto.getHuntingClubs().add(
                createClubDto(clubIndex.get(clubId), permit, summary)));

        return permitDto;
    }

    private LED_Amount createAmountDto(final HarvestPermitSpeciesAmount amount) {
        final LED_Amount amountDto = new LED_Amount();
        amountDto.setAmount(amount.getSpecimenAmount());
        Optional.ofNullable(amount.getRestrictionType())
                .ifPresent(restriction -> amountDto.setRestriction(LED_RestrictionType.fromValue(restriction.toString())));
        amountDto.setRestrictedAmount((amount.getRestrictionAmount()));
        return amountDto;
    }

    private LED_Club createClubDto(final HuntingClub club,
                                   final HarvestPermit permit,
                                   final ClubHuntingSummaryBasicInfoDTO summary) {

        final LED_Club clubDto = new LED_Club();
        clubDto.setClubOfficialCode(club.getOfficialCode());
        clubDto.setNameFinnish(club.getNameFinnish());
        clubDto.setRhyOfficialCode(rhyOfficialCodesByRhyId.get(club.getParentOrganisation().getId()));
        Optional.ofNullable(club.getGeoLocation())
                .ifPresent(location -> clubDto.setGeoLocation(createGeoLocationDto(location)));

        final boolean moderatorOverride = summary.isHuntingFinishedByModeration();

        clubDto.setGroups(groups.stream()
                .filter(g -> g.getHarvestPermit().equals(permit))
                .filter(g -> g.getParentOrganisation().getId().equals(club.getId()))
                .map(g -> createGroupDto(g, moderatorOverride))
                .collect(toList()));

        if (moderatorOverride) {
            Optional.ofNullable(moderatedHarvestCounts.findCount(permit, club))
                    .ifPresent(counts -> clubDto.setOverrides(createOverridesDto(counts, summary)));
        } else {
            clubDto.setHuntingSummary(createHuntingSummaryDto(summary));
        }

        return clubDto;
    }

    private static LED_GeoLocation createGeoLocationDto(final GeoLocation location) {
        final LED_GeoLocation dto = new LED_GeoLocation();
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        Optional.ofNullable(location.getSource())
                .ifPresent(item -> dto.setSource(LED_Source.fromValue(item.toString())));
        dto.setAccuracy(location.getAccuracy());
        dto.setAltitude(location.getAltitude());
        dto.setAltitudeAccuracy(location.getAltitudeAccuracy());
        return dto;
    }

    private LED_Group createGroupDto(final HuntingClubGroup group, final boolean harvestsOverridden) {
        final LED_Group dto = new LED_Group();
        dto.setDataSource(group.isFromMooseDataCard() ? MOOSE_DATA_CARD : WEB); // TODO: is this for wtd too?
        dto.setNameFinnish(group.getNameFinnish());

        if (!harvestsOverridden) {
            dto.getHarvests().addAll(
                    harvests.stream()
                            .filter(harvest -> harvest.getHuntingClubGroup().isPresent())
                            .filter(harvest -> harvest.getHuntingClubGroup().get().getId().equals(group.getId()))
                            .map(this::createHarvestDto)
                            .collect(toList()));
        }

        dto.getObservations().addAll(
                observations.stream()
                        .filter(observation -> observation.getHuntingClubGroup().isPresent())
                        .filter(observation -> observation.getHuntingClubGroup().get().getId().equals(group.getId()))
                        .map(this::createObservationDto)
                        .collect(toList()));

        return dto;
    }

    private LED_Harvest createHarvestDto(final Harvest harvest) {
        final LED_Harvest dto = new LED_Harvest();
        dto.setGeoLocation(createGeoLocationDto(harvest.getGeoLocation()));
        dto.setPointOfTime(DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTime()));
        dto.setHuntingType(createDeerHuntingTypeDto(harvest.getDeerHuntingType(), harvest.getDeerHuntingOtherTypeDescription()));

        final HarvestSpecimen specimen = harvestSpecimensByHarvestId.get(harvest.getId());
        addSpecimenData(dto, specimen);

        return dto;
    }

    private static LED_DeerHuntingType createDeerHuntingTypeDto(final DeerHuntingType huntingType,
                                                                final String huntingDescription) {
        final LED_DeerHuntingType dto = new LED_DeerHuntingType();
        Optional.ofNullable(huntingType)
                .ifPresent(item -> dto.setHuntingType(createHuntingMethodDto(huntingType)));
        dto.setHuntingTypeDescription(huntingDescription);
        return dto;
    }

    private static LED_HuntingMethod createHuntingMethodDto(final DeerHuntingType huntingType) {
        if (huntingType == DeerHuntingType.OTHER) {
            return LED_HuntingMethod.MUU;
        }
        return LED_HuntingMethod.fromValue(huntingType.toString());
    }

    private static void addSpecimenData(final LED_Harvest dto, final HarvestSpecimen specimen) {
        Optional.ofNullable(specimen.getGender())
                .ifPresent(gender -> dto.setGender(LED_GameGender.fromValue(gender.toString())));
        Optional.ofNullable(specimen.getAge())
                .ifPresent(age -> dto.setAge(LED_GameAge.fromValue(age.toString())));
        dto.setWeightEstimated(specimen.getWeightEstimated());
        dto.setWeightMeasured(specimen.getWeightMeasured());
        Optional.ofNullable(specimen.getAntlersType())
                .ifPresent(antlers -> dto.setAntlersType(LED_GameAntlersType.fromValue(antlers.toString()))); // NOTE! Not set for deers
        dto.setAntlersWidth(specimen.getAntlersWidth());
        dto.setAntlerPointsLeft(specimen.getAntlerPointsLeft());
        dto.setAntlerPointsRight(specimen.getAntlerPointsRight());
        dto.setNotEdible(specimen.getNotEdible());
        dto.setAdditionalInfo(specimen.getAdditionalInfo());
    }

    private LED_Observation createObservationDto(final Observation observation) {
        final LED_Observation dto = new LED_Observation();

        dto.setGeoLocation(createGeoLocationDto(observation.getGeoLocation()));
        dto.setPointOfTime(DateUtil.toLocalDateTimeNullSafe(observation.getPointOfTime()));
        dto.setHuntingType(createDeerHuntingTypeDto(observation.getDeerHuntingType(), observation.getDeerHuntingTypeDescription()));
        dto.setObservationType(LED_ObservationType.fromValue(observation.getObservationType().toString())); // must have
        dto.setGameSpeciesCode(observation.getSpecies().getOfficialCode());
        dto.setGameSpeciesNameFinnish(observation.getSpecies().getNameFinnish());
        dto.setMooselikeMaleAmount(Optional.ofNullable(observation.getMooselikeMaleAmount()).orElse(0));

        Optional.ofNullable(observation.getMooselikeFemaleAmount())
                .filter(amount -> amount > 0)
                .map(amount -> dto.getMooseLikeFemaleAndCalfs().add(createFemaleAndCalvesDto(amount, 0)));

        Optional.ofNullable(observation.getMooselikeFemale1CalfAmount())
                .filter(amount -> amount > 0)
                .ifPresent(amount -> dto.getMooseLikeFemaleAndCalfs().add(createFemaleAndCalvesDto(amount, 1)));

        Optional.ofNullable(observation.getMooselikeFemale2CalfsAmount())
                .filter(amount -> amount > 0)
                .ifPresent(amount -> dto.getMooseLikeFemaleAndCalfs().add(createFemaleAndCalvesDto(amount, 2)));

        Optional.ofNullable(observation.getMooselikeFemale3CalfsAmount())
                .filter(amount -> amount > 0)
                .ifPresent(amount -> dto.getMooseLikeFemaleAndCalfs().add(createFemaleAndCalvesDto(amount, 3)));

        Optional.ofNullable(observation.getMooselikeFemale4CalfsAmount())
                .filter(amount -> amount > 0)
                .ifPresent(amount -> dto.getMooseLikeFemaleAndCalfs().add(createFemaleAndCalvesDto(amount, 4)));

        dto.setMooselikeSolitaryCalfAmount(observation.getMooselikeCalfAmount()); // NOTE: Actually this is never set for deers
        dto.setMooselikeUnknownSpecimenAmount(Optional.ofNullable(observation.getMooselikeUnknownSpecimenAmount()).orElse(0));

        return dto;
    }

    private static LED_FemaleAndCalfs createFemaleAndCalvesDto(final int amount, final int calves) {
        final LED_FemaleAndCalfs dto = new LED_FemaleAndCalfs();
        dto.setAmount(amount);
        dto.setCalfs(calves);
        return dto;
    }

    private static LED_HuntingSummary createHuntingSummaryDto(final ClubHuntingSummaryBasicInfoDTO summary) {
        if (!summary.isHuntingFinished()) {
            return null;
        }

        final LED_HuntingSummary dto = new LED_HuntingSummary();
        dto.setHuntingEndDate(summary.getHuntingEndDate());
        dto.setHuntingFinished(summary.isHuntingFinished());
        dto.setTotalHuntingArea(summary.getTotalHuntingArea());
        dto.setEffectiveHuntingArea(summary.getEffectiveHuntingArea());
        dto.setPopulationRemainingInTotalHuntingArea(summary.getRemainingPopulationInTotalArea());
        dto.setPopulationRemainingInEffectiveHuntingArea(summary.getRemainingPopulationInEffectiveArea());
        return dto;
    }

    private static LED_Overrides createOverridesDto(final HasHarvestCountsForPermit moderatedCounts,
                                                    final ClubHuntingSummaryBasicInfoDTO areaAndPopulation) {
        final LED_Overrides dto = new LED_Overrides();
        dto.setAdultMales(moderatedCounts.getNumberOfAdultMales());
        dto.setAdultFemales(moderatedCounts.getNumberOfAdultFemales());
        dto.setYoungMales(moderatedCounts.getNumberOfYoungMales());
        dto.setYoungFemales(moderatedCounts.getNumberOfYoungFemales());
        dto.setNonEdibleAdults(moderatedCounts.getNumberOfNonEdibleAdults());
        dto.setNonEdibleYoung(moderatedCounts.getNumberOfNonEdibleYoungs());

        dto.setTotalHuntingArea(areaAndPopulation.getTotalHuntingArea());
        dto.setEffectiveHuntingArea(areaAndPopulation.getEffectiveHuntingArea());
        dto.setRemainingPopulationInTotalArea(areaAndPopulation.getRemainingPopulationInTotalArea());
        dto.setRemainingPopulationInEffectiveArea(areaAndPopulation.getRemainingPopulationInEffectiveArea());
        return dto;
    }

}
