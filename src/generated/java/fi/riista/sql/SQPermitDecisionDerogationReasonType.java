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
 * SQPermitDecisionDerogationReasonType is a Querydsl query type for SQPermitDecisionDerogationReasonType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionDerogationReasonType extends RelationalPathSpatial<SQPermitDecisionDerogationReasonType> {

    private static final long serialVersionUID = -979688076;

    public static final SQPermitDecisionDerogationReasonType permitDecisionDerogationReasonType = new SQPermitDecisionDerogationReasonType("permit_decision_derogation_reason_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionDerogationReasonType> permitDecisionDerogationReasonTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionDerogationReason> _permitDecisionDerogationReasonTypeFk = createInvForeignKey(name, "reason_type");

    public SQPermitDecisionDerogationReasonType(String variable) {
        super(SQPermitDecisionDerogationReasonType.class, forVariable(variable), "public", "permit_decision_derogation_reason_type");
        addMetadata();
    }

    public SQPermitDecisionDerogationReasonType(String variable, String schema, String table) {
        super(SQPermitDecisionDerogationReasonType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionDerogationReasonType(String variable, String schema) {
        super(SQPermitDecisionDerogationReasonType.class, forVariable(variable), schema, "permit_decision_derogation_reason_type");
        addMetadata();
    }

    public SQPermitDecisionDerogationReasonType(Path<? extends SQPermitDecisionDerogationReasonType> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_derogation_reason_type");
        addMetadata();
    }

    public SQPermitDecisionDerogationReasonType(PathMetadata metadata) {
        super(SQPermitDecisionDerogationReasonType.class, metadata, "public", "permit_decision_derogation_reason_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

