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
 * SQPermitDecisionRevisionReceiverType is a Querydsl query type for SQPermitDecisionRevisionReceiverType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionRevisionReceiverType extends RelationalPathSpatial<SQPermitDecisionRevisionReceiverType> {

    private static final long serialVersionUID = 709335822;

    public static final SQPermitDecisionRevisionReceiverType permitDecisionRevisionReceiverType = new SQPermitDecisionRevisionReceiverType("permit_decision_revision_receiver_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionRevisionReceiverType> permitDecisionRevisionReceiverTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionRevisionReceiver> _permitDecisionRevisionReceiverTypeFk = createInvForeignKey(name, "receiver_type");

    public SQPermitDecisionRevisionReceiverType(String variable) {
        super(SQPermitDecisionRevisionReceiverType.class, forVariable(variable), "public", "permit_decision_revision_receiver_type");
        addMetadata();
    }

    public SQPermitDecisionRevisionReceiverType(String variable, String schema, String table) {
        super(SQPermitDecisionRevisionReceiverType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionRevisionReceiverType(String variable, String schema) {
        super(SQPermitDecisionRevisionReceiverType.class, forVariable(variable), schema, "permit_decision_revision_receiver_type");
        addMetadata();
    }

    public SQPermitDecisionRevisionReceiverType(Path<? extends SQPermitDecisionRevisionReceiverType> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_revision_receiver_type");
        addMetadata();
    }

    public SQPermitDecisionRevisionReceiverType(PathMetadata metadata) {
        super(SQPermitDecisionRevisionReceiverType.class, metadata, "public", "permit_decision_revision_receiver_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

