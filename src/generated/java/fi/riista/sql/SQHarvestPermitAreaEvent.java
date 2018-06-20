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
 * SQHarvestPermitAreaEvent is a Querydsl query type for SQHarvestPermitAreaEvent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitAreaEvent extends RelationalPathSpatial<SQHarvestPermitAreaEvent> {

    private static final long serialVersionUID = 731672604;

    public static final SQHarvestPermitAreaEvent harvestPermitAreaEvent = new SQHarvestPermitAreaEvent("harvest_permit_area_event");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final DateTimePath<java.sql.Timestamp> eventTime = createDateTime("eventTime", java.sql.Timestamp.class);

    public final NumberPath<Long> harvestPermitAreaEventId = createNumber("harvestPermitAreaEventId", Long.class);

    public final NumberPath<Long> harvestPermitAreaId = createNumber("harvestPermitAreaId", Long.class);

    public final StringPath status = createString("status");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitAreaEvent> harvestPermitAreaEventPkey = createPrimaryKey(harvestPermitAreaEventId);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitAreaStatus> harvestPermitAreaEventStatusFk = createForeignKey(status, "name");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitArea> harvestPermitAreaEventPermitAreaFk = createForeignKey(harvestPermitAreaId, "harvest_permit_area_id");

    public SQHarvestPermitAreaEvent(String variable) {
        super(SQHarvestPermitAreaEvent.class, forVariable(variable), "public", "harvest_permit_area_event");
        addMetadata();
    }

    public SQHarvestPermitAreaEvent(String variable, String schema, String table) {
        super(SQHarvestPermitAreaEvent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitAreaEvent(String variable, String schema) {
        super(SQHarvestPermitAreaEvent.class, forVariable(variable), schema, "harvest_permit_area_event");
        addMetadata();
    }

    public SQHarvestPermitAreaEvent(Path<? extends SQHarvestPermitAreaEvent> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_area_event");
        addMetadata();
    }

    public SQHarvestPermitAreaEvent(PathMetadata metadata) {
        super(SQHarvestPermitAreaEvent.class, metadata, "public", "harvest_permit_area_event");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(eventTime, ColumnMetadata.named("event_time").withIndex(3).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(harvestPermitAreaEventId, ColumnMetadata.named("harvest_permit_area_event_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitAreaId, ColumnMetadata.named("harvest_permit_area_id").withIndex(6).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(status, ColumnMetadata.named("status").withIndex(5).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

