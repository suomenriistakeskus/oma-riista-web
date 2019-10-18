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
 * SQHarvestPermitAreaRhy is a Querydsl query type for SQHarvestPermitAreaRhy
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitAreaRhy extends RelationalPathSpatial<SQHarvestPermitAreaRhy> {

    private static final long serialVersionUID = 1859989253;

    public static final SQHarvestPermitAreaRhy harvestPermitAreaRhy = new SQHarvestPermitAreaRhy("harvest_permit_area_rhy");

    public final NumberPath<Double> areaSize = createNumber("areaSize", Double.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> harvestPermitAreaId = createNumber("harvestPermitAreaId", Long.class);

    public final NumberPath<Long> harvestPermitAreaRhyId = createNumber("harvestPermitAreaRhyId", Long.class);

    public final NumberPath<Double> landSize = createNumber("landSize", Double.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Double> privateLandSize = createNumber("privateLandSize", Double.class);

    public final NumberPath<Double> privateSize = createNumber("privateSize", Double.class);

    public final NumberPath<Double> privateWaterSize = createNumber("privateWaterSize", Double.class);

    public final NumberPath<Long> rhyId = createNumber("rhyId", Long.class);

    public final NumberPath<Double> stateLandSize = createNumber("stateLandSize", Double.class);

    public final NumberPath<Double> stateSize = createNumber("stateSize", Double.class);

    public final NumberPath<Double> stateWaterSize = createNumber("stateWaterSize", Double.class);

    public final NumberPath<Double> waterSize = createNumber("waterSize", Double.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitAreaRhy> harvestPermitAreaRhyPkey = createPrimaryKey(harvestPermitAreaRhyId);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitArea> harvestPermitAreaRhyParentFk = createForeignKey(harvestPermitAreaId, "harvest_permit_area_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestPermitAreaRhyRefFk = createForeignKey(rhyId, "organisation_id");

    public SQHarvestPermitAreaRhy(String variable) {
        super(SQHarvestPermitAreaRhy.class, forVariable(variable), "public", "harvest_permit_area_rhy");
        addMetadata();
    }

    public SQHarvestPermitAreaRhy(String variable, String schema, String table) {
        super(SQHarvestPermitAreaRhy.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitAreaRhy(String variable, String schema) {
        super(SQHarvestPermitAreaRhy.class, forVariable(variable), schema, "harvest_permit_area_rhy");
        addMetadata();
    }

    public SQHarvestPermitAreaRhy(Path<? extends SQHarvestPermitAreaRhy> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_area_rhy");
        addMetadata();
    }

    public SQHarvestPermitAreaRhy(PathMetadata metadata) {
        super(SQHarvestPermitAreaRhy.class, metadata, "public", "harvest_permit_area_rhy");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(areaSize, ColumnMetadata.named("area_size").withIndex(11).ofType(Types.DOUBLE).withSize(17).withDigits(17).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(harvestPermitAreaId, ColumnMetadata.named("harvest_permit_area_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitAreaRhyId, ColumnMetadata.named("harvest_permit_area_rhy_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(landSize, ColumnMetadata.named("land_size").withIndex(12).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(privateLandSize, ColumnMetadata.named("private_land_size").withIndex(18).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(privateSize, ColumnMetadata.named("private_size").withIndex(17).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(privateWaterSize, ColumnMetadata.named("private_water_size").withIndex(19).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(rhyId, ColumnMetadata.named("rhy_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(stateLandSize, ColumnMetadata.named("state_land_size").withIndex(15).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(stateSize, ColumnMetadata.named("state_size").withIndex(14).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(stateWaterSize, ColumnMetadata.named("state_water_size").withIndex(16).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(waterSize, ColumnMetadata.named("water_size").withIndex(13).ofType(Types.DOUBLE).withSize(17).withDigits(17));
    }

}

