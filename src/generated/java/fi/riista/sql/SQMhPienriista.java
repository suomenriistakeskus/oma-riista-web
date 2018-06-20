package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.spatial.GeometryPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * SQMhPienriista is a Querydsl query type for SQMhPienriista
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMhPienriista extends RelationalPathSpatial<SQMhPienriista> {

    private static final long serialVersionUID = -1394669314;

    public static final SQMhPienriista mhPienriista = new SQMhPienriista("mh_pienriista");

    public final GeometryPath<org.geolatte.geom.Geometry> geom = createGeometry("geom", org.geolatte.geom.Geometry.class);

    public final NumberPath<Integer> gid = createNumber("gid", Integer.class);

    public final NumberPath<Integer> koodi = createNumber("koodi", Integer.class);

    public final StringPath nimi = createString("nimi");

    public final NumberPath<Long> pintaAla = createNumber("pintaAla", Long.class);

    public final NumberPath<Integer> vuosi = createNumber("vuosi", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQMhPienriista> mhHirviPkey = createPrimaryKey(gid);

    public SQMhPienriista(String variable) {
        super(SQMhPienriista.class, forVariable(variable), "public", "mh_pienriista");
        addMetadata();
    }

    public SQMhPienriista(String variable, String schema, String table) {
        super(SQMhPienriista.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQMhPienriista(String variable, String schema) {
        super(SQMhPienriista.class, forVariable(variable), schema, "mh_pienriista");
        addMetadata();
    }

    public SQMhPienriista(Path<? extends SQMhPienriista> path) {
        super(path.getType(), path.getMetadata(), "public", "mh_pienriista");
        addMetadata();
    }

    public SQMhPienriista(PathMetadata metadata) {
        super(SQMhPienriista.class, metadata, "public", "mh_pienriista");
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

