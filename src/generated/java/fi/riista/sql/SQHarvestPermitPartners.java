package fi.riista.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import java.util.*;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import com.querydsl.sql.spatial.RelationalPathSpatial;

import com.querydsl.spatial.*;



/**
 * SQHarvestPermitPartners is a Querydsl query type for SQHarvestPermitPartners
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitPartners extends RelationalPathSpatial<SQHarvestPermitPartners> {

    private static final long serialVersionUID = 385339580;

    public static final SQHarvestPermitPartners harvestPermitPartners = new SQHarvestPermitPartners("harvest_permit_partners");

    public final NumberPath<Long> harvestPermitId = createNumber("harvestPermitId", Long.class);

    public final NumberPath<Long> organisationId = createNumber("organisationId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitPartners> harvestPermitPartnersPkey = createPrimaryKey(harvestPermitId, organisationId);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> harvestPermitPartnersHarvestPermitFk = createForeignKey(harvestPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestPermitPartnersOrganisationFk = createForeignKey(organisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQMooseHuntingSummary> _mooseHuntingSummaryHarvestPermitPartnersFk = createInvForeignKey(Arrays.asList(harvestPermitId, organisationId), Arrays.asList("harvest_permit_id", "club_id"));

    public SQHarvestPermitPartners(String variable) {
        super(SQHarvestPermitPartners.class, forVariable(variable), "public", "harvest_permit_partners");
        addMetadata();
    }

    public SQHarvestPermitPartners(String variable, String schema, String table) {
        super(SQHarvestPermitPartners.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitPartners(String variable, String schema) {
        super(SQHarvestPermitPartners.class, forVariable(variable), schema, "harvest_permit_partners");
        addMetadata();
    }

    public SQHarvestPermitPartners(Path<? extends SQHarvestPermitPartners> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_partners");
        addMetadata();
    }

    public SQHarvestPermitPartners(PathMetadata metadata) {
        super(SQHarvestPermitPartners.class, metadata, "public", "harvest_permit_partners");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(harvestPermitId, ColumnMetadata.named("harvest_permit_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organisationId, ColumnMetadata.named("organisation_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

