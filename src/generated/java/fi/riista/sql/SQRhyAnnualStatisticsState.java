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
 * SQRhyAnnualStatisticsState is a Querydsl query type for SQRhyAnnualStatisticsState
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQRhyAnnualStatisticsState extends RelationalPathSpatial<SQRhyAnnualStatisticsState> {

    private static final long serialVersionUID = 1043673567;

    public static final SQRhyAnnualStatisticsState rhyAnnualStatisticsState = new SQRhyAnnualStatisticsState("rhy_annual_statistics_state");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQRhyAnnualStatisticsState> rhyAnnualStatisticsStatePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQRhyAnnualStatistics> _rhyAnnualStatisticsStateFk = createInvForeignKey(name, "state");

    public final com.querydsl.sql.ForeignKey<SQRhyAnnualStatisticsStateChangeEvent> _rhyAnnualStatisticsStateChangeEventStateFk = createInvForeignKey(name, "state");

    public SQRhyAnnualStatisticsState(String variable) {
        super(SQRhyAnnualStatisticsState.class, forVariable(variable), "public", "rhy_annual_statistics_state");
        addMetadata();
    }

    public SQRhyAnnualStatisticsState(String variable, String schema, String table) {
        super(SQRhyAnnualStatisticsState.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQRhyAnnualStatisticsState(String variable, String schema) {
        super(SQRhyAnnualStatisticsState.class, forVariable(variable), schema, "rhy_annual_statistics_state");
        addMetadata();
    }

    public SQRhyAnnualStatisticsState(Path<? extends SQRhyAnnualStatisticsState> path) {
        super(path.getType(), path.getMetadata(), "public", "rhy_annual_statistics_state");
        addMetadata();
    }

    public SQRhyAnnualStatisticsState(PathMetadata metadata) {
        super(SQRhyAnnualStatisticsState.class, metadata, "public", "rhy_annual_statistics_state");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

