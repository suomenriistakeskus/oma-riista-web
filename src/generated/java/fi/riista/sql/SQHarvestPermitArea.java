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
 * SQHarvestPermitArea is a Querydsl query type for SQHarvestPermitArea
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitArea extends RelationalPathSpatial<SQHarvestPermitArea> {

    private static final long serialVersionUID = -439511778;

    public static final SQHarvestPermitArea harvestPermitArea = new SQHarvestPermitArea("harvest_permit_area");

    public final NumberPath<Long> clubId = createNumber("clubId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath externalId = createString("externalId");

    public final BooleanPath freeHunting = createBoolean("freeHunting");

    public final NumberPath<Long> harvestPermitAreaId = createNumber("harvestPermitAreaId", Long.class);

    public final NumberPath<Integer> huntingYear = createNumber("huntingYear", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath nameFinnish = createString("nameFinnish");

    public final StringPath nameSwedish = createString("nameSwedish");

    public final StringPath status = createString("status");

    public final DateTimePath<java.sql.Timestamp> statusTime = createDateTime("statusTime", java.sql.Timestamp.class);

    public final NumberPath<Long> zoneId = createNumber("zoneId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitArea> harvestPermitAreaPkey = createPrimaryKey(harvestPermitAreaId);

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestPermitAreaClubFk = createForeignKey(clubId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitAreaStatus> harvestPermitAreaStatusFk = createForeignKey(status, "name");

    public final com.querydsl.sql.ForeignKey<SQZone> harvestPermitAreaZoneFk = createForeignKey(zoneId, "zone_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitAreaRhy> _harvestPermitAreaRhyParentFk = createInvForeignKey(harvestPermitAreaId, "harvest_permit_area_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitAreaHta> _harvestPermitAreaHtaParentFk = createInvForeignKey(harvestPermitAreaId, "harvest_permit_area_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitAreaPartner> _harvestPermitAreaPartnerParentFk = createInvForeignKey(harvestPermitAreaId, "harvest_permit_area_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitAreaEvent> _harvestPermitAreaEventPermitAreaFk = createInvForeignKey(harvestPermitAreaId, "harvest_permit_area_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> _harvestPermitApplicationAreaFk = createInvForeignKey(harvestPermitAreaId, "area_id");

    public SQHarvestPermitArea(String variable) {
        super(SQHarvestPermitArea.class, forVariable(variable), "public", "harvest_permit_area");
        addMetadata();
    }

    public SQHarvestPermitArea(String variable, String schema, String table) {
        super(SQHarvestPermitArea.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitArea(String variable, String schema) {
        super(SQHarvestPermitArea.class, forVariable(variable), schema, "harvest_permit_area");
        addMetadata();
    }

    public SQHarvestPermitArea(Path<? extends SQHarvestPermitArea> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_area");
        addMetadata();
    }

    public SQHarvestPermitArea(PathMetadata metadata) {
        super(SQHarvestPermitArea.class, metadata, "public", "harvest_permit_area");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(clubId, ColumnMetadata.named("club_id").withIndex(14).ofType(Types.BIGINT).withSize(19));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(externalId, ColumnMetadata.named("external_id").withIndex(12).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(freeHunting, ColumnMetadata.named("free_hunting").withIndex(17).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(harvestPermitAreaId, ColumnMetadata.named("harvest_permit_area_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(huntingYear, ColumnMetadata.named("hunting_year").withIndex(13).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(nameFinnish, ColumnMetadata.named("name_finnish").withIndex(10).ofType(Types.VARCHAR).withSize(255));
        addMetadata(nameSwedish, ColumnMetadata.named("name_swedish").withIndex(11).ofType(Types.VARCHAR).withSize(255));
        addMetadata(status, ColumnMetadata.named("status").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(statusTime, ColumnMetadata.named("status_time").withIndex(16).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(zoneId, ColumnMetadata.named("zone_id").withIndex(15).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

