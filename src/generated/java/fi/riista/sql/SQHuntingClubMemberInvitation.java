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
 * SQHuntingClubMemberInvitation is a Querydsl query type for SQHuntingClubMemberInvitation
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHuntingClubMemberInvitation extends RelationalPathSpatial<SQHuntingClubMemberInvitation> {

    private static final long serialVersionUID = -1489398267;

    public static final SQHuntingClubMemberInvitation huntingClubMemberInvitation = new SQHuntingClubMemberInvitation("hunting_club_member_invitation");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> huntingClubId = createNumber("huntingClubId", Long.class);

    public final NumberPath<Long> huntingClubMemberInvitationId = createNumber("huntingClubMemberInvitationId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath occupationType = createString("occupationType");

    public final NumberPath<Long> personId = createNumber("personId", Long.class);

    public final DateTimePath<java.sql.Timestamp> userRejectedTime = createDateTime("userRejectedTime", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<SQHuntingClubMemberInvitation> huntingClubMemberInvitationPkey = createPrimaryKey(huntingClubMemberInvitationId);

    public final com.querydsl.sql.ForeignKey<SQOccupationType> huntingClubMemberInvitationOccupationTypeFk = createForeignKey(occupationType, "name");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> huntingClubMemberInvitationHuntingClubFk = createForeignKey(huntingClubId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQPerson> huntingClubMemberInvitationPersonFk = createForeignKey(personId, "person_id");

    public SQHuntingClubMemberInvitation(String variable) {
        super(SQHuntingClubMemberInvitation.class, forVariable(variable), "public", "hunting_club_member_invitation");
        addMetadata();
    }

    public SQHuntingClubMemberInvitation(String variable, String schema, String table) {
        super(SQHuntingClubMemberInvitation.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHuntingClubMemberInvitation(String variable, String schema) {
        super(SQHuntingClubMemberInvitation.class, forVariable(variable), schema, "hunting_club_member_invitation");
        addMetadata();
    }

    public SQHuntingClubMemberInvitation(Path<? extends SQHuntingClubMemberInvitation> path) {
        super(path.getType(), path.getMetadata(), "public", "hunting_club_member_invitation");
        addMetadata();
    }

    public SQHuntingClubMemberInvitation(PathMetadata metadata) {
        super(SQHuntingClubMemberInvitation.class, metadata, "public", "hunting_club_member_invitation");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(huntingClubId, ColumnMetadata.named("hunting_club_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(huntingClubMemberInvitationId, ColumnMetadata.named("hunting_club_member_invitation_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(occupationType, ColumnMetadata.named("occupation_type").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(personId, ColumnMetadata.named("person_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(userRejectedTime, ColumnMetadata.named("user_rejected_time").withIndex(12).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
    }

}

