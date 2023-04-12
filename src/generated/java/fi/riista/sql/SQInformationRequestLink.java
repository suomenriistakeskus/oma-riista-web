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
 * SQInformationRequestLink is a Querydsl query type for SQInformationRequestLink
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQInformationRequestLink extends RelationalPathSpatial<SQInformationRequestLink> {

    private static final long serialVersionUID = 261459184;

    public static final SQInformationRequestLink informationRequestLink = new SQInformationRequestLink("information_request_link");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> informationRequestLinkId = createNumber("informationRequestLinkId", Long.class);

    public final StringPath informationRequestLinkType = createString("informationRequestLinkType");

    public final StringPath linkIdentifier = createString("linkIdentifier");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> permitDecisionId = createNumber("permitDecisionId", Long.class);

    public final StringPath recipientEmail = createString("recipientEmail");

    public final StringPath recipientName = createString("recipientName");

    public final StringPath title = createString("title");

    public final DateTimePath<java.sql.Timestamp> validUntil = createDateTime("validUntil", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<SQInformationRequestLink> informationRequestLinkPkey = createPrimaryKey(informationRequestLinkId);

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> informationRequestLinkDecisionFk = createForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQInformationRequestLinkType> informationRequestLinkTypeFk = createForeignKey(informationRequestLinkType, "type");

    public SQInformationRequestLink(String variable) {
        super(SQInformationRequestLink.class, forVariable(variable), "public", "information_request_link");
        addMetadata();
    }

    public SQInformationRequestLink(String variable, String schema, String table) {
        super(SQInformationRequestLink.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQInformationRequestLink(String variable, String schema) {
        super(SQInformationRequestLink.class, forVariable(variable), schema, "information_request_link");
        addMetadata();
    }

    public SQInformationRequestLink(Path<? extends SQInformationRequestLink> path) {
        super(path.getType(), path.getMetadata(), "public", "information_request_link");
        addMetadata();
    }

    public SQInformationRequestLink(PathMetadata metadata) {
        super(SQInformationRequestLink.class, metadata, "public", "information_request_link");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(description, ColumnMetadata.named("description").withIndex(16).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(informationRequestLinkId, ColumnMetadata.named("information_request_link_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(informationRequestLinkType, ColumnMetadata.named("information_request_link_type").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(linkIdentifier, ColumnMetadata.named("link_identifier").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitDecisionId, ColumnMetadata.named("permit_decision_id").withIndex(11).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(recipientEmail, ColumnMetadata.named("recipient_email").withIndex(12).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(recipientName, ColumnMetadata.named("recipient_name").withIndex(13).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(title, ColumnMetadata.named("title").withIndex(15).ofType(Types.VARCHAR).withSize(255));
        addMetadata(validUntil, ColumnMetadata.named("valid_until").withIndex(14).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
    }

}

