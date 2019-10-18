package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQPermitDecisionPartner is a Querydsl query type for SQPermitDecisionPartner
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitDecisionPartner extends RelationalPathSpatial<SQPermitDecisionPartner> {

    private static final long serialVersionUID = 86743742;

    public static final SQPermitDecisionPartner permitDecisionPartner = new SQPermitDecisionPartner("permit_decision_partner");

    public final NumberPath<Long> organisationId = createNumber("organisationId", Long.class);

    public final NumberPath<Long> permitDecisionId = createNumber("permitDecisionId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQPermitDecisionPartner> permitDecisionPartnerPkey = createPrimaryKey(permitDecisionId, organisationId);

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> permitDecisionPartnerDecisionFk = createForeignKey(permitDecisionId, "permit_decision_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> permitDecisionPartnerOrganisationFk = createForeignKey(organisationId, "organisation_id");

    public SQPermitDecisionPartner(String variable) {
        super(SQPermitDecisionPartner.class, forVariable(variable), "public", "permit_decision_partner");
        addMetadata();
    }

    public SQPermitDecisionPartner(String variable, String schema, String table) {
        super(SQPermitDecisionPartner.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitDecisionPartner(String variable, String schema) {
        super(SQPermitDecisionPartner.class, forVariable(variable), schema, "permit_decision_partner");
        addMetadata();
    }

    public SQPermitDecisionPartner(Path<? extends SQPermitDecisionPartner> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_decision_partner");
        addMetadata();
    }

    public SQPermitDecisionPartner(PathMetadata metadata) {
        super(SQPermitDecisionPartner.class, metadata, "public", "permit_decision_partner");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(organisationId, ColumnMetadata.named("organisation_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permitDecisionId, ColumnMetadata.named("permit_decision_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

