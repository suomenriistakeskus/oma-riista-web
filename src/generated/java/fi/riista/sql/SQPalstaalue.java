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
 * SQPalstaalue is a Querydsl query type for SQPalstaalue
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPalstaalue extends RelationalPathSpatial<SQPalstaalue> {

    private static final long serialVersionUID = 2084880435;

    public static final SQPalstaalue palstaalue = new SQPalstaalue("palstaalue");

    public final GeometryPath<org.geolatte.geom.Geometry> geom = createGeometry("geom", org.geolatte.geom.Geometry.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath ktunnus = createString("ktunnus");

    public final StringPath nimi = createString("nimi");

    public final StringPath tpteksti = createString("tpteksti");

    public final NumberPath<Long> tunnus = createNumber("tunnus", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQPalstaalue> palstaaluePkey = createPrimaryKey(id);

    public SQPalstaalue(String variable) {
        super(SQPalstaalue.class, forVariable(variable), "public", "palstaalue");
        addMetadata();
    }

    public SQPalstaalue(String variable, String schema, String table) {
        super(SQPalstaalue.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPalstaalue(Path<? extends SQPalstaalue> path) {
        super(path.getType(), path.getMetadata(), "public", "palstaalue");
        addMetadata();
    }

    public SQPalstaalue(PathMetadata metadata) {
        super(SQPalstaalue.class, metadata, "public", "palstaalue");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(geom, ColumnMetadata.named("geom").withIndex(6).ofType(Types.OTHER).withSize(2147483647).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(ktunnus, ColumnMetadata.named("ktunnus").withIndex(3).ofType(Types.CHAR).withSize(3).notNull());
        addMetadata(nimi, ColumnMetadata.named("nimi").withIndex(4).ofType(Types.VARCHAR).withSize(120));
        addMetadata(tpteksti, ColumnMetadata.named("tpteksti").withIndex(5).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(tunnus, ColumnMetadata.named("tunnus").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

