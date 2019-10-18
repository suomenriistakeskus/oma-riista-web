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
 * SQPaytrailPaymentEvent is a Querydsl query type for SQPaytrailPaymentEvent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPaytrailPaymentEvent extends RelationalPathSpatial<SQPaytrailPaymentEvent> {

    private static final long serialVersionUID = 1729438981;

    public static final SQPaytrailPaymentEvent paytrailPaymentEvent = new SQPaytrailPaymentEvent("paytrail_payment_event");

    public final StringPath amount = createString("amount");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final StringPath currency = createString("currency");

    public final DateTimePath<java.sql.Timestamp> eventTime = createDateTime("eventTime", java.sql.Timestamp.class);

    public final StringPath eventType = createString("eventType");

    public final StringPath orderNumber = createString("orderNumber");

    public final StringPath paymentId = createString("paymentId");

    public final StringPath paymentMethod = createString("paymentMethod");

    public final NumberPath<Long> paytrailPaymentEventId = createNumber("paytrailPaymentEventId", Long.class);

    public final StringPath remoteAddress = createString("remoteAddress");

    public final StringPath settlementReferenceNumber = createString("settlementReferenceNumber");

    public final StringPath status = createString("status");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQPaytrailPaymentEvent> paytrailPaymentEventPkey = createPrimaryKey(paytrailPaymentEventId);

    public SQPaytrailPaymentEvent(String variable) {
        super(SQPaytrailPaymentEvent.class, forVariable(variable), "public", "paytrail_payment_event");
        addMetadata();
    }

    public SQPaytrailPaymentEvent(String variable, String schema, String table) {
        super(SQPaytrailPaymentEvent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPaytrailPaymentEvent(String variable, String schema) {
        super(SQPaytrailPaymentEvent.class, forVariable(variable), schema, "paytrail_payment_event");
        addMetadata();
    }

    public SQPaytrailPaymentEvent(Path<? extends SQPaytrailPaymentEvent> path) {
        super(path.getType(), path.getMetadata(), "public", "paytrail_payment_event");
        addMetadata();
    }

    public SQPaytrailPaymentEvent(PathMetadata metadata) {
        super(SQPaytrailPaymentEvent.class, metadata, "public", "paytrail_payment_event");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(11).ofType(Types.VARCHAR).withSize(255));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(currency, ColumnMetadata.named("currency").withIndex(10).ofType(Types.VARCHAR).withSize(255));
        addMetadata(eventTime, ColumnMetadata.named("event_time").withIndex(4).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(eventType, ColumnMetadata.named("event_type").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(orderNumber, ColumnMetadata.named("order_number").withIndex(7).ofType(Types.VARCHAR).withSize(255));
        addMetadata(paymentId, ColumnMetadata.named("payment_id").withIndex(8).ofType(Types.VARCHAR).withSize(255));
        addMetadata(paymentMethod, ColumnMetadata.named("payment_method").withIndex(12).ofType(Types.VARCHAR).withSize(255));
        addMetadata(paytrailPaymentEventId, ColumnMetadata.named("paytrail_payment_event_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(remoteAddress, ColumnMetadata.named("remote_address").withIndex(5).ofType(Types.VARCHAR).withSize(255));
        addMetadata(settlementReferenceNumber, ColumnMetadata.named("settlement_reference_number").withIndex(13).ofType(Types.VARCHAR).withSize(255));
        addMetadata(status, ColumnMetadata.named("status").withIndex(9).ofType(Types.VARCHAR).withSize(255));
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(6).ofType(Types.BIGINT).withSize(19));
    }

}

