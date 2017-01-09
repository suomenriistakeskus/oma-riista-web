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
 * SQJhtTraining is a Querydsl query type for SQJhtTraining
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQJhtTraining extends RelationalPathSpatial<SQJhtTraining> {

    private static final long serialVersionUID = -511887043;

    public static final SQJhtTraining jhtTraining = new SQJhtTraining("jht_training");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath externalId = createString("externalId");

    public final NumberPath<Long> jhtTrainingId = createNumber("jhtTrainingId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath occupationType = createString("occupationType");

    public final NumberPath<Long> personId = createNumber("personId", Long.class);

    public final DatePath<java.sql.Date> trainingDate = createDate("trainingDate", java.sql.Date.class);

    public final StringPath trainingLocation = createString("trainingLocation");

    public final StringPath trainingType = createString("trainingType");

    public final com.querydsl.sql.PrimaryKey<SQJhtTraining> jhtTrainingPkey = createPrimaryKey(jhtTrainingId);

    public final com.querydsl.sql.ForeignKey<SQPerson> jhtTrainingPersonIdFk = createForeignKey(personId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQOccupationType> jhtTrainingOccupationTypeFk = createForeignKey(occupationType, "name");

    public SQJhtTraining(String variable) {
        super(SQJhtTraining.class, forVariable(variable), "public", "jht_training");
        addMetadata();
    }

    public SQJhtTraining(String variable, String schema, String table) {
        super(SQJhtTraining.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQJhtTraining(Path<? extends SQJhtTraining> path) {
        super(path.getType(), path.getMetadata(), "public", "jht_training");
        addMetadata();
    }

    public SQJhtTraining(PathMetadata metadata) {
        super(SQJhtTraining.class, metadata, "public", "jht_training");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(externalId, ColumnMetadata.named("external_id").withIndex(9).ofType(Types.VARCHAR).withSize(255));
        addMetadata(jhtTrainingId, ColumnMetadata.named("jht_training_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(occupationType, ColumnMetadata.named("occupation_type").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(personId, ColumnMetadata.named("person_id").withIndex(14).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(trainingDate, ColumnMetadata.named("training_date").withIndex(12).ofType(Types.DATE).withSize(13).notNull());
        addMetadata(trainingLocation, ColumnMetadata.named("training_location").withIndex(13).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(trainingType, ColumnMetadata.named("training_type").withIndex(11).ofType(Types.CHAR).withSize(1).notNull());
    }

}

