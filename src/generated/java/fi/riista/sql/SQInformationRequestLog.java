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
 * SQInformationRequestLog is a Querydsl query type for SQInformationRequestLog
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQInformationRequestLog extends RelationalPathSpatial<SQInformationRequestLog> {

    private static final long serialVersionUID = -822849650;

    public static final SQInformationRequestLog informationRequestLog = new SQInformationRequestLog("information_request_log");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> informationRequestLinkId = createNumber("informationRequestLinkId", Long.class);

    public final NumberPath<Long> informationRequestLogId = createNumber("informationRequestLogId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> permitDecisionId = createNumber("permitDecisionId", Long.class);

    public final StringPath permitTypeCode = createString("permitTypeCode");

    public final com.querydsl.sql.PrimaryKey<SQInformationRequestLog> informationRequestLogPkey = createPrimaryKey(informationRequestLogId);

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> informationRequestLogDecisionFk = createForeignKey(permitDecisionId, "permit_decision_id");

    public SQInformationRequestLog(String variable) {
        super(SQInformationRequestLog.class, forVariable(variable), "public", "information_request_log");
        addMetadata();
    }

    public SQInformationRequestLog(String variable, String schema, String table) {
        super(SQInformationRequestLog.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQInformationRequestLog(String variable, String schema) {
        super(SQInformationRequestLog.class, forVariable(variable), schema, "information_request_log");
        addMetadata();
    }

    public SQInformationRequestLog(Path<? extends SQInformationRequestLog> path) {
        super(path.getType(), path.getMetadata(), "public", "information_request_log");
        addMetadata();
    }

    public SQInformationRequestLog(PathMetadata metadata) {
        super(SQInformationRequestLog.class, metadata, "public", "information_request_log");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(9).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(informationRequestLinkId, ColumnMetadata.named("information_request_link_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(informationRequestLogId, ColumnMetadata.named("information_request_log_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(6).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitDecisionId, ColumnMetadata.named("permit_decision_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitTypeCode, ColumnMetadata.named("permit_type_code").withIndex(11).ofType(Types.VARCHAR).withSize(3));
    }

}

