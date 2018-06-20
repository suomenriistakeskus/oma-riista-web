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
 * SQKiinteistoNimet is a Querydsl query type for SQKiinteistoNimet
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQKiinteistoNimet extends RelationalPathSpatial<SQKiinteistoNimet> {

    private static final long serialVersionUID = 242650805;

    public static final SQKiinteistoNimet kiinteistoNimet = new SQKiinteistoNimet("kiinteisto_nimet");

    public final StringPath nimi = createString("nimi");

    public final NumberPath<Long> tunnus = createNumber("tunnus", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQKiinteistoNimet> kiinteistoNimetPkey = createPrimaryKey(tunnus);

    public SQKiinteistoNimet(String variable) {
        super(SQKiinteistoNimet.class, forVariable(variable), "public", "kiinteisto_nimet");
        addMetadata();
    }

    public SQKiinteistoNimet(String variable, String schema, String table) {
        super(SQKiinteistoNimet.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQKiinteistoNimet(String variable, String schema) {
        super(SQKiinteistoNimet.class, forVariable(variable), schema, "kiinteisto_nimet");
        addMetadata();
    }

    public SQKiinteistoNimet(Path<? extends SQKiinteistoNimet> path) {
        super(path.getType(), path.getMetadata(), "public", "kiinteisto_nimet");
        addMetadata();
    }

    public SQKiinteistoNimet(PathMetadata metadata) {
        super(SQKiinteistoNimet.class, metadata, "public", "kiinteisto_nimet");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(nimi, ColumnMetadata.named("nimi").withIndex(2).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(tunnus, ColumnMetadata.named("tunnus").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

