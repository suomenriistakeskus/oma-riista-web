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
 * SQHarvestChangeHistory is a Querydsl query type for SQHarvestChangeHistory
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestChangeHistory extends RelationalPathSpatial<SQHarvestChangeHistory> {

    private static final long serialVersionUID = -1620559570;

    public static final SQHarvestChangeHistory harvestChangeHistory = new SQHarvestChangeHistory("harvest_change_history");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> harvestChangeHistoryId = createNumber("harvestChangeHistoryId", Long.class);

    public final NumberPath<Long> harvestId = createNumber("harvestId", Long.class);

    public final StringPath harvestReportState = createString("harvestReportState");

    public final DateTimePath<java.sql.Timestamp> pointOfTime = createDateTime("pointOfTime", java.sql.Timestamp.class);

    public final StringPath reasonForChange = createString("reasonForChange");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestChangeHistory> harvestChangeHistoryPkey = createPrimaryKey(harvestChangeHistoryId);

    public final com.querydsl.sql.ForeignKey<SQHarvest> harvestChangeHistoryHarvestFk = createForeignKey(harvestId, "harvest_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportState> harvestChangeHistoryStateFk = createForeignKey(harvestReportState, "name");

    public SQHarvestChangeHistory(String variable) {
        super(SQHarvestChangeHistory.class, forVariable(variable), "public", "harvest_change_history");
        addMetadata();
    }

    public SQHarvestChangeHistory(String variable, String schema, String table) {
        super(SQHarvestChangeHistory.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestChangeHistory(String variable, String schema) {
        super(SQHarvestChangeHistory.class, forVariable(variable), schema, "harvest_change_history");
        addMetadata();
    }

    public SQHarvestChangeHistory(Path<? extends SQHarvestChangeHistory> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_change_history");
        addMetadata();
    }

    public SQHarvestChangeHistory(PathMetadata metadata) {
        super(SQHarvestChangeHistory.class, metadata, "public", "harvest_change_history");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(harvestChangeHistoryId, ColumnMetadata.named("harvest_change_history_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestId, ColumnMetadata.named("harvest_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestReportState, ColumnMetadata.named("harvest_report_state").withIndex(6).ofType(Types.VARCHAR).withSize(255));
        addMetadata(pointOfTime, ColumnMetadata.named("point_of_time").withIndex(4).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(reasonForChange, ColumnMetadata.named("reason_for_change").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
    }

}

