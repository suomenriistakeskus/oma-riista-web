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
 * SQMailMessageBounce is a Querydsl query type for SQMailMessageBounce
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMailMessageBounce extends RelationalPathSpatial<SQMailMessageBounce> {

    private static final long serialVersionUID = -717748603;

    public static final SQMailMessageBounce mailMessageBounce = new SQMailMessageBounce("mail_message_bounce");

    public final StringPath bounceFeedbackId = createString("bounceFeedbackId");

    public final StringPath bounceSubType = createString("bounceSubType");

    public final DateTimePath<java.sql.Timestamp> bounceTimestamp = createDateTime("bounceTimestamp", java.sql.Timestamp.class);

    public final StringPath bounceType = createString("bounceType");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> mailMessageBounceId = createNumber("mailMessageBounceId", Long.class);

    public final StringPath mailMessageId = createString("mailMessageId");

    public final StringPath mailSubject = createString("mailSubject");

    public final DateTimePath<java.sql.Timestamp> mailTimestamp = createDateTime("mailTimestamp", java.sql.Timestamp.class);

    public final StringPath recipientAction = createString("recipientAction");

    public final StringPath recipientDiagnosticCode = createString("recipientDiagnosticCode");

    public final StringPath recipientEmailAddress = createString("recipientEmailAddress");

    public final StringPath recipientStatus = createString("recipientStatus");

    public final com.querydsl.sql.PrimaryKey<SQMailMessageBounce> mailMessageBouncePkey = createPrimaryKey(mailMessageBounceId);

    public SQMailMessageBounce(String variable) {
        super(SQMailMessageBounce.class, forVariable(variable), "public", "mail_message_bounce");
        addMetadata();
    }

    public SQMailMessageBounce(String variable, String schema, String table) {
        super(SQMailMessageBounce.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQMailMessageBounce(String variable, String schema) {
        super(SQMailMessageBounce.class, forVariable(variable), schema, "mail_message_bounce");
        addMetadata();
    }

    public SQMailMessageBounce(Path<? extends SQMailMessageBounce> path) {
        super(path.getType(), path.getMetadata(), "public", "mail_message_bounce");
        addMetadata();
    }

    public SQMailMessageBounce(PathMetadata metadata) {
        super(SQMailMessageBounce.class, metadata, "public", "mail_message_bounce");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(bounceFeedbackId, ColumnMetadata.named("bounce_feedback_id").withIndex(6).ofType(Types.VARCHAR).withSize(255));
        addMetadata(bounceSubType, ColumnMetadata.named("bounce_sub_type").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(bounceTimestamp, ColumnMetadata.named("bounce_timestamp").withIndex(5).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(bounceType, ColumnMetadata.named("bounce_type").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(mailMessageBounceId, ColumnMetadata.named("mail_message_bounce_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(mailMessageId, ColumnMetadata.named("mail_message_id").withIndex(12).ofType(Types.VARCHAR).withSize(255));
        addMetadata(mailSubject, ColumnMetadata.named("mail_subject").withIndex(13).ofType(Types.VARCHAR).withSize(255));
        addMetadata(mailTimestamp, ColumnMetadata.named("mail_timestamp").withIndex(11).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(recipientAction, ColumnMetadata.named("recipient_action").withIndex(8).ofType(Types.VARCHAR).withSize(255));
        addMetadata(recipientDiagnosticCode, ColumnMetadata.named("recipient_diagnostic_code").withIndex(10).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(recipientEmailAddress, ColumnMetadata.named("recipient_email_address").withIndex(7).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(recipientStatus, ColumnMetadata.named("recipient_status").withIndex(9).ofType(Types.VARCHAR).withSize(255));
    }

}

