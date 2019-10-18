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
 * SQPermitHarvestInvoice is a Querydsl query type for SQPermitHarvestInvoice
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitHarvestInvoice extends RelationalPathSpatial<SQPermitHarvestInvoice> {

    private static final long serialVersionUID = 1923218386;

    public static final SQPermitHarvestInvoice permitHarvestInvoice = new SQPermitHarvestInvoice("permit_harvest_invoice");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> harvestPermitSpeciesAmountId = createNumber("harvestPermitSpeciesAmountId", Long.class);

    public final NumberPath<Long> invoiceId = createNumber("invoiceId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<java.math.BigDecimal> paymentAmount = createNumber("paymentAmount", java.math.BigDecimal.class);

    public final NumberPath<Long> permitHarvestInvoiceId = createNumber("permitHarvestInvoiceId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQPermitHarvestInvoice> permitHarvestInvoicePkey = createPrimaryKey(permitHarvestInvoiceId);

    public final com.querydsl.sql.ForeignKey<SQInvoice> permitHarvestInvoiceInvoiceFk = createForeignKey(invoiceId, "invoice_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitSpeciesAmount> permitHarvestInvoiceSpeciesAmountFk = createForeignKey(harvestPermitSpeciesAmountId, "harvest_permit_species_amount_id");

    public SQPermitHarvestInvoice(String variable) {
        super(SQPermitHarvestInvoice.class, forVariable(variable), "public", "permit_harvest_invoice");
        addMetadata();
    }

    public SQPermitHarvestInvoice(String variable, String schema, String table) {
        super(SQPermitHarvestInvoice.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitHarvestInvoice(String variable, String schema) {
        super(SQPermitHarvestInvoice.class, forVariable(variable), schema, "permit_harvest_invoice");
        addMetadata();
    }

    public SQPermitHarvestInvoice(Path<? extends SQPermitHarvestInvoice> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_harvest_invoice");
        addMetadata();
    }

    public SQPermitHarvestInvoice(PathMetadata metadata) {
        super(SQPermitHarvestInvoice.class, metadata, "public", "permit_harvest_invoice");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(harvestPermitSpeciesAmountId, ColumnMetadata.named("harvest_permit_species_amount_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(invoiceId, ColumnMetadata.named("invoice_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(paymentAmount, ColumnMetadata.named("payment_amount").withIndex(11).ofType(Types.NUMERIC).withSize(8).withDigits(2));
        addMetadata(permitHarvestInvoiceId, ColumnMetadata.named("permit_harvest_invoice_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

