package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQInvoicePaymentLine is a Querydsl query type for SQInvoicePaymentLine
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQInvoicePaymentLine extends RelationalPathSpatial<SQInvoicePaymentLine> {

    private static final long serialVersionUID = 1339920832;

    public static final SQInvoicePaymentLine invoicePaymentLine = new SQInvoicePaymentLine("invoice_payment_line");

    public final NumberPath<Long> accountTransferId = createNumber("accountTransferId", Long.class);

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> invoiceId = createNumber("invoiceId", Long.class);

    public final NumberPath<Long> invoicePaymentLineId = createNumber("invoicePaymentLineId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final DatePath<java.sql.Date> paymentDate = createDate("paymentDate", java.sql.Date.class);

    public final com.querydsl.sql.PrimaryKey<SQInvoicePaymentLine> invoicePaymentLinePkey = createPrimaryKey(invoicePaymentLineId);

    public final com.querydsl.sql.ForeignKey<SQInvoice> invoicePaymentLineInvoiceFk = createForeignKey(invoiceId, "invoice_id");

    public final com.querydsl.sql.ForeignKey<SQAccountTransfer> invoicePaymentLineAccountTransferFk = createForeignKey(accountTransferId, "account_transfer_id");

    public SQInvoicePaymentLine(String variable) {
        super(SQInvoicePaymentLine.class, forVariable(variable), "public", "invoice_payment_line");
        addMetadata();
    }

    public SQInvoicePaymentLine(String variable, String schema, String table) {
        super(SQInvoicePaymentLine.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQInvoicePaymentLine(String variable, String schema) {
        super(SQInvoicePaymentLine.class, forVariable(variable), schema, "invoice_payment_line");
        addMetadata();
    }

    public SQInvoicePaymentLine(Path<? extends SQInvoicePaymentLine> path) {
        super(path.getType(), path.getMetadata(), "public", "invoice_payment_line");
        addMetadata();
    }

    public SQInvoicePaymentLine(PathMetadata metadata) {
        super(SQInvoicePaymentLine.class, metadata, "public", "invoice_payment_line");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(accountTransferId, ColumnMetadata.named("account_transfer_id").withIndex(12).ofType(Types.BIGINT).withSize(19));
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(11).ofType(Types.NUMERIC).withSize(8).withDigits(2).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(invoiceId, ColumnMetadata.named("invoice_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(invoicePaymentLineId, ColumnMetadata.named("invoice_payment_line_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(paymentDate, ColumnMetadata.named("payment_date").withIndex(10).ofType(Types.DATE).withSize(13).notNull());
    }

}

