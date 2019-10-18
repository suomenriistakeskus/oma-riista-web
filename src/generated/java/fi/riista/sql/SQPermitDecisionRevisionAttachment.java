package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQPermitDecisionRevisionAttachment is a Querydsl query type for SQPermitDecisionRevisionAttachment
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionRevisionAttachment extends RelationalPathSpatial<SQPermitDecisionRevisionAttachment> {

    private static final long serialVersionUID = -1287117912;

    public static final SQPermitDecisionRevisionAttachment permitDecisionRevisionAttachment = new SQPermitDecisionRevisionAttachment("permit_decision_revision_attachment");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Integer> orderingNumber = createNumber("orderingNumber", Integer.class);

    public final NumberPath<Long> permitDecisionAttachmentId = createNumber("permitDecisionAttachmentId", Long.class);

    public final NumberPath<Long> permitDecisionRevisionAttachmentId = createNumber("permitDecisionRevisionAttachmentId", Long.class);

    public final NumberPath<Long> permitDecisionRevisionId = createNumber("permitDecisionRevisionId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionRevisionAttachment> permitDecisionRevisionAttachmentPkey = createPrimaryKey(permitDecisionRevisionAttachmentId);

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionAttachment> permitDecisionRevisionAttachmentDecisionIdFk = createForeignKey(permitDecisionAttachmentId, "permit_decision_attachment_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionRevision> permitDecisionRevisionAttachmentRevisionIdFk = createForeignKey(permitDecisionRevisionId, "permit_decision_revision_id");

    public SQPermitDecisionRevisionAttachment(String variable) {
        super(SQPermitDecisionRevisionAttachment.class, forVariable(variable), "public", "permit_decision_revision_attachment");
        addMetadata();
    }

    public SQPermitDecisionRevisionAttachment(String variable, String schema, String table) {
        super(SQPermitDecisionRevisionAttachment.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionRevisionAttachment(String variable, String schema) {
        super(SQPermitDecisionRevisionAttachment.class, forVariable(variable), schema, "permit_decision_revision_attachment");
        addMetadata();
    }

    public SQPermitDecisionRevisionAttachment(Path<? extends SQPermitDecisionRevisionAttachment> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_revision_attachment");
        addMetadata();
    }

    public SQPermitDecisionRevisionAttachment(PathMetadata metadata) {
        super(SQPermitDecisionRevisionAttachment.class, metadata, "public", "permit_decision_revision_attachment");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(orderingNumber, ColumnMetadata.named("ordering_number").withIndex(9).ofType(Types.INTEGER).withSize(10));
        addMetadata(permitDecisionAttachmentId, ColumnMetadata.named("permit_decision_attachment_id").withIndex(11).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitDecisionRevisionAttachmentId, ColumnMetadata.named("permit_decision_revision_attachment_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitDecisionRevisionId, ColumnMetadata.named("permit_decision_revision_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

