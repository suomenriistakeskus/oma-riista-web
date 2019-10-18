package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQZoneAreaSize is a Querydsl query type for SQZoneAreaSize
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQZoneAreaSize extends RelationalPathSpatial<SQZoneAreaSize> {

    private static final long serialVersionUID = -1812753331;

    public static final SQZoneAreaSize zoneAreaSize = new SQZoneAreaSize("zone_area_size");

    public final NumberPath<Double> landArea = createNumber("landArea", Double.class);

    public final NumberPath<Double> privateLandArea = createNumber("privateLandArea", Double.class);

    public final NumberPath<Double> privateTotalArea = createNumber("privateTotalArea", Double.class);

    public final NumberPath<Double> privateWaterArea = createNumber("privateWaterArea", Double.class);

    public final NumberPath<Double> stateLandArea = createNumber("stateLandArea", Double.class);

    public final NumberPath<Double> stateTotalArea = createNumber("stateTotalArea", Double.class);

    public final NumberPath<Double> stateWaterArea = createNumber("stateWaterArea", Double.class);

    public final NumberPath<Double> totalArea = createNumber("totalArea", Double.class);

    public final NumberPath<Double> waterArea = createNumber("waterArea", Double.class);

    public final NumberPath<Long> zoneId = createNumber("zoneId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQZoneAreaSize> zoneAreaSizePkey = createPrimaryKey(zoneId);

    public SQZoneAreaSize(String variable) {
        super(SQZoneAreaSize.class, forVariable(variable), "public", "zone_area_size");
        addMetadata();
    }

    public SQZoneAreaSize(String variable, String schema, String table) {
        super(SQZoneAreaSize.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQZoneAreaSize(String variable, String schema) {
        super(SQZoneAreaSize.class, forVariable(variable), schema, "zone_area_size");
        addMetadata();
    }

    public SQZoneAreaSize(Path<? extends SQZoneAreaSize> path) {
        super(path.getType(), path.getMetadata(), "public", "zone_area_size");
        addMetadata();
    }

    public SQZoneAreaSize(PathMetadata metadata) {
        super(SQZoneAreaSize.class, metadata, "public", "zone_area_size");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(landArea, ColumnMetadata.named("land_area").withIndex(4).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(privateLandArea, ColumnMetadata.named("private_land_area").withIndex(10).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(privateTotalArea, ColumnMetadata.named("private_total_area").withIndex(8).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(privateWaterArea, ColumnMetadata.named("private_water_area").withIndex(9).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(stateLandArea, ColumnMetadata.named("state_land_area").withIndex(7).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(stateTotalArea, ColumnMetadata.named("state_total_area").withIndex(5).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(stateWaterArea, ColumnMetadata.named("state_water_area").withIndex(6).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(totalArea, ColumnMetadata.named("total_area").withIndex(2).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(waterArea, ColumnMetadata.named("water_area").withIndex(3).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(zoneId, ColumnMetadata.named("zone_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

