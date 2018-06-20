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
 * SQHarvestPermitAreaPartner is a Querydsl query type for SQHarvestPermitAreaPartner
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitAreaPartner extends RelationalPathSpatial<SQHarvestPermitAreaPartner> {

    private static final long serialVersionUID = -653688374;

    public static final SQHarvestPermitAreaPartner harvestPermitAreaPartner = new SQHarvestPermitAreaPartner("harvest_permit_area_partner");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> harvestPermitAreaId = createNumber("harvestPermitAreaId", Long.class);

    public final NumberPath<Long> harvestPermitAreaPartnerId = createNumber("harvestPermitAreaPartnerId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> sourceAreaId = createNumber("sourceAreaId", Long.class);

    public final NumberPath<Long> zoneId = createNumber("zoneId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitAreaPartner> harvestPermitAreaPartnerPkey = createPrimaryKey(harvestPermitAreaPartnerId);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitArea> harvestPermitAreaPartnerParentFk = createForeignKey(harvestPermitAreaId, "harvest_permit_area_id");

    public final com.querydsl.sql.ForeignKey<SQHuntingClubArea> harvestPermitAreaPartnerSourceAreaFk = createForeignKey(sourceAreaId, "hunting_club_area_id");

    public final com.querydsl.sql.ForeignKey<SQZone> harvestPermitAreaPartnerZoneFk = createForeignKey(zoneId, "zone_id");

    public SQHarvestPermitAreaPartner(String variable) {
        super(SQHarvestPermitAreaPartner.class, forVariable(variable), "public", "harvest_permit_area_partner");
        addMetadata();
    }

    public SQHarvestPermitAreaPartner(String variable, String schema, String table) {
        super(SQHarvestPermitAreaPartner.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitAreaPartner(String variable, String schema) {
        super(SQHarvestPermitAreaPartner.class, forVariable(variable), schema, "harvest_permit_area_partner");
        addMetadata();
    }

    public SQHarvestPermitAreaPartner(Path<? extends SQHarvestPermitAreaPartner> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_area_partner");
        addMetadata();
    }

    public SQHarvestPermitAreaPartner(PathMetadata metadata) {
        super(SQHarvestPermitAreaPartner.class, metadata, "public", "harvest_permit_area_partner");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(harvestPermitAreaId, ColumnMetadata.named("harvest_permit_area_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitAreaPartnerId, ColumnMetadata.named("harvest_permit_area_partner_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(sourceAreaId, ColumnMetadata.named("source_area_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(zoneId, ColumnMetadata.named("zone_id").withIndex(11).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

