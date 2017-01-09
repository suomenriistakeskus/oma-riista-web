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
 * SQVenue is a Querydsl query type for SQVenue
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQVenue extends RelationalPathSpatial<SQVenue> {

    private static final long serialVersionUID = 978169724;

    public static final SQVenue venue = new SQVenue("venue");

    public final NumberPath<Double> accuracy = createNumber("accuracy", Double.class);

    public final NumberPath<Long> addressId = createNumber("addressId", Long.class);

    public final NumberPath<Double> altitude = createNumber("altitude", Double.class);

    public final NumberPath<Double> altitudeAccuracy = createNumber("altitudeAccuracy", Double.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath geolocationSource = createString("geolocationSource");

    public final StringPath info = createString("info");

    public final NumberPath<Integer> latitude = createNumber("latitude", Integer.class);

    public final NumberPath<Integer> longitude = createNumber("longitude", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> venueId = createNumber("venueId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQVenue> venuePkey = createPrimaryKey(venueId);

    public final com.querydsl.sql.ForeignKey<SQAddress> venueAddressFk = createForeignKey(addressId, "address_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisationVenue> _organisationVenueVenueFk = createInvForeignKey(venueId, "venue_id");

    public final com.querydsl.sql.ForeignKey<SQCalendarEvent> _calendarEventVenueFk = createInvForeignKey(venueId, "venue_id");

    public SQVenue(String variable) {
        super(SQVenue.class, forVariable(variable), "public", "venue");
        addMetadata();
    }

    public SQVenue(String variable, String schema, String table) {
        super(SQVenue.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQVenue(Path<? extends SQVenue> path) {
        super(path.getType(), path.getMetadata(), "public", "venue");
        addMetadata();
    }

    public SQVenue(PathMetadata metadata) {
        super(SQVenue.class, metadata, "public", "venue");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(accuracy, ColumnMetadata.named("accuracy").withIndex(14).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(addressId, ColumnMetadata.named("address_id").withIndex(11).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(altitude, ColumnMetadata.named("altitude").withIndex(15).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(altitudeAccuracy, ColumnMetadata.named("altitude_accuracy").withIndex(16).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(geolocationSource, ColumnMetadata.named("geolocation_source").withIndex(17).ofType(Types.VARCHAR).withSize(255));
        addMetadata(info, ColumnMetadata.named("info").withIndex(10).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(latitude, ColumnMetadata.named("latitude").withIndex(13).ofType(Types.INTEGER).withSize(10));
        addMetadata(longitude, ColumnMetadata.named("longitude").withIndex(12).ofType(Types.INTEGER).withSize(10));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(name, ColumnMetadata.named("name").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(venueId, ColumnMetadata.named("venue_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

