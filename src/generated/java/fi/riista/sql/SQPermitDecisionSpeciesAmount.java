package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQPermitDecisionSpeciesAmount is a Querydsl query type for SQPermitDecisionSpeciesAmount
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionSpeciesAmount extends RelationalPathSpatial<SQPermitDecisionSpeciesAmount> {

    private static final long serialVersionUID = 915639850;

    public static final SQPermitDecisionSpeciesAmount permitDecisionSpeciesAmount = new SQPermitDecisionSpeciesAmount("permit_decision_species_amount");

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final BooleanPath amountComplete = createBoolean("amountComplete");

    public final DatePath<java.sql.Date> beginDate = createDate("beginDate", java.sql.Date.class);

    public final DatePath<java.sql.Date> beginDate2 = createDate("beginDate2", java.sql.Date.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DatePath<java.sql.Date> endDate = createDate("endDate", java.sql.Date.class);

    public final DatePath<java.sql.Date> endDate2 = createDate("endDate2", java.sql.Date.class);

    public final BooleanPath forbiddenMethodComplete = createBoolean("forbiddenMethodComplete");

    public final NumberPath<Long> gameSpeciesId = createNumber("gameSpeciesId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> permitDecisionId = createNumber("permitDecisionId", Long.class);

    public final NumberPath<Long> permitDecisionSpeciesAmountId = createNumber("permitDecisionSpeciesAmountId", Long.class);

    public final NumberPath<java.math.BigDecimal> restrictionAmount = createNumber("restrictionAmount", java.math.BigDecimal.class);

    public final StringPath restrictionType = createString("restrictionType");

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionSpeciesAmount> permitDecisionSpeciesAmountPkey = createPrimaryKey(permitDecisionSpeciesAmountId);

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> permitDecisionSpeciesAmountDecisionFk = createForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQGameSpecies> permitDecisionSpeciesAmountGameSpeciesFk = createForeignKey(gameSpeciesId, "game_species_id");

    public SQPermitDecisionSpeciesAmount(String variable) {
        super(SQPermitDecisionSpeciesAmount.class, forVariable(variable), "public", "permit_decision_species_amount");
        addMetadata();
    }

    public SQPermitDecisionSpeciesAmount(String variable, String schema, String table) {
        super(SQPermitDecisionSpeciesAmount.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionSpeciesAmount(String variable, String schema) {
        super(SQPermitDecisionSpeciesAmount.class, forVariable(variable), schema, "permit_decision_species_amount");
        addMetadata();
    }

    public SQPermitDecisionSpeciesAmount(Path<? extends SQPermitDecisionSpeciesAmount> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_species_amount");
        addMetadata();
    }

    public SQPermitDecisionSpeciesAmount(PathMetadata metadata) {
        super(SQPermitDecisionSpeciesAmount.class, metadata, "public", "permit_decision_species_amount");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(11).ofType(Types.NUMERIC).withSize(6).withDigits(1).notNull());
        addMetadata(amountComplete, ColumnMetadata.named("amount_complete").withIndex(18).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(beginDate, ColumnMetadata.named("begin_date").withIndex(12).ofType(Types.DATE).withSize(13).notNull());
        addMetadata(beginDate2, ColumnMetadata.named("begin_date2").withIndex(14).ofType(Types.DATE).withSize(13));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(endDate, ColumnMetadata.named("end_date").withIndex(13).ofType(Types.DATE).withSize(13).notNull());
        addMetadata(endDate2, ColumnMetadata.named("end_date2").withIndex(15).ofType(Types.DATE).withSize(13));
        addMetadata(forbiddenMethodComplete, ColumnMetadata.named("forbidden_method_complete").withIndex(19).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(gameSpeciesId, ColumnMetadata.named("game_species_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitDecisionId, ColumnMetadata.named("permit_decision_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitDecisionSpeciesAmountId, ColumnMetadata.named("permit_decision_species_amount_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(restrictionAmount, ColumnMetadata.named("restriction_amount").withIndex(17).ofType(Types.NUMERIC).withSize(6).withDigits(1));
        addMetadata(restrictionType, ColumnMetadata.named("restriction_type").withIndex(16).ofType(Types.VARCHAR).withSize(2));
    }

}

