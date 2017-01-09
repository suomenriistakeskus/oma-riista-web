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
 * SQHuntingMethod is a Querydsl query type for SQHuntingMethod
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHuntingMethod extends RelationalPathSpatial<SQHuntingMethod> {

    private static final long serialVersionUID = 2006619453;

    public static final SQHuntingMethod huntingMethod = new SQHuntingMethod("hunting_method");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQHuntingMethod> huntingMethodPk = createPrimaryKey(name);

    public SQHuntingMethod(String variable) {
        super(SQHuntingMethod.class, forVariable(variable), "public", "hunting_method");
        addMetadata();
    }

    public SQHuntingMethod(String variable, String schema, String table) {
        super(SQHuntingMethod.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHuntingMethod(Path<? extends SQHuntingMethod> path) {
        super(path.getType(), path.getMetadata(), "public", "hunting_method");
        addMetadata();
    }

    public SQHuntingMethod(PathMetadata metadata) {
        super(SQHuntingMethod.class, metadata, "public", "hunting_method");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

