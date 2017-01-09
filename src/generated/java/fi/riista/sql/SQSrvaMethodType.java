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
 * SQSrvaMethodType is a Querydsl query type for SQSrvaMethodType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQSrvaMethodType extends RelationalPathSpatial<SQSrvaMethodType> {

    private static final long serialVersionUID = -1850221000;

    public static final SQSrvaMethodType srvaMethodType = new SQSrvaMethodType("srva_method_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQSrvaMethodType> srvaMethodTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQSrvaMethod> _srvaMethodNameFk = createInvForeignKey(name, "name");

    public SQSrvaMethodType(String variable) {
        super(SQSrvaMethodType.class, forVariable(variable), "public", "srva_method_type");
        addMetadata();
    }

    public SQSrvaMethodType(String variable, String schema, String table) {
        super(SQSrvaMethodType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQSrvaMethodType(Path<? extends SQSrvaMethodType> path) {
        super(path.getType(), path.getMetadata(), "public", "srva_method_type");
        addMetadata();
    }

    public SQSrvaMethodType(PathMetadata metadata) {
        super(SQSrvaMethodType.class, metadata, "public", "srva_method_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

