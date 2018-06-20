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
 * SQValtionmaa is a Querydsl query type for SQValtionmaa
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQValtionmaa extends RelationalPathSpatial<SQValtionmaa> {

    private static final long serialVersionUID = 1491699339;

    public static final SQValtionmaa valtionmaa = new SQValtionmaa("valtionmaa");

    public final GeometryPath<org.geolatte.geom.Geometry> geom = createGeometry("geom", org.geolatte.geom.Geometry.class);

    public final NumberPath<Integer> gid = createNumber("gid", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQValtionmaa> valtionmaaPkey = createPrimaryKey(gid);

    public SQValtionmaa(String variable) {
        super(SQValtionmaa.class, forVariable(variable), "public", "valtionmaa");
        addMetadata();
    }

    public SQValtionmaa(String variable, String schema, String table) {
        super(SQValtionmaa.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQValtionmaa(String variable, String schema) {
        super(SQValtionmaa.class, forVariable(variable), schema, "valtionmaa");
        addMetadata();
    }

    public SQValtionmaa(Path<? extends SQValtionmaa> path) {
        super(path.getType(), path.getMetadata(), "public", "valtionmaa");
        addMetadata();
    }

    public SQValtionmaa(PathMetadata metadata) {
        super(SQValtionmaa.class, metadata, "public", "valtionmaa");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(geom, ColumnMetadata.named("geom").withIndex(2).ofType(Types.OTHER).withSize(2147483647).notNull());
        addMetadata(gid, ColumnMetadata.named("gid").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

