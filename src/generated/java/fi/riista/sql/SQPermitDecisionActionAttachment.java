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
 * SQPermitDecisionActionAttachment is a Querydsl query type for SQPermitDecisionActionAttachment
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionActionAttachment extends RelationalPathSpatial<SQPermitDecisionActionAttachment> {

    private static final long serialVersionUID = -1797685565;

    public static final SQPermitDecisionActionAttachment permitDecisionActionAttachment = new SQPermitDecisionActionAttachment("permit_decision_action_attachment");

    public final StringPath attachmentMetadataId = createString("attachmentMetadataId");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> permitDecisionActionAttachmentId = createNumber("permitDecisionActionAttachmentId", Long.class);

    public final NumberPath<Long> permitDecisionActionId = createNumber("permitDecisionActionId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionActionAttachment> permitDecisionActionAttachmentPkey = createPrimaryKey(permitDecisionActionAttachmentId);

    public final com.querydsl.sql.ForeignKey<SQFileMetadata> permitDecisionActionAttachmentMetadataIdFk = createForeignKey(attachmentMetadataId, "file_metadata_uuid");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionAction> permitDecisionActionAttachmentDecisionIdFk = createForeignKey(permitDecisionActionId, "permit_decision_action_id");

    public SQPermitDecisionActionAttachment(String variable) {
        super(SQPermitDecisionActionAttachment.class, forVariable(variable), "public", "permit_decision_action_attachment");
        addMetadata();
    }

    public SQPermitDecisionActionAttachment(String variable, String schema, String table) {
        super(SQPermitDecisionActionAttachment.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionActionAttachment(String variable, String schema) {
        super(SQPermitDecisionActionAttachment.class, forVariable(variable), schema, "permit_decision_action_attachment");
        addMetadata();
    }

    public SQPermitDecisionActionAttachment(Path<? extends SQPermitDecisionActionAttachment> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_action_attachment");
        addMetadata();
    }

    public SQPermitDecisionActionAttachment(PathMetadata metadata) {
        super(SQPermitDecisionActionAttachment.class, metadata, "public", "permit_decision_action_attachment");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(attachmentMetadataId, ColumnMetadata.named("attachment_metadata_id").withIndex(10).ofType(Types.CHAR).withSize(36).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitDecisionActionAttachmentId, ColumnMetadata.named("permit_decision_action_attachment_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitDecisionActionId, ColumnMetadata.named("permit_decision_action_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

