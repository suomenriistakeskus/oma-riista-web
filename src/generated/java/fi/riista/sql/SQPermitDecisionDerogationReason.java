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
 * SQPermitDecisionDerogationReason is a Querydsl query type for SQPermitDecisionDerogationReason
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionDerogationReason extends RelationalPathSpatial<SQPermitDecisionDerogationReason> {

    private static final long serialVersionUID = -902021350;

    public static final SQPermitDecisionDerogationReason permitDecisionDerogationReason = new SQPermitDecisionDerogationReason("permit_decision_derogation_reason");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> permitDecisionDerogationReasonId = createNumber("permitDecisionDerogationReasonId", Long.class);

    public final NumberPath<Long> permitDecisionId = createNumber("permitDecisionId", Long.class);

    public final StringPath reasonType = createString("reasonType");

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionDerogationReason> permitDecisionDerogationReasonPkey = createPrimaryKey(permitDecisionDerogationReasonId);

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionDerogationReasonType> permitDecisionDerogationReasonTypeFk = createForeignKey(reasonType, "name");

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> permitDecisionDerogationReasonDecisionIdFk = createForeignKey(permitDecisionId, "permit_decision_id");

    public SQPermitDecisionDerogationReason(String variable) {
        super(SQPermitDecisionDerogationReason.class, forVariable(variable), "public", "permit_decision_derogation_reason");
        addMetadata();
    }

    public SQPermitDecisionDerogationReason(String variable, String schema, String table) {
        super(SQPermitDecisionDerogationReason.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionDerogationReason(String variable, String schema) {
        super(SQPermitDecisionDerogationReason.class, forVariable(variable), schema, "permit_decision_derogation_reason");
        addMetadata();
    }

    public SQPermitDecisionDerogationReason(Path<? extends SQPermitDecisionDerogationReason> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_derogation_reason");
        addMetadata();
    }

    public SQPermitDecisionDerogationReason(PathMetadata metadata) {
        super(SQPermitDecisionDerogationReason.class, metadata, "public", "permit_decision_derogation_reason");
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
        addMetadata(permitDecisionDerogationReasonId, ColumnMetadata.named("permit_decision_derogation_reason_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitDecisionId, ColumnMetadata.named("permit_decision_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(reasonType, ColumnMetadata.named("reason_type").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

