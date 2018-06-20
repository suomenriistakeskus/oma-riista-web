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
 * SQVesialue is a Querydsl query type for SQVesialue
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQVesialue extends RelationalPathSpatial<SQVesialue> {

    private static final long serialVersionUID = -566803821;

    public static final SQVesialue vesialue = new SQVesialue("vesialue");

    public final NumberPath<Double> ainlahde = createNumber("ainlahde", Double.class);

    public final NumberPath<Double> aluejakoon = createNumber("aluejakoon", Double.class);

    public final NumberPath<Double> attr2 = createNumber("attr2", Double.class);

    public final NumberPath<Double> attr3 = createNumber("attr3", Double.class);

    public final GeometryPath<org.geolatte.geom.Geometry> geom = createGeometry("geom", org.geolatte.geom.Geometry.class);

    public final NumberPath<Integer> gid = createNumber("gid", Integer.class);

    public final NumberPath<Double> kartoglk = createNumber("kartoglk", Double.class);

    public final NumberPath<Double> kohdeoso = createNumber("kohdeoso", Double.class);

    public final NumberPath<Double> korarv = createNumber("korarv", Double.class);

    public final NumberPath<Double> korkeus = createNumber("korkeus", Double.class);

    public final NumberPath<Double> kortar = createNumber("kortar", Double.class);

    public final NumberPath<Double> kulkutapa = createNumber("kulkutapa", Double.class);

    public final StringPath kuolhetki = createString("kuolhetki");

    public final NumberPath<Double> luokka = createNumber("luokka", Double.class);

    public final NumberPath<Double> ryhma = createNumber("ryhma", Double.class);

    public final NumberPath<Double> siirtDx = createNumber("siirtDx", Double.class);

    public final NumberPath<Double> siirtDy = createNumber("siirtDy", Double.class);

    public final NumberPath<Double> suunta = createNumber("suunta", Double.class);

    public final StringPath syntyhetki = createString("syntyhetki");

    public final NumberPath<Double> tastar = createNumber("tastar", Double.class);

    public final StringPath teksti = createString("teksti");

    public final NumberPath<Double> versuh = createNumber("versuh", Double.class);

    public final com.querydsl.sql.PrimaryKey<SQVesialue> vesialuePkey = createPrimaryKey(gid);

    public SQVesialue(String variable) {
        super(SQVesialue.class, forVariable(variable), "public", "vesialue");
        addMetadata();
    }

    public SQVesialue(String variable, String schema, String table) {
        super(SQVesialue.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQVesialue(String variable, String schema) {
        super(SQVesialue.class, forVariable(variable), schema, "vesialue");
        addMetadata();
    }

    public SQVesialue(Path<? extends SQVesialue> path) {
        super(path.getType(), path.getMetadata(), "public", "vesialue");
        addMetadata();
    }

    public SQVesialue(PathMetadata metadata) {
        super(SQVesialue.class, metadata, "public", "vesialue");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(ainlahde, ColumnMetadata.named("ainlahde").withIndex(10).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(aluejakoon, ColumnMetadata.named("aluejakoon").withIndex(14).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(attr2, ColumnMetadata.named("attr2").withIndex(20).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(attr3, ColumnMetadata.named("attr3").withIndex(21).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(geom, ColumnMetadata.named("geom").withIndex(22).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(gid, ColumnMetadata.named("gid").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(kartoglk, ColumnMetadata.named("kartoglk").withIndex(13).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(kohdeoso, ColumnMetadata.named("kohdeoso").withIndex(9).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(korarv, ColumnMetadata.named("korarv").withIndex(7).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(korkeus, ColumnMetadata.named("korkeus").withIndex(19).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(kortar, ColumnMetadata.named("kortar").withIndex(6).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(kulkutapa, ColumnMetadata.named("kulkutapa").withIndex(8).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(kuolhetki, ColumnMetadata.named("kuolhetki").withIndex(12).ofType(Types.VARCHAR).withSize(8));
        addMetadata(luokka, ColumnMetadata.named("luokka").withIndex(4).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(ryhma, ColumnMetadata.named("ryhma").withIndex(3).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(siirtDx, ColumnMetadata.named("siirt_dx").withIndex(17).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(siirtDy, ColumnMetadata.named("siirt_dy").withIndex(18).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(suunta, ColumnMetadata.named("suunta").withIndex(16).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(syntyhetki, ColumnMetadata.named("syntyhetki").withIndex(11).ofType(Types.VARCHAR).withSize(8));
        addMetadata(tastar, ColumnMetadata.named("tastar").withIndex(5).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(teksti, ColumnMetadata.named("teksti").withIndex(2).ofType(Types.VARCHAR).withSize(80));
        addMetadata(versuh, ColumnMetadata.named("versuh").withIndex(15).ofType(Types.DOUBLE).withSize(17).withDigits(17));
    }

}

