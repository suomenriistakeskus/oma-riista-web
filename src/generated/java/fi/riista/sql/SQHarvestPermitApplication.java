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
 * SQHarvestPermitApplication is a Querydsl query type for SQHarvestPermitApplication
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitApplication extends RelationalPathSpatial<SQHarvestPermitApplication> {

    private static final long serialVersionUID = -217437281;

    public static final SQHarvestPermitApplication harvestPermitApplication = new SQHarvestPermitApplication("harvest_permit_application");

    public final StringPath applicationName = createString("applicationName");

    public final NumberPath<Integer> applicationNumber = createNumber("applicationNumber", Integer.class);

    public final NumberPath<Long> areaId = createNumber("areaId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> contactPersonId = createNumber("contactPersonId", Long.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final StringPath decisionLocaleId = createString("decisionLocaleId");

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath deliveryAddressCity = createString("deliveryAddressCity");

    public final StringPath deliveryAddressPostalCode = createString("deliveryAddressPostalCode");

    public final StringPath deliveryAddressRecipient = createString("deliveryAddressRecipient");

    public final StringPath deliveryAddressStreetAddress = createString("deliveryAddressStreetAddress");

    public final BooleanPath deliveryByMail = createBoolean("deliveryByMail");

    public final StringPath email1 = createString("email1");

    public final StringPath email2 = createString("email2");

    public final NumberPath<Long> harvestPermitApplicationId = createNumber("harvestPermitApplicationId", Long.class);

    public final StringPath harvestPermitCategory = createString("harvestPermitCategory");

    public final NumberPath<Integer> huntingYear = createNumber("huntingYear", Integer.class);

    public final StringPath localeId = createString("localeId");

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath permitHolderCode = createString("permitHolderCode");

    public final NumberPath<Long> permitHolderId = createNumber("permitHolderId", Long.class);

    public final StringPath permitHolderName = createString("permitHolderName");

    public final StringPath permitHolderType = createString("permitHolderType");

    public final StringPath printingUrl = createString("printingUrl");

    public final NumberPath<Long> rhyId = createNumber("rhyId", Long.class);

    public final NumberPath<Integer> shooterOnlyClub = createNumber("shooterOnlyClub", Integer.class);

    public final NumberPath<Integer> shooterOtherClubActive = createNumber("shooterOtherClubActive", Integer.class);

    public final NumberPath<Integer> shooterOtherClubPassive = createNumber("shooterOtherClubPassive", Integer.class);

    public final StringPath status = createString("status");

    public final DateTimePath<java.sql.Timestamp> submitDate = createDateTime("submitDate", java.sql.Timestamp.class);

    public final StringPath uuid = createString("uuid");

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitApplication> harvestPermitApplicationPkey = createPrimaryKey(harvestPermitApplicationId);

    public final com.querydsl.sql.ForeignKey<SQPerson> harvestPermitApplicationContactPersonFk = createForeignKey(contactPersonId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQPermitHolderType> harvestPermitApplicationHolderTypeFk = createForeignKey(permitHolderType, "name");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestPermitApplicationRhyFk = createForeignKey(rhyId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationStatus> harvestPermitApplicationStatusFk = createForeignKey(status, "name");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitArea> harvestPermitApplicationAreaFk = createForeignKey(areaId, "harvest_permit_area_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitCategory> harvestPermitApplicationCategoryFk = createForeignKey(harvestPermitCategory, "name");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestPermitApplicationHolderFk = createForeignKey(permitHolderId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationConflict> _harvestPermitApplicationConflictSecondFk = createInvForeignKey(harvestPermitApplicationId, "second_application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationConflictPalsta> _harvestPermitApplicationConflictPalstaFirstFk = createInvForeignKey(harvestPermitApplicationId, "first_application_id");

    public final com.querydsl.sql.ForeignKey<SQPermitApplicationArchive> _permitApplicationArchiveApplicationFk = createInvForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public final com.querydsl.sql.ForeignKey<SQBirdPermitApplication> _birdPermitApplicationApplicationIdFk = createInvForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationAttachment> _harvestPermitApplicationAttachmentApplicationFk = createInvForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationSpeciesAmount> _harvestPermitApplicationSpeciesAmountApplicationFk = createInvForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public final com.querydsl.sql.ForeignKey<SQAmendmentApplicationData> _amendmentApplicationDataApplicationIdFk = createInvForeignKey(harvestPermitApplicationId, "application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationConflict> _harvestPermitApplicationConflictFirstFk = createInvForeignKey(harvestPermitApplicationId, "first_application_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> _permitDecisionApplicationFk = createInvForeignKey(harvestPermitApplicationId, "application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationConflictPalsta> _harvestPermitApplicationConflictPalstaSecondFk = createInvForeignKey(harvestPermitApplicationId, "second_application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationPartner> _harvestPermitApplicationPartnerApplicationFk = createInvForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationRhy> _harvestPermitApplicationRhyApplicationFk = createInvForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public SQHarvestPermitApplication(String variable) {
        super(SQHarvestPermitApplication.class, forVariable(variable), "public", "harvest_permit_application");
        addMetadata();
    }

    public SQHarvestPermitApplication(String variable, String schema, String table) {
        super(SQHarvestPermitApplication.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitApplication(String variable, String schema) {
        super(SQHarvestPermitApplication.class, forVariable(variable), schema, "harvest_permit_application");
        addMetadata();
    }

    public SQHarvestPermitApplication(Path<? extends SQHarvestPermitApplication> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_application");
        addMetadata();
    }

    public SQHarvestPermitApplication(PathMetadata metadata) {
        super(SQHarvestPermitApplication.class, metadata, "public", "harvest_permit_application");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(applicationName, ColumnMetadata.named("application_name").withIndex(16).ofType(Types.VARCHAR).withSize(255));
        addMetadata(applicationNumber, ColumnMetadata.named("application_number").withIndex(15).ofType(Types.INTEGER).withSize(10));
        addMetadata(areaId, ColumnMetadata.named("area_id").withIndex(11).ofType(Types.BIGINT).withSize(19));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(contactPersonId, ColumnMetadata.named("contact_person_id").withIndex(14).ofType(Types.BIGINT).withSize(19));
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(decisionLocaleId, ColumnMetadata.named("decision_locale_id").withIndex(29).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(deliveryAddressCity, ColumnMetadata.named("delivery_address_city").withIndex(35).ofType(Types.VARCHAR).withSize(255));
        addMetadata(deliveryAddressPostalCode, ColumnMetadata.named("delivery_address_postal_code").withIndex(34).ofType(Types.VARCHAR).withSize(255));
        addMetadata(deliveryAddressRecipient, ColumnMetadata.named("delivery_address_recipient").withIndex(32).ofType(Types.VARCHAR).withSize(255));
        addMetadata(deliveryAddressStreetAddress, ColumnMetadata.named("delivery_address_street_address").withIndex(33).ofType(Types.VARCHAR).withSize(255));
        addMetadata(deliveryByMail, ColumnMetadata.named("delivery_by_mail").withIndex(22).ofType(Types.BIT).withSize(1));
        addMetadata(email1, ColumnMetadata.named("email1").withIndex(20).ofType(Types.VARCHAR).withSize(255));
        addMetadata(email2, ColumnMetadata.named("email2").withIndex(21).ofType(Types.VARCHAR).withSize(255));
        addMetadata(harvestPermitApplicationId, ColumnMetadata.named("harvest_permit_application_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitCategory, ColumnMetadata.named("harvest_permit_category").withIndex(31).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(huntingYear, ColumnMetadata.named("hunting_year").withIndex(23).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(localeId, ColumnMetadata.named("locale_id").withIndex(25).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitHolderCode, ColumnMetadata.named("permit_holder_code").withIndex(27).ofType(Types.VARCHAR).withSize(255));
        addMetadata(permitHolderId, ColumnMetadata.named("permit_holder_id").withIndex(9).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitHolderName, ColumnMetadata.named("permit_holder_name").withIndex(26).ofType(Types.VARCHAR).withSize(255));
        addMetadata(permitHolderType, ColumnMetadata.named("permit_holder_type").withIndex(28).ofType(Types.VARCHAR).withSize(255));
        addMetadata(printingUrl, ColumnMetadata.named("printing_url").withIndex(10).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(rhyId, ColumnMetadata.named("rhy_id").withIndex(12).ofType(Types.BIGINT).withSize(19));
        addMetadata(shooterOnlyClub, ColumnMetadata.named("shooter_only_club").withIndex(17).ofType(Types.INTEGER).withSize(10));
        addMetadata(shooterOtherClubActive, ColumnMetadata.named("shooter_other_club_active").withIndex(19).ofType(Types.INTEGER).withSize(10));
        addMetadata(shooterOtherClubPassive, ColumnMetadata.named("shooter_other_club_passive").withIndex(18).ofType(Types.INTEGER).withSize(10));
        addMetadata(status, ColumnMetadata.named("status").withIndex(13).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(submitDate, ColumnMetadata.named("submit_date").withIndex(24).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(uuid, ColumnMetadata.named("uuid").withIndex(30).ofType(Types.CHAR).withSize(36));
    }

}

