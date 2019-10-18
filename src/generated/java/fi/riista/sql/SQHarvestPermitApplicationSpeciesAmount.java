package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQHarvestPermitApplicationSpeciesAmount is a Querydsl query type for SQHarvestPermitApplicationSpeciesAmount
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitApplicationSpeciesAmount extends RelationalPathSpatial<SQHarvestPermitApplicationSpeciesAmount> {

    private static final long serialVersionUID = 1142734677;

    public static final SQHarvestPermitApplicationSpeciesAmount harvestPermitApplicationSpeciesAmount = new SQHarvestPermitApplicationSpeciesAmount("harvest_permit_application_species_amount");

    public final StringPath additionalPeriodInfo = createString("additionalPeriodInfo");

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final DatePath<java.sql.Date> beginDate = createDate("beginDate", java.sql.Date.class);

    public final NumberPath<Integer> causedDamageAmount = createNumber("causedDamageAmount", Integer.class);

    public final StringPath causedDamageDescription = createString("causedDamageDescription");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DatePath<java.sql.Date> endDate = createDate("endDate", java.sql.Date.class);

    public final StringPath evictionMeasureDescription = createString("evictionMeasureDescription");

    public final StringPath evictionMeasureEffect = createString("evictionMeasureEffect");

    public final StringPath forbiddenMethodJustification = createString("forbiddenMethodJustification");

    public final BooleanPath forbiddenMethodsUsed = createBoolean("forbiddenMethodsUsed");

    public final NumberPath<Long> gameSpeciesId = createNumber("gameSpeciesId", Long.class);

    public final NumberPath<Long> harvestPermitApplicationId = createNumber("harvestPermitApplicationId", Long.class);

    public final NumberPath<Long> harvestPermitApplicationSpeciesAmountId = createNumber("harvestPermitApplicationSpeciesAmountId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath mooselikeDescription = createString("mooselikeDescription");

    public final StringPath populationAmount = createString("populationAmount");

    public final StringPath populationDescription = createString("populationDescription");

    public final NumberPath<Integer> validityYears = createNumber("validityYears", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitApplicationSpeciesAmount> harvestPermitApplicationSpeciesAmountPkey = createPrimaryKey(harvestPermitApplicationSpeciesAmountId);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> harvestPermitApplicationSpeciesAmountApplicationFk = createForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public final com.querydsl.sql.ForeignKey<SQGameSpecies> harvestPermitApplicationSpeciesAmountSpeciesFk = createForeignKey(gameSpeciesId, "game_species_id");

    public SQHarvestPermitApplicationSpeciesAmount(String variable) {
        super(SQHarvestPermitApplicationSpeciesAmount.class, forVariable(variable), "public", "harvest_permit_application_species_amount");
        addMetadata();
    }

    public SQHarvestPermitApplicationSpeciesAmount(String variable, String schema, String table) {
        super(SQHarvestPermitApplicationSpeciesAmount.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitApplicationSpeciesAmount(String variable, String schema) {
        super(SQHarvestPermitApplicationSpeciesAmount.class, forVariable(variable), schema, "harvest_permit_application_species_amount");
        addMetadata();
    }

    public SQHarvestPermitApplicationSpeciesAmount(Path<? extends SQHarvestPermitApplicationSpeciesAmount> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_application_species_amount");
        addMetadata();
    }

    public SQHarvestPermitApplicationSpeciesAmount(PathMetadata metadata) {
        super(SQHarvestPermitApplicationSpeciesAmount.class, metadata, "public", "harvest_permit_application_species_amount");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(additionalPeriodInfo, ColumnMetadata.named("additional_period_info").withIndex(16).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(11).ofType(Types.NUMERIC).withSize(6).withDigits(1).notNull());
        addMetadata(beginDate, ColumnMetadata.named("begin_date").withIndex(13).ofType(Types.DATE).withSize(13));
        addMetadata(causedDamageAmount, ColumnMetadata.named("caused_damage_amount").withIndex(17).ofType(Types.INTEGER).withSize(10));
        addMetadata(causedDamageDescription, ColumnMetadata.named("caused_damage_description").withIndex(18).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(endDate, ColumnMetadata.named("end_date").withIndex(14).ofType(Types.DATE).withSize(13));
        addMetadata(evictionMeasureDescription, ColumnMetadata.named("eviction_measure_description").withIndex(19).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(evictionMeasureEffect, ColumnMetadata.named("eviction_measure_effect").withIndex(20).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(forbiddenMethodJustification, ColumnMetadata.named("forbidden_method_justification").withIndex(23).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(forbiddenMethodsUsed, ColumnMetadata.named("forbidden_methods_used").withIndex(24).ofType(Types.BIT).withSize(1));
        addMetadata(gameSpeciesId, ColumnMetadata.named("game_species_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitApplicationId, ColumnMetadata.named("harvest_permit_application_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitApplicationSpeciesAmountId, ColumnMetadata.named("harvest_permit_application_species_amount_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(mooselikeDescription, ColumnMetadata.named("mooselike_description").withIndex(12).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(populationAmount, ColumnMetadata.named("population_amount").withIndex(21).ofType(Types.VARCHAR).withSize(255));
        addMetadata(populationDescription, ColumnMetadata.named("population_description").withIndex(22).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(validityYears, ColumnMetadata.named("validity_years").withIndex(15).ofType(Types.INTEGER).withSize(10));
    }

}

