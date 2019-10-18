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
 * SQPermitDecisionRevisionReceiver is a Querydsl query type for SQPermitDecisionRevisionReceiver
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionRevisionReceiver extends RelationalPathSpatial<SQPermitDecisionRevisionReceiver> {

    private static final long serialVersionUID = -1335389772;

    public static final SQPermitDecisionRevisionReceiver permitDecisionRevisionReceiver = new SQPermitDecisionRevisionReceiver("permit_decision_revision_receiver");

    public final BooleanPath cancelled = createBoolean("cancelled");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath email = createString("email");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> permitDecisionRevisionId = createNumber("permitDecisionRevisionId", Long.class);

    public final NumberPath<Long> permitDecisionRevisionReceiverId = createNumber("permitDecisionRevisionReceiverId", Long.class);

    public final StringPath receiverType = createString("receiverType");

    public final DateTimePath<java.sql.Timestamp> scheduledDate = createDateTime("scheduledDate", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> sentDate = createDateTime("sentDate", java.sql.Timestamp.class);

    public final StringPath uuid = createString("uuid");

    public final NumberPath<Integer> viewCount = createNumber("viewCount", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionRevisionReceiver> permitDecisionRevisionReceiverPkey = createPrimaryKey(permitDecisionRevisionReceiverId);

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionRevisionReceiverType> permitDecisionRevisionReceiverTypeFk = createForeignKey(receiverType, "name");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionRevision> permitDecisionRevisionReceiverRevisionIdFk = createForeignKey(permitDecisionRevisionId, "permit_decision_revision_id");

    public SQPermitDecisionRevisionReceiver(String variable) {
        super(SQPermitDecisionRevisionReceiver.class, forVariable(variable), "public", "permit_decision_revision_receiver");
        addMetadata();
    }

    public SQPermitDecisionRevisionReceiver(String variable, String schema, String table) {
        super(SQPermitDecisionRevisionReceiver.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionRevisionReceiver(String variable, String schema) {
        super(SQPermitDecisionRevisionReceiver.class, forVariable(variable), schema, "permit_decision_revision_receiver");
        addMetadata();
    }

    public SQPermitDecisionRevisionReceiver(Path<? extends SQPermitDecisionRevisionReceiver> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_revision_receiver");
        addMetadata();
    }

    public SQPermitDecisionRevisionReceiver(PathMetadata metadata) {
        super(SQPermitDecisionRevisionReceiver.class, metadata, "public", "permit_decision_revision_receiver");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(cancelled, ColumnMetadata.named("cancelled").withIndex(15).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(email, ColumnMetadata.named("email").withIndex(10).ofType(Types.VARCHAR).withSize(255));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(name, ColumnMetadata.named("name").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(permitDecisionRevisionId, ColumnMetadata.named("permit_decision_revision_id").withIndex(13).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitDecisionRevisionReceiverId, ColumnMetadata.named("permit_decision_revision_receiver_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(receiverType, ColumnMetadata.named("receiver_type").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(scheduledDate, ColumnMetadata.named("scheduled_date").withIndex(12).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(sentDate, ColumnMetadata.named("sent_date").withIndex(14).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(uuid, ColumnMetadata.named("uuid").withIndex(16).ofType(Types.CHAR).withSize(36).notNull());
        addMetadata(viewCount, ColumnMetadata.named("view_count").withIndex(17).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

