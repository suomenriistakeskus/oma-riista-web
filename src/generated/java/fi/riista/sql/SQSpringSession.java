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
 * SQSpringSession is a Querydsl query type for SQSpringSession
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQSpringSession extends RelationalPathSpatial<SQSpringSession> {

    private static final long serialVersionUID = 2065237974;

    public static final SQSpringSession springSession = new SQSpringSession("spring_session");

    public final NumberPath<Long> creationTime = createNumber("creationTime", Long.class);

    public final NumberPath<Long> lastAccessTime = createNumber("lastAccessTime", Long.class);

    public final NumberPath<Integer> maxInactiveInterval = createNumber("maxInactiveInterval", Integer.class);

    public final StringPath principalName = createString("principalName");

    public final StringPath sessionId = createString("sessionId");

    public final com.querydsl.sql.PrimaryKey<SQSpringSession> springSessionPk = createPrimaryKey(sessionId);

    public final com.querydsl.sql.ForeignKey<SQSpringSessionAttributes> _springSessionAttributesFk = createInvForeignKey(sessionId, "session_id");

    public SQSpringSession(String variable) {
        super(SQSpringSession.class, forVariable(variable), "public", "spring_session");
        addMetadata();
    }

    public SQSpringSession(String variable, String schema, String table) {
        super(SQSpringSession.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQSpringSession(String variable, String schema) {
        super(SQSpringSession.class, forVariable(variable), schema, "spring_session");
        addMetadata();
    }

    public SQSpringSession(Path<? extends SQSpringSession> path) {
        super(path.getType(), path.getMetadata(), "public", "spring_session");
        addMetadata();
    }

    public SQSpringSession(PathMetadata metadata) {
        super(SQSpringSession.class, metadata, "public", "spring_session");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(lastAccessTime, ColumnMetadata.named("last_access_time").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(maxInactiveInterval, ColumnMetadata.named("max_inactive_interval").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(principalName, ColumnMetadata.named("principal_name").withIndex(5).ofType(Types.VARCHAR).withSize(100));
        addMetadata(sessionId, ColumnMetadata.named("session_id").withIndex(1).ofType(Types.CHAR).withSize(36).notNull());
    }

}

