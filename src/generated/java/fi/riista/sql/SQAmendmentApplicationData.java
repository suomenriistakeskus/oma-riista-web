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
 * SQAmendmentApplicationData is a Querydsl query type for
 * SQAmendmentApplicationData
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQAmendmentApplicationData extends RelationalPathSpatial<SQAmendmentApplicationData> {

    private static final long serialVersionUID = -137529312;

    public static final SQAmendmentApplicationData amendmentApplicationData =
            new SQAmendmentApplicationData("amendment_application_data");

    public final NumberPath<Double> accuracy = createNumber("accuracy", Double.class);

    public final StringPath age = createString("age");

    public final NumberPath<Double> altitude = createNumber("altitude", Double.class);

    public final NumberPath<Double> altitudeAccuracy = createNumber("altitudeAccuracy", Double.class);

    public final NumberPath<Long> amendmentApplicationDataId = createNumber("amendmentApplicationDataId", Long.class);

    public final NumberPath<Long> applicationId = createNumber("applicationId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath gender = createString("gender");

    public final StringPath geolocationSource = createString("geolocationSource");

    public final NumberPath<Integer> latitude = createNumber("latitude", Integer.class);

    public final NumberPath<Integer> longitude = createNumber("longitude", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> nonEdibleHarvestId = createNumber("nonEdibleHarvestId", Long.class);

    public final NumberPath<Long> originalPermitId = createNumber("originalPermitId", Long.class);

    public final NumberPath<Long> partnerId = createNumber("partnerId", Long.class);

    public final DateTimePath<java.sql.Timestamp> pointOfTime = createDateTime("pointOfTime", java.sql.Timestamp.class);

    public final NumberPath<Long> shooterId = createNumber("shooterId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQAmendmentApplicationData> amendmentApplicationDataPkey = createPrimaryKey(amendmentApplicationDataId);

    public final com.querydsl.sql.ForeignKey<SQGameAge> amendmentApplicationDataAgeFk = createForeignKey(age, "name");

    public final com.querydsl.sql.ForeignKey<SQHarvest> amendmentApplicationDataHarvestIdFk = createForeignKey(nonEdibleHarvestId, "harvest_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> amendmentApplicationDataPermitIdFk = createForeignKey(originalPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> amendmentApplicationDataApplicationIdFk = createForeignKey(applicationId, "harvest_permit_application_id");

    public final com.querydsl.sql.ForeignKey<SQGameGender> amendmentApplicationDataGenderFk = createForeignKey(gender, "name");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> amendmentApplicationDataPartnerFk = createForeignKey(partnerId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQPerson> amendmentApplicationDataShooterFk = createForeignKey(shooterId, "person_id");

    public SQAmendmentApplicationData(String variable) {
        super(SQAmendmentApplicationData.class, forVariable(variable), "public", "amendment_application_data");
        addMetadata();
    }

    public SQAmendmentApplicationData(String variable, String schema, String table) {
        super(SQAmendmentApplicationData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQAmendmentApplicationData(String variable, String schema) {
        super(SQAmendmentApplicationData.class, forVariable(variable), schema, "amendment_application_data");
        addMetadata();
    }

    public SQAmendmentApplicationData(Path<? extends SQAmendmentApplicationData> path) {
        super(path.getType(), path.getMetadata(), "public", "amendment_application_data");
        addMetadata();
    }

    public SQAmendmentApplicationData(PathMetadata metadata) {
        super(SQAmendmentApplicationData.class, metadata, "public", "amendment_application_data");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(accuracy, ColumnMetadata.named("accuracy").withIndex(19).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(age, ColumnMetadata.named("age").withIndex(13).ofType(Types.VARCHAR).withSize(255));
        addMetadata(altitude, ColumnMetadata.named("altitude").withIndex(20).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(altitudeAccuracy, ColumnMetadata.named("altitude_accuracy").withIndex(21).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(amendmentApplicationDataId, ColumnMetadata.named("amendment_application_data_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(applicationId, ColumnMetadata.named("application_id").withIndex(9).ofType(Types.BIGINT).withSize(19));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(gender, ColumnMetadata.named("gender").withIndex(14).ofType(Types.VARCHAR).withSize(255));
        addMetadata(geolocationSource, ColumnMetadata.named("geolocation_source").withIndex(22).ofType(Types.VARCHAR).withSize(255));
        addMetadata(latitude, ColumnMetadata.named("latitude").withIndex(17).ofType(Types.INTEGER).withSize(10));
        addMetadata(longitude, ColumnMetadata.named("longitude").withIndex(18).ofType(Types.INTEGER).withSize(10));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(nonEdibleHarvestId, ColumnMetadata.named("non_edible_harvest_id").withIndex(11).ofType(Types.BIGINT).withSize(19));
        addMetadata(originalPermitId, ColumnMetadata.named("original_permit_id").withIndex(10).ofType(Types.BIGINT).withSize(19));
        addMetadata(partnerId, ColumnMetadata.named("partner_id").withIndex(16).ofType(Types.BIGINT).withSize(19));
        addMetadata(pointOfTime, ColumnMetadata.named("point_of_time").withIndex(12).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(shooterId, ColumnMetadata.named("shooter_id").withIndex(15).ofType(Types.BIGINT).withSize(19));
    }

}

