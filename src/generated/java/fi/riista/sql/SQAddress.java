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
 * SQAddress is a Querydsl query type for SQAddress
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQAddress extends RelationalPathSpatial<SQAddress> {

    private static final long serialVersionUID = -2072391231;

    public static final SQAddress address = new SQAddress("address");

    public final NumberPath<Long> addressId = createNumber("addressId", Long.class);

    public final StringPath city = createString("city");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final StringPath countryCode = createString("countryCode");

    public final StringPath countryName = createString("countryName");

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath postalCode = createString("postalCode");

    public final StringPath streetAddress = createString("streetAddress");

    public final com.querydsl.sql.PrimaryKey<SQAddress> addressPkey = createPrimaryKey(addressId);

    public final com.querydsl.sql.ForeignKey<SQOrganisation> _organisationAddressFk = createInvForeignKey(addressId, "address_id");

    public final com.querydsl.sql.ForeignKey<SQVenue> _venueAddressFk = createInvForeignKey(addressId, "address_id");

    public final com.querydsl.sql.ForeignKey<SQInvoice> _invoiceRecipientAddressFk = createInvForeignKey(addressId, "recipient_address_id");

    public final com.querydsl.sql.ForeignKey<SQPerson> _personMrAddressFk = createInvForeignKey(addressId, "mr_address_id");

    public final com.querydsl.sql.ForeignKey<SQPerson> _personOtherAddressFk = createInvForeignKey(addressId, "other_address_id");

    public SQAddress(String variable) {
        super(SQAddress.class, forVariable(variable), "public", "address");
        addMetadata();
    }

    public SQAddress(String variable, String schema, String table) {
        super(SQAddress.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQAddress(String variable, String schema) {
        super(SQAddress.class, forVariable(variable), schema, "address");
        addMetadata();
    }

    public SQAddress(Path<? extends SQAddress> path) {
        super(path.getType(), path.getMetadata(), "public", "address");
        addMetadata();
    }

    public SQAddress(PathMetadata metadata) {
        super(SQAddress.class, metadata, "public", "address");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(addressId, ColumnMetadata.named("address_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(city, ColumnMetadata.named("city").withIndex(11).ofType(Types.VARCHAR).withSize(255));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(countryCode, ColumnMetadata.named("country_code").withIndex(13).ofType(Types.CHAR).withSize(2));
        addMetadata(countryName, ColumnMetadata.named("country_name").withIndex(12).ofType(Types.VARCHAR).withSize(255));
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(postalCode, ColumnMetadata.named("postal_code").withIndex(10).ofType(Types.VARCHAR).withSize(255));
        addMetadata(streetAddress, ColumnMetadata.named("street_address").withIndex(9).ofType(Types.VARCHAR).withSize(255));
    }

}

