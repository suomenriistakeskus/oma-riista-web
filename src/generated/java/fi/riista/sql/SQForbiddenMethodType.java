package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQForbiddenMethodType is a Querydsl query type for SQForbiddenMethodType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQForbiddenMethodType extends RelationalPathSpatial<SQForbiddenMethodType> {

    private static final long serialVersionUID = -1481901343;

    public static final SQForbiddenMethodType forbiddenMethodType = new SQForbiddenMethodType("forbidden_method_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQForbiddenMethodType> forbiddenMethodTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionForbiddenMethod> _permitDecisionForbiddenMethodTypeFk = createInvForeignKey(name, "method");

    public SQForbiddenMethodType(String variable) {
        super(SQForbiddenMethodType.class, forVariable(variable), "public", "forbidden_method_type");
        addMetadata();
    }

    public SQForbiddenMethodType(String variable, String schema, String table) {
        super(SQForbiddenMethodType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQForbiddenMethodType(String variable, String schema) {
        super(SQForbiddenMethodType.class, forVariable(variable), schema, "forbidden_method_type");
        addMetadata();
    }

    public SQForbiddenMethodType(Path<? extends SQForbiddenMethodType> path) {
        super(path.getType(), path.getMetadata(), "public", "forbidden_method_type");
        addMetadata();
    }

    public SQForbiddenMethodType(PathMetadata metadata) {
        super(SQForbiddenMethodType.class, metadata, "public", "forbidden_method_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

