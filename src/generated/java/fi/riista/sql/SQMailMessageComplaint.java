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
 * SQMailMessageComplaint is a Querydsl query type for SQMailMessageComplaint
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMailMessageComplaint extends RelationalPathSpatial<SQMailMessageComplaint> {

    private static final long serialVersionUID = 1941540750;

    public static final SQMailMessageComplaint mailMessageComplaint = new SQMailMessageComplaint("mail_message_complaint");

    public final StringPath complaintFeedbackId = createString("complaintFeedbackId");

    public final DateTimePath<java.sql.Timestamp> complaintTimestamp = createDateTime("complaintTimestamp", java.sql.Timestamp.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> mailMessageComplaintId = createNumber("mailMessageComplaintId", Long.class);

    public final StringPath mailMessageId = createString("mailMessageId");

    public final StringPath mailSubject = createString("mailSubject");

    public final DateTimePath<java.sql.Timestamp> mailTimestamp = createDateTime("mailTimestamp", java.sql.Timestamp.class);

    public final StringPath recipientAction = createString("recipientAction");

    public final StringPath recipientDiagnosticCode = createString("recipientDiagnosticCode");

    public final StringPath recipientEmailAddress = createString("recipientEmailAddress");

    public final StringPath recipientStatus = createString("recipientStatus");

    public final com.querydsl.sql.PrimaryKey<SQMailMessageComplaint> mailMessageComplaintPkey = createPrimaryKey(mailMessageComplaintId);

    public SQMailMessageComplaint(String variable) {
        super(SQMailMessageComplaint.class, forVariable(variable), "public", "mail_message_complaint");
        addMetadata();
    }

    public SQMailMessageComplaint(String variable, String schema, String table) {
        super(SQMailMessageComplaint.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQMailMessageComplaint(String variable, String schema) {
        super(SQMailMessageComplaint.class, forVariable(variable), schema, "mail_message_complaint");
        addMetadata();
    }

    public SQMailMessageComplaint(Path<? extends SQMailMessageComplaint> path) {
        super(path.getType(), path.getMetadata(), "public", "mail_message_complaint");
        addMetadata();
    }

    public SQMailMessageComplaint(PathMetadata metadata) {
        super(SQMailMessageComplaint.class, metadata, "public", "mail_message_complaint");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(complaintFeedbackId, ColumnMetadata.named("complaint_feedback_id").withIndex(4).ofType(Types.VARCHAR).withSize(255));
        addMetadata(complaintTimestamp, ColumnMetadata.named("complaint_timestamp").withIndex(3).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(mailMessageComplaintId, ColumnMetadata.named("mail_message_complaint_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(mailMessageId, ColumnMetadata.named("mail_message_id").withIndex(10).ofType(Types.VARCHAR).withSize(255));
        addMetadata(mailSubject, ColumnMetadata.named("mail_subject").withIndex(11).ofType(Types.VARCHAR).withSize(255));
        addMetadata(mailTimestamp, ColumnMetadata.named("mail_timestamp").withIndex(9).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(recipientAction, ColumnMetadata.named("recipient_action").withIndex(6).ofType(Types.VARCHAR).withSize(255));
        addMetadata(recipientDiagnosticCode, ColumnMetadata.named("recipient_diagnostic_code").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(recipientEmailAddress, ColumnMetadata.named("recipient_email_address").withIndex(5).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(recipientStatus, ColumnMetadata.named("recipient_status").withIndex(7).ofType(Types.VARCHAR).withSize(255));
    }

}

