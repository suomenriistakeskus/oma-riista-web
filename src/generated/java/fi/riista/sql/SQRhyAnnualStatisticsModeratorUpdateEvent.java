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
 * SQRhyAnnualStatisticsModeratorUpdateEvent is a Querydsl query type for SQRhyAnnualStatisticsModeratorUpdateEvent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQRhyAnnualStatisticsModeratorUpdateEvent extends RelationalPathSpatial<SQRhyAnnualStatisticsModeratorUpdateEvent> {

    private static final long serialVersionUID = 1830423038;

    public static final SQRhyAnnualStatisticsModeratorUpdateEvent rhyAnnualStatisticsModeratorUpdateEvent = new SQRhyAnnualStatisticsModeratorUpdateEvent("rhy_annual_statistics_moderator_update_event");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final StringPath dataGroup = createString("dataGroup");

    public final DateTimePath<java.sql.Timestamp> eventTime = createDateTime("eventTime", java.sql.Timestamp.class);

    public final NumberPath<Long> rhyAnnualStatisticsModeratorUpdateEventId = createNumber("rhyAnnualStatisticsModeratorUpdateEventId", Long.class);

    public final NumberPath<Long> statisticsId = createNumber("statisticsId", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQRhyAnnualStatisticsModeratorUpdateEvent> rhyAnnualStatisticsModeratorUpdateEventPkey = createPrimaryKey(rhyAnnualStatisticsModeratorUpdateEventId);

    public final com.querydsl.sql.ForeignKey<SQRhyAnnualStatistics> rhyAnnualStatisticsModeratorUpdateEventStatisticsFk = createForeignKey(statisticsId, "rhy_annual_statistics_id");

    public final com.querydsl.sql.ForeignKey<SQRhyAnnualStatisticsEditableDataGroup> rhyAnnualStatisticsModeratorUpdateEventDataGroupFk = createForeignKey(dataGroup, "name");

    public SQRhyAnnualStatisticsModeratorUpdateEvent(String variable) {
        super(SQRhyAnnualStatisticsModeratorUpdateEvent.class, forVariable(variable), "public", "rhy_annual_statistics_moderator_update_event");
        addMetadata();
    }

    public SQRhyAnnualStatisticsModeratorUpdateEvent(String variable, String schema, String table) {
        super(SQRhyAnnualStatisticsModeratorUpdateEvent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQRhyAnnualStatisticsModeratorUpdateEvent(String variable, String schema) {
        super(SQRhyAnnualStatisticsModeratorUpdateEvent.class, forVariable(variable), schema, "rhy_annual_statistics_moderator_update_event");
        addMetadata();
    }

    public SQRhyAnnualStatisticsModeratorUpdateEvent(Path<? extends SQRhyAnnualStatisticsModeratorUpdateEvent> path) {
        super(path.getType(), path.getMetadata(), "public", "rhy_annual_statistics_moderator_update_event");
        addMetadata();
    }

    public SQRhyAnnualStatisticsModeratorUpdateEvent(PathMetadata metadata) {
        super(SQRhyAnnualStatisticsModeratorUpdateEvent.class, metadata, "public", "rhy_annual_statistics_moderator_update_event");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(dataGroup, ColumnMetadata.named("data_group").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(eventTime, ColumnMetadata.named("event_time").withIndex(5).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(rhyAnnualStatisticsModeratorUpdateEventId, ColumnMetadata.named("rhy_annual_statistics_moderator_update_event_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(statisticsId, ColumnMetadata.named("statistics_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(6).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

