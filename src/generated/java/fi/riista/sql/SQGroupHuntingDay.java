package fi.riista.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import com.querydsl.sql.spatial.RelationalPathSpatial;


/**
 * SQGroupHuntingDay is a Querydsl query type for SQGroupHuntingDay
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQGroupHuntingDay extends RelationalPathSpatial<SQGroupHuntingDay> {

    private static final long serialVersionUID = 1211218521;

    public static final SQGroupHuntingDay groupHuntingDay = new SQGroupHuntingDay("group_hunting_day");

    public final NumberPath<Integer> breakDurationMinutes = createNumber("breakDurationMinutes", Integer.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final DatePath<java.sql.Date> endDate = createDate("endDate", java.sql.Date.class);

    public final TimePath<java.sql.Time> endTime = createTime("endTime", java.sql.Time.class);

    public final NumberPath<Long> groupHuntingDayId = createNumber("groupHuntingDayId", Long.class);

    public final NumberPath<Long> huntingGroupId = createNumber("huntingGroupId", Long.class);

    public final NumberPath<Integer> huntingMethod = createNumber("huntingMethod", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final NumberPath<Long> mooseDataCardImportId = createNumber("mooseDataCardImportId", Long.class);

    public final NumberPath<Integer> numberOfHounds = createNumber("numberOfHounds", Integer.class);

    public final NumberPath<Short> numberOfHunters = createNumber("numberOfHunters", Short.class);

    public final NumberPath<Short> snowDepth = createNumber("snowDepth", Short.class);

    public final DatePath<java.sql.Date> startDate = createDate("startDate", java.sql.Date.class);

    public final TimePath<java.sql.Time> startTime = createTime("startTime", java.sql.Time.class);

    public final com.querydsl.sql.PrimaryKey<SQGroupHuntingDay> groupHuntingDayPkey = createPrimaryKey(groupHuntingDayId);

    public final com.querydsl.sql.ForeignKey<SQMooseDataCardImport> groupHuntingDayMooseDataCardImportFk = createForeignKey(mooseDataCardImportId, "moose_data_card_import_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> groupHuntingDayGroupFk = createForeignKey(huntingGroupId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvest> _harvestGroupHuntingDayFk = createInvForeignKey(groupHuntingDayId, "group_hunting_day_id");

    public final com.querydsl.sql.ForeignKey<SQObservation> _gameObservationGroupHuntingDayFk = createInvForeignKey(groupHuntingDayId, "group_hunting_day_id");

    public SQGroupHuntingDay(String variable) {
        super(SQGroupHuntingDay.class, forVariable(variable), "public", "group_hunting_day");
        addMetadata();
    }

    public SQGroupHuntingDay(String variable, String schema, String table) {
        super(SQGroupHuntingDay.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQGroupHuntingDay(Path<? extends SQGroupHuntingDay> path) {
        super(path.getType(), path.getMetadata(), "public", "group_hunting_day");
        addMetadata();
    }

    public SQGroupHuntingDay(PathMetadata metadata) {
        super(SQGroupHuntingDay.class, metadata, "public", "group_hunting_day");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(breakDurationMinutes, ColumnMetadata.named("break_duration_minutes").withIndex(16).ofType(Types.INTEGER).withSize(10));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(endDate, ColumnMetadata.named("end_date").withIndex(13).ofType(Types.DATE).withSize(13).notNull());
        addMetadata(endTime, ColumnMetadata.named("end_time").withIndex(15).ofType(Types.TIME).withSize(15).withDigits(6).notNull());
        addMetadata(groupHuntingDayId, ColumnMetadata.named("group_hunting_day_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(huntingGroupId, ColumnMetadata.named("hunting_group_id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(huntingMethod, ColumnMetadata.named("hunting_method").withIndex(18).ofType(Types.INTEGER).withSize(10));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(mooseDataCardImportId, ColumnMetadata.named("moose_data_card_import_id").withIndex(19).ofType(Types.BIGINT).withSize(19));
        addMetadata(numberOfHounds, ColumnMetadata.named("number_of_hounds").withIndex(17).ofType(Types.INTEGER).withSize(10));
        addMetadata(numberOfHunters, ColumnMetadata.named("number_of_hunters").withIndex(12).ofType(Types.SMALLINT).withSize(5));
        addMetadata(snowDepth, ColumnMetadata.named("snow_depth").withIndex(11).ofType(Types.SMALLINT).withSize(5));
        addMetadata(startDate, ColumnMetadata.named("start_date").withIndex(10).ofType(Types.DATE).withSize(13).notNull());
        addMetadata(startTime, ColumnMetadata.named("start_time").withIndex(14).ofType(Types.TIME).withSize(15).withDigits(6).notNull());
    }

}

