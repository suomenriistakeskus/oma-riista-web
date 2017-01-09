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
 * SQHarvestPermit is a Querydsl query type for SQHarvestPermit
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermit extends RelationalPathSpatial<SQHarvestPermit> {

    private static final long serialVersionUID = -2063453967;

    public static final SQHarvestPermit harvestPermit = new SQHarvestPermit("harvest_permit");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> endOfHuntingReportId = createNumber("endOfHuntingReportId", Long.class);

    public final NumberPath<Long> harvestPermitId = createNumber("harvestPermitId", Long.class);

    public final BooleanPath harvestsAsList = createBoolean("harvestsAsList");

    public final DateTimePath<java.sql.Timestamp> lhSyncTime = createDateTime("lhSyncTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Integer> mooseAreaId = createNumber("mooseAreaId", Integer.class);

    public final NumberPath<Long> originalContactPersonId = createNumber("originalContactPersonId", Long.class);

    public final NumberPath<Long> originalPermitId = createNumber("originalPermitId", Long.class);

    public final StringPath parsingInfo = createString("parsingInfo");

    public final NumberPath<Integer> permitAreaSize = createNumber("permitAreaSize", Integer.class);

    public final NumberPath<Long> permitHolderId = createNumber("permitHolderId", Long.class);

    public final StringPath permitNumber = createString("permitNumber");

    public final StringPath permitType = createString("permitType");

    public final StringPath permitTypeCode = createString("permitTypeCode");

    public final StringPath printingUrl = createString("printingUrl");

    public final NumberPath<Long> rhyId = createNumber("rhyId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermit> harvestPermitPkey = createPrimaryKey(harvestPermitId);

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestPermitRhyFk = createForeignKey(rhyId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHta> harvestPermitMooseAreaFk = createForeignKey(mooseAreaId, "gid");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestPermitPermitHolderFk = createForeignKey(permitHolderId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestReport> harvestPermitEndOfHuntingReportFk = createForeignKey(endOfHuntingReportId, "harvest_report_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> harvestPermitOriginalPermitFk = createForeignKey(originalPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQPerson> harvestPermitOwnerFk = createForeignKey(originalContactPersonId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitPartners> _harvestPermitPartnersHarvestPermitFk = createInvForeignKey(harvestPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQMooseHuntingSummary> _mooseHuntingSummaryPermitFk = createInvForeignKey(harvestPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitSpeciesAmount> _harvestPermitSpeciesAmountPermitFk = createInvForeignKey(harvestPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitRhys> _harvestPermitRhysHarvestPermitFk = createInvForeignKey(harvestPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> _organisationHarvestPermitFk = createInvForeignKey(harvestPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitContactPerson> _harvestPermitContactPersonHarvestPermitFk = createInvForeignKey(harvestPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQHarvest> _harvestHarvestPermitFk = createInvForeignKey(harvestPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> _harvestPermitOriginalPermitFk = createInvForeignKey(harvestPermitId, "original_permit_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestReport> _harvestReportHarvestPermitFk = createInvForeignKey(harvestPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitAllocation> _harvestPermitAllocationHarvestPermitFk = createInvForeignKey(harvestPermitId, "harvest_permit_id");

    public SQHarvestPermit(String variable) {
        super(SQHarvestPermit.class, forVariable(variable), "public", "harvest_permit");
        addMetadata();
    }

    public SQHarvestPermit(String variable, String schema, String table) {
        super(SQHarvestPermit.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermit(Path<? extends SQHarvestPermit> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit");
        addMetadata();
    }

    public SQHarvestPermit(PathMetadata metadata) {
        super(SQHarvestPermit.class, metadata, "public", "harvest_permit");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(endOfHuntingReportId, ColumnMetadata.named("end_of_hunting_report_id").withIndex(17).ofType(Types.BIGINT).withSize(19));
        addMetadata(harvestPermitId, ColumnMetadata.named("harvest_permit_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestsAsList, ColumnMetadata.named("harvests_as_list").withIndex(16).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(lhSyncTime, ColumnMetadata.named("lh_sync_time").withIndex(14).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(mooseAreaId, ColumnMetadata.named("moose_area_id").withIndex(21).ofType(Types.INTEGER).withSize(10));
        addMetadata(originalContactPersonId, ColumnMetadata.named("original_contact_person_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(originalPermitId, ColumnMetadata.named("original_permit_id").withIndex(19).ofType(Types.BIGINT).withSize(19));
        addMetadata(parsingInfo, ColumnMetadata.named("parsing_info").withIndex(12).ofType(Types.VARCHAR).withSize(255));
        addMetadata(permitAreaSize, ColumnMetadata.named("permit_area_size").withIndex(22).ofType(Types.INTEGER).withSize(10));
        addMetadata(permitHolderId, ColumnMetadata.named("permit_holder_id").withIndex(18).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitNumber, ColumnMetadata.named("permit_number").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(permitType, ColumnMetadata.named("permit_type").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(permitTypeCode, ColumnMetadata.named("permit_type_code").withIndex(15).ofType(Types.CHAR).withSize(3).notNull());
        addMetadata(printingUrl, ColumnMetadata.named("printing_url").withIndex(20).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(rhyId, ColumnMetadata.named("rhy_id").withIndex(13).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

