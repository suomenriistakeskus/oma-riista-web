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
 * SQMooseHarvestReport is a Querydsl query type for SQMooseHarvestReport
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMooseHarvestReport extends RelationalPathSpatial<SQMooseHarvestReport> {

    private static final long serialVersionUID = -1267243471;

    public static final SQMooseHarvestReport mooseHarvestReport = new SQMooseHarvestReport("moose_harvest_report");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final BooleanPath moderatorOverride = createBoolean("moderatorOverride");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> mooseHarvestReportId = createNumber("mooseHarvestReportId", Long.class);

    public final BooleanPath noHarvests = createBoolean("noHarvests");

    public final StringPath receiptFileMetadataId = createString("receiptFileMetadataId");

    public final NumberPath<Long> speciesAmountId = createNumber("speciesAmountId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQMooseHarvestReport> mooseHarvestReportPkey = createPrimaryKey(mooseHarvestReportId);

    public final com.querydsl.sql.ForeignKey<SQFileMetadata> mooseHarvestReportReceiptFileMetadataFk = createForeignKey(receiptFileMetadataId, "file_metadata_uuid");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitSpeciesAmount> mooseHarvestReportSpeciesAmountFk = createForeignKey(speciesAmountId, "harvest_permit_species_amount_id");

    public SQMooseHarvestReport(String variable) {
        super(SQMooseHarvestReport.class, forVariable(variable), "public", "moose_harvest_report");
        addMetadata();
    }

    public SQMooseHarvestReport(String variable, String schema, String table) {
        super(SQMooseHarvestReport.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQMooseHarvestReport(String variable, String schema) {
        super(SQMooseHarvestReport.class, forVariable(variable), schema, "moose_harvest_report");
        addMetadata();
    }

    public SQMooseHarvestReport(Path<? extends SQMooseHarvestReport> path) {
        super(path.getType(), path.getMetadata(), "public", "moose_harvest_report");
        addMetadata();
    }

    public SQMooseHarvestReport(PathMetadata metadata) {
        super(SQMooseHarvestReport.class, metadata, "public", "moose_harvest_report");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(moderatorOverride, ColumnMetadata.named("moderator_override").withIndex(12).ofType(Types.BIT).withSize(1));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(mooseHarvestReportId, ColumnMetadata.named("moose_harvest_report_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(noHarvests, ColumnMetadata.named("no_harvests").withIndex(10).ofType(Types.BIT).withSize(1));
        addMetadata(receiptFileMetadataId, ColumnMetadata.named("receipt_file_metadata_id").withIndex(9).ofType(Types.CHAR).withSize(36));
        addMetadata(speciesAmountId, ColumnMetadata.named("species_amount_id").withIndex(11).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

