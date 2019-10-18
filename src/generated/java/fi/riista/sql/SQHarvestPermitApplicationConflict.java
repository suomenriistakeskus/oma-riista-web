package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;



/**
 * SQHarvestPermitApplicationConflict is a Querydsl query type for SQHarvestPermitApplicationConflict
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitApplicationConflict extends RelationalPathSpatial<SQHarvestPermitApplicationConflict> {

    private static final long serialVersionUID = -1082676079;

    public static final SQHarvestPermitApplicationConflict harvestPermitApplicationConflict = new SQHarvestPermitApplicationConflict("harvest_permit_application_conflict");

    public final NumberPath<Long> batchId = createNumber("batchId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> firstApplicationId = createNumber("firstApplicationId", Long.class);

    public final NumberPath<Long> harvestPermitApplicationConflictId = createNumber("harvestPermitApplicationConflictId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> processingPalstaSeconds = createNumber("processingPalstaSeconds", Long.class);

    public final NumberPath<Long> processingSeconds = createNumber("processingSeconds", Long.class);

    public final NumberPath<Long> secondApplicationId = createNumber("secondApplicationId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitApplicationConflict> harvestPermitApplicationConflictPkey = createPrimaryKey(harvestPermitApplicationConflictId);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> harvestPermitApplicationConflictSecondFk = createForeignKey(secondApplicationId, "harvest_permit_application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> harvestPermitApplicationConflictFirstFk = createForeignKey(firstApplicationId, "harvest_permit_application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationConflictBatch> harvestPermitApplicationConflictBatchIdFk = createForeignKey(batchId, "harvest_permit_application_conflict_batch_id");

    public SQHarvestPermitApplicationConflict(String variable) {
        super(SQHarvestPermitApplicationConflict.class, forVariable(variable), "public", "harvest_permit_application_conflict");
        addMetadata();
    }

    public SQHarvestPermitApplicationConflict(String variable, String schema, String table) {
        super(SQHarvestPermitApplicationConflict.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitApplicationConflict(String variable, String schema) {
        super(SQHarvestPermitApplicationConflict.class, forVariable(variable), schema, "harvest_permit_application_conflict");
        addMetadata();
    }

    public SQHarvestPermitApplicationConflict(Path<? extends SQHarvestPermitApplicationConflict> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_application_conflict");
        addMetadata();
    }

    public SQHarvestPermitApplicationConflict(PathMetadata metadata) {
        super(SQHarvestPermitApplicationConflict.class, metadata, "public", "harvest_permit_application_conflict");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(batchId, ColumnMetadata.named("batch_id").withIndex(13).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(firstApplicationId, ColumnMetadata.named("first_application_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitApplicationConflictId, ColumnMetadata.named("harvest_permit_application_conflict_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(processingPalstaSeconds, ColumnMetadata.named("processing_palsta_seconds").withIndex(12).ofType(Types.BIGINT).withSize(19));
        addMetadata(processingSeconds, ColumnMetadata.named("processing_seconds").withIndex(11).ofType(Types.BIGINT).withSize(19));
        addMetadata(secondApplicationId, ColumnMetadata.named("second_application_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

