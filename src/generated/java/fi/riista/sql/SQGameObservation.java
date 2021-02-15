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
 * SQGameObservation is a Querydsl query type for SQGameObservation
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQGameObservation extends RelationalPathSpatial<SQGameObservation> {

    private static final long serialVersionUID = 1787806119;

    public static final SQGameObservation gameObservation = new SQGameObservation("game_observation");

    public final NumberPath<Double> accuracy = createNumber("accuracy", Double.class);

    public final NumberPath<Double> altitude = createNumber("altitude", Double.class);

    public final NumberPath<Double> altitudeAccuracy = createNumber("altitudeAccuracy", Double.class);

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final NumberPath<Long> approverToHuntingDayId = createNumber("approverToHuntingDayId", Long.class);

    public final NumberPath<Long> authorId = createNumber("authorId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final StringPath deerHuntingType = createString("deerHuntingType");

    public final StringPath deerHuntingTypeDescription = createString("deerHuntingTypeDescription");

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath description = createString("description");

    public final BooleanPath fromMobile = createBoolean("fromMobile");

    public final NumberPath<Long> gameObservationId = createNumber("gameObservationId", Long.class);

    public final NumberPath<Long> gameSpeciesId = createNumber("gameSpeciesId", Long.class);

    public final StringPath geolocationSource = createString("geolocationSource");

    public final GeometryPath<org.geolatte.geom.Geometry> geom = createGeometry("geom", org.geolatte.geom.Geometry.class);

    public final NumberPath<Long> groupHuntingDayId = createNumber("groupHuntingDayId", Long.class);

    public final NumberPath<Integer> inYardDistanceToResidence = createNumber("inYardDistanceToResidence", Integer.class);

    public final NumberPath<Integer> latitude = createNumber("latitude", Integer.class);

    public final NumberPath<Integer> longitude = createNumber("longitude", Integer.class);

    public final NumberPath<Long> mobileClientRefId = createNumber("mobileClientRefId", Long.class);

    public final BooleanPath moderatorOverride = createBoolean("moderatorOverride");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Integer> mooselikeCalfAmount = createNumber("mooselikeCalfAmount", Integer.class);

    public final NumberPath<Integer> mooselikeFemale1CalfAmount = createNumber("mooselikeFemale1CalfAmount", Integer.class);

    public final NumberPath<Integer> mooselikeFemale2CalfsAmount = createNumber("mooselikeFemale2CalfsAmount", Integer.class);

    public final NumberPath<Integer> mooselikeFemale3CalfsAmount = createNumber("mooselikeFemale3CalfsAmount", Integer.class);

    public final NumberPath<Integer> mooselikeFemale4CalfsAmount = createNumber("mooselikeFemale4CalfsAmount", Integer.class);

    public final NumberPath<Integer> mooselikeFemaleAmount = createNumber("mooselikeFemaleAmount", Integer.class);

    public final NumberPath<Integer> mooselikeMaleAmount = createNumber("mooselikeMaleAmount", Integer.class);

    public final NumberPath<Integer> mooselikeUnknownSpecimenAmount = createNumber("mooselikeUnknownSpecimenAmount", Integer.class);

    public final StringPath observationCategory = createString("observationCategory");

    public final StringPath observationType = createString("observationType");

    public final NumberPath<Long> observerId = createNumber("observerId", Long.class);

    public final StringPath observerName = createString("observerName");

    public final StringPath observerPhoneNumber = createString("observerPhoneNumber");

    public final StringPath officialAdditionalInfo = createString("officialAdditionalInfo");

    public final DateTimePath<java.sql.Timestamp> pointOfTime = createDateTime("pointOfTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> pointOfTimeApprovedToHuntingDay = createDateTime("pointOfTimeApprovedToHuntingDay", java.sql.Timestamp.class);

    public final NumberPath<Long> rhyId = createNumber("rhyId", Long.class);

    public final BooleanPath verifiedByCarnivoreAuthority = createBoolean("verifiedByCarnivoreAuthority");

    public final BooleanPath withinMooseHunting = createBoolean("withinMooseHunting");

    public final com.querydsl.sql.PrimaryKey<SQGameObservation> gameObservationPkey = createPrimaryKey(gameObservationId);

    public final com.querydsl.sql.ForeignKey<SQPerson> gameObservationObserverFk = createForeignKey(observerId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQObservationCategory> gameObservationObservationCategoryFk = createForeignKey(observationCategory, "name");

    public final com.querydsl.sql.ForeignKey<SQGroupHuntingDay> gameObservationGroupHuntingDayFk = createForeignKey(groupHuntingDayId, "group_hunting_day_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> gameObservationRhyFk = createForeignKey(rhyId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQPerson> gameObservationAuthorFk = createForeignKey(authorId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQObservationType> gameObservationObservationTypeFk = createForeignKey(observationType, "name");

    public final com.querydsl.sql.ForeignKey<SQDeerHuntingType> gameObservationDeerHuntingTypeFk = createForeignKey(deerHuntingType, "name");

    public final com.querydsl.sql.ForeignKey<SQPerson> gameObservationApproverToHuntingDayPersonIdFk = createForeignKey(approverToHuntingDayId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQGameSpecies> gameObservationSpeciesFk = createForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQGroupObservationRejection> _groupObservationRejectionGameObservationFk = createInvForeignKey(gameObservationId, "observation_id");

    public final com.querydsl.sql.ForeignKey<SQObservationSpecimen> _observationSpecimenObservationFk = createInvForeignKey(gameObservationId, "game_observation_id");

    public final com.querydsl.sql.ForeignKey<SQGameDiaryImage> _gameDiaryImageGameObservationFk = createInvForeignKey(gameObservationId, "observation_id");

    public SQGameObservation(String variable) {
        super(SQGameObservation.class, forVariable(variable), "public", "game_observation");
        addMetadata();
    }

    public SQGameObservation(String variable, String schema, String table) {
        super(SQGameObservation.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQGameObservation(String variable, String schema) {
        super(SQGameObservation.class, forVariable(variable), schema, "game_observation");
        addMetadata();
    }

    public SQGameObservation(Path<? extends SQGameObservation> path) {
        super(path.getType(), path.getMetadata(), "public", "game_observation");
        addMetadata();
    }

    public SQGameObservation(PathMetadata metadata) {
        super(SQGameObservation.class, metadata, "public", "game_observation");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(accuracy, ColumnMetadata.named("accuracy").withIndex(11).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(altitude, ColumnMetadata.named("altitude").withIndex(12).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(altitudeAccuracy, ColumnMetadata.named("altitude_accuracy").withIndex(13).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(19).ofType(Types.INTEGER).withSize(10));
        addMetadata(approverToHuntingDayId, ColumnMetadata.named("approver_to_hunting_day_id").withIndex(35).ofType(Types.BIGINT).withSize(19));
        addMetadata(authorId, ColumnMetadata.named("author_id").withIndex(17).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deerHuntingType, ColumnMetadata.named("deer_hunting_type").withIndex(45).ofType(Types.VARCHAR).withSize(255));
        addMetadata(deerHuntingTypeDescription, ColumnMetadata.named("deer_hunting_type_description").withIndex(46).ofType(Types.VARCHAR).withSize(255));
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(description, ColumnMetadata.named("description").withIndex(18).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(fromMobile, ColumnMetadata.named("from_mobile").withIndex(22).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(gameObservationId, ColumnMetadata.named("game_observation_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(gameSpeciesId, ColumnMetadata.named("game_species_id").withIndex(16).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(geolocationSource, ColumnMetadata.named("geolocation_source").withIndex(14).ofType(Types.VARCHAR).withSize(255));
        addMetadata(geom, ColumnMetadata.named("geom").withIndex(43).ofType(Types.OTHER).withSize(2147483647).notNull());
        addMetadata(groupHuntingDayId, ColumnMetadata.named("group_hunting_day_id").withIndex(24).ofType(Types.BIGINT).withSize(19));
        addMetadata(inYardDistanceToResidence, ColumnMetadata.named("in_yard_distance_to_residence").withIndex(42).ofType(Types.INTEGER).withSize(10));
        addMetadata(latitude, ColumnMetadata.named("latitude").withIndex(9).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(longitude, ColumnMetadata.named("longitude").withIndex(10).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(mobileClientRefId, ColumnMetadata.named("mobile_client_ref_id").withIndex(23).ofType(Types.BIGINT).withSize(19));
        addMetadata(moderatorOverride, ColumnMetadata.named("moderator_override").withIndex(34).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(mooselikeCalfAmount, ColumnMetadata.named("mooselike_calf_amount").withIndex(37).ofType(Types.INTEGER).withSize(10));
        addMetadata(mooselikeFemale1CalfAmount, ColumnMetadata.named("mooselike_female_1_calf_amount").withIndex(29).ofType(Types.INTEGER).withSize(10));
        addMetadata(mooselikeFemale2CalfsAmount, ColumnMetadata.named("mooselike_female_2_calfs_amount").withIndex(30).ofType(Types.INTEGER).withSize(10));
        addMetadata(mooselikeFemale3CalfsAmount, ColumnMetadata.named("mooselike_female_3_calfs_amount").withIndex(31).ofType(Types.INTEGER).withSize(10));
        addMetadata(mooselikeFemale4CalfsAmount, ColumnMetadata.named("mooselike_female_4_calfs_amount").withIndex(32).ofType(Types.INTEGER).withSize(10));
        addMetadata(mooselikeFemaleAmount, ColumnMetadata.named("mooselike_female_amount").withIndex(28).ofType(Types.INTEGER).withSize(10));
        addMetadata(mooselikeMaleAmount, ColumnMetadata.named("mooselike_male_amount").withIndex(27).ofType(Types.INTEGER).withSize(10));
        addMetadata(mooselikeUnknownSpecimenAmount, ColumnMetadata.named("mooselike_unknown_specimen_amount").withIndex(33).ofType(Types.INTEGER).withSize(10));
        addMetadata(observationCategory, ColumnMetadata.named("observation_category").withIndex(44).ofType(Types.VARCHAR).withSize(255));
        addMetadata(observationType, ColumnMetadata.named("observation_type").withIndex(25).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(observerId, ColumnMetadata.named("observer_id").withIndex(20).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(observerName, ColumnMetadata.named("observer_name").withIndex(39).ofType(Types.VARCHAR).withSize(255));
        addMetadata(observerPhoneNumber, ColumnMetadata.named("observer_phone_number").withIndex(40).ofType(Types.VARCHAR).withSize(255));
        addMetadata(officialAdditionalInfo, ColumnMetadata.named("official_additional_info").withIndex(41).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(pointOfTime, ColumnMetadata.named("point_of_time").withIndex(15).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(pointOfTimeApprovedToHuntingDay, ColumnMetadata.named("point_of_time_approved_to_hunting_day").withIndex(36).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(rhyId, ColumnMetadata.named("rhy_id").withIndex(21).ofType(Types.BIGINT).withSize(19));
        addMetadata(verifiedByCarnivoreAuthority, ColumnMetadata.named("verified_by_carnivore_authority").withIndex(38).ofType(Types.BIT).withSize(1));
        addMetadata(withinMooseHunting, ColumnMetadata.named("within_moose_hunting").withIndex(26).ofType(Types.BIT).withSize(1));
    }

}

