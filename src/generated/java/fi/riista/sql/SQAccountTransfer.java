package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQAccountTransfer is a Querydsl query type for SQAccountTransfer
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQAccountTransfer extends RelationalPathSpatial<SQAccountTransfer> {

    private static final long serialVersionUID = 663634437;

    public static final SQAccountTransfer accountTransfer = new SQAccountTransfer("account_transfer");

    public final StringPath accountServiceReference = createString("accountServiceReference");

    public final NumberPath<Long> accountTransferId = createNumber("accountTransferId", Long.class);

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final NumberPath<Long> batchId = createNumber("batchId", Long.class);

    public final DatePath<java.sql.Date> bookingDate = createDate("bookingDate", java.sql.Date.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final StringPath creditorIban = createString("creditorIban");

    public final StringPath creditorReference = createString("creditorReference");

    public final StringPath debtorName = createString("debtorName");

    public final DatePath<java.sql.Date> transactionDate = createDate("transactionDate", java.sql.Date.class);

    public final com.querydsl.sql.PrimaryKey<SQAccountTransfer> accountTransferPkey = createPrimaryKey(accountTransferId);

    public final com.querydsl.sql.ForeignKey<SQAccountTransferBatch> accountTransferBatchFk = createForeignKey(batchId, "account_transfer_batch_id");

    public final com.querydsl.sql.ForeignKey<SQInvoicePaymentLine> _invoicePaymentLineAccountTransferFk = createInvForeignKey(accountTransferId, "account_transfer_id");

    public SQAccountTransfer(String variable) {
        super(SQAccountTransfer.class, forVariable(variable), "public", "account_transfer");
        addMetadata();
    }

    public SQAccountTransfer(String variable, String schema, String table) {
        super(SQAccountTransfer.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQAccountTransfer(String variable, String schema) {
        super(SQAccountTransfer.class, forVariable(variable), schema, "account_transfer");
        addMetadata();
    }

    public SQAccountTransfer(Path<? extends SQAccountTransfer> path) {
        super(path.getType(), path.getMetadata(), "public", "account_transfer");
        addMetadata();
    }

    public SQAccountTransfer(PathMetadata metadata) {
        super(SQAccountTransfer.class, metadata, "public", "account_transfer");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(accountServiceReference, ColumnMetadata.named("account_service_reference").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(accountTransferId, ColumnMetadata.named("account_transfer_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(8).ofType(Types.NUMERIC).withSize(8).withDigits(2).notNull());
        addMetadata(batchId, ColumnMetadata.named("batch_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(bookingDate, ColumnMetadata.named("booking_date").withIndex(6).ofType(Types.DATE).withSize(13).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(creditorIban, ColumnMetadata.named("creditor_iban").withIndex(4).ofType(Types.CHAR).withSize(18).notNull());
        addMetadata(creditorReference, ColumnMetadata.named("creditor_reference").withIndex(9).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(debtorName, ColumnMetadata.named("debtor_name").withIndex(7).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(transactionDate, ColumnMetadata.named("transaction_date").withIndex(5).ofType(Types.DATE).withSize(13).notNull());
    }

}

