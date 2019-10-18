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
 * SQAdditionalCalendarEvent is a Querydsl query type for SQAdditionalCalendarEvent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQAdditionalCalendarEvent extends RelationalPathSpatial<SQAdditionalCalendarEvent> {

    private static final long serialVersionUID = -833632030;

    public static final SQAdditionalCalendarEvent additionalCalendarEvent = new SQAdditionalCalendarEvent("additional_calendar_event");

    public final NumberPath<Long> additionalCalendarEventId = createNumber("additionalCalendarEventId", Long.class);

    public final TimePath<java.sql.Time> beginTime = createTime("beginTime", java.sql.Time.class);

    public final NumberPath<Long> calendarEventId = createNumber("calendarEventId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> date = createDateTime("date", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final TimePath<java.sql.Time> endTime = createTime("endTime", java.sql.Time.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> venueId = createNumber("venueId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQAdditionalCalendarEvent> additionalCalendarEventPkey = createPrimaryKey(additionalCalendarEventId);

    public final com.querydsl.sql.ForeignKey<SQVenue> additionalCalendarEventVenueFk = createForeignKey(venueId, "venue_id");

    public final com.querydsl.sql.ForeignKey<SQCalendarEvent> additionalCalendarEventCalendarEventFk = createForeignKey(calendarEventId, "calendar_event_id");

    public SQAdditionalCalendarEvent(String variable) {
        super(SQAdditionalCalendarEvent.class, forVariable(variable), "public", "additional_calendar_event");
        addMetadata();
    }

    public SQAdditionalCalendarEvent(String variable, String schema, String table) {
        super(SQAdditionalCalendarEvent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQAdditionalCalendarEvent(String variable, String schema) {
        super(SQAdditionalCalendarEvent.class, forVariable(variable), schema, "additional_calendar_event");
        addMetadata();
    }

    public SQAdditionalCalendarEvent(Path<? extends SQAdditionalCalendarEvent> path) {
        super(path.getType(), path.getMetadata(), "public", "additional_calendar_event");
        addMetadata();
    }

    public SQAdditionalCalendarEvent(PathMetadata metadata) {
        super(SQAdditionalCalendarEvent.class, metadata, "public", "additional_calendar_event");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(additionalCalendarEventId, ColumnMetadata.named("additional_calendar_event_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(beginTime, ColumnMetadata.named("begin_time").withIndex(12).ofType(Types.TIME).withSize(15).withDigits(6).notNull());
        addMetadata(calendarEventId, ColumnMetadata.named("calendar_event_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(date, ColumnMetadata.named("date").withIndex(11).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(endTime, ColumnMetadata.named("end_time").withIndex(13).ofType(Types.TIME).withSize(15).withDigits(6));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(venueId, ColumnMetadata.named("venue_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

