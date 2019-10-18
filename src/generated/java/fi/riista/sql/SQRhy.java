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
 * SQRhy is a Querydsl query type for SQRhy
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQRhy extends RelationalPathSpatial<SQRhy> {

    private static final long serialVersionUID = 215539024;

    public static final SQRhy rhy = new SQRhy("rhy");

    public final GeometryPath<org.geolatte.geom.Geometry> geom = createGeometry("geom", org.geolatte.geom.Geometry.class);

    public final NumberPath<Integer> gid = createNumber("gid", Integer.class);

    public final StringPath id = createString("id");

    public final NumberPath<java.math.BigInteger> kokoAla = createNumber("kokoAla", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> maaAla = createNumber("maaAla", java.math.BigInteger.class);

    public final StringPath nimiFi = createString("nimiFi");

    public final StringPath nimiSv = createString("nimiSv");

    public final StringPath nuts2Id = createString("nuts2Id");

    public final StringPath nuts3Id = createString("nuts3Id");

    public final NumberPath<java.math.BigInteger> vesiAla = createNumber("vesiAla", java.math.BigInteger.class);

    public final com.querydsl.sql.PrimaryKey<SQRhy> rhyPkey = createPrimaryKey(gid);

    public SQRhy(String variable) {
        super(SQRhy.class, forVariable(variable), "public", "rhy");
        addMetadata();
    }

    public SQRhy(String variable, String schema, String table) {
        super(SQRhy.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQRhy(String variable, String schema) {
        super(SQRhy.class, forVariable(variable), schema, "rhy");
        addMetadata();
    }

    public SQRhy(Path<? extends SQRhy> path) {
        super(path.getType(), path.getMetadata(), "public", "rhy");
        addMetadata();
    }

    public SQRhy(PathMetadata metadata) {
        super(SQRhy.class, metadata, "public", "rhy");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(geom, ColumnMetadata.named("geom").withIndex(8).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(gid, ColumnMetadata.named("gid").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(7).ofType(Types.CHAR).withSize(3));
        addMetadata(kokoAla, ColumnMetadata.named("koko_ala").withIndex(4).ofType(Types.NUMERIC).withSize(131089));
        addMetadata(maaAla, ColumnMetadata.named("maa_ala").withIndex(3).ofType(Types.NUMERIC).withSize(131089));
        addMetadata(nimiFi, ColumnMetadata.named("nimi_fi").withIndex(5).ofType(Types.VARCHAR).withSize(50));
        addMetadata(nimiSv, ColumnMetadata.named("nimi_sv").withIndex(6).ofType(Types.VARCHAR).withSize(50));
        addMetadata(nuts2Id, ColumnMetadata.named("nuts2_id").withIndex(9).ofType(Types.VARCHAR).withSize(4));
        addMetadata(nuts3Id, ColumnMetadata.named("nuts3_id").withIndex(10).ofType(Types.VARCHAR).withSize(5));
        addMetadata(vesiAla, ColumnMetadata.named("vesi_ala").withIndex(2).ofType(Types.NUMERIC).withSize(131089));
    }

}

