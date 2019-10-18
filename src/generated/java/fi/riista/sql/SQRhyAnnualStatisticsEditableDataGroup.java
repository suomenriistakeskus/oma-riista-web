package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQRhyAnnualStatisticsEditableDataGroup is a Querydsl query type for SQRhyAnnualStatisticsEditableDataGroup
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQRhyAnnualStatisticsEditableDataGroup extends RelationalPathSpatial<SQRhyAnnualStatisticsEditableDataGroup> {

    private static final long serialVersionUID = -2056627393;

    public static final SQRhyAnnualStatisticsEditableDataGroup rhyAnnualStatisticsEditableDataGroup = new SQRhyAnnualStatisticsEditableDataGroup("rhy_annual_statistics_editable_data_group");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQRhyAnnualStatisticsEditableDataGroup> rhyAnnualStatisticsEditableDataGroupPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQRhyAnnualStatisticsModeratorUpdateEvent> _rhyAnnualStatisticsModeratorUpdateEventDataGroupFk = createInvForeignKey(name, "data_group");

    public SQRhyAnnualStatisticsEditableDataGroup(String variable) {
        super(SQRhyAnnualStatisticsEditableDataGroup.class, forVariable(variable), "public", "rhy_annual_statistics_editable_data_group");
        addMetadata();
    }

    public SQRhyAnnualStatisticsEditableDataGroup(String variable, String schema, String table) {
        super(SQRhyAnnualStatisticsEditableDataGroup.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQRhyAnnualStatisticsEditableDataGroup(String variable, String schema) {
        super(SQRhyAnnualStatisticsEditableDataGroup.class, forVariable(variable), schema, "rhy_annual_statistics_editable_data_group");
        addMetadata();
    }

    public SQRhyAnnualStatisticsEditableDataGroup(Path<? extends SQRhyAnnualStatisticsEditableDataGroup> path) {
        super(path.getType(), path.getMetadata(), "public", "rhy_annual_statistics_editable_data_group");
        addMetadata();
    }

    public SQRhyAnnualStatisticsEditableDataGroup(PathMetadata metadata) {
        super(SQRhyAnnualStatisticsEditableDataGroup.class, metadata, "public", "rhy_annual_statistics_editable_data_group");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

