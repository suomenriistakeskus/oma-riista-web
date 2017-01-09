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
 * SQZonePalsta is a Querydsl query type for SQZonePalsta
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQZonePalsta extends RelationalPathSpatial<SQZonePalsta> {

    private static final long serialVersionUID = -794624636;

    public static final SQZonePalsta zonePalsta = new SQZonePalsta("zone_palsta");

    public final NumberPath<Double> diffArea = createNumber("diffArea", Double.class);

    public final GeometryPath<org.geolatte.geom.Geometry> geom = createGeometry("geom", org.geolatte.geom.Geometry.class);

    public final BooleanPath isChanged = createBoolean("isChanged");

    public final NumberPath<Integer> newPalstaId = createNumber("newPalstaId", Integer.class);

    public final NumberPath<Long> newPalstaTunnus = createNumber("newPalstaTunnus", Long.class);

    public final NumberPath<Integer> palstaId = createNumber("palstaId", Integer.class);

    public final NumberPath<Long> palstaTunnus = createNumber("palstaTunnus", Long.class);

    public final NumberPath<Long> zoneId = createNumber("zoneId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQZonePalsta> zonePalstaPkey = createPrimaryKey(zoneId, palstaId);

    public final com.querydsl.sql.ForeignKey<SQZone> zonePalstaOwnerFk = createForeignKey(zoneId, "zone_id");

    public SQZonePalsta(String variable) {
        super(SQZonePalsta.class, forVariable(variable), "public", "zone_palsta");
        addMetadata();
    }

    public SQZonePalsta(String variable, String schema, String table) {
        super(SQZonePalsta.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQZonePalsta(Path<? extends SQZonePalsta> path) {
        super(path.getType(), path.getMetadata(), "public", "zone_palsta");
        addMetadata();
    }

    public SQZonePalsta(PathMetadata metadata) {
        super(SQZonePalsta.class, metadata, "public", "zone_palsta");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(diffArea, ColumnMetadata.named("diff_area").withIndex(8).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(geom, ColumnMetadata.named("geom").withIndex(3).ofType(Types.OTHER).withSize(2147483647).notNull());
        addMetadata(isChanged, ColumnMetadata.named("is_changed").withIndex(5).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(newPalstaId, ColumnMetadata.named("new_palsta_id").withIndex(6).ofType(Types.INTEGER).withSize(10));
        addMetadata(newPalstaTunnus, ColumnMetadata.named("new_palsta_tunnus").withIndex(7).ofType(Types.BIGINT).withSize(19));
        addMetadata(palstaId, ColumnMetadata.named("palsta_id").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(palstaTunnus, ColumnMetadata.named("palsta_tunnus").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(zoneId, ColumnMetadata.named("zone_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

