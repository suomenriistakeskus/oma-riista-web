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
 * SQSrvaEventName is a Querydsl query type for SQSrvaEventName
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQSrvaEventName extends RelationalPathSpatial<SQSrvaEventName> {

    private static final long serialVersionUID = 1518886440;

    public static final SQSrvaEventName srvaEventName = new SQSrvaEventName("srva_event_name");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQSrvaEventName> srvaEventNamePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQSrvaEvent> _srvaEventEventFk = createInvForeignKey(name, "event_name");

    public SQSrvaEventName(String variable) {
        super(SQSrvaEventName.class, forVariable(variable), "public", "srva_event_name");
        addMetadata();
    }

    public SQSrvaEventName(String variable, String schema, String table) {
        super(SQSrvaEventName.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQSrvaEventName(String variable, String schema) {
        super(SQSrvaEventName.class, forVariable(variable), schema, "srva_event_name");
        addMetadata();
    }

    public SQSrvaEventName(Path<? extends SQSrvaEventName> path) {
        super(path.getType(), path.getMetadata(), "public", "srva_event_name");
        addMetadata();
    }

    public SQSrvaEventName(PathMetadata metadata) {
        super(SQSrvaEventName.class, metadata, "public", "srva_event_name");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

