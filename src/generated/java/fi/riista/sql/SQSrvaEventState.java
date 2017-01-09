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
 * SQSrvaEventState is a Querydsl query type for SQSrvaEventState
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQSrvaEventState extends RelationalPathSpatial<SQSrvaEventState> {

    private static final long serialVersionUID = -153987948;

    public static final SQSrvaEventState srvaEventState = new SQSrvaEventState("srva_event_state");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQSrvaEventState> srvaEventStatePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQSrvaEvent> _srvaEventStateFk = createInvForeignKey(name, "state");

    public SQSrvaEventState(String variable) {
        super(SQSrvaEventState.class, forVariable(variable), "public", "srva_event_state");
        addMetadata();
    }

    public SQSrvaEventState(String variable, String schema, String table) {
        super(SQSrvaEventState.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQSrvaEventState(Path<? extends SQSrvaEventState> path) {
        super(path.getType(), path.getMetadata(), "public", "srva_event_state");
        addMetadata();
    }

    public SQSrvaEventState(PathMetadata metadata) {
        super(SQSrvaEventState.class, metadata, "public", "srva_event_state");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

