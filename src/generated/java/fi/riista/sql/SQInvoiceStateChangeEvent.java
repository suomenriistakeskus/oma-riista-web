package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQInvoiceStateChangeEvent is a Querydsl query type for SQInvoiceStateChangeEvent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQInvoiceStateChangeEvent extends RelationalPathSpatial<SQInvoiceStateChangeEvent> {

    private static final long serialVersionUID = -1103113069;

    public static final SQInvoiceStateChangeEvent invoiceStateChangeEvent = new SQInvoiceStateChangeEvent("invoice_state_change_event");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final DateTimePath<java.sql.Timestamp> eventTime = createDateTime("eventTime", java.sql.Timestamp.class);

    public final NumberPath<Long> invoiceId = createNumber("invoiceId", Long.class);

    public final NumberPath<Long> invoiceStateChangeEventId = createNumber("invoiceStateChangeEventId", Long.class);

    public final StringPath type = createString("type");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQInvoiceStateChangeEvent> invoiceStateChangeEventPkey = createPrimaryKey(invoiceStateChangeEventId);

    public final com.querydsl.sql.ForeignKey<SQInvoice> invoiceStateChangeEventInvoiceFk = createForeignKey(invoiceId, "invoice_id");

    public SQInvoiceStateChangeEvent(String variable) {
        super(SQInvoiceStateChangeEvent.class, forVariable(variable), "public", "invoice_state_change_event");
        addMetadata();
    }

    public SQInvoiceStateChangeEvent(String variable, String schema, String table) {
        super(SQInvoiceStateChangeEvent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQInvoiceStateChangeEvent(String variable, String schema) {
        super(SQInvoiceStateChangeEvent.class, forVariable(variable), schema, "invoice_state_change_event");
        addMetadata();
    }

    public SQInvoiceStateChangeEvent(Path<? extends SQInvoiceStateChangeEvent> path) {
        super(path.getType(), path.getMetadata(), "public", "invoice_state_change_event");
        addMetadata();
    }

    public SQInvoiceStateChangeEvent(PathMetadata metadata) {
        super(SQInvoiceStateChangeEvent.class, metadata, "public", "invoice_state_change_event");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(eventTime, ColumnMetadata.named("event_time").withIndex(5).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(invoiceId, ColumnMetadata.named("invoice_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(invoiceStateChangeEventId, ColumnMetadata.named("invoice_state_change_event_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(type, ColumnMetadata.named("type").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(6).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

