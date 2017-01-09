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
 * SQAnnouncementSubscriber is a Querydsl query type for SQAnnouncementSubscriber
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQAnnouncementSubscriber extends RelationalPathSpatial<SQAnnouncementSubscriber> {

    private static final long serialVersionUID = 1782783906;

    public static final SQAnnouncementSubscriber announcementSubscriber = new SQAnnouncementSubscriber("announcement_subscriber");

    public final NumberPath<Long> announcementId = createNumber("announcementId", Long.class);

    public final NumberPath<Long> announcementSubscriberId = createNumber("announcementSubscriberId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath occupationType = createString("occupationType");

    public final NumberPath<Long> organisationId = createNumber("organisationId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQAnnouncementSubscriber> announcementSubscriberPkey = createPrimaryKey(announcementSubscriberId);

    public final com.querydsl.sql.ForeignKey<SQOccupationType> announcementSubscriberOccupationTypeFk = createForeignKey(occupationType, "name");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> announcementSubscriberOrganisationFk = createForeignKey(organisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQAnnouncement> announcementSubscriberAnnouncementFk = createForeignKey(announcementId, "announcement_id");

    public SQAnnouncementSubscriber(String variable) {
        super(SQAnnouncementSubscriber.class, forVariable(variable), "public", "announcement_subscriber");
        addMetadata();
    }

    public SQAnnouncementSubscriber(String variable, String schema, String table) {
        super(SQAnnouncementSubscriber.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQAnnouncementSubscriber(Path<? extends SQAnnouncementSubscriber> path) {
        super(path.getType(), path.getMetadata(), "public", "announcement_subscriber");
        addMetadata();
    }

    public SQAnnouncementSubscriber(PathMetadata metadata) {
        super(SQAnnouncementSubscriber.class, metadata, "public", "announcement_subscriber");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(announcementId, ColumnMetadata.named("announcement_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(announcementSubscriberId, ColumnMetadata.named("announcement_subscriber_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(occupationType, ColumnMetadata.named("occupation_type").withIndex(11).ofType(Types.VARCHAR).withSize(255));
        addMetadata(organisationId, ColumnMetadata.named("organisation_id").withIndex(10).ofType(Types.BIGINT).withSize(19));
    }

}

