package fi.riista.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import com.querydsl.sql.spatial.RelationalPathSpatial;

import com.querydsl.spatial.*;



/**
 * SQSmsMessage is a Querydsl query type for SQSmsMessage
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQSmsMessage extends RelationalPathSpatial<SQSmsMessage> {

    private static final long serialVersionUID = 817803137;

    public static final SQSmsMessage smsMessage = new SQSmsMessage("sms_message");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath direction = createString("direction");

    public final StringPath message = createString("message");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final StringPath numberFrom = createString("numberFrom");

    public final StringPath numberTo = createString("numberTo");

    public final NumberPath<Long> smsMessageId = createNumber("smsMessageId", Long.class);

    public final StringPath statusCode = createString("statusCode");

    public final StringPath statusMessage = createString("statusMessage");

    public final DateTimePath<java.sql.Timestamp> statusTimestamp = createDateTime("statusTimestamp", java.sql.Timestamp.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQSmsMessage> smsMessagePkey = createPrimaryKey(smsMessageId);

    public final com.querydsl.sql.ForeignKey<SQSystemUser> smsMessageSystemUserFk = createForeignKey(userId, "user_id");

    public SQSmsMessage(String variable) {
        super(SQSmsMessage.class, forVariable(variable), "public", "sms_message");
        addMetadata();
    }

    public SQSmsMessage(String variable, String schema, String table) {
        super(SQSmsMessage.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQSmsMessage(String variable, String schema) {
        super(SQSmsMessage.class, forVariable(variable), schema, "sms_message");
        addMetadata();
    }

    public SQSmsMessage(Path<? extends SQSmsMessage> path) {
        super(path.getType(), path.getMetadata(), "public", "sms_message");
        addMetadata();
    }

    public SQSmsMessage(PathMetadata metadata) {
        super(SQSmsMessage.class, metadata, "public", "sms_message");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(3).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(5).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(direction, ColumnMetadata.named("direction").withIndex(7).ofType(Types.VARCHAR).withSize(16).notNull());
        addMetadata(message, ColumnMetadata.named("message").withIndex(13).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(4).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(numberFrom, ColumnMetadata.named("number_from").withIndex(11).ofType(Types.VARCHAR).withSize(35));
        addMetadata(numberTo, ColumnMetadata.named("number_to").withIndex(12).ofType(Types.VARCHAR).withSize(35));
        addMetadata(smsMessageId, ColumnMetadata.named("sms_message_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(statusCode, ColumnMetadata.named("status_code").withIndex(8).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(statusMessage, ColumnMetadata.named("status_message").withIndex(10).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(statusTimestamp, ColumnMetadata.named("status_timestamp").withIndex(9).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(6).ofType(Types.BIGINT).withSize(19));
    }

}

