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
 * SQHarvestPermitAllocation is a Querydsl query type for SQHarvestPermitAllocation
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitAllocation extends RelationalPathSpatial<SQHarvestPermitAllocation> {

    private static final long serialVersionUID = -1138318159;

    public static final SQHarvestPermitAllocation harvestPermitAllocation = new SQHarvestPermitAllocation("harvest_permit_allocation");

    public final NumberPath<Integer> adultFemales = createNumber("adultFemales", Integer.class);

    public final NumberPath<Integer> adultMales = createNumber("adultMales", Integer.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> gameSpeciesId = createNumber("gameSpeciesId", Long.class);

    public final NumberPath<Long> harvestPermitAllocationId = createNumber("harvestPermitAllocationId", Long.class);

    public final NumberPath<Long> harvestPermitId = createNumber("harvestPermitId", Long.class);

    public final NumberPath<Long> huntingClubId = createNumber("huntingClubId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<java.math.BigDecimal> total = createNumber("total", java.math.BigDecimal.class);

    public final NumberPath<Integer> young = createNumber("young", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitAllocation> harvestPermitAllocationPkey = createPrimaryKey(harvestPermitAllocationId);

    public final com.querydsl.sql.ForeignKey<SQGameSpecies> harvestPermitAllocationGameSpeciesFk = createForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestPermitAllocationOrganisationFk = createForeignKey(huntingClubId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> harvestPermitAllocationHarvestPermitFk = createForeignKey(harvestPermitId, "harvest_permit_id");

    public SQHarvestPermitAllocation(String variable) {
        super(SQHarvestPermitAllocation.class, forVariable(variable), "public", "harvest_permit_allocation");
        addMetadata();
    }

    public SQHarvestPermitAllocation(String variable, String schema, String table) {
        super(SQHarvestPermitAllocation.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitAllocation(String variable, String schema) {
        super(SQHarvestPermitAllocation.class, forVariable(variable), schema, "harvest_permit_allocation");
        addMetadata();
    }

    public SQHarvestPermitAllocation(Path<? extends SQHarvestPermitAllocation> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_allocation");
        addMetadata();
    }

    public SQHarvestPermitAllocation(PathMetadata metadata) {
        super(SQHarvestPermitAllocation.class, metadata, "public", "harvest_permit_allocation");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(adultFemales, ColumnMetadata.named("adult_females").withIndex(14).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(adultMales, ColumnMetadata.named("adult_males").withIndex(13).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(gameSpeciesId, ColumnMetadata.named("game_species_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitAllocationId, ColumnMetadata.named("harvest_permit_allocation_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitId, ColumnMetadata.named("harvest_permit_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(huntingClubId, ColumnMetadata.named("hunting_club_id").withIndex(11).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(total, ColumnMetadata.named("total").withIndex(12).ofType(Types.NUMERIC).withSize(4).withDigits(1).notNull());
        addMetadata(young, ColumnMetadata.named("young").withIndex(15).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

