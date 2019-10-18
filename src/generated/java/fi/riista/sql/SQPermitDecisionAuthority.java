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
 * SQPermitDecisionAuthority is a Querydsl query type for SQPermitDecisionAuthority
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionAuthority extends RelationalPathSpatial<SQPermitDecisionAuthority> {

    private static final long serialVersionUID = 732635065;

    public static final SQPermitDecisionAuthority permitDecisionAuthority = new SQPermitDecisionAuthority("permit_decision_authority");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath email = createString("email");

    public final StringPath firstName = createString("firstName");

    public final StringPath lastName = createString("lastName");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> permitDecisionAuthorityId = createNumber("permitDecisionAuthorityId", Long.class);

    public final NumberPath<Long> permitDecisionId = createNumber("permitDecisionId", Long.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath title = createString("title");

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionAuthority> permitDecisionAuthorityPkey = createPrimaryKey(permitDecisionAuthorityId);

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> permitDecisionAuthorityDecisionFk = createForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> _permitDecisionPresenterAuthorityFk = createInvForeignKey(permitDecisionAuthorityId, "presenter_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> _permitDecisionDecisionMakerAuthorityFk = createInvForeignKey(permitDecisionAuthorityId, "decision_maker_id");

    public SQPermitDecisionAuthority(String variable) {
        super(SQPermitDecisionAuthority.class, forVariable(variable), "public", "permit_decision_authority");
        addMetadata();
    }

    public SQPermitDecisionAuthority(String variable, String schema, String table) {
        super(SQPermitDecisionAuthority.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionAuthority(String variable, String schema) {
        super(SQPermitDecisionAuthority.class, forVariable(variable), schema, "permit_decision_authority");
        addMetadata();
    }

    public SQPermitDecisionAuthority(Path<? extends SQPermitDecisionAuthority> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_authority");
        addMetadata();
    }

    public SQPermitDecisionAuthority(PathMetadata metadata) {
        super(SQPermitDecisionAuthority.class, metadata, "public", "permit_decision_authority");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(email, ColumnMetadata.named("email").withIndex(14).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(firstName, ColumnMetadata.named("first_name").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(lastName, ColumnMetadata.named("last_name").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitDecisionAuthorityId, ColumnMetadata.named("permit_decision_authority_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitDecisionId, ColumnMetadata.named("permit_decision_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(phoneNumber, ColumnMetadata.named("phone_number").withIndex(13).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(title, ColumnMetadata.named("title").withIndex(12).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

