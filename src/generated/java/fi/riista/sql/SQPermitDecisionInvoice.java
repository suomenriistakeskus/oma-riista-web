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
 * SQPermitDecisionInvoice is a Querydsl query type for SQPermitDecisionInvoice
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionInvoice extends RelationalPathSpatial<SQPermitDecisionInvoice> {

    private static final long serialVersionUID = -1455095517;

    public static final SQPermitDecisionInvoice permitDecisionInvoice = new SQPermitDecisionInvoice("permit_decision_invoice");

    public final NumberPath<Long> batchId = createNumber("batchId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> invoiceId = createNumber("invoiceId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> permitDecisionId = createNumber("permitDecisionId", Long.class);

    public final NumberPath<Long> permitDecisionInvoiceId = createNumber("permitDecisionInvoiceId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionInvoice> permitDecisionInvoicePkey = createPrimaryKey(permitDecisionInvoiceId);

    public final com.querydsl.sql.ForeignKey<SQInvoice> permitDecisionInvoiceInvoiceFk = createForeignKey(invoiceId, "invoice_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> permitDecisionInvoiceDecisionFk = createForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionInvoiceBatch> permitDecisionInvoiceBatchFk = createForeignKey(batchId, "permit_decision_invoice_batch_id");

    public SQPermitDecisionInvoice(String variable) {
        super(SQPermitDecisionInvoice.class, forVariable(variable), "public", "permit_decision_invoice");
        addMetadata();
    }

    public SQPermitDecisionInvoice(String variable, String schema, String table) {
        super(SQPermitDecisionInvoice.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionInvoice(String variable, String schema) {
        super(SQPermitDecisionInvoice.class, forVariable(variable), schema, "permit_decision_invoice");
        addMetadata();
    }

    public SQPermitDecisionInvoice(Path<? extends SQPermitDecisionInvoice> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_invoice");
        addMetadata();
    }

    public SQPermitDecisionInvoice(PathMetadata metadata) {
        super(SQPermitDecisionInvoice.class, metadata, "public", "permit_decision_invoice");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(batchId, ColumnMetadata.named("batch_id").withIndex(11).ofType(Types.BIGINT).withSize(19));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(invoiceId, ColumnMetadata.named("invoice_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitDecisionId, ColumnMetadata.named("permit_decision_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitDecisionInvoiceId, ColumnMetadata.named("permit_decision_invoice_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

