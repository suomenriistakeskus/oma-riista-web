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
 * SQSrvaResultType is a Querydsl query type for SQSrvaResultType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQSrvaResultType extends RelationalPathSpatial<SQSrvaResultType> {

    private static final long serialVersionUID = -1699096716;

    public static final SQSrvaResultType srvaResultType = new SQSrvaResultType("srva_result_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQSrvaResultType> srvaResultTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQSrvaEvent> _srvaEventEventResultFk = createInvForeignKey(name, "event_result");

    public SQSrvaResultType(String variable) {
        super(SQSrvaResultType.class, forVariable(variable), "public", "srva_result_type");
        addMetadata();
    }

    public SQSrvaResultType(String variable, String schema, String table) {
        super(SQSrvaResultType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQSrvaResultType(Path<? extends SQSrvaResultType> path) {
        super(path.getType(), path.getMetadata(), "public", "srva_result_type");
        addMetadata();
    }

    public SQSrvaResultType(PathMetadata metadata) {
        super(SQSrvaResultType.class, metadata, "public", "srva_result_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

