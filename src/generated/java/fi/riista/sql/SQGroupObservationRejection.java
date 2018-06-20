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
 * SQGroupObservationRejection is a Querydsl query type for SQGroupObservationRejection
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQGroupObservationRejection extends RelationalPathSpatial<SQGroupObservationRejection> {

    private static final long serialVersionUID = 1017214697;

    public static final SQGroupObservationRejection groupObservationRejection = new SQGroupObservationRejection("group_observation_rejection");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> groupObservationRejectionId = createNumber("groupObservationRejectionId", Long.class);

    public final NumberPath<Long> huntingClubGroupId = createNumber("huntingClubGroupId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> observationId = createNumber("observationId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQGroupObservationRejection> groupObservationRejectionPkey = createPrimaryKey(groupObservationRejectionId);

    public final com.querydsl.sql.ForeignKey<SQGameObservation> groupObservationRejectionGameObservationFk = createForeignKey(observationId, "game_observation_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> groupObservationRejectionHuntingClubGroupFk = createForeignKey(huntingClubGroupId, "organisation_id");

    public SQGroupObservationRejection(String variable) {
        super(SQGroupObservationRejection.class, forVariable(variable), "public", "group_observation_rejection");
        addMetadata();
    }

    public SQGroupObservationRejection(String variable, String schema, String table) {
        super(SQGroupObservationRejection.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQGroupObservationRejection(String variable, String schema) {
        super(SQGroupObservationRejection.class, forVariable(variable), schema, "group_observation_rejection");
        addMetadata();
    }

    public SQGroupObservationRejection(Path<? extends SQGroupObservationRejection> path) {
        super(path.getType(), path.getMetadata(), "public", "group_observation_rejection");
        addMetadata();
    }

    public SQGroupObservationRejection(PathMetadata metadata) {
        super(SQGroupObservationRejection.class, metadata, "public", "group_observation_rejection");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(groupObservationRejectionId, ColumnMetadata.named("group_observation_rejection_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(huntingClubGroupId, ColumnMetadata.named("hunting_club_group_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(observationId, ColumnMetadata.named("observation_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

