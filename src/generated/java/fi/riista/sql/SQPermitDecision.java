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
 * SQPermitDecision is a Querydsl query type for SQPermitDecision
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecision extends RelationalPathSpatial<SQPermitDecision> {

    private static final long serialVersionUID = -429726038;

    public static final SQPermitDecision permitDecision = new SQPermitDecision("permit_decision");

    public final StringPath additionalInfoBody = createString("additionalInfoBody");

    public final BooleanPath additionalInfoComplete = createBoolean("additionalInfoComplete");

    public final StringPath administrativeCourtBody = createString("administrativeCourtBody");

    public final BooleanPath administrativeCourtComplete = createBoolean("administrativeCourtComplete");

    public final StringPath appealBody = createString("appealBody");

    public final BooleanPath appealComplete = createBoolean("appealComplete");

    public final StringPath appealStatus = createString("appealStatus");

    public final StringPath applicationBody = createString("applicationBody");

    public final BooleanPath applicationComplete = createBoolean("applicationComplete");

    public final NumberPath<Long> applicationId = createNumber("applicationId", Long.class);

    public final StringPath applicationReasoningBody = createString("applicationReasoningBody");

    public final BooleanPath applicationReasoningComplete = createBoolean("applicationReasoningComplete");

    public final StringPath attachmentsBody = createString("attachmentsBody");

    public final BooleanPath attachmentsComplete = createBoolean("attachmentsComplete");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> contactPersonId = createNumber("contactPersonId", Long.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final StringPath decisionBody = createString("decisionBody");

    public final BooleanPath decisionComplete = createBoolean("decisionComplete");

    public final StringPath decisionExtra = createString("decisionExtra");

    public final NumberPath<Long> decisionMakerId = createNumber("decisionMakerId", Long.class);

    public final NumberPath<Integer> decisionNumber = createNumber("decisionNumber", Integer.class);

    public final StringPath decisionReasoningBody = createString("decisionReasoningBody");

    public final BooleanPath decisionReasoningComplete = createBoolean("decisionReasoningComplete");

    public final StringPath decisionType = createString("decisionType");

    public final NumberPath<Integer> decisionYear = createNumber("decisionYear", Integer.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath deliveryAddressCity = createString("deliveryAddressCity");

    public final StringPath deliveryAddressPostalCode = createString("deliveryAddressPostalCode");

    public final StringPath deliveryAddressRecipient = createString("deliveryAddressRecipient");

    public final StringPath deliveryAddressStreetAddress = createString("deliveryAddressStreetAddress");

    public final StringPath deliveryBody = createString("deliveryBody");

    public final BooleanPath deliveryComplete = createBoolean("deliveryComplete");

    public final StringPath executionBody = createString("executionBody");

    public final BooleanPath executionComplete = createBoolean("executionComplete");

    public final StringPath grantStatus = createString("grantStatus");

    public final NumberPath<Long> handlerId = createNumber("handlerId", Long.class);

    public final NumberPath<Integer> htaId = createNumber("htaId", Integer.class);

    public final StringPath legalAdviceBody = createString("legalAdviceBody");

    public final BooleanPath legalAdviceComplete = createBoolean("legalAdviceComplete");

    public final BooleanPath legalSection32 = createBoolean("legalSection32");

    public final BooleanPath legalSection33 = createBoolean("legalSection33");

    public final BooleanPath legalSection34 = createBoolean("legalSection34");

    public final BooleanPath legalSection35 = createBoolean("legalSection35");

    public final BooleanPath legalSection51 = createBoolean("legalSection51");

    public final StringPath localeId = createString("localeId");

    public final DateTimePath<java.sql.Timestamp> lockedDate = createDateTime("lockedDate", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath notificationObligationBody = createString("notificationObligationBody");

    public final BooleanPath notificationObligationComplete = createBoolean("notificationObligationComplete");

    public final NumberPath<Long> originalDecisionId = createNumber("originalDecisionId", Long.class);

    public final NumberPath<java.math.BigDecimal> paymentAmount = createNumber("paymentAmount", java.math.BigDecimal.class);

    public final StringPath paymentBody = createString("paymentBody");

    public final BooleanPath paymentComplete = createBoolean("paymentComplete");

    public final NumberPath<Long> permitDecisionId = createNumber("permitDecisionId", Long.class);

    public final StringPath permitHolderCode = createString("permitHolderCode");

    public final NumberPath<Long> permitHolderId = createNumber("permitHolderId", Long.class);

    public final StringPath permitHolderName = createString("permitHolderName");

    public final StringPath permitHolderType = createString("permitHolderType");

    public final StringPath permitTypeCode = createString("permitTypeCode");

    public final NumberPath<Long> presenterId = createNumber("presenterId", Long.class);

    public final StringPath processingBody = createString("processingBody");

    public final BooleanPath processingComplete = createBoolean("processingComplete");

    public final DateTimePath<java.sql.Timestamp> publishDate = createDateTime("publishDate", java.sql.Timestamp.class);

    public final NumberPath<Long> referenceId = createNumber("referenceId", Long.class);

    public final StringPath restrictionBody = createString("restrictionBody");

    public final BooleanPath restrictionComplete = createBoolean("restrictionComplete");

    public final StringPath restrictionExtra = createString("restrictionExtra");

    public final NumberPath<Long> rhyId = createNumber("rhyId", Long.class);

    public final StringPath status = createString("status");

    public final NumberPath<Integer> validityYears = createNumber("validityYears", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQPermitDecision> permitDecisionPkey = createPrimaryKey(permitDecisionId);

    public final com.querydsl.sql.ForeignKey<SQHta> permitDecisionHtaFk = createForeignKey(htaId, "gid");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionStatus> permitDecisionStatusFk = createForeignKey(status, "name");

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> permitDecisionReferenceFk = createForeignKey(referenceId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQPermitHolderType> permitDecisionHolderTypeFk = createForeignKey(permitHolderType, "name");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionAuthority> permitDecisionPresenterAuthorityFk = createForeignKey(presenterId, "permit_decision_authority_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> permitDecisionRhyFk = createForeignKey(rhyId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> permitDecisionOriginalDecisionFk = createForeignKey(originalDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> permitDecisionHolderFk = createForeignKey(permitHolderId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionAuthority> permitDecisionDecisionMakerAuthorityFk = createForeignKey(decisionMakerId, "permit_decision_authority_id");

    public final com.querydsl.sql.ForeignKey<SQSystemUser> permitDecisionHandlerIdFk = createForeignKey(handlerId, "user_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> permitDecisionApplicationFk = createForeignKey(applicationId, "harvest_permit_application_id");

    public final com.querydsl.sql.ForeignKey<SQPerson> permitDecisionContactPersonFk = createForeignKey(contactPersonId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionProtectedAreaType> _permitDecisionProtectedAreaTypeDecisionIdFk = createInvForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> _permitDecisionReferenceFk = createInvForeignKey(permitDecisionId, "reference_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionDelivery> _permitDecisionDeliveryDecisionFk = createInvForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionRevision> _permitDecisionRevisionDecisionIdFk = createInvForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionAuthority> _permitDecisionAuthorityDecisionFk = createInvForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionSpeciesAmount> _permitDecisionSpeciesAmountDecisionFk = createInvForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> _harvestPermitPermitDecisionFk = createInvForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionInvoice> _permitDecisionInvoiceDecisionFk = createInvForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> _permitDecisionOriginalDecisionFk = createInvForeignKey(permitDecisionId, "original_decision_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionAction> _permitDecisionActionDecisionFk = createInvForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionAttachment> _permitDecisionAttachmentDecisionIdFk = createInvForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionDerogationReason> _permitDecisionDerogationReasonDecisionIdFk = createInvForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionForbiddenMethod> _permitDecisionForbiddenMethodDecisionIdFk = createInvForeignKey(permitDecisionId, "permit_decision_id");

    public SQPermitDecision(String variable) {
        super(SQPermitDecision.class, forVariable(variable), "public", "permit_decision");
        addMetadata();
    }

    public SQPermitDecision(String variable, String schema, String table) {
        super(SQPermitDecision.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecision(String variable, String schema) {
        super(SQPermitDecision.class, forVariable(variable), schema, "permit_decision");
        addMetadata();
    }

    public SQPermitDecision(Path<? extends SQPermitDecision> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision");
        addMetadata();
    }

    public SQPermitDecision(PathMetadata metadata) {
        super(SQPermitDecision.class, metadata, "public", "permit_decision");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(additionalInfoBody, ColumnMetadata.named("additional_info_body").withIndex(38).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(additionalInfoComplete, ColumnMetadata.named("additional_info_complete").withIndex(39).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(administrativeCourtBody, ColumnMetadata.named("administrative_court_body").withIndex(48).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(administrativeCourtComplete, ColumnMetadata.named("administrative_court_complete").withIndex(49).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(appealBody, ColumnMetadata.named("appeal_body").withIndex(36).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(appealComplete, ColumnMetadata.named("appeal_complete").withIndex(37).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(appealStatus, ColumnMetadata.named("appeal_status").withIndex(56).ofType(Types.VARCHAR).withSize(255));
        addMetadata(applicationBody, ColumnMetadata.named("application_body").withIndex(20).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(applicationComplete, ColumnMetadata.named("application_complete").withIndex(21).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(applicationId, ColumnMetadata.named("application_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(applicationReasoningBody, ColumnMetadata.named("application_reasoning_body").withIndex(22).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(applicationReasoningComplete, ColumnMetadata.named("application_reasoning_complete").withIndex(23).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(attachmentsBody, ColumnMetadata.named("attachments_body").withIndex(44).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(attachmentsComplete, ColumnMetadata.named("attachments_complete").withIndex(45).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(contactPersonId, ColumnMetadata.named("contact_person_id").withIndex(14).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(decisionBody, ColumnMetadata.named("decision_body").withIndex(18).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(decisionComplete, ColumnMetadata.named("decision_complete").withIndex(19).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(decisionExtra, ColumnMetadata.named("decision_extra").withIndex(69).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(decisionMakerId, ColumnMetadata.named("decision_maker_id").withIndex(52).ofType(Types.BIGINT).withSize(19));
        addMetadata(decisionNumber, ColumnMetadata.named("decision_number").withIndex(67).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(decisionReasoningBody, ColumnMetadata.named("decision_reasoning_body").withIndex(26).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(decisionReasoningComplete, ColumnMetadata.named("decision_reasoning_complete").withIndex(27).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(decisionType, ColumnMetadata.named("decision_type").withIndex(57).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(decisionYear, ColumnMetadata.named("decision_year").withIndex(65).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(deliveryAddressCity, ColumnMetadata.named("delivery_address_city").withIndex(64).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(deliveryAddressPostalCode, ColumnMetadata.named("delivery_address_postal_code").withIndex(63).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(deliveryAddressRecipient, ColumnMetadata.named("delivery_address_recipient").withIndex(61).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(deliveryAddressStreetAddress, ColumnMetadata.named("delivery_address_street_address").withIndex(62).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(deliveryBody, ColumnMetadata.named("delivery_body").withIndex(40).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(deliveryComplete, ColumnMetadata.named("delivery_complete").withIndex(41).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(executionBody, ColumnMetadata.named("execution_body").withIndex(30).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(executionComplete, ColumnMetadata.named("execution_complete").withIndex(31).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(grantStatus, ColumnMetadata.named("grant_status").withIndex(55).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(handlerId, ColumnMetadata.named("handler_id").withIndex(46).ofType(Types.BIGINT).withSize(19));
        addMetadata(htaId, ColumnMetadata.named("hta_id").withIndex(13).ofType(Types.INTEGER).withSize(10));
        addMetadata(legalAdviceBody, ColumnMetadata.named("legal_advice_body").withIndex(32).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(legalAdviceComplete, ColumnMetadata.named("legal_advice_complete").withIndex(33).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(legalSection32, ColumnMetadata.named("legal_section_32").withIndex(70).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(legalSection33, ColumnMetadata.named("legal_section_33").withIndex(71).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(legalSection34, ColumnMetadata.named("legal_section_34").withIndex(72).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(legalSection35, ColumnMetadata.named("legal_section_35").withIndex(73).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(legalSection51, ColumnMetadata.named("legal_section_51").withIndex(74).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(localeId, ColumnMetadata.named("locale_id").withIndex(54).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(lockedDate, ColumnMetadata.named("locked_date").withIndex(16).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(notificationObligationBody, ColumnMetadata.named("notification_obligation_body").withIndex(34).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(notificationObligationComplete, ColumnMetadata.named("notification_obligation_complete").withIndex(35).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(originalDecisionId, ColumnMetadata.named("original_decision_id").withIndex(11).ofType(Types.BIGINT).withSize(19));
        addMetadata(paymentAmount, ColumnMetadata.named("payment_amount").withIndex(50).ofType(Types.NUMERIC).withSize(6).withDigits(2));
        addMetadata(paymentBody, ColumnMetadata.named("payment_body").withIndex(42).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(paymentComplete, ColumnMetadata.named("payment_complete").withIndex(43).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(permitDecisionId, ColumnMetadata.named("permit_decision_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitHolderCode, ColumnMetadata.named("permit_holder_code").withIndex(59).ofType(Types.VARCHAR).withSize(255));
        addMetadata(permitHolderId, ColumnMetadata.named("permit_holder_id").withIndex(15).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitHolderName, ColumnMetadata.named("permit_holder_name").withIndex(58).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(permitHolderType, ColumnMetadata.named("permit_holder_type").withIndex(60).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(permitTypeCode, ColumnMetadata.named("permit_type_code").withIndex(68).ofType(Types.VARCHAR).withSize(3).notNull());
        addMetadata(presenterId, ColumnMetadata.named("presenter_id").withIndex(51).ofType(Types.BIGINT).withSize(19));
        addMetadata(processingBody, ColumnMetadata.named("processing_body").withIndex(24).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(processingComplete, ColumnMetadata.named("processing_complete").withIndex(25).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(publishDate, ColumnMetadata.named("publish_date").withIndex(17).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(referenceId, ColumnMetadata.named("reference_id").withIndex(47).ofType(Types.BIGINT).withSize(19));
        addMetadata(restrictionBody, ColumnMetadata.named("restriction_body").withIndex(28).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(restrictionComplete, ColumnMetadata.named("restriction_complete").withIndex(29).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(restrictionExtra, ColumnMetadata.named("restriction_extra").withIndex(53).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(rhyId, ColumnMetadata.named("rhy_id").withIndex(12).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(status, ColumnMetadata.named("status").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(validityYears, ColumnMetadata.named("validity_years").withIndex(66).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

