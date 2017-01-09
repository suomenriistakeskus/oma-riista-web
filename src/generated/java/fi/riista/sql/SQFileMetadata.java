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
 * SQFileMetadata is a Querydsl query type for SQFileMetadata
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQFileMetadata extends RelationalPathSpatial<SQFileMetadata> {

    private static final long serialVersionUID = -593429730;

    public static final SQFileMetadata fileMetadata = new SQFileMetadata("file_metadata");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> contentSize = createNumber("contentSize", Long.class);

    public final StringPath contentType = createString("contentType");

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath fileMetadataUuid = createString("fileMetadataUuid");

    public final StringPath md5Hash = createString("md5Hash");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath originalFileName = createString("originalFileName");

    public final StringPath resourceUrl = createString("resourceUrl");

    public final StringPath resourceUrlLocal = createString("resourceUrlLocal");

    public final StringPath storageType = createString("storageType");

    public final com.querydsl.sql.PrimaryKey<SQFileMetadata> fileMetadataPkey = createPrimaryKey(fileMetadataUuid);

    public final com.querydsl.sql.ForeignKey<SQMooseDataCardImport> _mooseDataCardImportPdfFileMetadataFk = createInvForeignKey(fileMetadataUuid, "pdf_file_metadata_id");

    public final com.querydsl.sql.ForeignKey<SQGameDiaryImage> _gameDiaryImageFileMetadataFk = createInvForeignKey(fileMetadataUuid, "file_metadata_id");

    public final com.querydsl.sql.ForeignKey<SQMooseDataCardImport> _mooseDataCardImportXmlFileMetadataFk = createInvForeignKey(fileMetadataUuid, "xml_file_metadata_id");

    public final com.querydsl.sql.ForeignKey<SQZone> _zoneUploadMetadataUuidFk = createInvForeignKey(fileMetadataUuid, "upload_file_id");

    public final com.querydsl.sql.ForeignKey<SQFileContent> _fileContentMetadataUuidFk = createInvForeignKey(fileMetadataUuid, "file_metadata_uuid");

    public final com.querydsl.sql.ForeignKey<SQMooseHarvestReport> _mooseHarvestReportReceiptFileMetadataFk = createInvForeignKey(fileMetadataUuid, "receipt_file_metadata_id");

    public SQFileMetadata(String variable) {
        super(SQFileMetadata.class, forVariable(variable), "public", "file_metadata");
        addMetadata();
    }

    public SQFileMetadata(String variable, String schema, String table) {
        super(SQFileMetadata.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQFileMetadata(Path<? extends SQFileMetadata> path) {
        super(path.getType(), path.getMetadata(), "public", "file_metadata");
        addMetadata();
    }

    public SQFileMetadata(PathMetadata metadata) {
        super(SQFileMetadata.class, metadata, "public", "file_metadata");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(contentSize, ColumnMetadata.named("content_size").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(contentType, ColumnMetadata.named("content_type").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(9).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(12).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(10).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(13).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(fileMetadataUuid, ColumnMetadata.named("file_metadata_uuid").withIndex(1).ofType(Types.CHAR).withSize(36).notNull());
        addMetadata(md5Hash, ColumnMetadata.named("md5_hash").withIndex(8).ofType(Types.CHAR).withSize(32));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(14).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(11).ofType(Types.BIGINT).withSize(19));
        addMetadata(originalFileName, ColumnMetadata.named("original_file_name").withIndex(6).ofType(Types.VARCHAR).withSize(255));
        addMetadata(resourceUrl, ColumnMetadata.named("resource_url").withIndex(7).ofType(Types.VARCHAR).withSize(255));
        addMetadata(resourceUrlLocal, ColumnMetadata.named("resource_url_local").withIndex(15).ofType(Types.VARCHAR).withSize(255));
        addMetadata(storageType, ColumnMetadata.named("storage_type").withIndex(5).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

