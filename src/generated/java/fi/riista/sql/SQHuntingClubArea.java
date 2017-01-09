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
 * SQHuntingClubArea is a Querydsl query type for SQHuntingClubArea
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHuntingClubArea extends RelationalPathSpatial<SQHuntingClubArea> {

    private static final long serialVersionUID = 1612037055;

    public static final SQHuntingClubArea huntingClubArea = new SQHuntingClubArea("hunting_club_area");

    public final NumberPath<Long> clubId = createNumber("clubId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath externalId = createString("externalId");

    public final NumberPath<Long> huntingClubAreaId = createNumber("huntingClubAreaId", Long.class);

    public final NumberPath<Integer> huntingYear = createNumber("huntingYear", Integer.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final NumberPath<Integer> metsahallitusYear = createNumber("metsahallitusYear", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath nameFinnish = createString("nameFinnish");

    public final StringPath nameSwedish = createString("nameSwedish");

    public final NumberPath<Long> zoneId = createNumber("zoneId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHuntingClubArea> huntingClubAreaIdPk = createPrimaryKey(huntingClubAreaId);

    public final com.querydsl.sql.ForeignKey<SQOrganisation> huntingClubAreaClubFk = createForeignKey(clubId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQZone> huntingClubAreaZoneFk = createForeignKey(zoneId, "zone_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> _organisationHuntingClubAreaIdFk = createInvForeignKey(huntingClubAreaId, "hunting_area_id");

    public SQHuntingClubArea(String variable) {
        super(SQHuntingClubArea.class, forVariable(variable), "public", "hunting_club_area");
        addMetadata();
    }

    public SQHuntingClubArea(String variable, String schema, String table) {
        super(SQHuntingClubArea.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHuntingClubArea(Path<? extends SQHuntingClubArea> path) {
        super(path.getType(), path.getMetadata(), "public", "hunting_club_area");
        addMetadata();
    }

    public SQHuntingClubArea(PathMetadata metadata) {
        super(SQHuntingClubArea.class, metadata, "public", "hunting_club_area");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(clubId, ColumnMetadata.named("club_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(externalId, ColumnMetadata.named("external_id").withIndex(15).ofType(Types.VARCHAR).withSize(255));
        addMetadata(huntingClubAreaId, ColumnMetadata.named("hunting_club_area_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(huntingYear, ColumnMetadata.named("hunting_year").withIndex(10).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(isActive, ColumnMetadata.named("is_active").withIndex(14).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(metsahallitusYear, ColumnMetadata.named("metsahallitus_year").withIndex(16).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(nameFinnish, ColumnMetadata.named("name_finnish").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(nameSwedish, ColumnMetadata.named("name_swedish").withIndex(12).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(zoneId, ColumnMetadata.named("zone_id").withIndex(13).ofType(Types.BIGINT).withSize(19));
    }

}

