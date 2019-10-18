package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQPermitDecisionInvoiceBatch is a Querydsl query type for SQPermitDecisionInvoiceBatch
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionInvoiceBatch extends RelationalPathSpatial<SQPermitDecisionInvoiceBatch> {

    private static final long serialVersionUID = 1312629719;

    public static final SQPermitDecisionInvoiceBatch permitDecisionInvoiceBatch = new SQPermitDecisionInvoiceBatch("permit_decision_invoice_batch");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final BooleanPath downloaded = createBoolean("downloaded");

    public final StringPath fileMetadataId = createString("fileMetadataId");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> permitDecisionInvoiceBatchId = createNumber("permitDecisionInvoiceBatchId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionInvoiceBatch> permitDecisionInvoiceBatchPkey = createPrimaryKey(permitDecisionInvoiceBatchId);

    public final com.querydsl.sql.ForeignKey<SQFileMetadata> permitDecisionInvoiceBatchFileMetadataFk = createForeignKey(fileMetadataId, "file_metadata_uuid");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionInvoice> _permitDecisionInvoiceBatchFk = createInvForeignKey(permitDecisionInvoiceBatchId, "batch_id");

    public SQPermitDecisionInvoiceBatch(String variable) {
        super(SQPermitDecisionInvoiceBatch.class, forVariable(variable), "public", "permit_decision_invoice_batch");
        addMetadata();
    }

    public SQPermitDecisionInvoiceBatch(String variable, String schema, String table) {
        super(SQPermitDecisionInvoiceBatch.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionInvoiceBatch(String variable, String schema) {
        super(SQPermitDecisionInvoiceBatch.class, forVariable(variable), schema, "permit_decision_invoice_batch");
        addMetadata();
    }

    public SQPermitDecisionInvoiceBatch(Path<? extends SQPermitDecisionInvoiceBatch> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_invoice_batch");
        addMetadata();
    }

    public SQPermitDecisionInvoiceBatch(PathMetadata metadata) {
        super(SQPermitDecisionInvoiceBatch.class, metadata, "public", "permit_decision_invoice_batch");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(downloaded, ColumnMetadata.named("downloaded").withIndex(10).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(fileMetadataId, ColumnMetadata.named("file_metadata_id").withIndex(9).ofType(Types.CHAR).withSize(36).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitDecisionInvoiceBatchId, ColumnMetadata.named("permit_decision_invoice_batch_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

