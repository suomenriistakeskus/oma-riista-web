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
 * SQInvoice is a Querydsl query type for SQInvoice
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQInvoice extends RelationalPathSpatial<SQInvoice> {

    private static final long serialVersionUID = 1035499770;

    public static final SQInvoice invoice = new SQInvoice("invoice");

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final StringPath bic = createString("bic");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<java.math.BigDecimal> correctedAmount = createNumber("correctedAmount", java.math.BigDecimal.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final StringPath creditorReference = createString("creditorReference");

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DatePath<java.sql.Date> dueDate = createDate("dueDate", java.sql.Date.class);

    public final BooleanPath electronicInvoicingEnabled = createBoolean("electronicInvoicingEnabled");

    public final StringPath fileMetadataId = createString("fileMetadataId");

    public final StringPath iban = createString("iban");

    public final DatePath<java.sql.Date> invoiceDate = createDate("invoiceDate", java.sql.Date.class);

    public final NumberPath<Long> invoiceId = createNumber("invoiceId", Long.class);

    public final NumberPath<Integer> invoiceNumber = createNumber("invoiceNumber", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final BooleanPath originalDeliveryByEmail = createBoolean("originalDeliveryByEmail");

    public final DatePath<java.sql.Date> paymentDate = createDate("paymentDate", java.sql.Date.class);

    public final StringPath paytrailPaymentId = createString("paytrailPaymentId");

    public final StringPath paytrailSettlementReferenceNumber = createString("paytrailSettlementReferenceNumber");

    public final NumberPath<java.math.BigDecimal> receivedAmount = createNumber("receivedAmount", java.math.BigDecimal.class);

    public final NumberPath<Long> recipientAddressId = createNumber("recipientAddressId", Long.class);

    public final StringPath recipientName = createString("recipientName");

    public final StringPath state = createString("state");

    public final StringPath type = createString("type");

    public final com.querydsl.sql.PrimaryKey<SQInvoice> invoicePkey = createPrimaryKey(invoiceId);

    public final com.querydsl.sql.ForeignKey<SQFileMetadata> invoiceFileMetadataFk = createForeignKey(fileMetadataId, "file_metadata_uuid");

    public final com.querydsl.sql.ForeignKey<SQAddress> invoiceRecipientAddressFk = createForeignKey(recipientAddressId, "address_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionInvoice> _permitDecisionInvoiceInvoiceFk = createInvForeignKey(invoiceId, "invoice_id");

    public final com.querydsl.sql.ForeignKey<SQPermitHarvestInvoice> _permitHarvestInvoiceInvoiceFk = createInvForeignKey(invoiceId, "invoice_id");

    public final com.querydsl.sql.ForeignKey<SQInvoicePaymentLine> _invoicePaymentLineInvoiceFk = createInvForeignKey(invoiceId, "invoice_id");

    public final com.querydsl.sql.ForeignKey<SQInvoiceStateChangeEvent> _invoiceStateChangeEventInvoiceFk = createInvForeignKey(invoiceId, "invoice_id");

    public SQInvoice(String variable) {
        super(SQInvoice.class, forVariable(variable), "public", "invoice");
        addMetadata();
    }

    public SQInvoice(String variable, String schema, String table) {
        super(SQInvoice.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQInvoice(String variable, String schema) {
        super(SQInvoice.class, forVariable(variable), schema, "invoice");
        addMetadata();
    }

    public SQInvoice(Path<? extends SQInvoice> path) {
        super(path.getType(), path.getMetadata(), "public", "invoice");
        addMetadata();
    }

    public SQInvoice(PathMetadata metadata) {
        super(SQInvoice.class, metadata, "public", "invoice");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(15).ofType(Types.NUMERIC).withSize(8).withDigits(2).notNull());
        addMetadata(bic, ColumnMetadata.named("bic").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(correctedAmount, ColumnMetadata.named("corrected_amount").withIndex(27).ofType(Types.NUMERIC).withSize(8).withDigits(2));
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(creditorReference, ColumnMetadata.named("creditor_reference").withIndex(16).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(dueDate, ColumnMetadata.named("due_date").withIndex(14).ofType(Types.DATE).withSize(13).notNull());
        addMetadata(electronicInvoicingEnabled, ColumnMetadata.named("electronic_invoicing_enabled").withIndex(19).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(fileMetadataId, ColumnMetadata.named("file_metadata_id").withIndex(20).ofType(Types.CHAR).withSize(36).notNull());
        addMetadata(iban, ColumnMetadata.named("iban").withIndex(12).ofType(Types.CHAR).withSize(18).notNull());
        addMetadata(invoiceDate, ColumnMetadata.named("invoice_date").withIndex(13).ofType(Types.DATE).withSize(13).notNull());
        addMetadata(invoiceId, ColumnMetadata.named("invoice_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(invoiceNumber, ColumnMetadata.named("invoice_number").withIndex(10).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(originalDeliveryByEmail, ColumnMetadata.named("original_delivery_by_email").withIndex(25).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(paymentDate, ColumnMetadata.named("payment_date").withIndex(22).ofType(Types.DATE).withSize(13));
        addMetadata(paytrailPaymentId, ColumnMetadata.named("paytrail_payment_id").withIndex(23).ofType(Types.VARCHAR).withSize(255));
        addMetadata(paytrailSettlementReferenceNumber, ColumnMetadata.named("paytrail_settlement_reference_number").withIndex(24).ofType(Types.VARCHAR).withSize(255));
        addMetadata(receivedAmount, ColumnMetadata.named("received_amount").withIndex(26).ofType(Types.NUMERIC).withSize(8).withDigits(2));
        addMetadata(recipientAddressId, ColumnMetadata.named("recipient_address_id").withIndex(18).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(recipientName, ColumnMetadata.named("recipient_name").withIndex(17).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(state, ColumnMetadata.named("state").withIndex(21).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(type, ColumnMetadata.named("type").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

