package fi.riista.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import java.util.*;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import com.querydsl.sql.spatial.RelationalPathSpatial;

import com.querydsl.spatial.*;



/**
 * SQMooseHuntingSummary is a Querydsl query type for SQMooseHuntingSummary
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMooseHuntingSummary extends RelationalPathSpatial<SQMooseHuntingSummary> {

    private static final long serialVersionUID = 696946563;

    public static final SQMooseHuntingSummary mooseHuntingSummary = new SQMooseHuntingSummary("moose_hunting_summary");

    public final DatePath<java.sql.Date> beginDate = createDate("beginDate", java.sql.Date.class);

    public final StringPath causeOfDeath = createString("causeOfDeath");

    public final NumberPath<Long> clubId = createNumber("clubId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final DatePath<java.sql.Date> dateOfFirstDeerFlySeen = createDate("dateOfFirstDeerFlySeen", java.sql.Date.class);

    public final DatePath<java.sql.Date> dateOfLastDeerFlySeen = createDate("dateOfLastDeerFlySeen", java.sql.Date.class);

    public final BooleanPath deerFliesAppeared = createBoolean("deerFliesAppeared");

    public final StringPath deerFlyPopulationGrowth = createString("deerFlyPopulationGrowth");

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Integer> drownedMooses = createNumber("drownedMooses", Integer.class);

    public final NumberPath<Integer> effectiveHuntingArea = createNumber("effectiveHuntingArea", Integer.class);

    public final NumberPath<java.math.BigDecimal> effectiveHuntingAreaPercentage = createNumber("effectiveHuntingAreaPercentage", java.math.BigDecimal.class);

    public final DatePath<java.sql.Date> endDate = createDate("endDate", java.sql.Date.class);

    public final BooleanPath fallowDeerAppeared = createBoolean("fallowDeerAppeared");

    public final NumberPath<Integer> fallowDeerEstimatedSpecimenAmount = createNumber("fallowDeerEstimatedSpecimenAmount", Integer.class);

    public final StringPath fallowDeerPopulationGrowth = createString("fallowDeerPopulationGrowth");

    public final NumberPath<Long> harvestPermitId = createNumber("harvestPermitId", Long.class);

    public final StringPath huntingAreaType = createString("huntingAreaType");

    public final DatePath<java.sql.Date> huntingEndDate = createDate("huntingEndDate", java.sql.Date.class);

    public final BooleanPath huntingFinished = createBoolean("huntingFinished");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final DatePath<java.sql.Date> mooseFawnBeginDate = createDate("mooseFawnBeginDate", java.sql.Date.class);

    public final DatePath<java.sql.Date> mooseFawnEndDate = createDate("mooseFawnEndDate", java.sql.Date.class);

    public final DatePath<java.sql.Date> mooseHeatBeginDate = createDate("mooseHeatBeginDate", java.sql.Date.class);

    public final DatePath<java.sql.Date> mooseHeatEndDate = createDate("mooseHeatEndDate", java.sql.Date.class);

    public final NumberPath<Long> mooseHuntingSummaryId = createNumber("mooseHuntingSummaryId", Long.class);

    public final NumberPath<Integer> moosesDeceasedByOtherReason = createNumber("moosesDeceasedByOtherReason", Integer.class);

    public final NumberPath<Integer> moosesKilledByBear = createNumber("moosesKilledByBear", Integer.class);

    public final NumberPath<Integer> moosesKilledByPoaching = createNumber("moosesKilledByPoaching", Integer.class);

    public final NumberPath<Integer> moosesKilledByWolf = createNumber("moosesKilledByWolf", Integer.class);

    public final NumberPath<Integer> moosesKilledInRutFight = createNumber("moosesKilledInRutFight", Integer.class);

    public final NumberPath<Integer> moosesKilledInTrafficAccident = createNumber("moosesKilledInTrafficAccident", Integer.class);

    public final NumberPath<Integer> moosesRemainingInEffectiveHuntingArea = createNumber("moosesRemainingInEffectiveHuntingArea", Integer.class);

    public final NumberPath<Integer> moosesRemainingInTotalHuntingArea = createNumber("moosesRemainingInTotalHuntingArea", Integer.class);

    public final NumberPath<Integer> numberOfAdultMoosesHavingFlies = createNumber("numberOfAdultMoosesHavingFlies", Integer.class);

    public final NumberPath<Integer> numberOfYoungMoosesHavingFlies = createNumber("numberOfYoungMoosesHavingFlies", Integer.class);

    public final BooleanPath roeDeerAppeared = createBoolean("roeDeerAppeared");

    public final NumberPath<Integer> roeDeerEstimatedSpecimenAmount = createNumber("roeDeerEstimatedSpecimenAmount", Integer.class);

    public final StringPath roeDeerPopulationGrowth = createString("roeDeerPopulationGrowth");

    public final NumberPath<Integer> starvedMooses = createNumber("starvedMooses", Integer.class);

    public final NumberPath<Integer> totalHuntingArea = createNumber("totalHuntingArea", Integer.class);

    public final BooleanPath whiteTailedDeerAppeared = createBoolean("whiteTailedDeerAppeared");

    public final NumberPath<Integer> whiteTailedDeerEstimatedSpecimenAmount = createNumber("whiteTailedDeerEstimatedSpecimenAmount", Integer.class);

    public final StringPath whiteTailedDeerPopulationGrowth = createString("whiteTailedDeerPopulationGrowth");

    public final BooleanPath wildBoarAppeared = createBoolean("wildBoarAppeared");

    public final NumberPath<Integer> wildBoarEstimatedAmountOfSowWithPiglets = createNumber("wildBoarEstimatedAmountOfSowWithPiglets", Integer.class);

    public final NumberPath<Integer> wildBoarEstimatedSpecimenAmount = createNumber("wildBoarEstimatedSpecimenAmount", Integer.class);

    public final StringPath wildBoarPopulationGrowth = createString("wildBoarPopulationGrowth");

    public final BooleanPath wildForestReindeerAppeared = createBoolean("wildForestReindeerAppeared");

    public final NumberPath<Integer> wildForestReindeerEstimatedSpecimenAmount = createNumber("wildForestReindeerEstimatedSpecimenAmount", Integer.class);

    public final StringPath wildForestReindeerPopulationGrowth = createString("wildForestReindeerPopulationGrowth");

    public final com.querydsl.sql.PrimaryKey<SQMooseHuntingSummary> mooseHuntingSummaryPkey = createPrimaryKey(mooseHuntingSummaryId);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> mooseHuntingSummaryPermitFk = createForeignKey(harvestPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> mooseHuntingSummaryClubFk = createForeignKey(clubId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitPartners> mooseHuntingSummaryHarvestPermitPartnersFk = createForeignKey(Arrays.asList(harvestPermitId, clubId), Arrays.asList("harvest_permit_id", "organisation_id"));

    public SQMooseHuntingSummary(String variable) {
        super(SQMooseHuntingSummary.class, forVariable(variable), "public", "moose_hunting_summary");
        addMetadata();
    }

    public SQMooseHuntingSummary(String variable, String schema, String table) {
        super(SQMooseHuntingSummary.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQMooseHuntingSummary(Path<? extends SQMooseHuntingSummary> path) {
        super(path.getType(), path.getMetadata(), "public", "moose_hunting_summary");
        addMetadata();
    }

    public SQMooseHuntingSummary(PathMetadata metadata) {
        super(SQMooseHuntingSummary.class, metadata, "public", "moose_hunting_summary");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(beginDate, ColumnMetadata.named("begin_date").withIndex(11).ofType(Types.DATE).withSize(13));
        addMetadata(causeOfDeath, ColumnMetadata.named("cause_of_death").withIndex(29).ofType(Types.VARCHAR).withSize(255));
        addMetadata(clubId, ColumnMetadata.named("club_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(dateOfFirstDeerFlySeen, ColumnMetadata.named("date_of_first_deer_fly_seen").withIndex(46).ofType(Types.DATE).withSize(13));
        addMetadata(dateOfLastDeerFlySeen, ColumnMetadata.named("date_of_last_deer_fly_seen").withIndex(47).ofType(Types.DATE).withSize(13));
        addMetadata(deerFliesAppeared, ColumnMetadata.named("deer_flies_appeared").withIndex(50).ofType(Types.BIT).withSize(1));
        addMetadata(deerFlyPopulationGrowth, ColumnMetadata.named("deer_fly_population_growth").withIndex(51).ofType(Types.CHAR).withSize(1));
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(drownedMooses, ColumnMetadata.named("drowned_mooses").withIndex(21).ofType(Types.INTEGER).withSize(10));
        addMetadata(effectiveHuntingArea, ColumnMetadata.named("effective_hunting_area").withIndex(16).ofType(Types.INTEGER).withSize(10));
        addMetadata(effectiveHuntingAreaPercentage, ColumnMetadata.named("effective_hunting_area_percentage").withIndex(17).ofType(Types.NUMERIC).withSize(5).withDigits(2));
        addMetadata(endDate, ColumnMetadata.named("end_date").withIndex(12).ofType(Types.DATE).withSize(13));
        addMetadata(fallowDeerAppeared, ColumnMetadata.named("fallow_deer_appeared").withIndex(39).ofType(Types.BIT).withSize(1));
        addMetadata(fallowDeerEstimatedSpecimenAmount, ColumnMetadata.named("fallow_deer_estimated_specimen_amount").withIndex(41).ofType(Types.INTEGER).withSize(10));
        addMetadata(fallowDeerPopulationGrowth, ColumnMetadata.named("fallow_deer_population_growth").withIndex(40).ofType(Types.CHAR).withSize(1));
        addMetadata(harvestPermitId, ColumnMetadata.named("harvest_permit_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(huntingAreaType, ColumnMetadata.named("hunting_area_type").withIndex(20).ofType(Types.CHAR).withSize(1));
        addMetadata(huntingEndDate, ColumnMetadata.named("hunting_end_date").withIndex(13).ofType(Types.DATE).withSize(13));
        addMetadata(huntingFinished, ColumnMetadata.named("hunting_finished").withIndex(14).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(mooseFawnBeginDate, ColumnMetadata.named("moose_fawn_begin_date").withIndex(44).ofType(Types.DATE).withSize(13));
        addMetadata(mooseFawnEndDate, ColumnMetadata.named("moose_fawn_end_date").withIndex(45).ofType(Types.DATE).withSize(13));
        addMetadata(mooseHeatBeginDate, ColumnMetadata.named("moose_heat_begin_date").withIndex(42).ofType(Types.DATE).withSize(13));
        addMetadata(mooseHeatEndDate, ColumnMetadata.named("moose_heat_end_date").withIndex(43).ofType(Types.DATE).withSize(13));
        addMetadata(mooseHuntingSummaryId, ColumnMetadata.named("moose_hunting_summary_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(moosesDeceasedByOtherReason, ColumnMetadata.named("mooses_deceased_by_other_reason").withIndex(28).ofType(Types.INTEGER).withSize(10));
        addMetadata(moosesKilledByBear, ColumnMetadata.named("mooses_killed_by_bear").withIndex(22).ofType(Types.INTEGER).withSize(10));
        addMetadata(moosesKilledByPoaching, ColumnMetadata.named("mooses_killed_by_poaching").withIndex(25).ofType(Types.INTEGER).withSize(10));
        addMetadata(moosesKilledByWolf, ColumnMetadata.named("mooses_killed_by_wolf").withIndex(23).ofType(Types.INTEGER).withSize(10));
        addMetadata(moosesKilledInRutFight, ColumnMetadata.named("mooses_killed_in_rut_fight").withIndex(26).ofType(Types.INTEGER).withSize(10));
        addMetadata(moosesKilledInTrafficAccident, ColumnMetadata.named("mooses_killed_in_traffic_accident").withIndex(24).ofType(Types.INTEGER).withSize(10));
        addMetadata(moosesRemainingInEffectiveHuntingArea, ColumnMetadata.named("mooses_remaining_in_effective_hunting_area").withIndex(19).ofType(Types.INTEGER).withSize(10));
        addMetadata(moosesRemainingInTotalHuntingArea, ColumnMetadata.named("mooses_remaining_in_total_hunting_area").withIndex(18).ofType(Types.INTEGER).withSize(10));
        addMetadata(numberOfAdultMoosesHavingFlies, ColumnMetadata.named("number_of_adult_mooses_having_flies").withIndex(48).ofType(Types.INTEGER).withSize(10));
        addMetadata(numberOfYoungMoosesHavingFlies, ColumnMetadata.named("number_of_young_mooses_having_flies").withIndex(49).ofType(Types.INTEGER).withSize(10));
        addMetadata(roeDeerAppeared, ColumnMetadata.named("roe_deer_appeared").withIndex(33).ofType(Types.BIT).withSize(1));
        addMetadata(roeDeerEstimatedSpecimenAmount, ColumnMetadata.named("roe_deer_estimated_specimen_amount").withIndex(35).ofType(Types.INTEGER).withSize(10));
        addMetadata(roeDeerPopulationGrowth, ColumnMetadata.named("roe_deer_population_growth").withIndex(34).ofType(Types.CHAR).withSize(1));
        addMetadata(starvedMooses, ColumnMetadata.named("starved_mooses").withIndex(27).ofType(Types.INTEGER).withSize(10));
        addMetadata(totalHuntingArea, ColumnMetadata.named("total_hunting_area").withIndex(15).ofType(Types.INTEGER).withSize(10));
        addMetadata(whiteTailedDeerAppeared, ColumnMetadata.named("white_tailed_deer_appeared").withIndex(30).ofType(Types.BIT).withSize(1));
        addMetadata(whiteTailedDeerEstimatedSpecimenAmount, ColumnMetadata.named("white_tailed_deer_estimated_specimen_amount").withIndex(32).ofType(Types.INTEGER).withSize(10));
        addMetadata(whiteTailedDeerPopulationGrowth, ColumnMetadata.named("white_tailed_deer_population_growth").withIndex(31).ofType(Types.CHAR).withSize(1));
        addMetadata(wildBoarAppeared, ColumnMetadata.named("wild_boar_appeared").withIndex(52).ofType(Types.BIT).withSize(1));
        addMetadata(wildBoarEstimatedAmountOfSowWithPiglets, ColumnMetadata.named("wild_boar_estimated_amount_of_sow_with_piglets").withIndex(55).ofType(Types.INTEGER).withSize(10));
        addMetadata(wildBoarEstimatedSpecimenAmount, ColumnMetadata.named("wild_boar_estimated_specimen_amount").withIndex(54).ofType(Types.INTEGER).withSize(10));
        addMetadata(wildBoarPopulationGrowth, ColumnMetadata.named("wild_boar_population_growth").withIndex(53).ofType(Types.CHAR).withSize(1));
        addMetadata(wildForestReindeerAppeared, ColumnMetadata.named("wild_forest_reindeer_appeared").withIndex(36).ofType(Types.BIT).withSize(1));
        addMetadata(wildForestReindeerEstimatedSpecimenAmount, ColumnMetadata.named("wild_forest_reindeer_estimated_specimen_amount").withIndex(38).ofType(Types.INTEGER).withSize(10));
        addMetadata(wildForestReindeerPopulationGrowth, ColumnMetadata.named("wild_forest_reindeer_population_growth").withIndex(37).ofType(Types.CHAR).withSize(1));
    }

}

