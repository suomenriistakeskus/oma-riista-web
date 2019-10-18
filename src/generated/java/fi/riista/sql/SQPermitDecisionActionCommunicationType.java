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
 * SQPermitDecisionActionCommunicationType is a Querydsl query type for SQPermitDecisionActionCommunicationType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionActionCommunicationType extends RelationalPathSpatial<SQPermitDecisionActionCommunicationType> {

    private static final long serialVersionUID = -363774320;

    public static final SQPermitDecisionActionCommunicationType permitDecisionActionCommunicationType = new SQPermitDecisionActionCommunicationType("permit_decision_action_communication_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionActionCommunicationType> permitDecisionActionCommunicationTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionAction> _permitDecisionActionCommunicationTypeFk = createInvForeignKey(name, "communication_type");

    public SQPermitDecisionActionCommunicationType(String variable) {
        super(SQPermitDecisionActionCommunicationType.class, forVariable(variable), "public", "permit_decision_action_communication_type");
        addMetadata();
    }

    public SQPermitDecisionActionCommunicationType(String variable, String schema, String table) {
        super(SQPermitDecisionActionCommunicationType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionActionCommunicationType(String variable, String schema) {
        super(SQPermitDecisionActionCommunicationType.class, forVariable(variable), schema, "permit_decision_action_communication_type");
        addMetadata();
    }

    public SQPermitDecisionActionCommunicationType(Path<? extends SQPermitDecisionActionCommunicationType> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_action_communication_type");
        addMetadata();
    }

    public SQPermitDecisionActionCommunicationType(PathMetadata metadata) {
        super(SQPermitDecisionActionCommunicationType.class, metadata, "public", "permit_decision_action_communication_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

