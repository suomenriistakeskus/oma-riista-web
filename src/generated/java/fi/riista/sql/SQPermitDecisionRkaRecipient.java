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
 * SQPermitDecisionRkaRecipient is a Querydsl query type for SQPermitDecisionRkaRecipient
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionRkaRecipient extends RelationalPathSpatial<SQPermitDecisionRkaRecipient> {

    private static final long serialVersionUID = 1759597947;

    public static final SQPermitDecisionRkaRecipient permitDecisionRkaRecipient = new SQPermitDecisionRkaRecipient("permit_decision_rka_recipient");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath email = createString("email");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath nameFinnish = createString("nameFinnish");

    public final StringPath nameSwedish = createString("nameSwedish");

    public final NumberPath<Long> permitDecisionRkaRecipientId = createNumber("permitDecisionRkaRecipientId", Long.class);

    public final NumberPath<Long> rkaId = createNumber("rkaId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionRkaRecipient> permitDecisionRkaRecipientPkey = createPrimaryKey(permitDecisionRkaRecipientId);

    public final com.querydsl.sql.ForeignKey<SQOrganisation> permitDecisionRkaRecipientRkaFk = createForeignKey(rkaId, "organisation_id");

    public SQPermitDecisionRkaRecipient(String variable) {
        super(SQPermitDecisionRkaRecipient.class, forVariable(variable), "public", "permit_decision_rka_recipient");
        addMetadata();
    }

    public SQPermitDecisionRkaRecipient(String variable, String schema, String table) {
        super(SQPermitDecisionRkaRecipient.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionRkaRecipient(String variable, String schema) {
        super(SQPermitDecisionRkaRecipient.class, forVariable(variable), schema, "permit_decision_rka_recipient");
        addMetadata();
    }

    public SQPermitDecisionRkaRecipient(Path<? extends SQPermitDecisionRkaRecipient> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_rka_recipient");
        addMetadata();
    }

    public SQPermitDecisionRkaRecipient(PathMetadata metadata) {
        super(SQPermitDecisionRkaRecipient.class, metadata, "public", "permit_decision_rka_recipient");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(email, ColumnMetadata.named("email").withIndex(12).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(nameFinnish, ColumnMetadata.named("name_finnish").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(nameSwedish, ColumnMetadata.named("name_swedish").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(permitDecisionRkaRecipientId, ColumnMetadata.named("permit_decision_rka_recipient_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(rkaId, ColumnMetadata.named("rka_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

