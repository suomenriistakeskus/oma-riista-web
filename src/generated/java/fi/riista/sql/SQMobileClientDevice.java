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
 * SQMobileClientDevice is a Querydsl query type for SQMobileClientDevice
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMobileClientDevice extends RelationalPathSpatial<SQMobileClientDevice> {

    private static final long serialVersionUID = 70636502;

    public static final SQMobileClientDevice mobileClientDevice = new SQMobileClientDevice("mobile_client_device");

    public final StringPath clientVersion = createString("clientVersion");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath deviceName = createString("deviceName");

    public final NumberPath<Long> mobileClientDeviceId = createNumber("mobileClientDeviceId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> personId = createNumber("personId", Long.class);

    public final StringPath platform = createString("platform");

    public final StringPath pushToken = createString("pushToken");

    public final com.querydsl.sql.PrimaryKey<SQMobileClientDevice> mobileClientDevicePkey = createPrimaryKey(mobileClientDeviceId);

    public final com.querydsl.sql.ForeignKey<SQPerson> mobileClientDevicePersonIdFk = createForeignKey(personId, "person_id");

    public SQMobileClientDevice(String variable) {
        super(SQMobileClientDevice.class, forVariable(variable), "public", "mobile_client_device");
        addMetadata();
    }

    public SQMobileClientDevice(String variable, String schema, String table) {
        super(SQMobileClientDevice.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQMobileClientDevice(String variable, String schema) {
        super(SQMobileClientDevice.class, forVariable(variable), schema, "mobile_client_device");
        addMetadata();
    }

    public SQMobileClientDevice(Path<? extends SQMobileClientDevice> path) {
        super(path.getType(), path.getMetadata(), "public", "mobile_client_device");
        addMetadata();
    }

    public SQMobileClientDevice(PathMetadata metadata) {
        super(SQMobileClientDevice.class, metadata, "public", "mobile_client_device");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(clientVersion, ColumnMetadata.named("client_version").withIndex(12).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(deviceName, ColumnMetadata.named("device_name").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(mobileClientDeviceId, ColumnMetadata.named("mobile_client_device_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(personId, ColumnMetadata.named("person_id").withIndex(13).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(platform, ColumnMetadata.named("platform").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(pushToken, ColumnMetadata.named("push_token").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

