package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
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
 * SQInvoiceReminder is a Querydsl query type for SQInvoiceReminder
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQInvoiceReminder extends RelationalPathSpatial<SQInvoiceReminder> {

    private static final long serialVersionUID = -913964148;

    public static final SQInvoiceReminder invoiceReminder = new SQInvoiceReminder("invoice_reminder");

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final StringPath creditorReference = createString("creditorReference");

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath fileMetadataId = createString("fileMetadataId");

    public final NumberPath<Long> invoiceReminderId = createNumber("invoiceReminderId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> originalInvoiceId = createNumber("originalInvoiceId", Long.class);

    public final DatePath<java.sql.Date> overdueDate = createDate("overdueDate", java.sql.Date.class);

    public final NumberPath<Long> recipientAddressId = createNumber("recipientAddressId", Long.class);

    public final StringPath recipientName = createString("recipientName");

    public final DatePath<java.sql.Date> reminderDate = createDate("reminderDate", java.sql.Date.class);

    public final com.querydsl.sql.PrimaryKey<SQInvoiceReminder> invoiceReminderPkey = createPrimaryKey(invoiceReminderId);

    public final com.querydsl.sql.ForeignKey<SQInvoice> invoiceReminderOriginalInvoiceFk = createForeignKey(originalInvoiceId, "invoice_id");

    public final com.querydsl.sql.ForeignKey<SQAddress> invoiceReminderRecipientAddressFk = createForeignKey(recipientAddressId, "address_id");

    public final com.querydsl.sql.ForeignKey<SQFileMetadata> invoiceReminderFileMetadataFk = createForeignKey(fileMetadataId, "file_metadata_uuid");

    public SQInvoiceReminder(String variable) {
        super(SQInvoiceReminder.class, forVariable(variable), "public", "invoice_reminder");
        addMetadata();
    }

    public SQInvoiceReminder(String variable, String schema, String table) {
        super(SQInvoiceReminder.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQInvoiceReminder(String variable, String schema) {
        super(SQInvoiceReminder.class, forVariable(variable), schema, "invoice_reminder");
        addMetadata();
    }

    public SQInvoiceReminder(Path<? extends SQInvoiceReminder> path) {
        super(path.getType(), path.getMetadata(), "public", "invoice_reminder");
        addMetadata();
    }

    public SQInvoiceReminder(PathMetadata metadata) {
        super(SQInvoiceReminder.class, metadata, "public", "invoice_reminder");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(12).ofType(Types.NUMERIC).withSize(6).withDigits(2).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(creditorReference, ColumnMetadata.named("creditor_reference").withIndex(13).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(fileMetadataId, ColumnMetadata.named("file_metadata_id").withIndex(16).ofType(Types.CHAR).withSize(36).notNull());
        addMetadata(invoiceReminderId, ColumnMetadata.named("invoice_reminder_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(originalInvoiceId, ColumnMetadata.named("original_invoice_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(overdueDate, ColumnMetadata.named("overdue_date").withIndex(11).ofType(Types.DATE).withSize(13));
        addMetadata(recipientAddressId, ColumnMetadata.named("recipient_address_id").withIndex(15).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(recipientName, ColumnMetadata.named("recipient_name").withIndex(14).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(reminderDate, ColumnMetadata.named("reminder_date").withIndex(10).ofType(Types.DATE).withSize(13).notNull());
    }

}

