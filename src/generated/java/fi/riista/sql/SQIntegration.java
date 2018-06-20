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
 * SQIntegration is a Querydsl query type for SQIntegration
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQIntegration extends RelationalPathSpatial<SQIntegration> {

    private static final long serialVersionUID = -1024131807;

    public static final SQIntegration integration = new SQIntegration("integration");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath integrationId = createString("integrationId");

    public final DateTimePath<java.sql.Timestamp> lastRun = createDateTime("lastRun", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQIntegration> integrationPkey = createPrimaryKey(integrationId);

    public SQIntegration(String variable) {
        super(SQIntegration.class, forVariable(variable), "public", "integration");
        addMetadata();
    }

    public SQIntegration(String variable, String schema, String table) {
        super(SQIntegration.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQIntegration(String variable, String schema) {
        super(SQIntegration.class, forVariable(variable), schema, "integration");
        addMetadata();
    }

    public SQIntegration(Path<? extends SQIntegration> path) {
        super(path.getType(), path.getMetadata(), "public", "integration");
        addMetadata();
    }

    public SQIntegration(PathMetadata metadata) {
        super(SQIntegration.class, metadata, "public", "integration");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(integrationId, ColumnMetadata.named("integration_id").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(lastRun, ColumnMetadata.named("last_run").withIndex(9).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
    }

}

