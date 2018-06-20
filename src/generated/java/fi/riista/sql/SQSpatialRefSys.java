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
 * SQSpatialRefSys is a Querydsl query type for SQSpatialRefSys
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQSpatialRefSys extends RelationalPathSpatial<SQSpatialRefSys> {

    private static final long serialVersionUID = -877133973;

    public static final SQSpatialRefSys spatialRefSys = new SQSpatialRefSys("spatial_ref_sys");

    public final StringPath authName = createString("authName");

    public final NumberPath<Integer> authSrid = createNumber("authSrid", Integer.class);

    public final StringPath proj4text = createString("proj4text");

    public final NumberPath<Integer> srid = createNumber("srid", Integer.class);

    public final StringPath srtext = createString("srtext");

    public final com.querydsl.sql.PrimaryKey<SQSpatialRefSys> spatialRefSysPkey = createPrimaryKey(srid);

    public SQSpatialRefSys(String variable) {
        super(SQSpatialRefSys.class, forVariable(variable), "public", "spatial_ref_sys");
        addMetadata();
    }

    public SQSpatialRefSys(String variable, String schema, String table) {
        super(SQSpatialRefSys.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQSpatialRefSys(String variable, String schema) {
        super(SQSpatialRefSys.class, forVariable(variable), schema, "spatial_ref_sys");
        addMetadata();
    }

    public SQSpatialRefSys(Path<? extends SQSpatialRefSys> path) {
        super(path.getType(), path.getMetadata(), "public", "spatial_ref_sys");
        addMetadata();
    }

    public SQSpatialRefSys(PathMetadata metadata) {
        super(SQSpatialRefSys.class, metadata, "public", "spatial_ref_sys");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(authName, ColumnMetadata.named("auth_name").withIndex(2).ofType(Types.VARCHAR).withSize(256));
        addMetadata(authSrid, ColumnMetadata.named("auth_srid").withIndex(3).ofType(Types.INTEGER).withSize(10));
        addMetadata(proj4text, ColumnMetadata.named("proj4text").withIndex(5).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(srid, ColumnMetadata.named("srid").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(srtext, ColumnMetadata.named("srtext").withIndex(4).ofType(Types.VARCHAR).withSize(2048));
    }

}

