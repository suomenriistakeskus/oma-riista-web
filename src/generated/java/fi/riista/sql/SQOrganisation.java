package fi.riista.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import com.querydsl.sql.spatial.RelationalPathSpatial;


/**
 * SQOrganisation is a Querydsl query type for SQOrganisation
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQOrganisation extends RelationalPathSpatial<SQOrganisation> {

    private static final long serialVersionUID = 2103141165;

    public static final SQOrganisation organisation = new SQOrganisation("organisation");

    public final NumberPath<Double> accuracy = createNumber("accuracy", Double.class);

    public final NumberPath<Long> addressId = createNumber("addressId", Long.class);

    public final NumberPath<Double> altitude = createNumber("altitude", Double.class);

    public final NumberPath<Double> altitudeAccuracy = createNumber("altitudeAccuracy", Double.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath email = createString("email");

    public final BooleanPath fromMooseDataCard = createBoolean("fromMooseDataCard");

    public final NumberPath<Long> gameSpeciesId = createNumber("gameSpeciesId", Long.class);

    public final StringPath geolocationSource = createString("geolocationSource");

    public final StringPath hallialueId = createString("hallialueId");

    public final NumberPath<Long> harvestPermitId = createNumber("harvestPermitId", Long.class);

    public final DateTimePath<java.sql.Timestamp> harvestPermitModificationTime = createDateTime("harvestPermitModificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> huntingAreaId = createNumber("huntingAreaId", Long.class);

    public final NumberPath<java.math.BigDecimal> huntingAreaSize = createNumber("huntingAreaSize", java.math.BigDecimal.class);

    public final NumberPath<Integer> huntingYear = createNumber("huntingYear", Integer.class);

    public final BooleanPath isAtCoast = createBoolean("isAtCoast");

    public final NumberPath<Integer> latitude = createNumber("latitude", Integer.class);

    public final StringPath lhOrganisationId = createString("lhOrganisationId");

    public final NumberPath<Integer> longitude = createNumber("longitude", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> mooseAreaId = createNumber("mooseAreaId", Long.class);

    public final StringPath nameFinnish = createString("nameFinnish");

    public final StringPath nameSwedish = createString("nameSwedish");

    public final StringPath officialCode = createString("officialCode");

    public final NumberPath<Long> organisationId = createNumber("organisationId", Long.class);

    public final StringPath organisationType = createString("organisationType");

    public final NumberPath<Long> parentOrganisationId = createNumber("parentOrganisationId", Long.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath poronhoitoalueId = createString("poronhoitoalueId");

    public final com.querydsl.sql.PrimaryKey<SQOrganisation> organisationPkey = createPrimaryKey(organisationId);

    public final com.querydsl.sql.ForeignKey<SQGameSpecies> organisationGameSpeciesFk = createForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQAddress> organisationAddressFk = createForeignKey(addressId, "address_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisationType> organisationTypeFk = createForeignKey(organisationType, "name");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> organisationHarvestPermitFk = createForeignKey(harvestPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQHta> organisationMooseAreaFk = createForeignKey(mooseAreaId, "gid");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> organisationParentFk = createForeignKey(parentOrganisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHuntingClubArea> organisationHuntingClubAreaIdFk = createForeignKey(huntingAreaId, "hunting_club_area_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> _harvestPermitRhyFk = createInvForeignKey(organisationId, "rhy_id");

    public final com.querydsl.sql.ForeignKey<SQHarvest> _harvestRhyFk = createInvForeignKey(organisationId, "rhy_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitRhys> _harvestPermitRhysOrganisationFk = createInvForeignKey(organisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> _harvestPermitPermitHolderFk = createInvForeignKey(organisationId, "permit_holder_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisationVenue> _organisationVenueOrganisationFk = createInvForeignKey(organisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQPerson> _rhyMembershipFk = createInvForeignKey(organisationId, "rhy_membership_id");

    public final com.querydsl.sql.ForeignKey<SQOccupationNomination> _occupationNominationRhyIdFk = createInvForeignKey(organisationId, "rhy_id");

    public final com.querydsl.sql.ForeignKey<SQObservation> _gameObservationRhyFk = createInvForeignKey(organisationId, "rhy_id");

    public final com.querydsl.sql.ForeignKey<SQBasicClubHuntingSummary> _basicClubHuntingSummaryClubFk = createInvForeignKey(organisationId, "club_id");

    public final com.querydsl.sql.ForeignKey<SQHuntingClubArea> _huntingClubAreaClubFk = createInvForeignKey(organisationId, "club_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> _organisationParentFk = createInvForeignKey(organisationId, "parent_organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitAllocation> _harvestPermitAllocationOrganisationFk = createInvForeignKey(organisationId, "hunting_club_id");

    public final com.querydsl.sql.ForeignKey<SQHuntingClubMemberInvitation> _huntingClubMemberInvitationHuntingClubFk = createInvForeignKey(organisationId, "hunting_club_id");

    public final com.querydsl.sql.ForeignKey<SQAnnouncement> _announcementFromOrganisationFk = createInvForeignKey(organisationId, "from_organisation_id");

    public final com.querydsl.sql.ForeignKey<SQSrvaEvent> _srvaEventRhyFk = createInvForeignKey(organisationId, "rhy_id");

    public final com.querydsl.sql.ForeignKey<SQGroupObservationRejection> _groupObservationRejectionHuntingClubGroupFk = createInvForeignKey(organisationId, "hunting_club_group_id");

    public final com.querydsl.sql.ForeignKey<SQCalendarEvent> _calendarEventOrganisationFk = createInvForeignKey(organisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQOccupation> _occupationOrganisationFk = createInvForeignKey(organisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQMooseHuntingSummary> _mooseHuntingSummaryClubFk = createInvForeignKey(organisationId, "club_id");

    public final com.querydsl.sql.ForeignKey<SQAnnouncementSubscriber> _announcementSubscriberOrganisationFk = createInvForeignKey(organisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQGroupHuntingDay> _groupHuntingDayGroupFk = createInvForeignKey(organisationId, "hunting_group_id");

    public final com.querydsl.sql.ForeignKey<SQMooseDataCardImport> _mooseDataCardImportHuntingGroupFk = createInvForeignKey(organisationId, "hunting_group_id");

    public final com.querydsl.sql.ForeignKey<SQGroupHarvestRejection> _groupHarvestRejectionHuntingClubGroupFk = createInvForeignKey(organisationId, "hunting_club_group_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestAreaRhys> _harvestAreaRhysOrganisationFk = createInvForeignKey(organisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitPartners> _harvestPermitPartnersOrganisationFk = createInvForeignKey(organisationId, "organisation_id");

    public SQOrganisation(String variable) {
        super(SQOrganisation.class, forVariable(variable), "public", "organisation");
        addMetadata();
    }

    public SQOrganisation(String variable, String schema, String table) {
        super(SQOrganisation.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQOrganisation(Path<? extends SQOrganisation> path) {
        super(path.getType(), path.getMetadata(), "public", "organisation");
        addMetadata();
    }

    public SQOrganisation(PathMetadata metadata) {
        super(SQOrganisation.class, metadata, "public", "organisation");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(accuracy, ColumnMetadata.named("accuracy").withIndex(19).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(addressId, ColumnMetadata.named("address_id").withIndex(14).ofType(Types.BIGINT).withSize(19));
        addMetadata(altitude, ColumnMetadata.named("altitude").withIndex(20).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(altitudeAccuracy, ColumnMetadata.named("altitude_accuracy").withIndex(21).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(9).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(email, ColumnMetadata.named("email").withIndex(15).ofType(Types.VARCHAR).withSize(255));
        addMetadata(fromMooseDataCard, ColumnMetadata.named("from_moose_data_card").withIndex(31).ofType(Types.BIT).withSize(1));
        addMetadata(gameSpeciesId, ColumnMetadata.named("game_species_id").withIndex(27).ofType(Types.BIGINT).withSize(19));
        addMetadata(geolocationSource, ColumnMetadata.named("geolocation_source").withIndex(22).ofType(Types.VARCHAR).withSize(255));
        addMetadata(hallialueId, ColumnMetadata.named("hallialue_id").withIndex(25).ofType(Types.VARCHAR).withSize(255));
        addMetadata(harvestPermitId, ColumnMetadata.named("harvest_permit_id").withIndex(34).ofType(Types.BIGINT).withSize(19));
        addMetadata(harvestPermitModificationTime, ColumnMetadata.named("harvest_permit_modification_time").withIndex(33).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(huntingAreaId, ColumnMetadata.named("hunting_area_id").withIndex(29).ofType(Types.BIGINT).withSize(19));
        addMetadata(huntingAreaSize, ColumnMetadata.named("hunting_area_size").withIndex(30).ofType(Types.NUMERIC).withSize(10).withDigits(2));
        addMetadata(huntingYear, ColumnMetadata.named("hunting_year").withIndex(28).ofType(Types.INTEGER).withSize(10));
        addMetadata(isAtCoast, ColumnMetadata.named("is_at_coast").withIndex(26).ofType(Types.BIT).withSize(1));
        addMetadata(latitude, ColumnMetadata.named("latitude").withIndex(18).ofType(Types.INTEGER).withSize(10));
        addMetadata(lhOrganisationId, ColumnMetadata.named("lh_organisation_id").withIndex(23).ofType(Types.VARCHAR).withSize(255));
        addMetadata(longitude, ColumnMetadata.named("longitude").withIndex(17).ofType(Types.INTEGER).withSize(10));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(6).ofType(Types.BIGINT).withSize(19));
        addMetadata(mooseAreaId, ColumnMetadata.named("moose_area_id").withIndex(32).ofType(Types.BIGINT).withSize(19));
        addMetadata(nameFinnish, ColumnMetadata.named("name_finnish").withIndex(12).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(nameSwedish, ColumnMetadata.named("name_swedish").withIndex(13).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(officialCode, ColumnMetadata.named("official_code").withIndex(11).ofType(Types.VARCHAR).withSize(255));
        addMetadata(organisationId, ColumnMetadata.named("organisation_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organisationType, ColumnMetadata.named("organisation_type").withIndex(2).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(parentOrganisationId, ColumnMetadata.named("parent_organisation_id").withIndex(10).ofType(Types.BIGINT).withSize(19));
        addMetadata(phoneNumber, ColumnMetadata.named("phone_number").withIndex(16).ofType(Types.VARCHAR).withSize(255));
        addMetadata(poronhoitoalueId, ColumnMetadata.named("poronhoitoalue_id").withIndex(24).ofType(Types.VARCHAR).withSize(255));
    }

}

