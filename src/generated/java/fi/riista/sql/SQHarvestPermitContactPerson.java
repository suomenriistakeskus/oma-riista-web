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
 * SQHarvestPermitContactPerson is a Querydsl query type for SQHarvestPermitContactPerson
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitContactPerson extends RelationalPathSpatial<SQHarvestPermitContactPerson> {

    private static final long serialVersionUID = -1269702108;

    public static final SQHarvestPermitContactPerson harvestPermitContactPerson = new SQHarvestPermitContactPerson("harvest_permit_contact_person");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> contactPersonId = createNumber("contactPersonId", Long.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> harvestPermitContactPersonId = createNumber("harvestPermitContactPersonId", Long.class);

    public final NumberPath<Long> harvestPermitId = createNumber("harvestPermitId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitContactPerson> harvestPermitContactPersonPkey = createPrimaryKey(harvestPermitContactPersonId);

    public final com.querydsl.sql.ForeignKey<SQPerson> harvestPermitContactPersonPersonFk = createForeignKey(contactPersonId, "person_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> harvestPermitContactPersonHarvestPermitFk = createForeignKey(harvestPermitId, "harvest_permit_id");

    public SQHarvestPermitContactPerson(String variable) {
        super(SQHarvestPermitContactPerson.class, forVariable(variable), "public", "harvest_permit_contact_person");
        addMetadata();
    }

    public SQHarvestPermitContactPerson(String variable, String schema, String table) {
        super(SQHarvestPermitContactPerson.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitContactPerson(Path<? extends SQHarvestPermitContactPerson> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_contact_person");
        addMetadata();
    }

    public SQHarvestPermitContactPerson(PathMetadata metadata) {
        super(SQHarvestPermitContactPerson.class, metadata, "public", "harvest_permit_contact_person");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(contactPersonId, ColumnMetadata.named("contact_person_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(harvestPermitContactPersonId, ColumnMetadata.named("harvest_permit_contact_person_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitId, ColumnMetadata.named("harvest_permit_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
    }

}

