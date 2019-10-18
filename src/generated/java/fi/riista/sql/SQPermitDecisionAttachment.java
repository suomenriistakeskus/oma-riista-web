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
 * SQPermitDecisionAttachment is a Querydsl query type for SQPermitDecisionAttachment
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionAttachment extends RelationalPathSpatial<SQPermitDecisionAttachment> {

    private static final long serialVersionUID = 774066029;

    public static final SQPermitDecisionAttachment permitDecisionAttachment = new SQPermitDecisionAttachment("permit_decision_attachment");

    public final StringPath attachmentMetadataId = createString("attachmentMetadataId");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath description = createString("description");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Integer> orderingNumber = createNumber("orderingNumber", Integer.class);

    public final NumberPath<Long> permitDecisionAttachmentId = createNumber("permitDecisionAttachmentId", Long.class);

    public final NumberPath<Long> permitDecisionId = createNumber("permitDecisionId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionAttachment> permitDecisionAttachmentPkey = createPrimaryKey(permitDecisionAttachmentId);

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> permitDecisionAttachmentDecisionIdFk = createForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQFileMetadata> permitDecisionAttachmentMetadataIdFk = createForeignKey(attachmentMetadataId, "file_metadata_uuid");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionRevisionAttachment> _permitDecisionRevisionAttachmentDecisionIdFk = createInvForeignKey(permitDecisionAttachmentId, "permit_decision_attachment_id");

    public SQPermitDecisionAttachment(String variable) {
        super(SQPermitDecisionAttachment.class, forVariable(variable), "public", "permit_decision_attachment");
        addMetadata();
    }

    public SQPermitDecisionAttachment(String variable, String schema, String table) {
        super(SQPermitDecisionAttachment.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionAttachment(String variable, String schema) {
        super(SQPermitDecisionAttachment.class, forVariable(variable), schema, "permit_decision_attachment");
        addMetadata();
    }

    public SQPermitDecisionAttachment(Path<? extends SQPermitDecisionAttachment> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_attachment");
        addMetadata();
    }

    public SQPermitDecisionAttachment(PathMetadata metadata) {
        super(SQPermitDecisionAttachment.class, metadata, "public", "permit_decision_attachment");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(attachmentMetadataId, ColumnMetadata.named("attachment_metadata_id").withIndex(12).ofType(Types.CHAR).withSize(36).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(description, ColumnMetadata.named("description").withIndex(9).ofType(Types.VARCHAR).withSize(255));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(orderingNumber, ColumnMetadata.named("ordering_number").withIndex(10).ofType(Types.INTEGER).withSize(10));
        addMetadata(permitDecisionAttachmentId, ColumnMetadata.named("permit_decision_attachment_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitDecisionId, ColumnMetadata.named("permit_decision_id").withIndex(11).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

