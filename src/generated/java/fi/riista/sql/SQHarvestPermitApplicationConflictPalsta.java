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
 * SQHarvestPermitApplicationConflictPalsta is a Querydsl query type for SQHarvestPermitApplicationConflictPalsta
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitApplicationConflictPalsta extends RelationalPathSpatial<SQHarvestPermitApplicationConflictPalsta> {

    private static final long serialVersionUID = 1048292246;

    public static final SQHarvestPermitApplicationConflictPalsta harvestPermitApplicationConflictPalsta = new SQHarvestPermitApplicationConflictPalsta("harvest_permit_application_conflict_palsta");

    public final NumberPath<Double> conflictAreaSize = createNumber("conflictAreaSize", Double.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> firstApplicationId = createNumber("firstApplicationId", Long.class);

    public final NumberPath<Long> harvestPermitApplicationConflictPalstaId = createNumber("harvestPermitApplicationConflictPalstaId", Long.class);

    public final BooleanPath metsahallitus = createBoolean("metsahallitus");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Integer> palstaId = createNumber("palstaId", Integer.class);

    public final StringPath palstaNimi = createString("palstaNimi");

    public final NumberPath<Long> palstaTunnus = createNumber("palstaTunnus", Long.class);

    public final NumberPath<Long> secondApplicationId = createNumber("secondApplicationId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitApplicationConflictPalsta> harvestPermitApplicationConflictPalstaPkey = createPrimaryKey(harvestPermitApplicationConflictPalstaId);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> harvestPermitApplicationConflictPalstaFirstFk = createForeignKey(firstApplicationId, "harvest_permit_application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> harvestPermitApplicationConflictPalstaSecondFk = createForeignKey(secondApplicationId, "harvest_permit_application_id");

    public SQHarvestPermitApplicationConflictPalsta(String variable) {
        super(SQHarvestPermitApplicationConflictPalsta.class, forVariable(variable), "public", "harvest_permit_application_conflict_palsta");
        addMetadata();
    }

    public SQHarvestPermitApplicationConflictPalsta(String variable, String schema, String table) {
        super(SQHarvestPermitApplicationConflictPalsta.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitApplicationConflictPalsta(String variable, String schema) {
        super(SQHarvestPermitApplicationConflictPalsta.class, forVariable(variable), schema, "harvest_permit_application_conflict_palsta");
        addMetadata();
    }

    public SQHarvestPermitApplicationConflictPalsta(Path<? extends SQHarvestPermitApplicationConflictPalsta> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_application_conflict_palsta");
        addMetadata();
    }

    public SQHarvestPermitApplicationConflictPalsta(PathMetadata metadata) {
        super(SQHarvestPermitApplicationConflictPalsta.class, metadata, "public", "harvest_permit_application_conflict_palsta");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(conflictAreaSize, ColumnMetadata.named("conflict_area_size").withIndex(15).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(firstApplicationId, ColumnMetadata.named("first_application_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitApplicationConflictPalstaId, ColumnMetadata.named("harvest_permit_application_conflict_palsta_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(metsahallitus, ColumnMetadata.named("metsahallitus").withIndex(14).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(palstaId, ColumnMetadata.named("palsta_id").withIndex(11).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(palstaNimi, ColumnMetadata.named("palsta_nimi").withIndex(13).ofType(Types.VARCHAR).withSize(255));
        addMetadata(palstaTunnus, ColumnMetadata.named("palsta_tunnus").withIndex(12).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(secondApplicationId, ColumnMetadata.named("second_application_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

