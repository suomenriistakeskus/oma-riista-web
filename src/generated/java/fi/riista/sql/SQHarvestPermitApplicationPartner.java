package fi.riista.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import com.querydsl.sql.spatial.RelationalPathSpatial;

import com.querydsl.spatial.*;



/**
 * SQHarvestPermitApplicationPartner is a Querydsl query type for SQHarvestPermitApplicationPartner
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitApplicationPartner extends RelationalPathSpatial<SQHarvestPermitApplicationPartner> {

    private static final long serialVersionUID = -116406231;

    public static final SQHarvestPermitApplicationPartner harvestPermitApplicationPartner = new SQHarvestPermitApplicationPartner("harvest_permit_application_partner");

    public final NumberPath<Long> harvestPermitApplicationId = createNumber("harvestPermitApplicationId", Long.class);

    public final NumberPath<Long> organisationId = createNumber("organisationId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitApplicationPartner> harvestPermitApplicationPartnerPkey = createPrimaryKey(harvestPermitApplicationId, organisationId);

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestPermitApplicationPartnerOrganisationFk = createForeignKey(organisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> harvestPermitApplicationPartnerApplicationFk = createForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public SQHarvestPermitApplicationPartner(String variable) {
        super(SQHarvestPermitApplicationPartner.class, forVariable(variable), "public", "harvest_permit_application_partner");
        addMetadata();
    }

    public SQHarvestPermitApplicationPartner(String variable, String schema, String table) {
        super(SQHarvestPermitApplicationPartner.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitApplicationPartner(String variable, String schema) {
        super(SQHarvestPermitApplicationPartner.class, forVariable(variable), schema, "harvest_permit_application_partner");
        addMetadata();
    }

    public SQHarvestPermitApplicationPartner(Path<? extends SQHarvestPermitApplicationPartner> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_application_partner");
        addMetadata();
    }

    public SQHarvestPermitApplicationPartner(PathMetadata metadata) {
        super(SQHarvestPermitApplicationPartner.class, metadata, "public", "harvest_permit_application_partner");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(harvestPermitApplicationId, ColumnMetadata.named("harvest_permit_application_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organisationId, ColumnMetadata.named("organisation_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

