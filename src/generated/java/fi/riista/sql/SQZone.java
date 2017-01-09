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
 * SQZone is a Querydsl query type for SQZone
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQZone extends RelationalPathSpatial<SQZone> {

    private static final long serialVersionUID = -1907980033;

    public static final SQZone zone = new SQZone("zone");

    public final NumberPath<Double> computedAreaSize = createNumber("computedAreaSize", Double.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final GeometryPath<org.geolatte.geom.Geometry> excludedGeom = createGeometry("excludedGeom", org.geolatte.geom.Geometry.class);

    public final GeometryPath<org.geolatte.geom.Geometry> geom = createGeometry("geom", org.geolatte.geom.Geometry.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath sourceType = createString("sourceType");

    public final StringPath uploadFileId = createString("uploadFileId");

    public final NumberPath<Double> waterAreaSize = createNumber("waterAreaSize", Double.class);

    public final NumberPath<Long> zoneId = createNumber("zoneId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQZone> zonePkey = createPrimaryKey(zoneId);

    public final com.querydsl.sql.ForeignKey<SQFileMetadata> zoneUploadMetadataUuidFk = createForeignKey(uploadFileId, "file_metadata_uuid");

    public final com.querydsl.sql.ForeignKey<SQZoneFeature> _zoneFeatureZoneIdFk = createInvForeignKey(zoneId, "zone_id");

    public final com.querydsl.sql.ForeignKey<SQZoneMhHirvi> _zoneMhHirviZoneFk = createInvForeignKey(zoneId, "zone_id");

    public final com.querydsl.sql.ForeignKey<SQHuntingClubArea> _huntingClubAreaZoneFk = createInvForeignKey(zoneId, "zone_id");

    public final com.querydsl.sql.ForeignKey<SQZonePalsta> _zonePalstaOwnerFk = createInvForeignKey(zoneId, "zone_id");

    public SQZone(String variable) {
        super(SQZone.class, forVariable(variable), "public", "zone");
        addMetadata();
    }

    public SQZone(String variable, String schema, String table) {
        super(SQZone.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQZone(Path<? extends SQZone> path) {
        super(path.getType(), path.getMetadata(), "public", "zone");
        addMetadata();
    }

    public SQZone(PathMetadata metadata) {
        super(SQZone.class, metadata, "public", "zone");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(computedAreaSize, ColumnMetadata.named("computed_area_size").withIndex(13).ofType(Types.DOUBLE).withSize(17).withDigits(17).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(excludedGeom, ColumnMetadata.named("excluded_geom").withIndex(12).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(geom, ColumnMetadata.named("geom").withIndex(9).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(sourceType, ColumnMetadata.named("source_type").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(uploadFileId, ColumnMetadata.named("upload_file_id").withIndex(11).ofType(Types.CHAR).withSize(36));
        addMetadata(waterAreaSize, ColumnMetadata.named("water_area_size").withIndex(14).ofType(Types.DOUBLE).withSize(17).withDigits(17).notNull());
        addMetadata(zoneId, ColumnMetadata.named("zone_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

