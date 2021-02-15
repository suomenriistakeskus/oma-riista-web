package fi.riista.integration.common.export;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.gamediary.harvest.specimen.QHarvestSpecimen;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.QGroupHuntingDay;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.integration.common.export.harvests.CHAR_GameAge;
import fi.riista.integration.common.export.harvests.CHAR_GameAntlersType;
import fi.riista.integration.common.export.harvests.CHAR_GameFitnessClass;
import fi.riista.integration.common.export.harvests.CHAR_GameGender;
import fi.riista.integration.common.export.harvests.CHAR_GeoLocation;
import fi.riista.integration.common.export.harvests.CHAR_Harvest;
import fi.riista.integration.common.export.harvests.CHAR_Harvests;
import fi.riista.integration.common.export.harvests.CHAR_Specimen;
import fi.riista.util.DateUtil;
import fi.riista.util.EnumUtils;
import fi.riista.util.F;
import fi.riista.util.JaxbUtils;
import org.joda.time.DateTime;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static fi.riista.integration.common.export.RvrConstants.RVR_SPECIES;
import static java.util.stream.Collectors.toList;

@Service
public class CommonHarvestExportFeature {

    private static final QHarvest HARVEST = QHarvest.harvest;
    private static final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
    private static final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
    private static final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
    private static final QHarvestPermit MOOSE_PERMIT = new QHarvestPermit("moosePermit");
    private static final QHuntingClubGroup GROUP = QHuntingClubGroup.huntingClubGroup;
    private static final QGroupHuntingDay HUNTING_DAY = QGroupHuntingDay.groupHuntingDay;
    private static final ComparableExpression<String> PERMIT_NUMBER =
            MOOSE_PERMIT.permitNumber.coalesce(PERMIT.permitNumber).getValue();
    private static final QHarvestSpecimen SPECIMEN = QHarvestSpecimen.harvestSpecimen;

    private static final BooleanExpression RVR_SPECIES_PREDICATE =
            SPECIES.officialCode.in(RVR_SPECIES);

    private static final BooleanExpression OFFICIAL_HARVEST =
            HARVEST.huntingDayOfGroup.id.isNotNull()
                    .or(HARVEST.harvestReportState.eq(HarvestReportState.APPROVED));

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource(name = "commonHarvestExportMarshaller")
    private Jaxb2Marshaller jaxbMarshaller;

    // LUKE

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUKE_COMMON')")
    public String exportAllHarvestsAsXml(final int year, final int month) {
        final CHAR_Harvests allHarvests = exportAllHarvests(year, month);
        return JaxbUtils.marshalToString(allHarvests, jaxbMarshaller);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUKE_COMMON')")
    public CHAR_Harvests exportAllHarvests(final int year, final int month) {
        Preconditions.checkArgument(0 < month && month < 13, "Month value must be valid");

        return fetchHarvestsWithSpecimen(harvestInMonthPredicate(year, month));
    }

    // RVR

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_RVR_COMMON')")
    public String exportRVRHarvestsAsXml(final int year, final int month) {
        final CHAR_Harvests RVRHarvests = exportRVRHarvests(year, month);
        return JaxbUtils.marshalToString(RVRHarvests, jaxbMarshaller);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_RVR_COMMON')")
    public CHAR_Harvests exportRVRHarvests(final int year, final int month) {
        Preconditions.checkArgument(0 < month && month < 13, "Month value must be valid");

        final Predicate rvrPredicate = RVR_SPECIES_PREDICATE
                .and(OFFICIAL_HARVEST)
                .and(harvestInMonthPredicate(year, month));

        return fetchHarvestsWithSpecimen(rvrPredicate);
    }

    private CHAR_Harvests fetchHarvestsWithSpecimen(Predicate predicate) {
        final Collection<CHAR_Harvest> harvests = fetchHarvests(predicate);
        final List<Long> harvestIds = harvests.stream().map(h -> h.getHarvestId()).collect(toList());

        return new CHAR_Harvests()
                .withHarvest(harvests)
                .withSpecimen(findSpecimenForHarvests(harvestIds));
    }

    private static CHAR_Harvest createHarvestFromTuple(final Tuple tuple) {
        return new CHAR_Harvest()
                .withHarvestId(tuple.get(HARVEST.id))
                .withRhyNumber(tuple.get(RHY.officialCode))
                .withPointOfTime(tuple.get(HARVEST.pointOfTime).toLocalDateTime())
                .withGeoLocation(convertLocation(tuple.get(HARVEST.geoLocation)))
                .withGameSpeciesCode(tuple.get(SPECIES.officialCode))
                .withAmount(tuple.get(HARVEST.amount))
                .withOfficialHarvest(isOfficialHarvest(tuple))
                .withPermitNumber(tuple.get(PERMIT_NUMBER));
    }

    private List<CHAR_Harvest> fetchHarvests(final Predicate harvestPredicate) {

        return queryFactory
                .select(HARVEST.id,
                        RHY.officialCode,
                        HARVEST.pointOfTime,
                        HARVEST.geoLocation,
                        SPECIES.officialCode,
                        HARVEST.amount,
                        PERMIT_NUMBER,
                        HARVEST.huntingDayOfGroup.id,
                        HARVEST.harvestReportState
                )
                .from(HARVEST)
                .join(HARVEST.rhy, RHY)
                .join(HARVEST.species, SPECIES)
                .leftJoin(HARVEST.harvestPermit, PERMIT)
                .leftJoin(HARVEST.huntingDayOfGroup, HUNTING_DAY)
                .leftJoin(HUNTING_DAY.group, GROUP)
                .leftJoin(GROUP.harvestPermit, MOOSE_PERMIT)
                .where(harvestPredicate)
                .fetch()
                .stream()
                .map(CommonHarvestExportFeature::createHarvestFromTuple)
                .collect(toList());
    }

    private static boolean isOfficialHarvest(final Tuple tuple) {
        // Harvest is seen as official when hunting club has accepted the harvest to hunting day
        // for mooselike harvest. For other hearvests, the report is official when riistakeskus
        // has accepted the harvest report.
        final boolean acceptedToGroupHuntingDay = tuple.get(HARVEST.huntingDayOfGroup.id) != null;
        final boolean acceptedAsHarvestByRiistakeskus = HarvestReportState.APPROVED
                == tuple.get(HARVEST.harvestReportState);
        return acceptedToGroupHuntingDay ||
                acceptedAsHarvestByRiistakeskus;
    }

    private Collection<CHAR_Specimen> findSpecimenForHarvests(final List<Long> ids) {

        final List<CHAR_Specimen> specimen = Lists.newArrayListWithExpectedSize(ids.size());

        Lists.partition(ids, 4096).forEach(partition -> {
            final List<Tuple> specimenResult = fetchSpecimensForHarvests(partition);
            specimen.addAll(F.mapNonNullsToList(specimenResult, t -> createSpecimenFromTuple(t)));
        });

        return specimen;
    }

    private static CHAR_Specimen createSpecimenFromTuple(final Tuple t) {
        final Double weight = t.get(SPECIMEN.weight);
        final Double measuredWeight = t.get(SPECIMEN.weightMeasured);
        final Double estimatedWeight = t.get(SPECIMEN.weightEstimated);

        return new CHAR_Specimen()
                .withHarvestId(t.get(SPECIMEN.harvest.id))
                .withGender(EnumUtils.convertNullableByEnumName(CHAR_GameGender.class,
                        t.get(SPECIMEN.gender)))
                .withAge(EnumUtils.convertNullableByEnumName(CHAR_GameAge.class, t.get(SPECIMEN.age)))
                .withWeight(F.firstNonNull(weight, measuredWeight, estimatedWeight))
                .withWeightEstimated(estimatedWeight)
                .withWeightMeasured(measuredWeight)
                .withFitnessClass(EnumUtils.convertNullableByEnumName(CHAR_GameFitnessClass.class,
                        t.get(SPECIMEN.fitnessClass)))
                .withAntlersType(EnumUtils.convertNullableByEnumName(CHAR_GameAntlersType.class,
                        t.get(SPECIMEN.antlersType)))
                .withAntlersWidth(t.get(SPECIMEN.antlersWidth))
                .withAntlerPointsLeft(t.get(SPECIMEN.antlerPointsLeft))
                .withAntlerPointsRight(t.get(SPECIMEN.antlerPointsRight))
                .withNotEdible(t.get(SPECIMEN.notEdible));
    }

    private List<Tuple> fetchSpecimensForHarvests(final List<Long> ids) {
        return queryFactory.select(SPECIMEN.harvest.id,
                SPECIMEN.gender,
                SPECIMEN.age,
                SPECIMEN.weight,
                SPECIMEN.weightEstimated,
                SPECIMEN.weightMeasured,
                SPECIMEN.fitnessClass,
                SPECIMEN.antlersType,
                SPECIMEN.antlersWidth,
                SPECIMEN.antlerPointsLeft,
                SPECIMEN.antlerPointsRight,
                SPECIMEN.notEdible)
                .from(SPECIMEN)
                .where(SPECIMEN.harvest.id.in(ids))
                .fetch();
    }

    private static CHAR_GeoLocation convertLocation(final GeoLocation location) {
        return new CHAR_GeoLocation().withLatitude(location.getLatitude()).withLongitude(location.getLongitude());
    }

    private static final Predicate harvestInMonthPredicate(final int year, final int month) {
        final Range<DateTime> range = DateUtil.monthAsRange(year, month);

        return HARVEST.pointOfTime.between(
                range.lowerEndpoint(),
                range.upperEndpoint());
    }
}
