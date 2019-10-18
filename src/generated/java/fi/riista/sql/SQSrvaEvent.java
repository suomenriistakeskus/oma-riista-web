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
 * SQSrvaEvent is a Querydsl query type for SQSrvaEvent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQSrvaEvent extends RelationalPathSpatial<SQSrvaEvent> {

    private static final long serialVersionUID = 30979581;

    public static final SQSrvaEvent srvaEvent = new SQSrvaEvent("srva_event");

    public final NumberPath<Double> accuracy = createNumber("accuracy", Double.class);

    public final NumberPath<Double> altitude = createNumber("altitude", Double.class);

    public final NumberPath<Double> altitudeAccuracy = createNumber("altitudeAccuracy", Double.class);

    public final NumberPath<Long> approverAsPersonId = createNumber("approverAsPersonId", Long.class);

    public final NumberPath<Long> approverAsUserId = createNumber("approverAsUserId", Long.class);

    public final NumberPath<Long> authorId = createNumber("authorId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath description = createString("description");

    public final StringPath eventName = createString("eventName");

    public final StringPath eventResult = createString("eventResult");

    public final StringPath eventType = createString("eventType");

    public final BooleanPath fromMobile = createBoolean("fromMobile");

    public final NumberPath<Long> gameSpeciesId = createNumber("gameSpeciesId", Long.class);

    public final StringPath geolocationSource = createString("geolocationSource");

    public final GeometryPath<org.geolatte.geom.Geometry> geom = createGeometry("geom", org.geolatte.geom.Geometry.class);

    public final NumberPath<Integer> latitude = createNumber("latitude", Integer.class);

    public final NumberPath<Integer> longitude = createNumber("longitude", Integer.class);

    public final NumberPath<Long> mobileClientRefId = createNumber("mobileClientRefId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath otherMethodDescription = createString("otherMethodDescription");

    public final StringPath otherSpeciesDescription = createString("otherSpeciesDescription");

    public final StringPath otherTypeDescription = createString("otherTypeDescription");

    public final NumberPath<Integer> personCount = createNumber("personCount", Integer.class);

    public final DateTimePath<java.sql.Timestamp> pointOfTime = createDateTime("pointOfTime", java.sql.Timestamp.class);

    public final NumberPath<Long> rhyId = createNumber("rhyId", Long.class);

    public final NumberPath<Long> srvaEventId = createNumber("srvaEventId", Long.class);

    public final StringPath state = createString("state");

    public final NumberPath<Integer> timeSpent = createNumber("timeSpent", Integer.class);

    public final NumberPath<Integer> totalSpecimenAmount = createNumber("totalSpecimenAmount", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQSrvaEvent> srvaEventPkey = createPrimaryKey(srvaEventId);

    public final com.querydsl.sql.ForeignKey<SQPerson> srvaEventAuthorFk = createForeignKey(authorId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQSrvaEventName> srvaEventEventFk = createForeignKey(eventName, "name");

    public final com.querydsl.sql.ForeignKey<SQGameSpecies> srvaEventGameSpeciesFk = createForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQSrvaResultType> srvaEventEventResultFk = createForeignKey(eventResult, "name");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> srvaEventRhyFk = createForeignKey(rhyId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQSystemUser> srvaEventApproverAsUserFk = createForeignKey(approverAsUserId, "user_id");

    public final com.querydsl.sql.ForeignKey<SQSrvaEventState> srvaEventStateFk = createForeignKey(state, "name");

    public final com.querydsl.sql.ForeignKey<SQPerson> srvaEventApproverAsPersonFk = createForeignKey(approverAsPersonId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQGeolocationSource> srvaEventGeolocationSourceFk = createForeignKey(geolocationSource, "name");

    public final com.querydsl.sql.ForeignKey<SQSrvaEventType> srvaEventEventTypeFk = createForeignKey(eventType, "name");

    public final com.querydsl.sql.ForeignKey<SQGameDiaryImage> _gameDiaryImageSrvaEventFk = createInvForeignKey(srvaEventId, "srva_event_id");

    public final com.querydsl.sql.ForeignKey<SQSrvaMethod> _srvaMethodSrvaEventFk = createInvForeignKey(srvaEventId, "srva_event_id");

    public final com.querydsl.sql.ForeignKey<SQSrvaSpecimen> _srvaSpecimenSrvaEventFk = createInvForeignKey(srvaEventId, "srva_event_id");

    public SQSrvaEvent(String variable) {
        super(SQSrvaEvent.class, forVariable(variable), "public", "srva_event");
        addMetadata();
    }

    public SQSrvaEvent(String variable, String schema, String table) {
        super(SQSrvaEvent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQSrvaEvent(String variable, String schema) {
        super(SQSrvaEvent.class, forVariable(variable), schema, "srva_event");
        addMetadata();
    }

    public SQSrvaEvent(Path<? extends SQSrvaEvent> path) {
        super(path.getType(), path.getMetadata(), "public", "srva_event");
        addMetadata();
    }

    public SQSrvaEvent(PathMetadata metadata) {
        super(SQSrvaEvent.class, metadata, "public", "srva_event");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(accuracy, ColumnMetadata.named("accuracy").withIndex(17).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(altitude, ColumnMetadata.named("altitude").withIndex(18).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(altitudeAccuracy, ColumnMetadata.named("altitude_accuracy").withIndex(19).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(approverAsPersonId, ColumnMetadata.named("approver_as_person_id").withIndex(33).ofType(Types.BIGINT).withSize(19));
        addMetadata(approverAsUserId, ColumnMetadata.named("approver_as_user_id").withIndex(32).ofType(Types.BIGINT).withSize(19));
        addMetadata(authorId, ColumnMetadata.named("author_id").withIndex(27).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(description, ColumnMetadata.named("description").withIndex(26).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(eventName, ColumnMetadata.named("event_name").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(eventResult, ColumnMetadata.named("event_result").withIndex(25).ofType(Types.VARCHAR).withSize(255));
        addMetadata(eventType, ColumnMetadata.named("event_type").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(fromMobile, ColumnMetadata.named("from_mobile").withIndex(29).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(gameSpeciesId, ColumnMetadata.named("game_species_id").withIndex(13).ofType(Types.BIGINT).withSize(19));
        addMetadata(geolocationSource, ColumnMetadata.named("geolocation_source").withIndex(20).ofType(Types.VARCHAR).withSize(255));
        addMetadata(geom, ColumnMetadata.named("geom").withIndex(34).ofType(Types.OTHER).withSize(2147483647).notNull());
        addMetadata(latitude, ColumnMetadata.named("latitude").withIndex(14).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(longitude, ColumnMetadata.named("longitude").withIndex(15).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(mobileClientRefId, ColumnMetadata.named("mobile_client_ref_id").withIndex(30).ofType(Types.BIGINT).withSize(19));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(otherMethodDescription, ColumnMetadata.named("other_method_description").withIndex(21).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(otherSpeciesDescription, ColumnMetadata.named("other_species_description").withIndex(31).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(otherTypeDescription, ColumnMetadata.named("other_type_description").withIndex(22).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(personCount, ColumnMetadata.named("person_count").withIndex(23).ofType(Types.INTEGER).withSize(10));
        addMetadata(pointOfTime, ColumnMetadata.named("point_of_time").withIndex(12).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(rhyId, ColumnMetadata.named("rhy_id").withIndex(16).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(srvaEventId, ColumnMetadata.named("srva_event_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(state, ColumnMetadata.named("state").withIndex(28).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(timeSpent, ColumnMetadata.named("time_spent").withIndex(24).ofType(Types.INTEGER).withSize(10));
        addMetadata(totalSpecimenAmount, ColumnMetadata.named("total_specimen_amount").withIndex(11).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

