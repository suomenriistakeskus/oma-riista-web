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
 * SQHarvestReportState is a Querydsl query type for SQHarvestReportState
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestReportState extends RelationalPathSpatial<SQHarvestReportState> {

    private static final long serialVersionUID = -2114025305;

    public static final SQHarvestReportState harvestReportState = new SQHarvestReportState("harvest_report_state");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQHarvestReportState> harvestReportStatePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQHarvestReport> _harvestReportStateFk = createInvForeignKey(name, "state");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportStateHistory> _harvestReportStateHistoryStateFk = createInvForeignKey(name, "state");

    public SQHarvestReportState(String variable) {
        super(SQHarvestReportState.class, forVariable(variable), "public", "harvest_report_state");
        addMetadata();
    }

    public SQHarvestReportState(String variable, String schema, String table) {
        super(SQHarvestReportState.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestReportState(Path<? extends SQHarvestReportState> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_report_state");
        addMetadata();
    }

    public SQHarvestReportState(PathMetadata metadata) {
        super(SQHarvestReportState.class, metadata, "public", "harvest_report_state");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

