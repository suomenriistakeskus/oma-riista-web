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
 * SQMooseDataCardImport is a Querydsl query type for SQMooseDataCardImport
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMooseDataCardImport extends RelationalPathSpatial<SQMooseDataCardImport> {

    private static final long serialVersionUID = 92948587;

    public static final SQMooseDataCardImport mooseDataCardImport = new SQMooseDataCardImport("moose_data_card_import");

    public final DatePath<java.sql.Date> beginDate = createDate("beginDate", java.sql.Date.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DatePath<java.sql.Date> endDate = createDate("endDate", java.sql.Date.class);

    public final DateTimePath<java.sql.Timestamp> filenameTimestamp = createDateTime("filenameTimestamp", java.sql.Timestamp.class);

    public final NumberPath<Long> huntingGroupId = createNumber("huntingGroupId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> mooseDataCardImportId = createNumber("mooseDataCardImportId", Long.class);

    public final StringPath pdfFileMetadataId = createString("pdfFileMetadataId");

    public final StringPath xmlFileMetadataId = createString("xmlFileMetadataId");

    public final com.querydsl.sql.PrimaryKey<SQMooseDataCardImport> mooseDataCardImportPkey = createPrimaryKey(mooseDataCardImportId);

    public final com.querydsl.sql.ForeignKey<SQOrganisation> mooseDataCardImportHuntingGroupFk = createForeignKey(huntingGroupId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQFileMetadata> mooseDataCardImportPdfFileMetadataFk = createForeignKey(pdfFileMetadataId, "file_metadata_uuid");

    public final com.querydsl.sql.ForeignKey<SQFileMetadata> mooseDataCardImportXmlFileMetadataFk = createForeignKey(xmlFileMetadataId, "file_metadata_uuid");

    public final com.querydsl.sql.ForeignKey<SQGroupHuntingDay> _groupHuntingDayMooseDataCardImportFk = createInvForeignKey(mooseDataCardImportId, "moose_data_card_import_id");

    public final com.querydsl.sql.ForeignKey<SQMooseDataCardImportMessage> _mooseDataCardImportMessageImportFk = createInvForeignKey(mooseDataCardImportId, "import_id");

    public SQMooseDataCardImport(String variable) {
        super(SQMooseDataCardImport.class, forVariable(variable), "public", "moose_data_card_import");
        addMetadata();
    }

    public SQMooseDataCardImport(String variable, String schema, String table) {
        super(SQMooseDataCardImport.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQMooseDataCardImport(Path<? extends SQMooseDataCardImport> path) {
        super(path.getType(), path.getMetadata(), "public", "moose_data_card_import");
        addMetadata();
    }

    public SQMooseDataCardImport(PathMetadata metadata) {
        super(SQMooseDataCardImport.class, metadata, "public", "moose_data_card_import");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(beginDate, ColumnMetadata.named("begin_date").withIndex(13).ofType(Types.DATE).withSize(13));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(endDate, ColumnMetadata.named("end_date").withIndex(14).ofType(Types.DATE).withSize(13));
        addMetadata(filenameTimestamp, ColumnMetadata.named("filename_timestamp").withIndex(12).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(huntingGroupId, ColumnMetadata.named("hunting_group_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(mooseDataCardImportId, ColumnMetadata.named("moose_data_card_import_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(pdfFileMetadataId, ColumnMetadata.named("pdf_file_metadata_id").withIndex(11).ofType(Types.CHAR).withSize(36).notNull());
        addMetadata(xmlFileMetadataId, ColumnMetadata.named("xml_file_metadata_id").withIndex(10).ofType(Types.CHAR).withSize(36).notNull());
    }

}

