package fi.riista.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import com.querydsl.sql.spatial.RelationalPathSpatial;

import com.querydsl.spatial.*;



/**
 * SQBasicClubHuntingSummary is a Querydsl query type for SQBasicClubHuntingSummary
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQBasicClubHuntingSummary extends RelationalPathSpatial<SQBasicClubHuntingSummary> {

    private static final long serialVersionUID = 131820584;

    public static final SQBasicClubHuntingSummary basicClubHuntingSummary = new SQBasicClubHuntingSummary("basic_club_hunting_summary");

    public final NumberPath<Long> clubId = createNumber("clubId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Integer> effectiveHuntingArea = createNumber("effectiveHuntingArea", Integer.class);

    public final DatePath<java.sql.Date> huntingEndDate = createDate("huntingEndDate", java.sql.Date.class);

    public final BooleanPath huntingFinished = createBoolean("huntingFinished");

    public final NumberPath<Long> huntingSummaryId = createNumber("huntingSummaryId", Long.class);

    public final BooleanPath moderatorOverride = createBoolean("moderatorOverride");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Integer> numberOfAdultFemales = createNumber("numberOfAdultFemales", Integer.class);

    public final NumberPath<Integer> numberOfAdultMales = createNumber("numberOfAdultMales", Integer.class);

    public final NumberPath<Integer> numberOfNonEdibleAdults = createNumber("numberOfNonEdibleAdults", Integer.class);

    public final NumberPath<Integer> numberOfNonEdibleYoungs = createNumber("numberOfNonEdibleYoungs", Integer.class);

    public final NumberPath<Integer> numberOfYoungFemales = createNumber("numberOfYoungFemales", Integer.class);

    public final NumberPath<Integer> numberOfYoungMales = createNumber("numberOfYoungMales", Integer.class);

    public final NumberPath<Integer> originalEffectiveHuntingArea = createNumber("originalEffectiveHuntingArea", Integer.class);

    public final DatePath<java.sql.Date> originalHuntingEndDate = createDate("originalHuntingEndDate", java.sql.Date.class);

    public final BooleanPath originalHuntingFinished = createBoolean("originalHuntingFinished");

    public final NumberPath<Integer> originalRemainingPopulationInEffectiveArea = createNumber("originalRemainingPopulationInEffectiveArea", Integer.class);

    public final NumberPath<Integer> originalRemainingPopulationInTotalArea = createNumber("originalRemainingPopulationInTotalArea", Integer.class);

    public final NumberPath<Integer> originalTotalHuntingArea = createNumber("originalTotalHuntingArea", Integer.class);

    public final NumberPath<Integer> remainingPopulationInEffectiveArea = createNumber("remainingPopulationInEffectiveArea", Integer.class);

    public final NumberPath<Integer> remainingPopulationInTotalArea = createNumber("remainingPopulationInTotalArea", Integer.class);

    public final NumberPath<Long> speciesAmountId = createNumber("speciesAmountId", Long.class);

    public final NumberPath<Integer> totalHuntingArea = createNumber("totalHuntingArea", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQBasicClubHuntingSummary> basicClubHuntingSummaryPkey = createPrimaryKey(huntingSummaryId);

    public final com.querydsl.sql.ForeignKey<SQOrganisation> basicClubHuntingSummaryClubFk = createForeignKey(clubId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitSpeciesAmount> basicClubHuntingSummarySpeciesAmountFk = createForeignKey(speciesAmountId, "harvest_permit_species_amount_id");

    public SQBasicClubHuntingSummary(String variable) {
        super(SQBasicClubHuntingSummary.class, forVariable(variable), "public", "basic_club_hunting_summary");
        addMetadata();
    }

    public SQBasicClubHuntingSummary(String variable, String schema, String table) {
        super(SQBasicClubHuntingSummary.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQBasicClubHuntingSummary(Path<? extends SQBasicClubHuntingSummary> path) {
        super(path.getType(), path.getMetadata(), "public", "basic_club_hunting_summary");
        addMetadata();
    }

    public SQBasicClubHuntingSummary(PathMetadata metadata) {
        super(SQBasicClubHuntingSummary.class, metadata, "public", "basic_club_hunting_summary");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(clubId, ColumnMetadata.named("club_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(effectiveHuntingArea, ColumnMetadata.named("effective_hunting_area").withIndex(14).ofType(Types.INTEGER).withSize(10));
        addMetadata(huntingEndDate, ColumnMetadata.named("hunting_end_date").withIndex(11).ofType(Types.DATE).withSize(13));
        addMetadata(huntingFinished, ColumnMetadata.named("hunting_finished").withIndex(28).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(huntingSummaryId, ColumnMetadata.named("hunting_summary_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(moderatorOverride, ColumnMetadata.named("moderator_override").withIndex(12).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(numberOfAdultFemales, ColumnMetadata.named("number_of_adult_females").withIndex(18).ofType(Types.INTEGER).withSize(10));
        addMetadata(numberOfAdultMales, ColumnMetadata.named("number_of_adult_males").withIndex(17).ofType(Types.INTEGER).withSize(10));
        addMetadata(numberOfNonEdibleAdults, ColumnMetadata.named("number_of_non_edible_adults").withIndex(21).ofType(Types.INTEGER).withSize(10));
        addMetadata(numberOfNonEdibleYoungs, ColumnMetadata.named("number_of_non_edible_youngs").withIndex(22).ofType(Types.INTEGER).withSize(10));
        addMetadata(numberOfYoungFemales, ColumnMetadata.named("number_of_young_females").withIndex(20).ofType(Types.INTEGER).withSize(10));
        addMetadata(numberOfYoungMales, ColumnMetadata.named("number_of_young_males").withIndex(19).ofType(Types.INTEGER).withSize(10));
        addMetadata(originalEffectiveHuntingArea, ColumnMetadata.named("original_effective_hunting_area").withIndex(25).ofType(Types.INTEGER).withSize(10));
        addMetadata(originalHuntingEndDate, ColumnMetadata.named("original_hunting_end_date").withIndex(23).ofType(Types.DATE).withSize(13));
        addMetadata(originalHuntingFinished, ColumnMetadata.named("original_hunting_finished").withIndex(29).ofType(Types.BIT).withSize(1));
        addMetadata(originalRemainingPopulationInEffectiveArea, ColumnMetadata.named("original_remaining_population_in_effective_area").withIndex(27).ofType(Types.INTEGER).withSize(10));
        addMetadata(originalRemainingPopulationInTotalArea, ColumnMetadata.named("original_remaining_population_in_total_area").withIndex(26).ofType(Types.INTEGER).withSize(10));
        addMetadata(originalTotalHuntingArea, ColumnMetadata.named("original_total_hunting_area").withIndex(24).ofType(Types.INTEGER).withSize(10));
        addMetadata(remainingPopulationInEffectiveArea, ColumnMetadata.named("remaining_population_in_effective_area").withIndex(16).ofType(Types.INTEGER).withSize(10));
        addMetadata(remainingPopulationInTotalArea, ColumnMetadata.named("remaining_population_in_total_area").withIndex(15).ofType(Types.INTEGER).withSize(10));
        addMetadata(speciesAmountId, ColumnMetadata.named("species_amount_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(totalHuntingArea, ColumnMetadata.named("total_hunting_area").withIndex(13).ofType(Types.INTEGER).withSize(10));
    }

}

