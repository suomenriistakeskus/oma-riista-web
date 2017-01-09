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
 * SQOccupationNomination is a Querydsl query type for SQOccupationNomination
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQOccupationNomination extends RelationalPathSpatial<SQOccupationNomination> {

    private static final long serialVersionUID = 2028812194;

    public static final SQOccupationNomination occupationNomination = new SQOccupationNomination("occupation_nomination");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final DatePath<java.sql.Date> decisionDate = createDate("decisionDate", java.sql.Date.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> moderatorUserId = createNumber("moderatorUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final DatePath<java.sql.Date> nominationDate = createDate("nominationDate", java.sql.Date.class);

    public final StringPath nominationStatus = createString("nominationStatus");

    public final NumberPath<Long> occupationId = createNumber("occupationId", Long.class);

    public final NumberPath<Long> occupationNominationId = createNumber("occupationNominationId", Long.class);

    public final StringPath occupationType = createString("occupationType");

    public final NumberPath<Long> personId = createNumber("personId", Long.class);

    public final NumberPath<Long> rhyId = createNumber("rhyId", Long.class);

    public final NumberPath<Long> rhyPersonId = createNumber("rhyPersonId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQOccupationNomination> occupationNominationPkey = createPrimaryKey(occupationNominationId);

    public final com.querydsl.sql.ForeignKey<SQPerson> occupationNominationPersonIdFk = createForeignKey(personId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQSystemUser> occupationNominationUserIdFk = createForeignKey(moderatorUserId, "user_id");

    public final com.querydsl.sql.ForeignKey<SQPerson> occupationNominationRhyPersonIdFk = createForeignKey(rhyPersonId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQOccupation> occupationNominationOccupationIdFk = createForeignKey(occupationId, "occupation_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> occupationNominationRhyIdFk = createForeignKey(rhyId, "organisation_id");

    public SQOccupationNomination(String variable) {
        super(SQOccupationNomination.class, forVariable(variable), "public", "occupation_nomination");
        addMetadata();
    }

    public SQOccupationNomination(String variable, String schema, String table) {
        super(SQOccupationNomination.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQOccupationNomination(Path<? extends SQOccupationNomination> path) {
        super(path.getType(), path.getMetadata(), "public", "occupation_nomination");
        addMetadata();
    }

    public SQOccupationNomination(PathMetadata metadata) {
        super(SQOccupationNomination.class, metadata, "public", "occupation_nomination");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(decisionDate, ColumnMetadata.named("decision_date").withIndex(12).ofType(Types.DATE).withSize(13));
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(moderatorUserId, ColumnMetadata.named("moderator_user_id").withIndex(16).ofType(Types.BIGINT).withSize(19));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(nominationDate, ColumnMetadata.named("nomination_date").withIndex(11).ofType(Types.DATE).withSize(13));
        addMetadata(nominationStatus, ColumnMetadata.named("nomination_status").withIndex(10).ofType(Types.CHAR).withSize(1).notNull());
        addMetadata(occupationId, ColumnMetadata.named("occupation_id").withIndex(17).ofType(Types.BIGINT).withSize(19));
        addMetadata(occupationNominationId, ColumnMetadata.named("occupation_nomination_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(occupationType, ColumnMetadata.named("occupation_type").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(personId, ColumnMetadata.named("person_id").withIndex(14).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(rhyId, ColumnMetadata.named("rhy_id").withIndex(13).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(rhyPersonId, ColumnMetadata.named("rhy_person_id").withIndex(15).ofType(Types.BIGINT).withSize(19));
    }

}

