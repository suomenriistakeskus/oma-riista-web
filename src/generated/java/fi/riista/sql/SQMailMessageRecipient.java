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
 * SQMailMessageRecipient is a Querydsl query type for SQMailMessageRecipient
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMailMessageRecipient extends RelationalPathSpatial<SQMailMessageRecipient> {

    private static final long serialVersionUID = -130284292;

    public static final SQMailMessageRecipient mailMessageRecipient = new SQMailMessageRecipient("mail_message_recipient");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final DateTimePath<java.sql.Timestamp> deliveryTime = createDateTime("deliveryTime", java.sql.Timestamp.class);

    public final StringPath email = createString("email");

    public final NumberPath<Integer> failureCounter = createNumber("failureCounter", Integer.class);

    public final NumberPath<Long> mailMessageId = createNumber("mailMessageId", Long.class);

    public final NumberPath<Long> mailMessageRecipientId = createNumber("mailMessageRecipientId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQMailMessageRecipient> mailMessageRecipientPkey = createPrimaryKey(mailMessageRecipientId);

    public final com.querydsl.sql.ForeignKey<SQMailMessage> mailMessageRecipientMailMessageFk = createForeignKey(mailMessageId, "mail_message_id");

    public SQMailMessageRecipient(String variable) {
        super(SQMailMessageRecipient.class, forVariable(variable), "public", "mail_message_recipient");
        addMetadata();
    }

    public SQMailMessageRecipient(String variable, String schema, String table) {
        super(SQMailMessageRecipient.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQMailMessageRecipient(String variable, String schema) {
        super(SQMailMessageRecipient.class, forVariable(variable), schema, "mail_message_recipient");
        addMetadata();
    }

    public SQMailMessageRecipient(Path<? extends SQMailMessageRecipient> path) {
        super(path.getType(), path.getMetadata(), "public", "mail_message_recipient");
        addMetadata();
    }

    public SQMailMessageRecipient(PathMetadata metadata) {
        super(SQMailMessageRecipient.class, metadata, "public", "mail_message_recipient");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(deliveryTime, ColumnMetadata.named("delivery_time").withIndex(5).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(email, ColumnMetadata.named("email").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(failureCounter, ColumnMetadata.named("failure_counter").withIndex(6).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(mailMessageId, ColumnMetadata.named("mail_message_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(mailMessageRecipientId, ColumnMetadata.named("mail_message_recipient_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

