package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQForeignPerson is a Querydsl query type for SQForeignPerson
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQForeignPerson extends RelationalPathSpatial<SQForeignPerson> {

    private static final long serialVersionUID = 267452758;

    public static final SQForeignPerson foreignPerson = new SQForeignPerson("foreign_person");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final DatePath<java.sql.Date> dateOfBirth = createDate("dateOfBirth", java.sql.Date.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath firstName = createString("firstName");

    public final NumberPath<Long> foreignPersonId = createNumber("foreignPersonId", Long.class);

    public final StringPath hunterNumber = createString("hunterNumber");

    public final StringPath lastName = createString("lastName");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> mrSyncTime = createDateTime("mrSyncTime", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<SQForeignPerson> foreignPersonPkey = createPrimaryKey(foreignPersonId);

    public final com.querydsl.sql.ForeignKey<SQShootingTestParticipant> _shootingTestParticipantForeignPersonFk = createInvForeignKey(foreignPersonId, "foreign_person_id");

    public SQForeignPerson(String variable) {
        super(SQForeignPerson.class, forVariable(variable), "public", "foreign_person");
        addMetadata();
    }

    public SQForeignPerson(String variable, String schema, String table) {
        super(SQForeignPerson.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQForeignPerson(String variable, String schema) {
        super(SQForeignPerson.class, forVariable(variable), schema, "foreign_person");
        addMetadata();
    }

    public SQForeignPerson(Path<? extends SQForeignPerson> path) {
        super(path.getType(), path.getMetadata(), "public", "foreign_person");
        addMetadata();
    }

    public SQForeignPerson(PathMetadata metadata) {
        super(SQForeignPerson.class, metadata, "public", "foreign_person");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(dateOfBirth, ColumnMetadata.named("date_of_birth").withIndex(12).ofType(Types.DATE).withSize(13).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(firstName, ColumnMetadata.named("first_name").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(foreignPersonId, ColumnMetadata.named("foreign_person_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(hunterNumber, ColumnMetadata.named("hunter_number").withIndex(11).ofType(Types.VARCHAR).withSize(8).notNull());
        addMetadata(lastName, ColumnMetadata.named("last_name").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(mrSyncTime, ColumnMetadata.named("mr_sync_time").withIndex(13).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
    }

}

