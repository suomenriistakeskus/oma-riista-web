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
 * SQGameDiaryImage is a Querydsl query type for SQGameDiaryImage
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQGameDiaryImage extends RelationalPathSpatial<SQGameDiaryImage> {

    private static final long serialVersionUID = 1482076797;

    public static final SQGameDiaryImage gameDiaryImage = new SQGameDiaryImage("game_diary_image");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath fileMetadataId = createString("fileMetadataId");

    public final NumberPath<Long> gameDiaryImageId = createNumber("gameDiaryImageId", Long.class);

    public final NumberPath<Long> harvestId = createNumber("harvestId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> observationId = createNumber("observationId", Long.class);

    public final NumberPath<Long> srvaEventId = createNumber("srvaEventId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQGameDiaryImage> gameDiaryImagePkey = createPrimaryKey(gameDiaryImageId);

    public final com.querydsl.sql.ForeignKey<SQFileMetadata> gameDiaryImageFileMetadataFk = createForeignKey(fileMetadataId, "file_metadata_uuid");

    public final com.querydsl.sql.ForeignKey<SQSrvaEvent> gameDiaryImageSrvaEventFk = createForeignKey(srvaEventId, "srva_event_id");

    public final com.querydsl.sql.ForeignKey<SQHarvest> gameDiaryImageHarvestFk = createForeignKey(harvestId, "harvest_id");

    public final com.querydsl.sql.ForeignKey<SQObservation> gameDiaryImageGameObservationFk = createForeignKey(observationId, "game_observation_id");

    public SQGameDiaryImage(String variable) {
        super(SQGameDiaryImage.class, forVariable(variable), "public", "game_diary_image");
        addMetadata();
    }

    public SQGameDiaryImage(String variable, String schema, String table) {
        super(SQGameDiaryImage.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQGameDiaryImage(Path<? extends SQGameDiaryImage> path) {
        super(path.getType(), path.getMetadata(), "public", "game_diary_image");
        addMetadata();
    }

    public SQGameDiaryImage(PathMetadata metadata) {
        super(SQGameDiaryImage.class, metadata, "public", "game_diary_image");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(fileMetadataId, ColumnMetadata.named("file_metadata_id").withIndex(10).ofType(Types.CHAR).withSize(36).notNull());
        addMetadata(gameDiaryImageId, ColumnMetadata.named("game_diary_image_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestId, ColumnMetadata.named("harvest_id").withIndex(9).ofType(Types.BIGINT).withSize(19));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(observationId, ColumnMetadata.named("observation_id").withIndex(11).ofType(Types.BIGINT).withSize(19));
        addMetadata(srvaEventId, ColumnMetadata.named("srva_event_id").withIndex(12).ofType(Types.BIGINT).withSize(19));
    }

}

