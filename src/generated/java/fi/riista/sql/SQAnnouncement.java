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
 * SQAnnouncement is a Querydsl query type for SQAnnouncement
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQAnnouncement extends RelationalPathSpatial<SQAnnouncement> {

    private static final long serialVersionUID = 1281404826;

    public static final SQAnnouncement announcement = new SQAnnouncement("announcement");

    public final NumberPath<Long> announcementId = createNumber("announcementId", Long.class);

    public final StringPath body = createString("body");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath fromOccupationType = createString("fromOccupationType");

    public final NumberPath<Long> fromOrganisationId = createNumber("fromOrganisationId", Long.class);

    public final NumberPath<Long> fromUserId = createNumber("fromUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath subject = createString("subject");

    public final com.querydsl.sql.PrimaryKey<SQAnnouncement> announcementPkey = createPrimaryKey(announcementId);

    public final com.querydsl.sql.ForeignKey<SQOccupationType> announcementFromOccupationTypeFk = createForeignKey(fromOccupationType, "name");

    public final com.querydsl.sql.ForeignKey<SQSystemUser> announcementUserIdFk = createForeignKey(fromUserId, "user_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> announcementFromOrganisationFk = createForeignKey(fromOrganisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQAnnouncementSubscriber> _announcementSubscriberAnnouncementFk = createInvForeignKey(announcementId, "announcement_id");

    public SQAnnouncement(String variable) {
        super(SQAnnouncement.class, forVariable(variable), "public", "announcement");
        addMetadata();
    }

    public SQAnnouncement(String variable, String schema, String table) {
        super(SQAnnouncement.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQAnnouncement(Path<? extends SQAnnouncement> path) {
        super(path.getType(), path.getMetadata(), "public", "announcement");
        addMetadata();
    }

    public SQAnnouncement(PathMetadata metadata) {
        super(SQAnnouncement.class, metadata, "public", "announcement");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(announcementId, ColumnMetadata.named("announcement_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(body, ColumnMetadata.named("body").withIndex(10).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(fromOccupationType, ColumnMetadata.named("from_occupation_type").withIndex(12).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(fromOrganisationId, ColumnMetadata.named("from_organisation_id").withIndex(13).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(fromUserId, ColumnMetadata.named("from_user_id").withIndex(11).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(subject, ColumnMetadata.named("subject").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

