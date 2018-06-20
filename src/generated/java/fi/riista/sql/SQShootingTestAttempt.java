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
 * SQShootingTestAttempt is a Querydsl query type for SQShootingTestAttempt
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQShootingTestAttempt extends RelationalPathSpatial<SQShootingTestAttempt> {

    private static final long serialVersionUID = -1077331771;

    public static final SQShootingTestAttempt shootingTestAttempt = new SQShootingTestAttempt("shooting_test_attempt");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Integer> hits = createNumber("hits", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath note = createString("note");

    public final NumberPath<Long> participantId = createNumber("participantId", Long.class);

    public final StringPath result = createString("result");

    public final NumberPath<Long> shootingTestAttemptId = createNumber("shootingTestAttemptId", Long.class);

    public final StringPath type = createString("type");

    public final com.querydsl.sql.PrimaryKey<SQShootingTestAttempt> shootingTestAttemptPkey = createPrimaryKey(shootingTestAttemptId);

    public final com.querydsl.sql.ForeignKey<SQShootingTestType> shootingTestAttemptTypeFk = createForeignKey(type, "name");

    public final com.querydsl.sql.ForeignKey<SQShootingTestAttemptResult> shootingTestAttemptResultFk = createForeignKey(result, "name");

    public final com.querydsl.sql.ForeignKey<SQShootingTestParticipant> shootingTestAttemptParticipantFk = createForeignKey(participantId, "shooting_test_participant_id");

    public SQShootingTestAttempt(String variable) {
        super(SQShootingTestAttempt.class, forVariable(variable), "public", "shooting_test_attempt");
        addMetadata();
    }

    public SQShootingTestAttempt(String variable, String schema, String table) {
        super(SQShootingTestAttempt.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQShootingTestAttempt(String variable, String schema) {
        super(SQShootingTestAttempt.class, forVariable(variable), schema, "shooting_test_attempt");
        addMetadata();
    }

    public SQShootingTestAttempt(Path<? extends SQShootingTestAttempt> path) {
        super(path.getType(), path.getMetadata(), "public", "shooting_test_attempt");
        addMetadata();
    }

    public SQShootingTestAttempt(PathMetadata metadata) {
        super(SQShootingTestAttempt.class, metadata, "public", "shooting_test_attempt");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(hits, ColumnMetadata.named("hits").withIndex(12).ofType(Types.INTEGER).withSize(10));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(note, ColumnMetadata.named("note").withIndex(13).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(participantId, ColumnMetadata.named("participant_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(result, ColumnMetadata.named("result").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(shootingTestAttemptId, ColumnMetadata.named("shooting_test_attempt_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(type, ColumnMetadata.named("type").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

