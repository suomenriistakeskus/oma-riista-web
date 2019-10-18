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
 * SQMhPermit is a Querydsl query type for SQMhPermit
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMhPermit extends RelationalPathSpatial<SQMhPermit> {

    private static final long serialVersionUID = -1709891223;

    public static final SQMhPermit mhPermit = new SQMhPermit("mh_permit");

    public final StringPath areaName = createString("areaName");

    public final StringPath areaNameEnglish = createString("areaNameEnglish");

    public final StringPath areaNameSwedish = createString("areaNameSwedish");

    public final StringPath areaNumber = createString("areaNumber");

    public final DatePath<java.sql.Date> beginDate = createDate("beginDate", java.sql.Date.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DatePath<java.sql.Date> endDate = createDate("endDate", java.sql.Date.class);

    public final StringPath hunterNumber = createString("hunterNumber");

    public final NumberPath<Long> mhPermitId = createNumber("mhPermitId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final StringPath permitIdentifier = createString("permitIdentifier");

    public final StringPath permitName = createString("permitName");

    public final StringPath permitNameEnglish = createString("permitNameEnglish");

    public final StringPath permitNameSwedish = createString("permitNameSwedish");

    public final StringPath permitType = createString("permitType");

    public final StringPath permitTypeEnglish = createString("permitTypeEnglish");

    public final StringPath permitTypeSwedish = createString("permitTypeSwedish");

    public final StringPath ssn = createString("ssn");

    public final StringPath status = createString("status");

    public final StringPath url = createString("url");

    public final com.querydsl.sql.PrimaryKey<SQMhPermit> mhPermitPkey = createPrimaryKey(mhPermitId);

    public SQMhPermit(String variable) {
        super(SQMhPermit.class, forVariable(variable), "public", "mh_permit");
        addMetadata();
    }

    public SQMhPermit(String variable, String schema, String table) {
        super(SQMhPermit.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQMhPermit(String variable, String schema) {
        super(SQMhPermit.class, forVariable(variable), schema, "mh_permit");
        addMetadata();
    }

    public SQMhPermit(Path<? extends SQMhPermit> path) {
        super(path.getType(), path.getMetadata(), "public", "mh_permit");
        addMetadata();
    }

    public SQMhPermit(PathMetadata metadata) {
        super(SQMhPermit.class, metadata, "public", "mh_permit");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(areaName, ColumnMetadata.named("area_name").withIndex(17).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(areaNameEnglish, ColumnMetadata.named("area_name_english").withIndex(19).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(areaNameSwedish, ColumnMetadata.named("area_name_swedish").withIndex(18).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(areaNumber, ColumnMetadata.named("area_number").withIndex(16).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(beginDate, ColumnMetadata.named("begin_date").withIndex(20).ofType(Types.DATE).withSize(13));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(endDate, ColumnMetadata.named("end_date").withIndex(21).ofType(Types.DATE).withSize(13));
        addMetadata(hunterNumber, ColumnMetadata.named("hunter_number").withIndex(25).ofType(Types.CHAR).withSize(8));
        addMetadata(mhPermitId, ColumnMetadata.named("mh_permit_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(permitIdentifier, ColumnMetadata.named("permit_identifier").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(permitName, ColumnMetadata.named("permit_name").withIndex(13).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(permitNameEnglish, ColumnMetadata.named("permit_name_english").withIndex(15).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(permitNameSwedish, ColumnMetadata.named("permit_name_swedish").withIndex(14).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(permitType, ColumnMetadata.named("permit_type").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(permitTypeEnglish, ColumnMetadata.named("permit_type_english").withIndex(12).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(permitTypeSwedish, ColumnMetadata.named("permit_type_swedish").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(ssn, ColumnMetadata.named("ssn").withIndex(24).ofType(Types.CHAR).withSize(11));
        addMetadata(status, ColumnMetadata.named("status").withIndex(23).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(url, ColumnMetadata.named("url").withIndex(22).ofType(Types.VARCHAR).withSize(255));
    }

}

