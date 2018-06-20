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
 * SQHta is a Querydsl query type for SQHta
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHta extends RelationalPathSpatial<SQHta> {

    private static final long serialVersionUID = 215529762;

    public static final SQHta hta = new SQHta("hta");

    public final GeometryPath<org.geolatte.geom.Geometry> geom = createGeometry("geom", org.geolatte.geom.Geometry.class);

    public final NumberPath<Integer> gid = createNumber("gid", Integer.class);

    public final StringPath nimi = createString("nimi");

    public final StringPath nimiLy = createString("nimiLy");

    public final StringPath nimiSe = createString("nimiSe");

    public final StringPath numero = createString("numero");

    public final com.querydsl.sql.PrimaryKey<SQHta> htaPkey = createPrimaryKey(gid);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> _harvestPermitMooseAreaFk = createInvForeignKey(gid, "moose_area_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitAreaHta> _harvestPermitAreaHtaRefFk = createInvForeignKey(gid, "hta_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> _organisationMooseAreaFk = createInvForeignKey(gid, "moose_area_id");

    public SQHta(String variable) {
        super(SQHta.class, forVariable(variable), "public", "hta");
        addMetadata();
    }

    public SQHta(String variable, String schema, String table) {
        super(SQHta.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHta(String variable, String schema) {
        super(SQHta.class, forVariable(variable), schema, "hta");
        addMetadata();
    }

    public SQHta(Path<? extends SQHta> path) {
        super(path.getType(), path.getMetadata(), "public", "hta");
        addMetadata();
    }

    public SQHta(PathMetadata metadata) {
        super(SQHta.class, metadata, "public", "hta");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(geom, ColumnMetadata.named("geom").withIndex(6).ofType(Types.OTHER).withSize(2147483647).notNull());
        addMetadata(gid, ColumnMetadata.named("gid").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(nimi, ColumnMetadata.named("nimi").withIndex(3).ofType(Types.VARCHAR).withSize(40).notNull());
        addMetadata(nimiLy, ColumnMetadata.named("nimi_ly").withIndex(4).ofType(Types.VARCHAR).withSize(9).notNull());
        addMetadata(nimiSe, ColumnMetadata.named("nimi_se").withIndex(5).ofType(Types.VARCHAR).withSize(54).notNull());
        addMetadata(numero, ColumnMetadata.named("numero").withIndex(2).ofType(Types.VARCHAR).withSize(7).notNull());
    }

}

