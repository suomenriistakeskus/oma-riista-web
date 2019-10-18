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
 * SQPermitDecisionType is a Querydsl query type for SQPermitDecisionType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionType extends RelationalPathSpatial<SQPermitDecisionType> {

    private static final long serialVersionUID = -1744599804;

    public static final SQPermitDecisionType permitDecisionType = new SQPermitDecisionType("permit_decision_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionType> permitDecisionTypePk = createPrimaryKey(name);

    public SQPermitDecisionType(String variable) {
        super(SQPermitDecisionType.class, forVariable(variable), "public", "permit_decision_type");
        addMetadata();
    }

    public SQPermitDecisionType(String variable, String schema, String table) {
        super(SQPermitDecisionType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionType(String variable, String schema) {
        super(SQPermitDecisionType.class, forVariable(variable), schema, "permit_decision_type");
        addMetadata();
    }

    public SQPermitDecisionType(Path<? extends SQPermitDecisionType> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_type");
        addMetadata();
    }

    public SQPermitDecisionType(PathMetadata metadata) {
        super(SQPermitDecisionType.class, metadata, "public", "permit_decision_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

