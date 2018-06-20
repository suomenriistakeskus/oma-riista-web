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
 * SQGroupHarvestRejection is a Querydsl query type for SQGroupHarvestRejection
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQGroupHarvestRejection extends RelationalPathSpatial<SQGroupHarvestRejection> {

    private static final long serialVersionUID = 789941836;

    public static final SQGroupHarvestRejection groupHarvestRejection = new SQGroupHarvestRejection("group_harvest_rejection");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> groupHarvestRejectionId = createNumber("groupHarvestRejectionId", Long.class);

    public final NumberPath<Long> harvestId = createNumber("harvestId", Long.class);

    public final NumberPath<Long> huntingClubGroupId = createNumber("huntingClubGroupId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQGroupHarvestRejection> groupHarvestRejectionPkey = createPrimaryKey(groupHarvestRejectionId);

    public final com.querydsl.sql.ForeignKey<SQHarvest> groupHarvestRejectionHarvestFk = createForeignKey(harvestId, "harvest_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> groupHarvestRejectionHuntingClubGroupFk = createForeignKey(huntingClubGroupId, "organisation_id");

    public SQGroupHarvestRejection(String variable) {
        super(SQGroupHarvestRejection.class, forVariable(variable), "public", "group_harvest_rejection");
        addMetadata();
    }

    public SQGroupHarvestRejection(String variable, String schema, String table) {
        super(SQGroupHarvestRejection.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQGroupHarvestRejection(String variable, String schema) {
        super(SQGroupHarvestRejection.class, forVariable(variable), schema, "group_harvest_rejection");
        addMetadata();
    }

    public SQGroupHarvestRejection(Path<? extends SQGroupHarvestRejection> path) {
        super(path.getType(), path.getMetadata(), "public", "group_harvest_rejection");
        addMetadata();
    }

    public SQGroupHarvestRejection(PathMetadata metadata) {
        super(SQGroupHarvestRejection.class, metadata, "public", "group_harvest_rejection");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(groupHarvestRejectionId, ColumnMetadata.named("group_harvest_rejection_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestId, ColumnMetadata.named("harvest_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(huntingClubGroupId, ColumnMetadata.named("hunting_club_group_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
    }

}

