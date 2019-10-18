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
 * SQPermitDecisionAppealStatus is a Querydsl query type for SQPermitDecisionAppealStatus
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionAppealStatus extends RelationalPathSpatial<SQPermitDecisionAppealStatus> {

    private static final long serialVersionUID = 925658667;

    public static final SQPermitDecisionAppealStatus permitDecisionAppealStatus = new SQPermitDecisionAppealStatus("permit_decision_appeal_status");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionAppealStatus> permitDecisionAppealStatusPk = createPrimaryKey(name);

    public SQPermitDecisionAppealStatus(String variable) {
        super(SQPermitDecisionAppealStatus.class, forVariable(variable), "public", "permit_decision_appeal_status");
        addMetadata();
    }

    public SQPermitDecisionAppealStatus(String variable, String schema, String table) {
        super(SQPermitDecisionAppealStatus.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionAppealStatus(String variable, String schema) {
        super(SQPermitDecisionAppealStatus.class, forVariable(variable), schema, "permit_decision_appeal_status");
        addMetadata();
    }

    public SQPermitDecisionAppealStatus(Path<? extends SQPermitDecisionAppealStatus> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_appeal_status");
        addMetadata();
    }

    public SQPermitDecisionAppealStatus(PathMetadata metadata) {
        super(SQPermitDecisionAppealStatus.class, metadata, "public", "permit_decision_appeal_status");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

