package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQPermitDecisionDelivery is a Querydsl query type for SQPermitDecisionDelivery
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionDelivery extends RelationalPathSpatial<SQPermitDecisionDelivery> {

    private static final long serialVersionUID = -1971446594;

    public static final SQPermitDecisionDelivery permitDecisionDelivery = new SQPermitDecisionDelivery("permit_decision_delivery");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath email = createString("email");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> permitDecisionDeliveryId = createNumber("permitDecisionDeliveryId", Long.class);

    public final NumberPath<Long> permitDecisionId = createNumber("permitDecisionId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionDelivery> permitDecisionDeliveryPkey = createPrimaryKey(permitDecisionDeliveryId);

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> permitDecisionDeliveryDecisionFk = createForeignKey(permitDecisionId, "permit_decision_id");

    public SQPermitDecisionDelivery(String variable) {
        super(SQPermitDecisionDelivery.class, forVariable(variable), "public", "permit_decision_delivery");
        addMetadata();
    }

    public SQPermitDecisionDelivery(String variable, String schema, String table) {
        super(SQPermitDecisionDelivery.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionDelivery(String variable, String schema) {
        super(SQPermitDecisionDelivery.class, forVariable(variable), schema, "permit_decision_delivery");
        addMetadata();
    }

    public SQPermitDecisionDelivery(Path<? extends SQPermitDecisionDelivery> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_delivery");
        addMetadata();
    }

    public SQPermitDecisionDelivery(PathMetadata metadata) {
        super(SQPermitDecisionDelivery.class, metadata, "public", "permit_decision_delivery");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(email, ColumnMetadata.named("email").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(name, ColumnMetadata.named("name").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(permitDecisionDeliveryId, ColumnMetadata.named("permit_decision_delivery_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitDecisionId, ColumnMetadata.named("permit_decision_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

