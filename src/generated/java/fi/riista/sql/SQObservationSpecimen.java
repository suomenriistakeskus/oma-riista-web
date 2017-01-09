package fi.riista.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import com.querydsl.sql.spatial.RelationalPathSpatial;


/**
 * SQObservationSpecimen is a Querydsl query type for SQObservationSpecimen
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQObservationSpecimen extends RelationalPathSpatial<SQObservationSpecimen> {

    private static final long serialVersionUID = -2137637471;

    public static final SQObservationSpecimen observationSpecimen = new SQObservationSpecimen("observation_specimen");

    public final StringPath age = createString("age");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> gameObservationId = createNumber("gameObservationId", Long.class);

    public final StringPath gender = createString("gender");

    public final StringPath marking = createString("marking");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> observationSpecimenId = createNumber("observationSpecimenId", Long.class);

    public final StringPath state = createString("state");

    public final com.querydsl.sql.PrimaryKey<SQObservationSpecimen> observationSpecimenPkey = createPrimaryKey(observationSpecimenId);

    public final com.querydsl.sql.ForeignKey<SQObservedGameAge> observationSpecimenAgeFk = createForeignKey(age, "name");

    public final com.querydsl.sql.ForeignKey<SQGameMarking> observationSpecimenMarkingFk = createForeignKey(marking, "name");

    public final com.querydsl.sql.ForeignKey<SQGameGender> observationSpecimenGenderFk = createForeignKey(gender, "name");

    public final com.querydsl.sql.ForeignKey<SQObservation> observationSpecimenObservationFk = createForeignKey(gameObservationId, "game_observation_id");

    public final com.querydsl.sql.ForeignKey<SQObservedGameState> observationSpecimenStateFk = createForeignKey(state, "name");

    public SQObservationSpecimen(String variable) {
        super(SQObservationSpecimen.class, forVariable(variable), "public", "observation_specimen");
        addMetadata();
    }

    public SQObservationSpecimen(String variable, String schema, String table) {
        super(SQObservationSpecimen.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQObservationSpecimen(Path<? extends SQObservationSpecimen> path) {
        super(path.getType(), path.getMetadata(), "public", "observation_specimen");
        addMetadata();
    }

    public SQObservationSpecimen(PathMetadata metadata) {
        super(SQObservationSpecimen.class, metadata, "public", "observation_specimen");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(age, ColumnMetadata.named("age").withIndex(11).ofType(Types.VARCHAR).withSize(255));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(gameObservationId, ColumnMetadata.named("game_observation_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(gender, ColumnMetadata.named("gender").withIndex(10).ofType(Types.VARCHAR).withSize(255));
        addMetadata(marking, ColumnMetadata.named("marking").withIndex(13).ofType(Types.VARCHAR).withSize(255));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(observationSpecimenId, ColumnMetadata.named("observation_specimen_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(state, ColumnMetadata.named("state").withIndex(12).ofType(Types.VARCHAR).withSize(255));
    }

}

