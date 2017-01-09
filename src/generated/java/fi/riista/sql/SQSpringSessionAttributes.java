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
 * SQSpringSessionAttributes is a Querydsl query type for SQSpringSessionAttributes
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQSpringSessionAttributes extends RelationalPathSpatial<SQSpringSessionAttributes> {

    private static final long serialVersionUID = 1994996173;

    public static final SQSpringSessionAttributes springSessionAttributes = new SQSpringSessionAttributes("spring_session_attributes");

    public final SimplePath<byte[]> attributeBytes = createSimple("attributeBytes", byte[].class);

    public final StringPath attributeName = createString("attributeName");

    public final StringPath sessionId = createString("sessionId");

    public final com.querydsl.sql.PrimaryKey<SQSpringSessionAttributes> springSessionAttributesPk = createPrimaryKey(sessionId, attributeName);

    public final com.querydsl.sql.ForeignKey<SQSpringSession> springSessionAttributesFk = createForeignKey(sessionId, "session_id");

    public SQSpringSessionAttributes(String variable) {
        super(SQSpringSessionAttributes.class, forVariable(variable), "public", "spring_session_attributes");
        addMetadata();
    }

    public SQSpringSessionAttributes(String variable, String schema, String table) {
        super(SQSpringSessionAttributes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQSpringSessionAttributes(Path<? extends SQSpringSessionAttributes> path) {
        super(path.getType(), path.getMetadata(), "public", "spring_session_attributes");
        addMetadata();
    }

    public SQSpringSessionAttributes(PathMetadata metadata) {
        super(SQSpringSessionAttributes.class, metadata, "public", "spring_session_attributes");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(attributeBytes, ColumnMetadata.named("attribute_bytes").withIndex(3).ofType(Types.BINARY).withSize(2147483647));
        addMetadata(attributeName, ColumnMetadata.named("attribute_name").withIndex(2).ofType(Types.VARCHAR).withSize(200).notNull());
        addMetadata(sessionId, ColumnMetadata.named("session_id").withIndex(1).ofType(Types.CHAR).withSize(36).notNull());
    }

}

