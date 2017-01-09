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
 * SQSrvaSpecimen is a Querydsl query type for SQSrvaSpecimen
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQSrvaSpecimen extends RelationalPathSpatial<SQSrvaSpecimen> {

    private static final long serialVersionUID = 1378803909;

    public static final SQSrvaSpecimen srvaSpecimen = new SQSrvaSpecimen("srva_specimen");

    public final StringPath age = createString("age");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath gender = createString("gender");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> srvaEventId = createNumber("srvaEventId", Long.class);

    public final NumberPath<Long> srvaSpecimenId = createNumber("srvaSpecimenId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQSrvaSpecimen> srvaEventSrvaSpecimenPkey = createPrimaryKey(srvaSpecimenId);

    public final com.querydsl.sql.ForeignKey<SQGameAge> srvaSpecimenAgeFk = createForeignKey(age, "name");

    public final com.querydsl.sql.ForeignKey<SQGameGender> srvaSpecimenGenderFk = createForeignKey(gender, "name");

    public final com.querydsl.sql.ForeignKey<SQSrvaEvent> srvaSpecimenSrvaEventFk = createForeignKey(srvaEventId, "srva_event_id");

    public SQSrvaSpecimen(String variable) {
        super(SQSrvaSpecimen.class, forVariable(variable), "public", "srva_specimen");
        addMetadata();
    }

    public SQSrvaSpecimen(String variable, String schema, String table) {
        super(SQSrvaSpecimen.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQSrvaSpecimen(Path<? extends SQSrvaSpecimen> path) {
        super(path.getType(), path.getMetadata(), "public", "srva_specimen");
        addMetadata();
    }

    public SQSrvaSpecimen(PathMetadata metadata) {
        super(SQSrvaSpecimen.class, metadata, "public", "srva_specimen");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(age, ColumnMetadata.named("age").withIndex(9).ofType(Types.VARCHAR).withSize(255));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(gender, ColumnMetadata.named("gender").withIndex(10).ofType(Types.VARCHAR).withSize(255));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(srvaEventId, ColumnMetadata.named("srva_event_id").withIndex(11).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(srvaSpecimenId, ColumnMetadata.named("srva_specimen_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

