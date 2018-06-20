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
 * SQShootingTestOfficial is a Querydsl query type for SQShootingTestOfficial
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQShootingTestOfficial extends RelationalPathSpatial<SQShootingTestOfficial> {

    private static final long serialVersionUID = -345574413;

    public static final SQShootingTestOfficial shootingTestOfficial = new SQShootingTestOfficial("shooting_test_official");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> occupationId = createNumber("occupationId", Long.class);

    public final NumberPath<Long> shootingTestEventId = createNumber("shootingTestEventId", Long.class);

    public final NumberPath<Long> shootingTestOfficialId = createNumber("shootingTestOfficialId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQShootingTestOfficial> shootingTestOfficialPkey = createPrimaryKey(shootingTestOfficialId);

    public final com.querydsl.sql.ForeignKey<SQOccupation> shootingTestOfficialOccupationFk = createForeignKey(occupationId, "occupation_id");

    public final com.querydsl.sql.ForeignKey<SQShootingTestEvent> shootingTestOfficialShootingTestEventFk = createForeignKey(shootingTestEventId, "shooting_test_event_id");

    public SQShootingTestOfficial(String variable) {
        super(SQShootingTestOfficial.class, forVariable(variable), "public", "shooting_test_official");
        addMetadata();
    }

    public SQShootingTestOfficial(String variable, String schema, String table) {
        super(SQShootingTestOfficial.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQShootingTestOfficial(String variable, String schema) {
        super(SQShootingTestOfficial.class, forVariable(variable), schema, "shooting_test_official");
        addMetadata();
    }

    public SQShootingTestOfficial(Path<? extends SQShootingTestOfficial> path) {
        super(path.getType(), path.getMetadata(), "public", "shooting_test_official");
        addMetadata();
    }

    public SQShootingTestOfficial(PathMetadata metadata) {
        super(SQShootingTestOfficial.class, metadata, "public", "shooting_test_official");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(occupationId, ColumnMetadata.named("occupation_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(shootingTestEventId, ColumnMetadata.named("shooting_test_event_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(shootingTestOfficialId, ColumnMetadata.named("shooting_test_official_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

