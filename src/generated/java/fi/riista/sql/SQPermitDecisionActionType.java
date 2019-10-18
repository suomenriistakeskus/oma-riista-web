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
 * SQPermitDecisionActionType is a Querydsl query type for SQPermitDecisionActionType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionActionType extends RelationalPathSpatial<SQPermitDecisionActionType> {

    private static final long serialVersionUID = 294481114;

    public static final SQPermitDecisionActionType permitDecisionActionType = new SQPermitDecisionActionType("permit_decision_action_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionActionType> permitDecisionActionTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionAction> _permitDecisionActionTypeFk = createInvForeignKey(name, "action_type");

    public SQPermitDecisionActionType(String variable) {
        super(SQPermitDecisionActionType.class, forVariable(variable), "public", "permit_decision_action_type");
        addMetadata();
    }

    public SQPermitDecisionActionType(String variable, String schema, String table) {
        super(SQPermitDecisionActionType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionActionType(String variable, String schema) {
        super(SQPermitDecisionActionType.class, forVariable(variable), schema, "permit_decision_action_type");
        addMetadata();
    }

    public SQPermitDecisionActionType(Path<? extends SQPermitDecisionActionType> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_action_type");
        addMetadata();
    }

    public SQPermitDecisionActionType(PathMetadata metadata) {
        super(SQPermitDecisionActionType.class, metadata, "public", "permit_decision_action_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

