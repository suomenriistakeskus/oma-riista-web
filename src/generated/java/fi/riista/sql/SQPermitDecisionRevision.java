package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQPermitDecisionRevision is a Querydsl query type for SQPermitDecisionRevision
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionRevision extends RelationalPathSpatial<SQPermitDecisionRevision> {

    private static final long serialVersionUID = 1239267493;

    public static final SQPermitDecisionRevision permitDecisionRevision = new SQPermitDecisionRevision("permit_decision_revision");

    public final StringPath additionalInfoBody = createString("additionalInfoBody");

    public final StringPath administrativeCourtBody = createString("administrativeCourtBody");

    public final StringPath appealBody = createString("appealBody");

    public final StringPath appealStatus = createString("appealStatus");

    public final StringPath applicationBody = createString("applicationBody");

    public final StringPath applicationReasoningBody = createString("applicationReasoningBody");

    public final StringPath attachmentsBody = createString("attachmentsBody");

    public final BooleanPath cancelled = createBoolean("cancelled");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final StringPath decisionBody = createString("decisionBody");

    public final StringPath decisionExtra = createString("decisionExtra");

    public final StringPath decisionReasoningBody = createString("decisionReasoningBody");

    public final StringPath decisionType = createString("decisionType");

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath deliveryBody = createString("deliveryBody");

    public final StringPath executionBody = createString("executionBody");

    public final StringPath externalId = createString("externalId");

    public final StringPath legalAdviceBody = createString("legalAdviceBody");

    public final DateTimePath<java.sql.Timestamp> lockedDate = createDateTime("lockedDate", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath notificationObligationBody = createString("notificationObligationBody");

    public final StringPath paymentBody = createString("paymentBody");

    public final StringPath pdfMetadataId = createString("pdfMetadataId");

    public final NumberPath<Long> permitDecisionId = createNumber("permitDecisionId", Long.class);

    public final NumberPath<Long> permitDecisionRevisionId = createNumber("permitDecisionRevisionId", Long.class);

    public final BooleanPath postalByMail = createBoolean("postalByMail");

    public final DateTimePath<java.sql.Timestamp> postedByMailDate = createDateTime("postedByMailDate", java.sql.Timestamp.class);

    public final StringPath postedByMailUsername = createString("postedByMailUsername");

    public final StringPath processingBody = createString("processingBody");

    public final DateTimePath<java.sql.Timestamp> publishDate = createDateTime("publishDate", java.sql.Timestamp.class);

    public final StringPath restrictionBody = createString("restrictionBody");

    public final StringPath restrictionExtra = createString("restrictionExtra");

    public final DateTimePath<java.sql.Timestamp> scheduledPublishDate = createDateTime("scheduledPublishDate", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionRevision> permitDecisionRevisionPkey = createPrimaryKey(permitDecisionRevisionId);

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> permitDecisionRevisionDecisionIdFk = createForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQFileMetadata> permitDecisionRevisionPdfFk = createForeignKey(pdfMetadataId, "file_metadata_uuid");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionRevisionReceiver> _permitDecisionRevisionReceiverRevisionIdFk = createInvForeignKey(permitDecisionRevisionId, "permit_decision_revision_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionRevisionAttachment> _permitDecisionRevisionAttachmentRevisionIdFk = createInvForeignKey(permitDecisionRevisionId, "permit_decision_revision_id");

    public SQPermitDecisionRevision(String variable) {
        super(SQPermitDecisionRevision.class, forVariable(variable), "public", "permit_decision_revision");
        addMetadata();
    }

    public SQPermitDecisionRevision(String variable, String schema, String table) {
        super(SQPermitDecisionRevision.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionRevision(String variable, String schema) {
        super(SQPermitDecisionRevision.class, forVariable(variable), schema, "permit_decision_revision");
        addMetadata();
    }

    public SQPermitDecisionRevision(Path<? extends SQPermitDecisionRevision> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_revision");
        addMetadata();
    }

    public SQPermitDecisionRevision(PathMetadata metadata) {
        super(SQPermitDecisionRevision.class, metadata, "public", "permit_decision_revision");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(additionalInfoBody, ColumnMetadata.named("additional_info_body").withIndex(23).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(administrativeCourtBody, ColumnMetadata.named("administrative_court_body").withIndex(27).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(appealBody, ColumnMetadata.named("appeal_body").withIndex(22).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(appealStatus, ColumnMetadata.named("appeal_status").withIndex(35).ofType(Types.VARCHAR).withSize(255));
        addMetadata(applicationBody, ColumnMetadata.named("application_body").withIndex(15).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(applicationReasoningBody, ColumnMetadata.named("application_reasoning_body").withIndex(16).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(attachmentsBody, ColumnMetadata.named("attachments_body").withIndex(26).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(cancelled, ColumnMetadata.named("cancelled").withIndex(32).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(decisionBody, ColumnMetadata.named("decision_body").withIndex(13).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(decisionExtra, ColumnMetadata.named("decision_extra").withIndex(37).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(decisionReasoningBody, ColumnMetadata.named("decision_reasoning_body").withIndex(14).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(decisionType, ColumnMetadata.named("decision_type").withIndex(36).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(deliveryBody, ColumnMetadata.named("delivery_body").withIndex(24).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(executionBody, ColumnMetadata.named("execution_body").withIndex(19).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(externalId, ColumnMetadata.named("external_id").withIndex(34).ofType(Types.VARCHAR).withSize(255));
        addMetadata(legalAdviceBody, ColumnMetadata.named("legal_advice_body").withIndex(20).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(lockedDate, ColumnMetadata.named("locked_date").withIndex(12).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(notificationObligationBody, ColumnMetadata.named("notification_obligation_body").withIndex(21).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(paymentBody, ColumnMetadata.named("payment_body").withIndex(25).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(pdfMetadataId, ColumnMetadata.named("pdf_metadata_id").withIndex(10).ofType(Types.CHAR).withSize(36).notNull());
        addMetadata(permitDecisionId, ColumnMetadata.named("permit_decision_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitDecisionRevisionId, ColumnMetadata.named("permit_decision_revision_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(postalByMail, ColumnMetadata.named("postal_by_mail").withIndex(30).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(postedByMailDate, ColumnMetadata.named("posted_by_mail_date").withIndex(31).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(postedByMailUsername, ColumnMetadata.named("posted_by_mail_username").withIndex(33).ofType(Types.VARCHAR).withSize(255));
        addMetadata(processingBody, ColumnMetadata.named("processing_body").withIndex(17).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(publishDate, ColumnMetadata.named("publish_date").withIndex(29).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(restrictionBody, ColumnMetadata.named("restriction_body").withIndex(18).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(restrictionExtra, ColumnMetadata.named("restriction_extra").withIndex(28).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(scheduledPublishDate, ColumnMetadata.named("scheduled_publish_date").withIndex(11).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
    }

}

