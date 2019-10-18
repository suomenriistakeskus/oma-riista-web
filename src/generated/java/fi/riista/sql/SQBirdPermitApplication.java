package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQBirdPermitApplication is a Querydsl query type for SQBirdPermitApplication
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQBirdPermitApplication extends RelationalPathSpatial<SQBirdPermitApplication> {

    private static final long serialVersionUID = -1661949559;

    public static final SQBirdPermitApplication birdPermitApplication = new SQBirdPermitApplication("bird_permit_application");

    public final NumberPath<Double> accuracy = createNumber("accuracy", Double.class);

    public final NumberPath<Double> altitude = createNumber("altitude", Double.class);

    public final NumberPath<Double> altitudeAccuracy = createNumber("altitudeAccuracy", Double.class);

    public final NumberPath<Long> birdPermitApplicationId = createNumber("birdPermitApplicationId", Long.class);

    public final BooleanPath causeAviationSafety = createBoolean("causeAviationSafety");

    public final BooleanPath causeCropsDamage = createBoolean("causeCropsDamage");

    public final BooleanPath causeDomesticPets = createBoolean("causeDomesticPets");

    public final BooleanPath causeFauna = createBoolean("causeFauna");

    public final BooleanPath causeFishing = createBoolean("causeFishing");

    public final BooleanPath causeFlora = createBoolean("causeFlora");

    public final BooleanPath causeForestDamage = createBoolean("causeForestDamage");

    public final BooleanPath causePublicHealth = createBoolean("causePublicHealth");

    public final BooleanPath causePublicSafety = createBoolean("causePublicSafety");

    public final BooleanPath causeResearch = createBoolean("causeResearch");

    public final BooleanPath causeWaterSystem = createBoolean("causeWaterSystem");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath deviateSection32 = createString("deviateSection32");

    public final StringPath deviateSection33 = createString("deviateSection33");

    public final StringPath deviateSection34 = createString("deviateSection34");

    public final StringPath deviateSection35 = createString("deviateSection35");

    public final StringPath deviateSection51 = createString("deviateSection51");

    public final StringPath geolocationSource = createString("geolocationSource");

    public final NumberPath<Long> harvestPermitApplicationId = createNumber("harvestPermitApplicationId", Long.class);

    public final NumberPath<Integer> latitude = createNumber("latitude", Integer.class);

    public final NumberPath<Integer> longitude = createNumber("longitude", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath protectedAreaCity = createString("protectedAreaCity");

    public final StringPath protectedAreaDescription = createString("protectedAreaDescription");

    public final StringPath protectedAreaName = createString("protectedAreaName");

    public final StringPath protectedAreaPostalCode = createString("protectedAreaPostalCode");

    public final NumberPath<Integer> protectedAreaSize = createNumber("protectedAreaSize", Integer.class);

    public final StringPath protectedAreaStreetAddress = createString("protectedAreaStreetAddress");

    public final StringPath protectedAreaType = createString("protectedAreaType");

    public final BooleanPath useTapeRecorders = createBoolean("useTapeRecorders");

    public final BooleanPath useTraps = createBoolean("useTraps");

    public final com.querydsl.sql.PrimaryKey<SQBirdPermitApplication> birdPermitApplicationPkey = createPrimaryKey(birdPermitApplicationId);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> birdPermitApplicationApplicationIdFk = createForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public final com.querydsl.sql.ForeignKey<SQBirdPermitApplicationProtectedAreaType> birdPermitApplicationProtectedAreaTypeFk = createForeignKey(protectedAreaType, "name");

    public SQBirdPermitApplication(String variable) {
        super(SQBirdPermitApplication.class, forVariable(variable), "public", "bird_permit_application");
        addMetadata();
    }

    public SQBirdPermitApplication(String variable, String schema, String table) {
        super(SQBirdPermitApplication.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQBirdPermitApplication(String variable, String schema) {
        super(SQBirdPermitApplication.class, forVariable(variable), schema, "bird_permit_application");
        addMetadata();
    }

    public SQBirdPermitApplication(Path<? extends SQBirdPermitApplication> path) {
        super(path.getType(), path.getMetadata(), "public", "bird_permit_application");
        addMetadata();
    }

    public SQBirdPermitApplication(PathMetadata metadata) {
        super(SQBirdPermitApplication.class, metadata, "public", "bird_permit_application");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(accuracy, ColumnMetadata.named("accuracy").withIndex(19).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(altitude, ColumnMetadata.named("altitude").withIndex(20).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(altitudeAccuracy, ColumnMetadata.named("altitude_accuracy").withIndex(21).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(birdPermitApplicationId, ColumnMetadata.named("bird_permit_application_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(causeAviationSafety, ColumnMetadata.named("cause_aviation_safety").withIndex(25).ofType(Types.BIT).withSize(1));
        addMetadata(causeCropsDamage, ColumnMetadata.named("cause_crops_damage").withIndex(26).ofType(Types.BIT).withSize(1));
        addMetadata(causeDomesticPets, ColumnMetadata.named("cause_domestic_pets").withIndex(27).ofType(Types.BIT).withSize(1));
        addMetadata(causeFauna, ColumnMetadata.named("cause_fauna").withIndex(32).ofType(Types.BIT).withSize(1));
        addMetadata(causeFishing, ColumnMetadata.named("cause_fishing").withIndex(29).ofType(Types.BIT).withSize(1));
        addMetadata(causeFlora, ColumnMetadata.named("cause_flora").withIndex(31).ofType(Types.BIT).withSize(1));
        addMetadata(causeForestDamage, ColumnMetadata.named("cause_forest_damage").withIndex(28).ofType(Types.BIT).withSize(1));
        addMetadata(causePublicHealth, ColumnMetadata.named("cause_public_health").withIndex(23).ofType(Types.BIT).withSize(1));
        addMetadata(causePublicSafety, ColumnMetadata.named("cause_public_safety").withIndex(24).ofType(Types.BIT).withSize(1));
        addMetadata(causeResearch, ColumnMetadata.named("cause_research").withIndex(33).ofType(Types.BIT).withSize(1));
        addMetadata(causeWaterSystem, ColumnMetadata.named("cause_water_system").withIndex(30).ofType(Types.BIT).withSize(1));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(deviateSection32, ColumnMetadata.named("deviate_section_32").withIndex(34).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(deviateSection33, ColumnMetadata.named("deviate_section_33").withIndex(35).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(deviateSection34, ColumnMetadata.named("deviate_section_34").withIndex(36).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(deviateSection35, ColumnMetadata.named("deviate_section_35").withIndex(37).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(deviateSection51, ColumnMetadata.named("deviate_section_51").withIndex(38).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(geolocationSource, ColumnMetadata.named("geolocation_source").withIndex(22).ofType(Types.VARCHAR).withSize(255));
        addMetadata(harvestPermitApplicationId, ColumnMetadata.named("harvest_permit_application_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(latitude, ColumnMetadata.named("latitude").withIndex(17).ofType(Types.INTEGER).withSize(10));
        addMetadata(longitude, ColumnMetadata.named("longitude").withIndex(18).ofType(Types.INTEGER).withSize(10));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(protectedAreaCity, ColumnMetadata.named("protected_area_city").withIndex(16).ofType(Types.VARCHAR).withSize(255));
        addMetadata(protectedAreaDescription, ColumnMetadata.named("protected_area_description").withIndex(12).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(protectedAreaName, ColumnMetadata.named("protected_area_name").withIndex(10).ofType(Types.VARCHAR).withSize(255));
        addMetadata(protectedAreaPostalCode, ColumnMetadata.named("protected_area_postal_code").withIndex(15).ofType(Types.VARCHAR).withSize(255));
        addMetadata(protectedAreaSize, ColumnMetadata.named("protected_area_size").withIndex(13).ofType(Types.INTEGER).withSize(10));
        addMetadata(protectedAreaStreetAddress, ColumnMetadata.named("protected_area_street_address").withIndex(14).ofType(Types.VARCHAR).withSize(255));
        addMetadata(protectedAreaType, ColumnMetadata.named("protected_area_type").withIndex(11).ofType(Types.VARCHAR).withSize(255));
        addMetadata(useTapeRecorders, ColumnMetadata.named("use_tape_recorders").withIndex(40).ofType(Types.BIT).withSize(1));
        addMetadata(useTraps, ColumnMetadata.named("use_traps").withIndex(39).ofType(Types.BIT).withSize(1));
    }

}

