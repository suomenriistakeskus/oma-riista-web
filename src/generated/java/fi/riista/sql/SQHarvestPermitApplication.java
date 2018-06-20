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
 * SQHarvestPermitApplication is a Querydsl query type for SQHarvestPermitApplication
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitApplication extends RelationalPathSpatial<SQHarvestPermitApplication> {

    private static final long serialVersionUID = -217437281;

    public static final SQHarvestPermitApplication harvestPermitApplication = new SQHarvestPermitApplication("harvest_permit_application");

    public final NumberPath<Long> areaId = createNumber("areaId", Long.class);

    public final NumberPath<Integer> areaSize = createNumber("areaSize", Integer.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> harvestPermitApplicationId = createNumber("harvestPermitApplicationId", Long.class);

    public final DateTimePath<java.sql.Timestamp> lhSyncTime = createDateTime("lhSyncTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath parsingInfo = createString("parsingInfo");

    public final NumberPath<Long> permitHolderId = createNumber("permitHolderId", Long.class);

    public final StringPath permitNumber = createString("permitNumber");

    public final StringPath printingUrl = createString("printingUrl");

    public final NumberPath<Long> rhyId = createNumber("rhyId", Long.class);

    public final StringPath status = createString("status");

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitApplication> harvestPermitApplicationPkey = createPrimaryKey(harvestPermitApplicationId);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationStatus> harvestPermitApplicationStatusFk = createForeignKey(status, "name");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestPermitApplicationRhyFk = createForeignKey(rhyId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestPermitApplicationHolderFk = createForeignKey(permitHolderId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitArea> harvestPermitApplicationAreaFk = createForeignKey(areaId, "harvest_permit_area_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationConflict> _harvestPermitApplicationConflictSecondFk = createInvForeignKey(harvestPermitApplicationId, "second_application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationConflictPalsta> _harvestPermitApplicationConflictPalstaFirstFk = createInvForeignKey(harvestPermitApplicationId, "first_application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationSpecies> _harvestPermitApplicationSpeciesApplicationFk = createInvForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationAttachment> _harvestPermitApplicationAttachmentApplicationFk = createInvForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationConflict> _harvestPermitApplicationConflictFirstFk = createInvForeignKey(harvestPermitApplicationId, "first_application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationConflictPalsta> _harvestPermitApplicationConflictPalstaSecondFk = createInvForeignKey(harvestPermitApplicationId, "second_application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationPartner> _harvestPermitApplicationPartnerApplicationFk = createInvForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationRhy> _harvestPermitApplicationRhyApplicationFk = createInvForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public SQHarvestPermitApplication(String variable) {
        super(SQHarvestPermitApplication.class, forVariable(variable), "public", "harvest_permit_application");
        addMetadata();
    }

    public SQHarvestPermitApplication(String variable, String schema, String table) {
        super(SQHarvestPermitApplication.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitApplication(String variable, String schema) {
        super(SQHarvestPermitApplication.class, forVariable(variable), schema, "harvest_permit_application");
        addMetadata();
    }

    public SQHarvestPermitApplication(Path<? extends SQHarvestPermitApplication> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_application");
        addMetadata();
    }

    public SQHarvestPermitApplication(PathMetadata metadata) {
        super(SQHarvestPermitApplication.class, metadata, "public", "harvest_permit_application");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(areaId, ColumnMetadata.named("area_id").withIndex(12).ofType(Types.BIGINT).withSize(19));
        addMetadata(areaSize, ColumnMetadata.named("area_size").withIndex(16).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(harvestPermitApplicationId, ColumnMetadata.named("harvest_permit_application_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(lhSyncTime, ColumnMetadata.named("lh_sync_time").withIndex(14).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(parsingInfo, ColumnMetadata.named("parsing_info").withIndex(15).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(permitHolderId, ColumnMetadata.named("permit_holder_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitNumber, ColumnMetadata.named("permit_number").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(printingUrl, ColumnMetadata.named("printing_url").withIndex(11).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(rhyId, ColumnMetadata.named("rhy_id").withIndex(13).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(status, ColumnMetadata.named("status").withIndex(17).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

