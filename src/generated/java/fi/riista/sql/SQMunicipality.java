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
 * SQMunicipality is a Querydsl query type for SQMunicipality
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMunicipality extends RelationalPathSpatial<SQMunicipality> {

    private static final long serialVersionUID = 16557775;

    public static final SQMunicipality municipality = new SQMunicipality("municipality");

    public final StringPath nameFinnish = createString("nameFinnish");

    public final StringPath nameSwedish = createString("nameSwedish");

    public final StringPath officialCode = createString("officialCode");

    public final com.querydsl.sql.PrimaryKey<SQMunicipality> municipalityPkey = createPrimaryKey(officialCode);

    public SQMunicipality(String variable) {
        super(SQMunicipality.class, forVariable(variable), "public", "municipality");
        addMetadata();
    }

    public SQMunicipality(String variable, String schema, String table) {
        super(SQMunicipality.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQMunicipality(Path<? extends SQMunicipality> path) {
        super(path.getType(), path.getMetadata(), "public", "municipality");
        addMetadata();
    }

    public SQMunicipality(PathMetadata metadata) {
        super(SQMunicipality.class, metadata, "public", "municipality");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(nameFinnish, ColumnMetadata.named("name_finnish").withIndex(2).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(nameSwedish, ColumnMetadata.named("name_swedish").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(officialCode, ColumnMetadata.named("official_code").withIndex(1).ofType(Types.CHAR).withSize(3).notNull());
    }

}

