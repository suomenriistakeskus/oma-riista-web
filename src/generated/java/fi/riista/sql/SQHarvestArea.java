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
 * SQHarvestArea is a Querydsl query type for SQHarvestArea
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestArea extends RelationalPathSpatial<SQHarvestArea> {

    private static final long serialVersionUID = 480099043;

    public static final SQHarvestArea harvestArea = new SQHarvestArea("harvest_area");

    public final DateTimePath<java.sql.Timestamp> beginDate = createDateTime("beginDate", java.sql.Timestamp.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> endDate = createDateTime("endDate", java.sql.Timestamp.class);

    public final NumberPath<Long> harvestAreaId = createNumber("harvestAreaId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath nameFinnish = createString("nameFinnish");

    public final StringPath nameSwedish = createString("nameSwedish");

    public final StringPath officialCode = createString("officialCode");

    public final StringPath type = createString("type");

    public final com.querydsl.sql.PrimaryKey<SQHarvestArea> harvestAreaPkey = createPrimaryKey(harvestAreaId);

    public final com.querydsl.sql.ForeignKey<SQHarvestAreaType> harvestAreaTypeFk = createForeignKey(type, "name");

    public final com.querydsl.sql.ForeignKey<SQHarvestQuota> _harvestQuotaHarvestAreaFk = createInvForeignKey(harvestAreaId, "harvest_area_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestAreaRhys> _harvestAreaRhysHarvestAreaFk = createInvForeignKey(harvestAreaId, "harvest_area_id");

    public SQHarvestArea(String variable) {
        super(SQHarvestArea.class, forVariable(variable), "public", "harvest_area");
        addMetadata();
    }

    public SQHarvestArea(String variable, String schema, String table) {
        super(SQHarvestArea.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestArea(String variable, String schema) {
        super(SQHarvestArea.class, forVariable(variable), schema, "harvest_area");
        addMetadata();
    }

    public SQHarvestArea(Path<? extends SQHarvestArea> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_area");
        addMetadata();
    }

    public SQHarvestArea(PathMetadata metadata) {
        super(SQHarvestArea.class, metadata, "public", "harvest_area");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(beginDate, ColumnMetadata.named("begin_date").withIndex(13).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(endDate, ColumnMetadata.named("end_date").withIndex(14).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(harvestAreaId, ColumnMetadata.named("harvest_area_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(nameFinnish, ColumnMetadata.named("name_finnish").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(nameSwedish, ColumnMetadata.named("name_swedish").withIndex(12).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(officialCode, ColumnMetadata.named("official_code").withIndex(10).ofType(Types.VARCHAR).withSize(255));
        addMetadata(type, ColumnMetadata.named("type").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

