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
 * SQHarvestPermitSpeciesAmount is a Querydsl query type for SQHarvestPermitSpeciesAmount
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitSpeciesAmount extends RelationalPathSpatial<SQHarvestPermitSpeciesAmount> {

    private static final long serialVersionUID = 1394569027;

    public static final SQHarvestPermitSpeciesAmount harvestPermitSpeciesAmount = new SQHarvestPermitSpeciesAmount("harvest_permit_species_amount");

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final DatePath<java.sql.Date> beginDate = createDate("beginDate", java.sql.Date.class);

    public final DatePath<java.sql.Date> beginDate2 = createDate("beginDate2", java.sql.Date.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final StringPath creditorReference = createString("creditorReference");

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DatePath<java.sql.Date> endDate = createDate("endDate", java.sql.Date.class);

    public final DatePath<java.sql.Date> endDate2 = createDate("endDate2", java.sql.Date.class);

    public final NumberPath<Long> gameSpeciesId = createNumber("gameSpeciesId", Long.class);

    public final NumberPath<Long> harvestPermitId = createNumber("harvestPermitId", Long.class);

    public final NumberPath<Long> harvestPermitSpeciesAmountId = createNumber("harvestPermitSpeciesAmountId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<java.math.BigDecimal> restrictionAmount = createNumber("restrictionAmount", java.math.BigDecimal.class);

    public final StringPath restrictionType = createString("restrictionType");

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitSpeciesAmount> harvestPermitSpeciesAmountPkey = createPrimaryKey(harvestPermitSpeciesAmountId);

    public final com.querydsl.sql.ForeignKey<SQGameSpecies> harvestPermitSpeciesAmountGameSpeciesFk = createForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> harvestPermitSpeciesAmountPermitFk = createForeignKey(harvestPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQBasicClubHuntingSummary> _basicClubHuntingSummarySpeciesAmountFk = createInvForeignKey(harvestPermitSpeciesAmountId, "species_amount_id");

    public final com.querydsl.sql.ForeignKey<SQMooseHarvestReport> _mooseHarvestReportSpeciesAmountFk = createInvForeignKey(harvestPermitSpeciesAmountId, "species_amount_id");

    public SQHarvestPermitSpeciesAmount(String variable) {
        super(SQHarvestPermitSpeciesAmount.class, forVariable(variable), "public", "harvest_permit_species_amount");
        addMetadata();
    }

    public SQHarvestPermitSpeciesAmount(String variable, String schema, String table) {
        super(SQHarvestPermitSpeciesAmount.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitSpeciesAmount(String variable, String schema) {
        super(SQHarvestPermitSpeciesAmount.class, forVariable(variable), schema, "harvest_permit_species_amount");
        addMetadata();
    }

    public SQHarvestPermitSpeciesAmount(Path<? extends SQHarvestPermitSpeciesAmount> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_species_amount");
        addMetadata();
    }

    public SQHarvestPermitSpeciesAmount(PathMetadata metadata) {
        super(SQHarvestPermitSpeciesAmount.class, metadata, "public", "harvest_permit_species_amount");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(11).ofType(Types.NUMERIC).withSize(6).withDigits(1).notNull());
        addMetadata(beginDate, ColumnMetadata.named("begin_date").withIndex(12).ofType(Types.DATE).withSize(13).notNull());
        addMetadata(beginDate2, ColumnMetadata.named("begin_date2").withIndex(14).ofType(Types.DATE).withSize(13));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(creditorReference, ColumnMetadata.named("creditor_reference").withIndex(18).ofType(Types.VARCHAR).withSize(20));
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(endDate, ColumnMetadata.named("end_date").withIndex(13).ofType(Types.DATE).withSize(13).notNull());
        addMetadata(endDate2, ColumnMetadata.named("end_date2").withIndex(15).ofType(Types.DATE).withSize(13));
        addMetadata(gameSpeciesId, ColumnMetadata.named("game_species_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitId, ColumnMetadata.named("harvest_permit_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitSpeciesAmountId, ColumnMetadata.named("harvest_permit_species_amount_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(restrictionAmount, ColumnMetadata.named("restriction_amount").withIndex(17).ofType(Types.NUMERIC).withSize(6).withDigits(2));
        addMetadata(restrictionType, ColumnMetadata.named("restriction_type").withIndex(16).ofType(Types.VARCHAR).withSize(2));
    }

}

