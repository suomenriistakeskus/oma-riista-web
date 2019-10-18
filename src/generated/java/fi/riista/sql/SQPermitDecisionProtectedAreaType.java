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
 * SQPermitDecisionProtectedAreaType is a Querydsl query type for SQPermitDecisionProtectedAreaType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionProtectedAreaType extends RelationalPathSpatial<SQPermitDecisionProtectedAreaType> {

    private static final long serialVersionUID = 1910000043;

    public static final SQPermitDecisionProtectedAreaType permitDecisionProtectedAreaType = new SQPermitDecisionProtectedAreaType("permit_decision_protected_area_type");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> permitDecisionId = createNumber("permitDecisionId", Long.class);

    public final NumberPath<Long> permitDecisionProtectedAreaTypeId = createNumber("permitDecisionProtectedAreaTypeId", Long.class);

    public final StringPath protectedAreaType = createString("protectedAreaType");

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionProtectedAreaType> permitDecisionProtectedAreaTypePkey = createPrimaryKey(permitDecisionProtectedAreaTypeId);

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> permitDecisionProtectedAreaTypeDecisionIdFk = createForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQBirdPermitApplicationProtectedAreaType> permitDecisionProtectedAreaTypeFk = createForeignKey(protectedAreaType, "name");

    public SQPermitDecisionProtectedAreaType(String variable) {
        super(SQPermitDecisionProtectedAreaType.class, forVariable(variable), "public", "permit_decision_protected_area_type");
        addMetadata();
    }

    public SQPermitDecisionProtectedAreaType(String variable, String schema, String table) {
        super(SQPermitDecisionProtectedAreaType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionProtectedAreaType(String variable, String schema) {
        super(SQPermitDecisionProtectedAreaType.class, forVariable(variable), schema, "permit_decision_protected_area_type");
        addMetadata();
    }

    public SQPermitDecisionProtectedAreaType(Path<? extends SQPermitDecisionProtectedAreaType> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_protected_area_type");
        addMetadata();
    }

    public SQPermitDecisionProtectedAreaType(PathMetadata metadata) {
        super(SQPermitDecisionProtectedAreaType.class, metadata, "public", "permit_decision_protected_area_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitDecisionId, ColumnMetadata.named("permit_decision_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitDecisionProtectedAreaTypeId, ColumnMetadata.named("permit_decision_protected_area_type_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(protectedAreaType, ColumnMetadata.named("protected_area_type").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

