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
 * SQOccupation is a Querydsl query type for SQOccupation
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQOccupation extends RelationalPathSpatial<SQOccupation> {

    private static final long serialVersionUID = 1822114910;

    public static final SQOccupation occupation = new SQOccupation("occupation");

    public final StringPath additionalInfo = createString("additionalInfo");

    public final DatePath<java.sql.Date> beginDate = createDate("beginDate", java.sql.Date.class);

    public final NumberPath<Integer> callOrder = createNumber("callOrder", Integer.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final StringPath contactInfoShare = createString("contactInfoShare");

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DatePath<java.sql.Date> endDate = createDate("endDate", java.sql.Date.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> occupationId = createNumber("occupationId", Long.class);

    public final StringPath occupationType = createString("occupationType");

    public final NumberPath<Long> organisationId = createNumber("organisationId", Long.class);

    public final NumberPath<Long> personId = createNumber("personId", Long.class);

    public final NumberPath<Integer> qualificationYear = createNumber("qualificationYear", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQOccupation> occupationPkey = createPrimaryKey(occupationId);

    public final com.querydsl.sql.ForeignKey<SQPerson> occupationPersonFk = createForeignKey(personId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> occupationOrganisationFk = createForeignKey(organisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQOccupationType> occupationTypeFk = createForeignKey(occupationType, "name");

    public final com.querydsl.sql.ForeignKey<SQContactInfoShare> occupationContactInfoShareFk = createForeignKey(contactInfoShare, "name");

    public final com.querydsl.sql.ForeignKey<SQShootingTestOfficial> _shootingTestOfficialOccupationFk = createInvForeignKey(occupationId, "occupation_id");

    public final com.querydsl.sql.ForeignKey<SQOccupationNomination> _occupationNominationOccupationIdFk = createInvForeignKey(occupationId, "occupation_id");

    public SQOccupation(String variable) {
        super(SQOccupation.class, forVariable(variable), "public", "occupation");
        addMetadata();
    }

    public SQOccupation(String variable, String schema, String table) {
        super(SQOccupation.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQOccupation(String variable, String schema) {
        super(SQOccupation.class, forVariable(variable), schema, "occupation");
        addMetadata();
    }

    public SQOccupation(Path<? extends SQOccupation> path) {
        super(path.getType(), path.getMetadata(), "public", "occupation");
        addMetadata();
    }

    public SQOccupation(PathMetadata metadata) {
        super(SQOccupation.class, metadata, "public", "occupation");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(additionalInfo, ColumnMetadata.named("additional_info").withIndex(16).ofType(Types.VARCHAR).withSize(255));
        addMetadata(beginDate, ColumnMetadata.named("begin_date").withIndex(12).ofType(Types.DATE).withSize(13));
        addMetadata(callOrder, ColumnMetadata.named("call_order").withIndex(14).ofType(Types.INTEGER).withSize(10));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(contactInfoShare, ColumnMetadata.named("contact_info_share").withIndex(17).ofType(Types.VARCHAR).withSize(255));
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(endDate, ColumnMetadata.named("end_date").withIndex(13).ofType(Types.DATE).withSize(13));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(occupationId, ColumnMetadata.named("occupation_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(occupationType, ColumnMetadata.named("occupation_type").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(organisationId, ColumnMetadata.named("organisation_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(personId, ColumnMetadata.named("person_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(qualificationYear, ColumnMetadata.named("qualification_year").withIndex(15).ofType(Types.INTEGER).withSize(10));
    }

}

