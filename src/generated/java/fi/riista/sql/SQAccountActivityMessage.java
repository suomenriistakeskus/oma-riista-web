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
 * SQAccountActivityMessage is a Querydsl query type for SQAccountActivityMessage
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQAccountActivityMessage extends RelationalPathSpatial<SQAccountActivityMessage> {

    private static final long serialVersionUID = 1450945182;

    public static final SQAccountActivityMessage accountActivityMessage = new SQAccountActivityMessage("account_activity_message");

    public final StringPath activityType = createString("activityType");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final StringPath exceptionMessage = createString("exceptionMessage");

    public final StringPath ipAddress = createString("ipAddress");

    public final StringPath message = createString("message");

    public final NumberPath<Long> messageId = createNumber("messageId", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final StringPath username = createString("username");

    public final com.querydsl.sql.PrimaryKey<SQAccountActivityMessage> accountActivityMessagePkey = createPrimaryKey(messageId);

    public SQAccountActivityMessage(String variable) {
        super(SQAccountActivityMessage.class, forVariable(variable), "public", "account_activity_message");
        addMetadata();
    }

    public SQAccountActivityMessage(String variable, String schema, String table) {
        super(SQAccountActivityMessage.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQAccountActivityMessage(String variable, String schema) {
        super(SQAccountActivityMessage.class, forVariable(variable), schema, "account_activity_message");
        addMetadata();
    }

    public SQAccountActivityMessage(Path<? extends SQAccountActivityMessage> path) {
        super(path.getType(), path.getMetadata(), "public", "account_activity_message");
        addMetadata();
    }

    public SQAccountActivityMessage(PathMetadata metadata) {
        super(SQAccountActivityMessage.class, metadata, "public", "account_activity_message");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(activityType, ColumnMetadata.named("activity_type").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(4).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(exceptionMessage, ColumnMetadata.named("exception_message").withIndex(9).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(ipAddress, ColumnMetadata.named("ip_address").withIndex(7).ofType(Types.VARCHAR).withSize(255));
        addMetadata(message, ColumnMetadata.named("message").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(messageId, ColumnMetadata.named("message_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(6).ofType(Types.BIGINT).withSize(19));
        addMetadata(username, ColumnMetadata.named("username").withIndex(5).ofType(Types.VARCHAR).withSize(255));
    }

}

