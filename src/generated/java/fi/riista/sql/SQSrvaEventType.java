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
 * SQSrvaEventType is a Querydsl query type for SQSrvaEventType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQSrvaEventType extends RelationalPathSpatial<SQSrvaEventType> {

    private static final long serialVersionUID = 1519088343;

    public static final SQSrvaEventType srvaEventType = new SQSrvaEventType("srva_event_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQSrvaEventType> srvaEventTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQSrvaEvent> _srvaEventEventTypeFk = createInvForeignKey(name, "event_type");

    public SQSrvaEventType(String variable) {
        super(SQSrvaEventType.class, forVariable(variable), "public", "srva_event_type");
        addMetadata();
    }

    public SQSrvaEventType(String variable, String schema, String table) {
        super(SQSrvaEventType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQSrvaEventType(String variable, String schema) {
        super(SQSrvaEventType.class, forVariable(variable), schema, "srva_event_type");
        addMetadata();
    }

    public SQSrvaEventType(Path<? extends SQSrvaEventType> path) {
        super(path.getType(), path.getMetadata(), "public", "srva_event_type");
        addMetadata();
    }

    public SQSrvaEventType(PathMetadata metadata) {
        super(SQSrvaEventType.class, metadata, "public", "srva_event_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

