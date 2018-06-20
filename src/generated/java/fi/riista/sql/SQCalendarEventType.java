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
 * SQCalendarEventType is a Querydsl query type for SQCalendarEventType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQCalendarEventType extends RelationalPathSpatial<SQCalendarEventType> {

    private static final long serialVersionUID = 65903843;

    public static final SQCalendarEventType calendarEventType = new SQCalendarEventType("calendar_event_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQCalendarEventType> calendarEventTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQCalendarEvent> _calendarEventTypeFk = createInvForeignKey(name, "calendar_event_type");

    public SQCalendarEventType(String variable) {
        super(SQCalendarEventType.class, forVariable(variable), "public", "calendar_event_type");
        addMetadata();
    }

    public SQCalendarEventType(String variable, String schema, String table) {
        super(SQCalendarEventType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQCalendarEventType(String variable, String schema) {
        super(SQCalendarEventType.class, forVariable(variable), schema, "calendar_event_type");
        addMetadata();
    }

    public SQCalendarEventType(Path<? extends SQCalendarEventType> path) {
        super(path.getType(), path.getMetadata(), "public", "calendar_event_type");
        addMetadata();
    }

    public SQCalendarEventType(PathMetadata metadata) {
        super(SQCalendarEventType.class, metadata, "public", "calendar_event_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

