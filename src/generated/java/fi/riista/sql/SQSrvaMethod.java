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
 * SQSrvaMethod is a Querydsl query type for SQSrvaMethod
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQSrvaMethod extends RelationalPathSpatial<SQSrvaMethod> {

    private static final long serialVersionUID = 1174141406;

    public static final SQSrvaMethod srvaMethod = new SQSrvaMethod("srva_method");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final BooleanPath isChecked = createBoolean("isChecked");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> srvaEventId = createNumber("srvaEventId", Long.class);

    public final NumberPath<Long> srvaMethodId = createNumber("srvaMethodId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQSrvaMethod> srvaEventSrvaMethodPkey = createPrimaryKey(srvaMethodId);

    public final com.querydsl.sql.ForeignKey<SQSrvaMethodType> srvaMethodNameFk = createForeignKey(name, "name");

    public final com.querydsl.sql.ForeignKey<SQSrvaEvent> srvaMethodSrvaEventFk = createForeignKey(srvaEventId, "srva_event_id");

    public SQSrvaMethod(String variable) {
        super(SQSrvaMethod.class, forVariable(variable), "public", "srva_method");
        addMetadata();
    }

    public SQSrvaMethod(String variable, String schema, String table) {
        super(SQSrvaMethod.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQSrvaMethod(Path<? extends SQSrvaMethod> path) {
        super(path.getType(), path.getMetadata(), "public", "srva_method");
        addMetadata();
    }

    public SQSrvaMethod(PathMetadata metadata) {
        super(SQSrvaMethod.class, metadata, "public", "srva_method");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(isChecked, ColumnMetadata.named("is_checked").withIndex(10).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(name, ColumnMetadata.named("name").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(srvaEventId, ColumnMetadata.named("srva_event_id").withIndex(11).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(srvaMethodId, ColumnMetadata.named("srva_method_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

