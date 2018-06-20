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
 * SQShootingTestParticipant is a Querydsl query type for SQShootingTestParticipant
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQShootingTestParticipant extends RelationalPathSpatial<SQShootingTestParticipant> {

    private static final long serialVersionUID = 1857198379;

    public static final SQShootingTestParticipant shootingTestParticipant = new SQShootingTestParticipant("shooting_test_participant");

    public final BooleanPath bearTestIntended = createBoolean("bearTestIntended");

    public final BooleanPath bowTestIntended = createBoolean("bowTestIntended");

    public final BooleanPath completed = createBoolean("completed");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final BooleanPath deerTestIntended = createBoolean("deerTestIntended");

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final BooleanPath mooseTestIntended = createBoolean("mooseTestIntended");

    public final NumberPath<java.math.BigDecimal> paidAmount = createNumber("paidAmount", java.math.BigDecimal.class);

    public final NumberPath<Long> personId = createNumber("personId", Long.class);

    public final DateTimePath<java.sql.Timestamp> registrationTime = createDateTime("registrationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> shootingTestEventId = createNumber("shootingTestEventId", Long.class);

    public final NumberPath<Long> shootingTestParticipantId = createNumber("shootingTestParticipantId", Long.class);

    public final NumberPath<java.math.BigDecimal> totalDueAmount = createNumber("totalDueAmount", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<SQShootingTestParticipant> shootingTestParticipantPkey = createPrimaryKey(shootingTestParticipantId);

    public final com.querydsl.sql.ForeignKey<SQPerson> shootingTestParticipantPersonFk = createForeignKey(personId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQShootingTestEvent> shootingTestParticipantShootingTestEventFk = createForeignKey(shootingTestEventId, "shooting_test_event_id");

    public final com.querydsl.sql.ForeignKey<SQShootingTestAttempt> _shootingTestAttemptParticipantFk = createInvForeignKey(shootingTestParticipantId, "participant_id");

    public SQShootingTestParticipant(String variable) {
        super(SQShootingTestParticipant.class, forVariable(variable), "public", "shooting_test_participant");
        addMetadata();
    }

    public SQShootingTestParticipant(String variable, String schema, String table) {
        super(SQShootingTestParticipant.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQShootingTestParticipant(String variable, String schema) {
        super(SQShootingTestParticipant.class, forVariable(variable), schema, "shooting_test_participant");
        addMetadata();
    }

    public SQShootingTestParticipant(Path<? extends SQShootingTestParticipant> path) {
        super(path.getType(), path.getMetadata(), "public", "shooting_test_participant");
        addMetadata();
    }

    public SQShootingTestParticipant(PathMetadata metadata) {
        super(SQShootingTestParticipant.class, metadata, "public", "shooting_test_participant");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(bearTestIntended, ColumnMetadata.named("bear_test_intended").withIndex(12).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(bowTestIntended, ColumnMetadata.named("bow_test_intended").withIndex(14).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(completed, ColumnMetadata.named("completed").withIndex(18).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deerTestIntended, ColumnMetadata.named("deer_test_intended").withIndex(13).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(mooseTestIntended, ColumnMetadata.named("moose_test_intended").withIndex(11).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(paidAmount, ColumnMetadata.named("paid_amount").withIndex(16).ofType(Types.NUMERIC).withSize(6).withDigits(2));
        addMetadata(personId, ColumnMetadata.named("person_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(registrationTime, ColumnMetadata.named("registration_time").withIndex(17).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(shootingTestEventId, ColumnMetadata.named("shooting_test_event_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(shootingTestParticipantId, ColumnMetadata.named("shooting_test_participant_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(totalDueAmount, ColumnMetadata.named("total_due_amount").withIndex(15).ofType(Types.NUMERIC).withSize(6).withDigits(2));
    }

}

