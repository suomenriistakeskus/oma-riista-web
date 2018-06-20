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
 * SQMobileLoginEvent is a Querydsl query type for SQMobileLoginEvent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMobileLoginEvent extends RelationalPathSpatial<SQMobileLoginEvent> {

    private static final long serialVersionUID = 1055020998;

    public static final SQMobileLoginEvent mobileLoginEvent = new SQMobileLoginEvent("mobile_login_event");

    public final StringPath deviceName = createString("deviceName");

    public final DateTimePath<java.sql.Timestamp> loginTime = createDateTime("loginTime", java.sql.Timestamp.class);

    public final NumberPath<Long> mobileLoginEventId = createNumber("mobileLoginEventId", Long.class);

    public final StringPath platform = createString("platform");

    public final StringPath softwareVersion = createString("softwareVersion");

    public final StringPath username = createString("username");

    public final com.querydsl.sql.PrimaryKey<SQMobileLoginEvent> mobileLoginEventPkey = createPrimaryKey(mobileLoginEventId);

    public SQMobileLoginEvent(String variable) {
        super(SQMobileLoginEvent.class, forVariable(variable), "public", "mobile_login_event");
        addMetadata();
    }

    public SQMobileLoginEvent(String variable, String schema, String table) {
        super(SQMobileLoginEvent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQMobileLoginEvent(String variable, String schema) {
        super(SQMobileLoginEvent.class, forVariable(variable), schema, "mobile_login_event");
        addMetadata();
    }

    public SQMobileLoginEvent(Path<? extends SQMobileLoginEvent> path) {
        super(path.getType(), path.getMetadata(), "public", "mobile_login_event");
        addMetadata();
    }

    public SQMobileLoginEvent(PathMetadata metadata) {
        super(SQMobileLoginEvent.class, metadata, "public", "mobile_login_event");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(deviceName, ColumnMetadata.named("device_name").withIndex(5).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(loginTime, ColumnMetadata.named("login_time").withIndex(2).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(mobileLoginEventId, ColumnMetadata.named("mobile_login_event_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(platform, ColumnMetadata.named("platform").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(softwareVersion, ColumnMetadata.named("software_version").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(username, ColumnMetadata.named("username").withIndex(6).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

