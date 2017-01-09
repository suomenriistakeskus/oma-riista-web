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
 * SQHarvestReportFields is a Querydsl query type for SQHarvestReportFields
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestReportFields extends RelationalPathSpatial<SQHarvestReportFields> {

    private static final long serialVersionUID = -1492501149;

    public static final SQHarvestReportFields harvestReportFields = new SQHarvestReportFields("harvest_report_fields");

    public final StringPath additionalInfo = createString("additionalInfo");

    public final StringPath age = createString("age");

    public final StringPath antlerPointsLeft = createString("antlerPointsLeft");

    public final StringPath antlerPointsRight = createString("antlerPointsRight");

    public final StringPath antlersType = createString("antlersType");

    public final StringPath antlersWidth = createString("antlersWidth");

    public final DatePath<java.sql.Date> beginDate = createDate("beginDate", java.sql.Date.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DatePath<java.sql.Date> endDate = createDate("endDate", java.sql.Date.class);

    public final StringPath fitnessClass = createString("fitnessClass");

    public final BooleanPath freeHuntingAlso = createBoolean("freeHuntingAlso");

    public final NumberPath<Long> gameSpeciesId = createNumber("gameSpeciesId", Long.class);

    public final StringPath gender = createString("gender");

    public final NumberPath<Long> harvestReportFieldsId = createNumber("harvestReportFieldsId", Long.class);

    public final BooleanPath harvestsAsList = createBoolean("harvestsAsList");

    public final StringPath huntingAreaSize = createString("huntingAreaSize");

    public final StringPath huntingAreaType = createString("huntingAreaType");

    public final StringPath huntingMethod = createString("huntingMethod");

    public final StringPath huntingParty = createString("huntingParty");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath name = createString("name");

    public final StringPath notEdible = createString("notEdible");

    public final StringPath permitNumber = createString("permitNumber");

    public final StringPath reportedWithPhoneCall = createString("reportedWithPhoneCall");

    public final BooleanPath usedWithPermit = createBoolean("usedWithPermit");

    public final StringPath weight = createString("weight");

    public final StringPath weightEstimated = createString("weightEstimated");

    public final StringPath weightMeasured = createString("weightMeasured");

    public final com.querydsl.sql.PrimaryKey<SQHarvestReportFields> harvestReportFieldsPkey = createPrimaryKey(harvestReportFieldsId);

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsFitnessClassFk = createForeignKey(fitnessClass, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsHuntingAreaTypeFk = createForeignKey(huntingAreaType, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsWeightEstimatedFk = createForeignKey(weightEstimated, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsAdditionalInfoFk = createForeignKey(additionalInfo, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsPermitNumberFk = createForeignKey(permitNumber, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsHuntingPartyFk = createForeignKey(huntingParty, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsHuntingAreaSizeFk = createForeignKey(huntingAreaSize, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsReportedWithPhoneCallFk = createForeignKey(reportedWithPhoneCall, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsAntlerPointsLeftFk = createForeignKey(antlerPointsLeft, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsAntlerPointsRightFk = createForeignKey(antlerPointsRight, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsAntlersTypeFk = createForeignKey(antlersType, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsAgeFk = createForeignKey(age, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsHuntingMethodFk = createForeignKey(huntingMethod, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsAntlersWidthFk = createForeignKey(antlersWidth, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsGenderFk = createForeignKey(gender, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsWeightFk = createForeignKey(weight, "name");

    public final com.querydsl.sql.ForeignKey<SQGameSpecies> harvestReportFieldsGameSpeciesFk = createForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQRequired> harvestReportFieldsWeightMeasuredFk = createForeignKey(weightEstimated, "name");

    public final com.querydsl.sql.ForeignKey<SQHarvestSeason> _harvestSeasonHarvestReportFieldsFk = createInvForeignKey(harvestReportFieldsId, "harvest_report_fields_id");

    public final com.querydsl.sql.ForeignKey<SQHarvest> _harvestHarvestHarvestReportFieldsFk = createInvForeignKey(harvestReportFieldsId, "harvest_report_fields_id");

    public SQHarvestReportFields(String variable) {
        super(SQHarvestReportFields.class, forVariable(variable), "public", "harvest_report_fields");
        addMetadata();
    }

    public SQHarvestReportFields(String variable, String schema, String table) {
        super(SQHarvestReportFields.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestReportFields(Path<? extends SQHarvestReportFields> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_report_fields");
        addMetadata();
    }

    public SQHarvestReportFields(PathMetadata metadata) {
        super(SQHarvestReportFields.class, metadata, "public", "harvest_report_fields");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(additionalInfo, ColumnMetadata.named("additional_info").withIndex(24).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(age, ColumnMetadata.named("age").withIndex(22).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(antlerPointsLeft, ColumnMetadata.named("antler_points_left").withIndex(30).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(antlerPointsRight, ColumnMetadata.named("antler_points_right").withIndex(31).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(antlersType, ColumnMetadata.named("antlers_type").withIndex(28).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(antlersWidth, ColumnMetadata.named("antlers_width").withIndex(29).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(beginDate, ColumnMetadata.named("begin_date").withIndex(12).ofType(Types.DATE).withSize(13));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(endDate, ColumnMetadata.named("end_date").withIndex(13).ofType(Types.DATE).withSize(13));
        addMetadata(fitnessClass, ColumnMetadata.named("fitness_class").withIndex(27).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(freeHuntingAlso, ColumnMetadata.named("free_hunting_also").withIndex(21).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(gameSpeciesId, ColumnMetadata.named("game_species_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(gender, ColumnMetadata.named("gender").withIndex(23).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(harvestReportFieldsId, ColumnMetadata.named("harvest_report_fields_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestsAsList, ColumnMetadata.named("harvests_as_list").withIndex(32).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(huntingAreaSize, ColumnMetadata.named("hunting_area_size").withIndex(16).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(huntingAreaType, ColumnMetadata.named("hunting_area_type").withIndex(14).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(huntingMethod, ColumnMetadata.named("hunting_method").withIndex(18).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(huntingParty, ColumnMetadata.named("hunting_party").withIndex(15).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(name, ColumnMetadata.named("name").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(notEdible, ColumnMetadata.named("not_edible").withIndex(33).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(permitNumber, ColumnMetadata.named("permit_number").withIndex(17).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(reportedWithPhoneCall, ColumnMetadata.named("reported_with_phone_call").withIndex(20).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(usedWithPermit, ColumnMetadata.named("used_with_permit").withIndex(11).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(weight, ColumnMetadata.named("weight").withIndex(19).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(weightEstimated, ColumnMetadata.named("weight_estimated").withIndex(25).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(weightMeasured, ColumnMetadata.named("weight_measured").withIndex(26).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

