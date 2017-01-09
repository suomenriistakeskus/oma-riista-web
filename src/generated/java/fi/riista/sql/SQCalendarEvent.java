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
 * SQCalendarEvent is a Querydsl query type for SQCalendarEvent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQCalendarEvent extends RelationalPathSpatial<SQCalendarEvent> {

    private static final long serialVersionUID = -1852193271;

    public static final SQCalendarEvent calendarEvent = new SQCalendarEvent("calendar_event");

    public final TimePath<java.sql.Time> beginTime = createTime("beginTime", java.sql.Time.class);

    public final NumberPath<Long> calendarEventId = createNumber("calendarEventId", Long.class);

    public final StringPath calendarEventType = createString("calendarEventType");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> date = createDateTime("date", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath description = createString("description");

    public final TimePath<java.sql.Time> endTime = createTime("endTime", java.sql.Time.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> organisationId = createNumber("organisationId", Long.class);

    public final NumberPath<Long> venueId = createNumber("venueId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQCalendarEvent> calendarEventPkey = createPrimaryKey(calendarEventId);

    public final com.querydsl.sql.ForeignKey<SQOrganisation> calendarEventOrganisationFk = createForeignKey(organisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQVenue> calendarEventVenueFk = createForeignKey(venueId, "venue_id");

    public final com.querydsl.sql.ForeignKey<SQCalendarEventType> calendarEventTypeFk = createForeignKey(calendarEventType, "name");

    public SQCalendarEvent(String variable) {
        super(SQCalendarEvent.class, forVariable(variable), "public", "calendar_event");
        addMetadata();
    }

    public SQCalendarEvent(String variable, String schema, String table) {
        super(SQCalendarEvent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQCalendarEvent(Path<? extends SQCalendarEvent> path) {
        super(path.getType(), path.getMetadata(), "public", "calendar_event");
        addMetadata();
    }

    public SQCalendarEvent(PathMetadata metadata) {
        super(SQCalendarEvent.class, metadata, "public", "calendar_event");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(beginTime, ColumnMetadata.named("begin_time").withIndex(10).ofType(Types.TIME).withSize(15).withDigits(6).notNull());
        addMetadata(calendarEventId, ColumnMetadata.named("calendar_event_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(calendarEventType, ColumnMetadata.named("calendar_event_type").withIndex(12).ofType(Types.VARCHAR).withSize(255));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(date, ColumnMetadata.named("date").withIndex(9).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(description, ColumnMetadata.named("description").withIndex(14).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(endTime, ColumnMetadata.named("end_time").withIndex(11).ofType(Types.TIME).withSize(15).withDigits(6));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(name, ColumnMetadata.named("name").withIndex(13).ofType(Types.VARCHAR).withSize(255));
        addMetadata(organisationId, ColumnMetadata.named("organisation_id").withIndex(15).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(venueId, ColumnMetadata.named("venue_id").withIndex(16).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

