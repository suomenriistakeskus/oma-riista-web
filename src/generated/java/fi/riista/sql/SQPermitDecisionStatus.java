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
 * SQPermitDecisionStatus is a Querydsl query type for SQPermitDecisionStatus
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionStatus extends RelationalPathSpatial<SQPermitDecisionStatus> {

    private static final long serialVersionUID = -1556841668;

    public static final SQPermitDecisionStatus permitDecisionStatus = new SQPermitDecisionStatus("permit_decision_status");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionStatus> permitDecisionStatusPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> _permitDecisionStatusFk = createInvForeignKey(name, "status");

    public SQPermitDecisionStatus(String variable) {
        super(SQPermitDecisionStatus.class, forVariable(variable), "public", "permit_decision_status");
        addMetadata();
    }

    public SQPermitDecisionStatus(String variable, String schema, String table) {
        super(SQPermitDecisionStatus.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionStatus(String variable, String schema) {
        super(SQPermitDecisionStatus.class, forVariable(variable), schema, "permit_decision_status");
        addMetadata();
    }

    public SQPermitDecisionStatus(Path<? extends SQPermitDecisionStatus> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_status");
        addMetadata();
    }

    public SQPermitDecisionStatus(PathMetadata metadata) {
        super(SQPermitDecisionStatus.class, metadata, "public", "permit_decision_status");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

