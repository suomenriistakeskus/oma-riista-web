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
 * SQHarvestPermitAreaHta is a Querydsl query type for SQHarvestPermitAreaHta
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitAreaHta extends RelationalPathSpatial<SQHarvestPermitAreaHta> {

    private static final long serialVersionUID = 1859979991;

    public static final SQHarvestPermitAreaHta harvestPermitAreaHta = new SQHarvestPermitAreaHta("harvest_permit_area_hta");

    public final NumberPath<Double> areaSize = createNumber("areaSize", Double.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> harvestPermitAreaHtaId = createNumber("harvestPermitAreaHtaId", Long.class);

    public final NumberPath<Long> harvestPermitAreaId = createNumber("harvestPermitAreaId", Long.class);

    public final NumberPath<Integer> htaId = createNumber("htaId", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitAreaHta> harvestPermitAreaHtaPkey = createPrimaryKey(harvestPermitAreaHtaId);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitArea> harvestPermitAreaHtaParentFk = createForeignKey(harvestPermitAreaId, "harvest_permit_area_id");

    public final com.querydsl.sql.ForeignKey<SQHta> harvestPermitAreaHtaRefFk = createForeignKey(htaId, "gid");

    public SQHarvestPermitAreaHta(String variable) {
        super(SQHarvestPermitAreaHta.class, forVariable(variable), "public", "harvest_permit_area_hta");
        addMetadata();
    }

    public SQHarvestPermitAreaHta(String variable, String schema, String table) {
        super(SQHarvestPermitAreaHta.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitAreaHta(String variable, String schema) {
        super(SQHarvestPermitAreaHta.class, forVariable(variable), schema, "harvest_permit_area_hta");
        addMetadata();
    }

    public SQHarvestPermitAreaHta(Path<? extends SQHarvestPermitAreaHta> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_area_hta");
        addMetadata();
    }

    public SQHarvestPermitAreaHta(PathMetadata metadata) {
        super(SQHarvestPermitAreaHta.class, metadata, "public", "harvest_permit_area_hta");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(areaSize, ColumnMetadata.named("area_size").withIndex(11).ofType(Types.DOUBLE).withSize(17).withDigits(17).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(harvestPermitAreaHtaId, ColumnMetadata.named("harvest_permit_area_hta_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitAreaId, ColumnMetadata.named("harvest_permit_area_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(htaId, ColumnMetadata.named("hta_id").withIndex(10).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
    }

}

