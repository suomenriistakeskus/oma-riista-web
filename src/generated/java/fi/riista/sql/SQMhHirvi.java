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
 * SQMhHirvi is a Querydsl query type for SQMhHirvi
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMhHirvi extends RelationalPathSpatial<SQMhHirvi> {

    private static final long serialVersionUID = 76120822;

    public static final SQMhHirvi mhHirvi = new SQMhHirvi("mh_hirvi");

    public final GeometryPath<org.geolatte.geom.Geometry> geom = createGeometry("geom", org.geolatte.geom.Geometry.class);

    public final NumberPath<Integer> gid = createNumber("gid", Integer.class);

    public final NumberPath<Integer> koodi = createNumber("koodi", Integer.class);

    public final StringPath nimi = createString("nimi");

    public final NumberPath<Long> pintaAla = createNumber("pintaAla", Long.class);

    public final NumberPath<Integer> vuosi = createNumber("vuosi", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQMhHirvi> mhHirviPkey = createPrimaryKey(gid);

    public SQMhHirvi(String variable) {
        super(SQMhHirvi.class, forVariable(variable), "public", "mh_hirvi");
        addMetadata();
    }

    public SQMhHirvi(String variable, String schema, String table) {
        super(SQMhHirvi.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQMhHirvi(String variable, String schema) {
        super(SQMhHirvi.class, forVariable(variable), schema, "mh_hirvi");
        addMetadata();
    }

    public SQMhHirvi(Path<? extends SQMhHirvi> path) {
        super(path.getType(), path.getMetadata(), "public", "mh_hirvi");
        addMetadata();
    }

    public SQMhHirvi(PathMetadata metadata) {
        super(SQMhHirvi.class, metadata, "public", "mh_hirvi");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(geom, ColumnMetadata.named("geom").withIndex(6).ofType(Types.OTHER).withSize(2147483647).notNull());
        addMetadata(gid, ColumnMetadata.named("gid").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(koodi, ColumnMetadata.named("koodi").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(nimi, ColumnMetadata.named("nimi").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(pintaAla, ColumnMetadata.named("pinta_ala").withIndex(5).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(vuosi, ColumnMetadata.named("vuosi").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

