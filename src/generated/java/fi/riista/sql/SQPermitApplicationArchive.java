package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQPermitApplicationArchive is a Querydsl query type for SQPermitApplicationArchive
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitApplicationArchive extends RelationalPathSpatial<SQPermitApplicationArchive> {

    private static final long serialVersionUID = -721230176;

    public static final SQPermitApplicationArchive permitApplicationArchive = new SQPermitApplicationArchive("permit_application_archive");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath fileMetadataId = createString("fileMetadataId");

    public final NumberPath<Long> harvestPermitApplicationId = createNumber("harvestPermitApplicationId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> permitApplicationArchiveId = createNumber("permitApplicationArchiveId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQPermitApplicationArchive> permitApplicationArchivePkey = createPrimaryKey(permitApplicationArchiveId);

    public final com.querydsl.sql.ForeignKey<SQFileMetadata> permitApplicationArchiveMetadataFk = createForeignKey(fileMetadataId, "file_metadata_uuid");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> permitApplicationArchiveApplicationFk = createForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public SQPermitApplicationArchive(String variable) {
        super(SQPermitApplicationArchive.class, forVariable(variable), "public", "permit_application_archive");
        addMetadata();
    }

    public SQPermitApplicationArchive(String variable, String schema, String table) {
        super(SQPermitApplicationArchive.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitApplicationArchive(String variable, String schema) {
        super(SQPermitApplicationArchive.class, forVariable(variable), schema, "permit_application_archive");
        addMetadata();
    }

    public SQPermitApplicationArchive(Path<? extends SQPermitApplicationArchive> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_application_archive");
        addMetadata();
    }

    public SQPermitApplicationArchive(PathMetadata metadata) {
        super(SQPermitApplicationArchive.class, metadata, "public", "permit_application_archive");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(fileMetadataId, ColumnMetadata.named("file_metadata_id").withIndex(10).ofType(Types.CHAR).withSize(36).notNull());
        addMetadata(harvestPermitApplicationId, ColumnMetadata.named("harvest_permit_application_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitApplicationArchiveId, ColumnMetadata.named("permit_application_archive_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

