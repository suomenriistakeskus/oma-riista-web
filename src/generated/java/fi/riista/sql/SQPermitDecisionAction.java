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
 * SQPermitDecisionAction is a Querydsl query type for SQPermitDecisionAction
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionAction extends RelationalPathSpatial<SQPermitDecisionAction> {

    private static final long serialVersionUID = -2087310976;

    public static final SQPermitDecisionAction permitDecisionAction = new SQPermitDecisionAction("permit_decision_action");

    public final StringPath actionType = createString("actionType");

    public final StringPath communicationType = createString("communicationType");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final StringPath decisionText = createString("decisionText");

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> permitDecisionActionId = createNumber("permitDecisionActionId", Long.class);

    public final NumberPath<Long> permitDecisionId = createNumber("permitDecisionId", Long.class);

    public final DateTimePath<java.sql.Timestamp> pointOfTime = createDateTime("pointOfTime", java.sql.Timestamp.class);

    public final StringPath text = createString("text");

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionAction> permitDecisionActionPkey = createPrimaryKey(permitDecisionActionId);

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionActionCommunicationType> permitDecisionActionCommunicationTypeFk = createForeignKey(communicationType, "name");

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> permitDecisionActionDecisionFk = createForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionActionType> permitDecisionActionTypeFk = createForeignKey(actionType, "name");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionActionAttachment> _permitDecisionActionAttachmentDecisionIdFk = createInvForeignKey(permitDecisionActionId, "permit_decision_action_id");

    public SQPermitDecisionAction(String variable) {
        super(SQPermitDecisionAction.class, forVariable(variable), "public", "permit_decision_action");
        addMetadata();
    }

    public SQPermitDecisionAction(String variable, String schema, String table) {
        super(SQPermitDecisionAction.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionAction(String variable, String schema) {
        super(SQPermitDecisionAction.class, forVariable(variable), schema, "permit_decision_action");
        addMetadata();
    }

    public SQPermitDecisionAction(Path<? extends SQPermitDecisionAction> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_action");
        addMetadata();
    }

    public SQPermitDecisionAction(PathMetadata metadata) {
        super(SQPermitDecisionAction.class, metadata, "public", "permit_decision_action");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(actionType, ColumnMetadata.named("action_type").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(communicationType, ColumnMetadata.named("communication_type").withIndex(12).ofType(Types.VARCHAR).withSize(255));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(decisionText, ColumnMetadata.named("decision_text").withIndex(14).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitDecisionActionId, ColumnMetadata.named("permit_decision_action_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitDecisionId, ColumnMetadata.named("permit_decision_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(pointOfTime, ColumnMetadata.named("point_of_time").withIndex(10).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(text, ColumnMetadata.named("text").withIndex(13).ofType(Types.VARCHAR).withSize(2147483647).notNull());
    }

}

