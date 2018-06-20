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
 * SQHarvestQuota is a Querydsl query type for SQHarvestQuota
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestQuota extends RelationalPathSpatial<SQHarvestQuota> {

    private static final long serialVersionUID = 2013044450;

    public static final SQHarvestQuota harvestQuota = new SQHarvestQuota("harvest_quota");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> harvestAreaId = createNumber("harvestAreaId", Long.class);

    public final NumberPath<Long> harvestQuotaId = createNumber("harvestQuotaId", Long.class);

    public final NumberPath<Long> harvestSeasonId = createNumber("harvestSeasonId", Long.class);

    public final BooleanPath huntingSuspended = createBoolean("huntingSuspended");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Integer> quota = createNumber("quota", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestQuota> harvestQuotaPkey = createPrimaryKey(harvestQuotaId);

    public final com.querydsl.sql.ForeignKey<SQHarvestArea> harvestQuotaHarvestAreaFk = createForeignKey(harvestAreaId, "harvest_area_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestSeason> harvestQuotaHarvestSeasonFk = createForeignKey(harvestSeasonId, "harvest_season_id");

    public final com.querydsl.sql.ForeignKey<SQHarvest> _harvestHarvestQuotaFk = createInvForeignKey(harvestQuotaId, "harvest_quota_id");

    public SQHarvestQuota(String variable) {
        super(SQHarvestQuota.class, forVariable(variable), "public", "harvest_quota");
        addMetadata();
    }

    public SQHarvestQuota(String variable, String schema, String table) {
        super(SQHarvestQuota.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestQuota(String variable, String schema) {
        super(SQHarvestQuota.class, forVariable(variable), schema, "harvest_quota");
        addMetadata();
    }

    public SQHarvestQuota(Path<? extends SQHarvestQuota> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_quota");
        addMetadata();
    }

    public SQHarvestQuota(PathMetadata metadata) {
        super(SQHarvestQuota.class, metadata, "public", "harvest_quota");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(harvestAreaId, ColumnMetadata.named("harvest_area_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestQuotaId, ColumnMetadata.named("harvest_quota_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestSeasonId, ColumnMetadata.named("harvest_season_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(huntingSuspended, ColumnMetadata.named("hunting_suspended").withIndex(12).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(quota, ColumnMetadata.named("quota").withIndex(11).ofType(Types.INTEGER).withSize(10));
    }

}

