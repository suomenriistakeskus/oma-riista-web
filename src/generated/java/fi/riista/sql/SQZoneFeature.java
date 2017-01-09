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
 * SQZoneFeature is a Querydsl query type for SQZoneFeature
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQZoneFeature extends RelationalPathSpatial<SQZoneFeature> {

    private static final long serialVersionUID = 955727095;

    public static final SQZoneFeature zoneFeature = new SQZoneFeature("zone_feature");

    public final GeometryPath<org.geolatte.geom.Geometry> geom = createGeometry("geom", org.geolatte.geom.Geometry.class);

    public final SimplePath<Integer[]> includedSpecies = createSimple("includedSpecies", Integer[].class);

    public final NumberPath<Long> propertyIdentifier = createNumber("propertyIdentifier", Long.class);

    public final NumberPath<Long> zoneFeatureId = createNumber("zoneFeatureId", Long.class);

    public final NumberPath<Long> zoneId = createNumber("zoneId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQZoneFeature> zoneFeaturePkey = createPrimaryKey(zoneFeatureId);

    public final com.querydsl.sql.ForeignKey<SQZone> zoneFeatureZoneIdFk = createForeignKey(zoneId, "zone_id");

    public SQZoneFeature(String variable) {
        super(SQZoneFeature.class, forVariable(variable), "public", "zone_feature");
        addMetadata();
    }

    public SQZoneFeature(String variable, String schema, String table) {
        super(SQZoneFeature.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQZoneFeature(Path<? extends SQZoneFeature> path) {
        super(path.getType(), path.getMetadata(), "public", "zone_feature");
        addMetadata();
    }

    public SQZoneFeature(PathMetadata metadata) {
        super(SQZoneFeature.class, metadata, "public", "zone_feature");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(geom, ColumnMetadata.named("geom").withIndex(4).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(includedSpecies, ColumnMetadata.named("included_species").withIndex(5).ofType(Types.ARRAY).withSize(10));
        addMetadata(propertyIdentifier, ColumnMetadata.named("property_identifier").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(zoneFeatureId, ColumnMetadata.named("zone_feature_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(zoneId, ColumnMetadata.named("zone_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

