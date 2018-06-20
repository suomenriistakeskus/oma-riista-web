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
 * SQHuntingAreaType is a Querydsl query type for SQHuntingAreaType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHuntingAreaType extends RelationalPathSpatial<SQHuntingAreaType> {

    private static final long serialVersionUID = -1301945661;

    public static final SQHuntingAreaType huntingAreaType = new SQHuntingAreaType("hunting_area_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQHuntingAreaType> huntingAreaTypePk = createPrimaryKey(name);

    public SQHuntingAreaType(String variable) {
        super(SQHuntingAreaType.class, forVariable(variable), "public", "hunting_area_type");
        addMetadata();
    }

    public SQHuntingAreaType(String variable, String schema, String table) {
        super(SQHuntingAreaType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHuntingAreaType(String variable, String schema) {
        super(SQHuntingAreaType.class, forVariable(variable), schema, "hunting_area_type");
        addMetadata();
    }

    public SQHuntingAreaType(Path<? extends SQHuntingAreaType> path) {
        super(path.getType(), path.getMetadata(), "public", "hunting_area_type");
        addMetadata();
    }

    public SQHuntingAreaType(PathMetadata metadata) {
        super(SQHuntingAreaType.class, metadata, "public", "hunting_area_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

