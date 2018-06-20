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
 * SQShootingTestEvent is a Querydsl query type for SQShootingTestEvent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQShootingTestEvent extends RelationalPathSpatial<SQShootingTestEvent> {

    private static final long serialVersionUID = -1642072430;

    public static final SQShootingTestEvent shootingTestEvent = new SQShootingTestEvent("shooting_test_event");

    public final NumberPath<Long> calendarEventId = createNumber("calendarEventId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> lockedTime = createDateTime("lockedTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> shootingTestEventId = createNumber("shootingTestEventId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQShootingTestEvent> shootingTestEventPkey = createPrimaryKey(shootingTestEventId);

    public final com.querydsl.sql.ForeignKey<SQCalendarEvent> shootingTestEventCalendarEventFk = createForeignKey(calendarEventId, "calendar_event_id");

    public final com.querydsl.sql.ForeignKey<SQShootingTestParticipant> _shootingTestParticipantShootingTestEventFk = createInvForeignKey(shootingTestEventId, "shooting_test_event_id");

    public final com.querydsl.sql.ForeignKey<SQShootingTestOfficial> _shootingTestOfficialShootingTestEventFk = createInvForeignKey(shootingTestEventId, "shooting_test_event_id");

    public SQShootingTestEvent(String variable) {
        super(SQShootingTestEvent.class, forVariable(variable), "public", "shooting_test_event");
        addMetadata();
    }

    public SQShootingTestEvent(String variable, String schema, String table) {
        super(SQShootingTestEvent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQShootingTestEvent(String variable, String schema) {
        super(SQShootingTestEvent.class, forVariable(variable), schema, "shooting_test_event");
        addMetadata();
    }

    public SQShootingTestEvent(Path<? extends SQShootingTestEvent> path) {
        super(path.getType(), path.getMetadata(), "public", "shooting_test_event");
        addMetadata();
    }

    public SQShootingTestEvent(PathMetadata metadata) {
        super(SQShootingTestEvent.class, metadata, "public", "shooting_test_event");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(calendarEventId, ColumnMetadata.named("calendar_event_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(lockedTime, ColumnMetadata.named("locked_time").withIndex(10).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(shootingTestEventId, ColumnMetadata.named("shooting_test_event_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

