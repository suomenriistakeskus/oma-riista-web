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
 * SQHarvestSeason is a Querydsl query type for SQHarvestSeason
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestSeason extends RelationalPathSpatial<SQHarvestSeason> {

    private static final long serialVersionUID = -1978067015;

    public static final SQHarvestSeason harvestSeason = new SQHarvestSeason("harvest_season");

    public final DatePath<java.sql.Date> beginDate = createDate("beginDate", java.sql.Date.class);

    public final DatePath<java.sql.Date> beginDate2 = createDate("beginDate2", java.sql.Date.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DatePath<java.sql.Date> endDate = createDate("endDate", java.sql.Date.class);

    public final DatePath<java.sql.Date> endDate2 = createDate("endDate2", java.sql.Date.class);

    public final DatePath<java.sql.Date> endOfReportingDate = createDate("endOfReportingDate", java.sql.Date.class);

    public final DatePath<java.sql.Date> endOfReportingDate2 = createDate("endOfReportingDate2", java.sql.Date.class);

    public final NumberPath<Long> harvestReportFieldsId = createNumber("harvestReportFieldsId", Long.class);

    public final NumberPath<Long> harvestSeasonId = createNumber("harvestSeasonId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath nameFinnish = createString("nameFinnish");

    public final StringPath nameSwedish = createString("nameSwedish");

    public final com.querydsl.sql.PrimaryKey<SQHarvestSeason> harvestSeasonPkey = createPrimaryKey(harvestSeasonId);

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> harvestSeasonHarvestReportFieldsFk = createForeignKey(harvestReportFieldsId, "harvest_report_fields_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestQuota> _harvestQuotaHarvestSeasonFk = createInvForeignKey(harvestSeasonId, "harvest_season_id");

    public final com.querydsl.sql.ForeignKey<SQHarvest> _harvestHarvestSeasonFk = createInvForeignKey(harvestSeasonId, "harvest_season_id");

    public SQHarvestSeason(String variable) {
        super(SQHarvestSeason.class, forVariable(variable), "public", "harvest_season");
        addMetadata();
    }

    public SQHarvestSeason(String variable, String schema, String table) {
        super(SQHarvestSeason.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestSeason(Path<? extends SQHarvestSeason> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_season");
        addMetadata();
    }

    public SQHarvestSeason(PathMetadata metadata) {
        super(SQHarvestSeason.class, metadata, "public", "harvest_season");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(beginDate, ColumnMetadata.named("begin_date").withIndex(10).ofType(Types.DATE).withSize(13).notNull());
        addMetadata(beginDate2, ColumnMetadata.named("begin_date2").withIndex(15).ofType(Types.DATE).withSize(13));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(endDate, ColumnMetadata.named("end_date").withIndex(11).ofType(Types.DATE).withSize(13).notNull());
        addMetadata(endDate2, ColumnMetadata.named("end_date2").withIndex(16).ofType(Types.DATE).withSize(13));
        addMetadata(endOfReportingDate, ColumnMetadata.named("end_of_reporting_date").withIndex(12).ofType(Types.DATE).withSize(13).notNull());
        addMetadata(endOfReportingDate2, ColumnMetadata.named("end_of_reporting_date2").withIndex(17).ofType(Types.DATE).withSize(13));
        addMetadata(harvestReportFieldsId, ColumnMetadata.named("harvest_report_fields_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestSeasonId, ColumnMetadata.named("harvest_season_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(nameFinnish, ColumnMetadata.named("name_finnish").withIndex(13).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(nameSwedish, ColumnMetadata.named("name_swedish").withIndex(14).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

