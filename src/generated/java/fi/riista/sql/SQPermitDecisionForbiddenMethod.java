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
 * SQPermitDecisionForbiddenMethod is a Querydsl query type for SQPermitDecisionForbiddenMethod
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionForbiddenMethod extends RelationalPathSpatial<SQPermitDecisionForbiddenMethod> {

    private static final long serialVersionUID = 1950163504;

    public static final SQPermitDecisionForbiddenMethod permitDecisionForbiddenMethod = new SQPermitDecisionForbiddenMethod("permit_decision_forbidden_method");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> gameSpeciesId = createNumber("gameSpeciesId", Long.class);

    public final StringPath method = createString("method");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> permitDecisionForbiddenMethodId = createNumber("permitDecisionForbiddenMethodId", Long.class);

    public final NumberPath<Long> permitDecisionId = createNumber("permitDecisionId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionForbiddenMethod> permitDecisionForbiddenMethodPkey = createPrimaryKey(permitDecisionForbiddenMethodId);

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> permitDecisionForbiddenMethodDecisionIdFk = createForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQGameSpecies> permitDecisionForbiddenMethodGameSpeciesIdFk = createForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQForbiddenMethodType> permitDecisionForbiddenMethodTypeFk = createForeignKey(method, "name");

    public SQPermitDecisionForbiddenMethod(String variable) {
        super(SQPermitDecisionForbiddenMethod.class, forVariable(variable), "public", "permit_decision_forbidden_method");
        addMetadata();
    }

    public SQPermitDecisionForbiddenMethod(String variable, String schema, String table) {
        super(SQPermitDecisionForbiddenMethod.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionForbiddenMethod(String variable, String schema) {
        super(SQPermitDecisionForbiddenMethod.class, forVariable(variable), schema, "permit_decision_forbidden_method");
        addMetadata();
    }

    public SQPermitDecisionForbiddenMethod(Path<? extends SQPermitDecisionForbiddenMethod> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_forbidden_method");
        addMetadata();
    }

    public SQPermitDecisionForbiddenMethod(PathMetadata metadata) {
        super(SQPermitDecisionForbiddenMethod.class, metadata, "public", "permit_decision_forbidden_method");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(gameSpeciesId, ColumnMetadata.named("game_species_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(method, ColumnMetadata.named("method").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitDecisionForbiddenMethodId, ColumnMetadata.named("permit_decision_forbidden_method_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitDecisionId, ColumnMetadata.named("permit_decision_id").withIndex(9).ofType(Types.BIGINT).withSize(19));
    }

}

