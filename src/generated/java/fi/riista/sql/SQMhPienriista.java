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
 * SQMhPienriista is a Querydsl query type for SQMhPienriista
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMhPienriista extends RelationalPathSpatial<SQMhPienriista> {

    private static final long serialVersionUID = -1394669314;

    public static final SQMhPienriista mhPienriista = new SQMhPienriista("mh_pienriista");

    public final GeometryPath<org.geolatte.geom.Geometry> geom = createGeometry("geom", org.geolatte.geom.Geometry.class);

    public final NumberPath<Integer> gid = createNumber("gid", Integer.class);

    public final NumberPath<java.math.BigInteger> karttaAla = createNumber("karttaAla", java.math.BigInteger.class);

    public final NumberPath<Integer> kohdeId = createNumber("kohdeId", Integer.class);

    public final NumberPath<Integer> kohdeKoodi = createNumber("kohdeKoodi", Integer.class);

    public final StringPath kohdeNimi = createString("kohdeNimi");

    public final NumberPath<Integer> palstanro = createNumber("palstanro", Integer.class);

    public final NumberPath<java.math.BigInteger> shapeArea = createNumber("shapeArea", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> shapeLeng = createNumber("shapeLeng", java.math.BigInteger.class);

    public final com.querydsl.sql.PrimaryKey<SQMhPienriista> mhPienriistaPkey = createPrimaryKey(gid);

    public SQMhPienriista(String variable) {
        super(SQMhPienriista.class, forVariable(variable), "public", "mh_pienriista");
        addMetadata();
    }

    public SQMhPienriista(String variable, String schema, String table) {
        super(SQMhPienriista.class, forVariable(variable), schema, table);
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
        addMetadata(geom, ColumnMetadata.named("geom").withIndex(8).ofType(Types.OTHER).withSize(2147483647).notNull());
        addMetadata(gid, ColumnMetadata.named("gid").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(karttaAla, ColumnMetadata.named("kartta_ala").withIndex(5).ofType(Types.NUMERIC).withSize(131089).notNull());
        addMetadata(kohdeId, ColumnMetadata.named("kohde_id").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(kohdeKoodi, ColumnMetadata.named("kohde_koodi").withIndex(9).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(kohdeNimi, ColumnMetadata.named("kohde_nimi").withIndex(4).ofType(Types.VARCHAR).withSize(200).notNull());
        addMetadata(palstanro, ColumnMetadata.named("palstanro").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(shapeArea, ColumnMetadata.named("shape_area").withIndex(7).ofType(Types.NUMERIC).withSize(131089).notNull());
        addMetadata(shapeLeng, ColumnMetadata.named("shape_leng").withIndex(6).ofType(Types.NUMERIC).withSize(131089).notNull());
    }

}

