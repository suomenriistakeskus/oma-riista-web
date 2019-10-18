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
 * SQHarvestPermitApplicationConflictBatch is a Querydsl query type for SQHarvestPermitApplicationConflictBatch
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitApplicationConflictBatch extends RelationalPathSpatial<SQHarvestPermitApplicationConflictBatch> {

    private static final long serialVersionUID = -810390231;

    public static final SQHarvestPermitApplicationConflictBatch harvestPermitApplicationConflictBatch = new SQHarvestPermitApplicationConflictBatch("harvest_permit_application_conflict_batch");

    public final DateTimePath<java.sql.Timestamp> completedAt = createDateTime("completedAt", java.sql.Timestamp.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> harvestPermitApplicationConflictBatchId = createNumber("harvestPermitApplicationConflictBatchId", Long.class);

    public final NumberPath<Integer> huntingYear = createNumber("huntingYear", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitApplicationConflictBatch> harvestPermitApplicationConflictBatchPkey = createPrimaryKey(harvestPermitApplicationConflictBatchId);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationConflict> _harvestPermitApplicationConflictBatchIdFk = createInvForeignKey(harvestPermitApplicationConflictBatchId, "batch_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationConflictPalsta> _harvestPermitApplicationConflictPalstaBatchIdFk = createInvForeignKey(harvestPermitApplicationConflictBatchId, "batch_id");

    public SQHarvestPermitApplicationConflictBatch(String variable) {
        super(SQHarvestPermitApplicationConflictBatch.class, forVariable(variable), "public", "harvest_permit_application_conflict_batch");
        addMetadata();
    }

    public SQHarvestPermitApplicationConflictBatch(String variable, String schema, String table) {
        super(SQHarvestPermitApplicationConflictBatch.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitApplicationConflictBatch(String variable, String schema) {
        super(SQHarvestPermitApplicationConflictBatch.class, forVariable(variable), schema, "harvest_permit_application_conflict_batch");
        addMetadata();
    }

    public SQHarvestPermitApplicationConflictBatch(Path<? extends SQHarvestPermitApplicationConflictBatch> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_application_conflict_batch");
        addMetadata();
    }

    public SQHarvestPermitApplicationConflictBatch(PathMetadata metadata) {
        super(SQHarvestPermitApplicationConflictBatch.class, metadata, "public", "harvest_permit_application_conflict_batch");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(completedAt, ColumnMetadata.named("completed_at").withIndex(10).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(harvestPermitApplicationConflictBatchId, ColumnMetadata.named("harvest_permit_application_conflict_batch_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(huntingYear, ColumnMetadata.named("hunting_year").withIndex(9).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
    }

}

