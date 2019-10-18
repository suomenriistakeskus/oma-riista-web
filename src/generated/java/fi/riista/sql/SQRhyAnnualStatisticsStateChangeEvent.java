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
 * SQRhyAnnualStatisticsStateChangeEvent is a Querydsl query type for SQRhyAnnualStatisticsStateChangeEvent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQRhyAnnualStatisticsStateChangeEvent extends RelationalPathSpatial<SQRhyAnnualStatisticsStateChangeEvent> {

    private static final long serialVersionUID = -227532437;

    public static final SQRhyAnnualStatisticsStateChangeEvent rhyAnnualStatisticsStateChangeEvent = new SQRhyAnnualStatisticsStateChangeEvent("rhy_annual_statistics_state_change_event");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final DateTimePath<java.sql.Timestamp> eventTime = createDateTime("eventTime", java.sql.Timestamp.class);

    public final NumberPath<Long> rhyAnnualStatisticsStateChangeEventId = createNumber("rhyAnnualStatisticsStateChangeEventId", Long.class);

    public final StringPath state = createString("state");

    public final NumberPath<Long> statisticsId = createNumber("statisticsId", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQRhyAnnualStatisticsStateChangeEvent> rhyAnnualStatisticsStateChangeEventPkey = createPrimaryKey(rhyAnnualStatisticsStateChangeEventId);

    public final com.querydsl.sql.ForeignKey<SQRhyAnnualStatistics> rhyAnnualStatisticsStateChangeEventStatisticsFk = createForeignKey(statisticsId, "rhy_annual_statistics_id");

    public final com.querydsl.sql.ForeignKey<SQRhyAnnualStatisticsState> rhyAnnualStatisticsStateChangeEventStateFk = createForeignKey(state, "name");

    public SQRhyAnnualStatisticsStateChangeEvent(String variable) {
        super(SQRhyAnnualStatisticsStateChangeEvent.class, forVariable(variable), "public", "rhy_annual_statistics_state_change_event");
        addMetadata();
    }

    public SQRhyAnnualStatisticsStateChangeEvent(String variable, String schema, String table) {
        super(SQRhyAnnualStatisticsStateChangeEvent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQRhyAnnualStatisticsStateChangeEvent(String variable, String schema) {
        super(SQRhyAnnualStatisticsStateChangeEvent.class, forVariable(variable), schema, "rhy_annual_statistics_state_change_event");
        addMetadata();
    }

    public SQRhyAnnualStatisticsStateChangeEvent(Path<? extends SQRhyAnnualStatisticsStateChangeEvent> path) {
        super(path.getType(), path.getMetadata(), "public", "rhy_annual_statistics_state_change_event");
        addMetadata();
    }

    public SQRhyAnnualStatisticsStateChangeEvent(PathMetadata metadata) {
        super(SQRhyAnnualStatisticsStateChangeEvent.class, metadata, "public", "rhy_annual_statistics_state_change_event");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(eventTime, ColumnMetadata.named("event_time").withIndex(5).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(rhyAnnualStatisticsStateChangeEventId, ColumnMetadata.named("rhy_annual_statistics_state_change_event_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(state, ColumnMetadata.named("state").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(statisticsId, ColumnMetadata.named("statistics_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(6).ofType(Types.BIGINT).withSize(19));
    }

}

