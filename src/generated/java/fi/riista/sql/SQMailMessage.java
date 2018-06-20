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
 * SQMailMessage is a Querydsl query type for SQMailMessage
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMailMessage extends RelationalPathSpatial<SQMailMessage> {

    private static final long serialVersionUID = -1301246563;

    public static final SQMailMessage mailMessage = new SQMailMessage("mail_message");

    public final StringPath body = createString("body");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final BooleanPath delivered = createBoolean("delivered");

    public final StringPath fromEmail = createString("fromEmail");

    public final NumberPath<Long> mailMessageId = createNumber("mailMessageId", Long.class);

    public final DateTimePath<java.sql.Timestamp> scheduledTime = createDateTime("scheduledTime", java.sql.Timestamp.class);

    public final StringPath subject = createString("subject");

    public final DateTimePath<java.sql.Timestamp> submitTime = createDateTime("submitTime", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<SQMailMessage> mailMessagePkey = createPrimaryKey(mailMessageId);

    public final com.querydsl.sql.ForeignKey<SQMailMessageRecipient> _mailMessageRecipientMailMessageFk = createInvForeignKey(mailMessageId, "mail_message_id");

    public SQMailMessage(String variable) {
        super(SQMailMessage.class, forVariable(variable), "public", "mail_message");
        addMetadata();
    }

    public SQMailMessage(String variable, String schema, String table) {
        super(SQMailMessage.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQMailMessage(String variable, String schema) {
        super(SQMailMessage.class, forVariable(variable), schema, "mail_message");
        addMetadata();
    }

    public SQMailMessage(Path<? extends SQMailMessage> path) {
        super(path.getType(), path.getMetadata(), "public", "mail_message");
        addMetadata();
    }

    public SQMailMessage(PathMetadata metadata) {
        super(SQMailMessage.class, metadata, "public", "mail_message");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(body, ColumnMetadata.named("body").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(delivered, ColumnMetadata.named("delivered").withIndex(6).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(fromEmail, ColumnMetadata.named("from_email").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(mailMessageId, ColumnMetadata.named("mail_message_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(scheduledTime, ColumnMetadata.named("scheduled_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(subject, ColumnMetadata.named("subject").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(submitTime, ColumnMetadata.named("submit_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
    }

}

