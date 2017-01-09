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
 * SQHarvestSpecimen is a Querydsl query type for SQHarvestSpecimen
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestSpecimen extends RelationalPathSpatial<SQHarvestSpecimen> {

    private static final long serialVersionUID = -1274686882;

    public static final SQHarvestSpecimen harvestSpecimen = new SQHarvestSpecimen("harvest_specimen");

    public final StringPath additionalInfo = createString("additionalInfo");

    public final StringPath age = createString("age");

    public final NumberPath<Integer> antlerPointsLeft = createNumber("antlerPointsLeft", Integer.class);

    public final NumberPath<Integer> antlerPointsRight = createNumber("antlerPointsRight", Integer.class);

    public final StringPath antlersType = createString("antlersType");

    public final NumberPath<Integer> antlersWidth = createNumber("antlersWidth", Integer.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath fitnessClass = createString("fitnessClass");

    public final StringPath gender = createString("gender");

    public final NumberPath<Long> harvestId = createNumber("harvestId", Long.class);

    public final NumberPath<Long> harvestSpecimenId = createNumber("harvestSpecimenId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final BooleanPath notEdible = createBoolean("notEdible");

    public final NumberPath<java.math.BigDecimal> weight = createNumber("weight", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> weightEstimated = createNumber("weightEstimated", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> weightMeasured = createNumber("weightMeasured", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestSpecimen> harvestSpecimenPkey = createPrimaryKey(harvestSpecimenId);

    public final com.querydsl.sql.ForeignKey<SQGameAge> harvestSpecimenAgeFk = createForeignKey(age, "name");

    public final com.querydsl.sql.ForeignKey<SQGameAntlersType> specimenAntlersTypeFk = createForeignKey(antlersType, "name");

    public final com.querydsl.sql.ForeignKey<SQGameGender> harvestSpecimenGenderFk = createForeignKey(gender, "name");

    public final com.querydsl.sql.ForeignKey<SQGameFitnessClass> specimenFitnessClassFk = createForeignKey(fitnessClass, "name");

    public final com.querydsl.sql.ForeignKey<SQHarvest> harvestSpecimenHarvestFk = createForeignKey(harvestId, "harvest_id");

    public SQHarvestSpecimen(String variable) {
        super(SQHarvestSpecimen.class, forVariable(variable), "public", "harvest_specimen");
        addMetadata();
    }

    public SQHarvestSpecimen(String variable, String schema, String table) {
        super(SQHarvestSpecimen.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestSpecimen(Path<? extends SQHarvestSpecimen> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_specimen");
        addMetadata();
    }

    public SQHarvestSpecimen(PathMetadata metadata) {
        super(SQHarvestSpecimen.class, metadata, "public", "harvest_specimen");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(additionalInfo, ColumnMetadata.named("additional_info").withIndex(20).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(age, ColumnMetadata.named("age").withIndex(11).ofType(Types.VARCHAR).withSize(255));
        addMetadata(antlerPointsLeft, ColumnMetadata.named("antler_points_left").withIndex(18).ofType(Types.INTEGER).withSize(10));
        addMetadata(antlerPointsRight, ColumnMetadata.named("antler_points_right").withIndex(19).ofType(Types.INTEGER).withSize(10));
        addMetadata(antlersType, ColumnMetadata.named("antlers_type").withIndex(16).ofType(Types.VARCHAR).withSize(255));
        addMetadata(antlersWidth, ColumnMetadata.named("antlers_width").withIndex(17).ofType(Types.INTEGER).withSize(10));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(fitnessClass, ColumnMetadata.named("fitness_class").withIndex(15).ofType(Types.VARCHAR).withSize(255));
        addMetadata(gender, ColumnMetadata.named("gender").withIndex(10).ofType(Types.VARCHAR).withSize(255));
        addMetadata(harvestId, ColumnMetadata.named("harvest_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestSpecimenId, ColumnMetadata.named("harvest_specimen_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(notEdible, ColumnMetadata.named("not_edible").withIndex(21).ofType(Types.BIT).withSize(1));
        addMetadata(weight, ColumnMetadata.named("weight").withIndex(12).ofType(Types.NUMERIC).withSize(4).withDigits(1));
        addMetadata(weightEstimated, ColumnMetadata.named("weight_estimated").withIndex(13).ofType(Types.NUMERIC).withSize(4).withDigits(1));
        addMetadata(weightMeasured, ColumnMetadata.named("weight_measured").withIndex(14).ofType(Types.NUMERIC).withSize(4).withDigits(1));
    }

}

