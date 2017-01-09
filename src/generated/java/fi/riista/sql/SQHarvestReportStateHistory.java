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
 * SQHarvestReportStateHistory is a Querydsl query type for SQHarvestReportStateHistory
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestReportStateHistory extends RelationalPathSpatial<SQHarvestReportStateHistory> {

    private static final long serialVersionUID = -495633939;

    public static final SQHarvestReportStateHistory harvestReportStateHistory = new SQHarvestReportStateHistory("harvest_report_state_history");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> harvestReportId = createNumber("harvestReportId", Long.class);

    public final NumberPath<Long> harvestReportStateHistoryId = createNumber("harvestReportStateHistoryId", Long.class);

    public final StringPath message = createString("message");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath state = createString("state");

    public final com.querydsl.sql.PrimaryKey<SQHarvestReportStateHistory> harvestReportStateHistoryPkey = createPrimaryKey(harvestReportStateHistoryId);

    public final com.querydsl.sql.ForeignKey<SQHarvestReportState> harvestReportStateHistoryStateFk = createForeignKey(state, "name");

    public final com.querydsl.sql.ForeignKey<SQHarvestReport> harvestReportStateHistoryHarvestReportFk = createForeignKey(harvestReportId, "harvest_report_id");

    public SQHarvestReportStateHistory(String variable) {
        super(SQHarvestReportStateHistory.class, forVariable(variable), "public", "harvest_report_state_history");
        addMetadata();
    }

    public SQHarvestReportStateHistory(String variable, String schema, String table) {
        super(SQHarvestReportStateHistory.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestReportStateHistory(Path<? extends SQHarvestReportStateHistory> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_report_state_history");
        addMetadata();
    }

    public SQHarvestReportStateHistory(PathMetadata metadata) {
        super(SQHarvestReportStateHistory.class, metadata, "public", "harvest_report_state_history");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(harvestReportId, ColumnMetadata.named("harvest_report_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestReportStateHistoryId, ColumnMetadata.named("harvest_report_state_history_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(message, ColumnMetadata.named("message").withIndex(11).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(state, ColumnMetadata.named("state").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

