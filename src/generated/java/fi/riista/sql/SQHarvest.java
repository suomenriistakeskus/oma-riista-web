package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.spatial.GeometryPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;



/**
 * SQHarvest is a Querydsl query type for SQHarvest
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvest extends RelationalPathSpatial<SQHarvest> {

    private static final long serialVersionUID = -227671754;

    public static final SQHarvest harvest = new SQHarvest("harvest");

    public final NumberPath<Double> accuracy = createNumber("accuracy", Double.class);

    public final NumberPath<Long> actualShooterId = createNumber("actualShooterId", Long.class);

    public final NumberPath<Double> altitude = createNumber("altitude", Double.class);

    public final NumberPath<Double> altitudeAccuracy = createNumber("altitudeAccuracy", Double.class);

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final NumberPath<Long> approverToHuntingDayId = createNumber("approverToHuntingDayId", Long.class);

    public final NumberPath<Long> authorId = createNumber("authorId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath description = createString("description");

    public final DateTimePath<java.sql.Timestamp> emailReminderSentTime = createDateTime("emailReminderSentTime", java.sql.Timestamp.class);

    public final BooleanPath feedingPlace = createBoolean("feedingPlace");

    public final BooleanPath fromMobile = createBoolean("fromMobile");

    public final NumberPath<Long> gameSpeciesId = createNumber("gameSpeciesId", Long.class);

    public final StringPath geolocationSource = createString("geolocationSource");

    public final GeometryPath<org.geolatte.geom.Geometry> geom = createGeometry("geom", org.geolatte.geom.Geometry.class);

    public final NumberPath<Long> groupHuntingDayId = createNumber("groupHuntingDayId", Long.class);

    public final NumberPath<Long> harvestId = createNumber("harvestId", Long.class);

    public final NumberPath<Long> harvestPermitId = createNumber("harvestPermitId", Long.class);

    public final NumberPath<Long> harvestQuotaId = createNumber("harvestQuotaId", Long.class);

    public final NumberPath<Long> harvestReportAuthorId = createNumber("harvestReportAuthorId", Long.class);

    public final DateTimePath<java.sql.Timestamp> harvestReportDate = createDateTime("harvestReportDate", java.sql.Timestamp.class);

    public final StringPath harvestReportMemo = createString("harvestReportMemo");

    public final BooleanPath harvestReportRequired = createBoolean("harvestReportRequired");

    public final StringPath harvestReportState = createString("harvestReportState");

    public final NumberPath<Long> harvestSeasonId = createNumber("harvestSeasonId", Long.class);

    public final NumberPath<java.math.BigDecimal> huntingAreaSize = createNumber("huntingAreaSize", java.math.BigDecimal.class);

    public final StringPath huntingAreaType = createString("huntingAreaType");

    public final StringPath huntingMethod = createString("huntingMethod");

    public final StringPath huntingParty = createString("huntingParty");

    public final NumberPath<Integer> latitude = createNumber("latitude", Integer.class);

    public final NumberPath<Integer> longitude = createNumber("longitude", Integer.class);

    public final StringPath lukeStatus = createString("lukeStatus");

    public final NumberPath<Integer> mhHirviId = createNumber("mhHirviId", Integer.class);

    public final NumberPath<Integer> mhPienriistaId = createNumber("mhPienriistaId", Integer.class);

    public final NumberPath<Long> mobileClientRefId = createNumber("mobileClientRefId", Long.class);

    public final BooleanPath moderatorOverride = createBoolean("moderatorOverride");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath municipalityCode = createString("municipalityCode");

    public final StringPath permittedMethodDescription = createString("permittedMethodDescription");

    public final BooleanPath permittedMethodOther = createBoolean("permittedMethodOther");

    public final BooleanPath permittedMethodTapeRecorders = createBoolean("permittedMethodTapeRecorders");

    public final BooleanPath permittedMethodTraps = createBoolean("permittedMethodTraps");

    public final DateTimePath<java.sql.Timestamp> pointOfTime = createDateTime("pointOfTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> pointOfTimeApprovedToHuntingDay = createDateTime("pointOfTimeApprovedToHuntingDay", java.sql.Timestamp.class);

    public final StringPath propertyIdentifier = createString("propertyIdentifier");

    public final BooleanPath reportedWithPhoneCall = createBoolean("reportedWithPhoneCall");

    public final NumberPath<Long> rhyId = createNumber("rhyId", Long.class);

    public final StringPath stateAcceptedToHarvestPermit = createString("stateAcceptedToHarvestPermit");

    public final NumberPath<Integer> subSpeciesCode = createNumber("subSpeciesCode", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvest> harvestPkey = createPrimaryKey(harvestId);

    public final com.querydsl.sql.ForeignKey<SQGroupHuntingDay> harvestGroupHuntingDayFk = createForeignKey(groupHuntingDayId, "group_hunting_day_id");

    public final com.querydsl.sql.ForeignKey<SQGeolocationSource> harvestGeolocationSourceFk = createForeignKey(geolocationSource, "name");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestRhyFk = createForeignKey(rhyId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestLukeStatus> harvestLukeStatusFk = createForeignKey(lukeStatus, "name");

    public final com.querydsl.sql.ForeignKey<SQHarvestSeason> harvestHarvestSeasonFk = createForeignKey(harvestSeasonId, "harvest_season_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> harvestHarvestPermitFk = createForeignKey(harvestPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportState> harvestHarvestReportStateFk = createForeignKey(harvestReportState, "name");

    public final com.querydsl.sql.ForeignKey<SQPerson> harvestApproverToHuntingDayPersonIdFk = createForeignKey(approverToHuntingDayId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQGameSpecies> harvestSpeciesFk = createForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestStateAcceptedToHarvestPermit> harvestHarvestStateAcceptedToHarvestPermitFk = createForeignKey(stateAcceptedToHarvestPermit, "name");

    public final com.querydsl.sql.ForeignKey<SQPerson> harvestAuthorFk = createForeignKey(authorId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestQuota> harvestHarvestQuotaFk = createForeignKey(harvestQuotaId, "harvest_quota_id");

    public final com.querydsl.sql.ForeignKey<SQPerson> harvestHarvestReportAuthorFk = createForeignKey(harvestReportAuthorId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQPerson> harvestActualShooterFk = createForeignKey(actualShooterId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestChangeHistory> _harvestChangeHistoryHarvestFk = createInvForeignKey(harvestId, "harvest_id");

    public final com.querydsl.sql.ForeignKey<SQGroupHarvestRejection> _groupHarvestRejectionHarvestFk = createInvForeignKey(harvestId, "harvest_id");

    public final com.querydsl.sql.ForeignKey<SQGameDiaryImage> _gameDiaryImageHarvestFk = createInvForeignKey(harvestId, "harvest_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestSpecimen> _harvestSpecimenHarvestFk = createInvForeignKey(harvestId, "harvest_id");

    public SQHarvest(String variable) {
        super(SQHarvest.class, forVariable(variable), "public", "harvest");
        addMetadata();
    }

    public SQHarvest(String variable, String schema, String table) {
        super(SQHarvest.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvest(String variable, String schema) {
        super(SQHarvest.class, forVariable(variable), schema, "harvest");
        addMetadata();
    }

    public SQHarvest(Path<? extends SQHarvest> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest");
        addMetadata();
    }

    public SQHarvest(PathMetadata metadata) {
        super(SQHarvest.class, metadata, "public", "harvest");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(accuracy, ColumnMetadata.named("accuracy").withIndex(11).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(actualShooterId, ColumnMetadata.named("actual_shooter_id").withIndex(21).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(altitude, ColumnMetadata.named("altitude").withIndex(12).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(altitudeAccuracy, ColumnMetadata.named("altitude_accuracy").withIndex(13).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(17).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(approverToHuntingDayId, ColumnMetadata.named("approver_to_hunting_day_id").withIndex(46).ofType(Types.BIGINT).withSize(19));
        addMetadata(authorId, ColumnMetadata.named("author_id").withIndex(19).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(description, ColumnMetadata.named("description").withIndex(18).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(emailReminderSentTime, ColumnMetadata.named("email_reminder_sent_time").withIndex(27).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(feedingPlace, ColumnMetadata.named("feeding_place").withIndex(48).ofType(Types.BIT).withSize(1));
        addMetadata(fromMobile, ColumnMetadata.named("from_mobile").withIndex(23).ofType(Types.BIT).withSize(1));
        addMetadata(gameSpeciesId, ColumnMetadata.named("game_species_id").withIndex(16).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(geolocationSource, ColumnMetadata.named("geolocation_source").withIndex(14).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(geom, ColumnMetadata.named("geom").withIndex(54).ofType(Types.OTHER).withSize(2147483647).notNull());
        addMetadata(groupHuntingDayId, ColumnMetadata.named("group_hunting_day_id").withIndex(39).ofType(Types.BIGINT).withSize(19));
        addMetadata(harvestId, ColumnMetadata.named("harvest_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitId, ColumnMetadata.named("harvest_permit_id").withIndex(35).ofType(Types.BIGINT).withSize(19));
        addMetadata(harvestQuotaId, ColumnMetadata.named("harvest_quota_id").withIndex(37).ofType(Types.BIGINT).withSize(19));
        addMetadata(harvestReportAuthorId, ColumnMetadata.named("harvest_report_author_id").withIndex(52).ofType(Types.BIGINT).withSize(19));
        addMetadata(harvestReportDate, ColumnMetadata.named("harvest_report_date").withIndex(51).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(harvestReportMemo, ColumnMetadata.named("harvest_report_memo").withIndex(53).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(harvestReportRequired, ColumnMetadata.named("harvest_report_required").withIndex(26).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(harvestReportState, ColumnMetadata.named("harvest_report_state").withIndex(50).ofType(Types.VARCHAR).withSize(255));
        addMetadata(harvestSeasonId, ColumnMetadata.named("harvest_season_id").withIndex(36).ofType(Types.BIGINT).withSize(19));
        addMetadata(huntingAreaSize, ColumnMetadata.named("hunting_area_size").withIndex(31).ofType(Types.NUMERIC).withSize(10).withDigits(2));
        addMetadata(huntingAreaType, ColumnMetadata.named("hunting_area_type").withIndex(29).ofType(Types.VARCHAR).withSize(255));
        addMetadata(huntingMethod, ColumnMetadata.named("hunting_method").withIndex(32).ofType(Types.VARCHAR).withSize(255));
        addMetadata(huntingParty, ColumnMetadata.named("hunting_party").withIndex(30).ofType(Types.VARCHAR).withSize(255));
        addMetadata(latitude, ColumnMetadata.named("latitude").withIndex(9).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(longitude, ColumnMetadata.named("longitude").withIndex(10).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(lukeStatus, ColumnMetadata.named("luke_status").withIndex(34).ofType(Types.VARCHAR).withSize(255));
        addMetadata(mhHirviId, ColumnMetadata.named("mh_hirvi_id").withIndex(24).ofType(Types.INTEGER).withSize(10));
        addMetadata(mhPienriistaId, ColumnMetadata.named("mh_pienriista_id").withIndex(25).ofType(Types.INTEGER).withSize(10));
        addMetadata(mobileClientRefId, ColumnMetadata.named("mobile_client_ref_id").withIndex(20).ofType(Types.BIGINT).withSize(19));
        addMetadata(moderatorOverride, ColumnMetadata.named("moderator_override").withIndex(45).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(municipalityCode, ColumnMetadata.named("municipality_code").withIndex(44).ofType(Types.CHAR).withSize(3));
        addMetadata(permittedMethodDescription, ColumnMetadata.named("permitted_method_description").withIndex(43).ofType(Types.VARCHAR).withSize(255));
        addMetadata(permittedMethodOther, ColumnMetadata.named("permitted_method_other").withIndex(42).ofType(Types.BIT).withSize(1));
        addMetadata(permittedMethodTapeRecorders, ColumnMetadata.named("permitted_method_tape_recorders").withIndex(40).ofType(Types.BIT).withSize(1));
        addMetadata(permittedMethodTraps, ColumnMetadata.named("permitted_method_traps").withIndex(41).ofType(Types.BIT).withSize(1));
        addMetadata(pointOfTime, ColumnMetadata.named("point_of_time").withIndex(15).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(pointOfTimeApprovedToHuntingDay, ColumnMetadata.named("point_of_time_approved_to_hunting_day").withIndex(47).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(propertyIdentifier, ColumnMetadata.named("property_identifier").withIndex(28).ofType(Types.VARCHAR).withSize(255));
        addMetadata(reportedWithPhoneCall, ColumnMetadata.named("reported_with_phone_call").withIndex(33).ofType(Types.BIT).withSize(1));
        addMetadata(rhyId, ColumnMetadata.named("rhy_id").withIndex(22).ofType(Types.BIGINT).withSize(19));
        addMetadata(stateAcceptedToHarvestPermit, ColumnMetadata.named("state_accepted_to_harvest_permit").withIndex(38).ofType(Types.VARCHAR).withSize(255));
        addMetadata(subSpeciesCode, ColumnMetadata.named("sub_species_code").withIndex(49).ofType(Types.INTEGER).withSize(10));
    }

}

