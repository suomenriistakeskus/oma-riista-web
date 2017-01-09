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
 * SQHarvestReport is a Querydsl query type for SQHarvestReport
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestReport extends RelationalPathSpatial<SQHarvestReport> {

    private static final long serialVersionUID = -2006253046;

    public static final SQHarvestReport harvestReport = new SQHarvestReport("harvest_report");

    public final NumberPath<Long> authorId = createNumber("authorId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> harvestPermitId = createNumber("harvestPermitId", Long.class);

    public final NumberPath<Long> harvestReportId = createNumber("harvestReportId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath state = createString("state");

    public final com.querydsl.sql.PrimaryKey<SQHarvestReport> harvestReportPkey = createPrimaryKey(harvestReportId);

    public final com.querydsl.sql.ForeignKey<SQPerson> harvestReportAuthorFk = createForeignKey(authorId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportState> harvestReportStateFk = createForeignKey(state, "name");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> harvestReportHarvestPermitFk = createForeignKey(harvestPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> _harvestPermitEndOfHuntingReportFk = createInvForeignKey(harvestReportId, "end_of_hunting_report_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportStateHistory> _harvestReportStateHistoryHarvestReportFk = createInvForeignKey(harvestReportId, "harvest_report_id");

    public final com.querydsl.sql.ForeignKey<SQHarvest> _harvestHarvestReportFk = createInvForeignKey(harvestReportId, "harvest_report_id");

    public SQHarvestReport(String variable) {
        super(SQHarvestReport.class, forVariable(variable), "public", "harvest_report");
        addMetadata();
    }

    public SQHarvestReport(String variable, String schema, String table) {
        super(SQHarvestReport.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestReport(Path<? extends SQHarvestReport> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_report");
        addMetadata();
    }

    public SQHarvestReport(PathMetadata metadata) {
        super(SQHarvestReport.class, metadata, "public", "harvest_report");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(authorId, ColumnMetadata.named("author_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(description, ColumnMetadata.named("description").withIndex(9).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(harvestPermitId, ColumnMetadata.named("harvest_permit_id").withIndex(12).ofType(Types.BIGINT).withSize(19));
        addMetadata(harvestReportId, ColumnMetadata.named("harvest_report_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(state, ColumnMetadata.named("state").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

