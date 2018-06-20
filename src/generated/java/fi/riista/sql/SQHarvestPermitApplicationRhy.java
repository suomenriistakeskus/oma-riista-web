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
 * SQHarvestPermitApplicationRhy is a Querydsl query type for SQHarvestPermitApplicationRhy
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitApplicationRhy extends RelationalPathSpatial<SQHarvestPermitApplicationRhy> {

    private static final long serialVersionUID = -863273756;

    public static final SQHarvestPermitApplicationRhy harvestPermitApplicationRhy = new SQHarvestPermitApplicationRhy("harvest_permit_application_rhy");

    public final NumberPath<Long> harvestPermitApplicationId = createNumber("harvestPermitApplicationId", Long.class);

    public final NumberPath<Long> organisationId = createNumber("organisationId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitApplicationRhy> harvestPermitApplicationRhyPkey = createPrimaryKey(harvestPermitApplicationId, organisationId);

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestPermitApplicationRhyOrganisationFk = createForeignKey(organisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> harvestPermitApplicationRhyApplicationFk = createForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public SQHarvestPermitApplicationRhy(String variable) {
        super(SQHarvestPermitApplicationRhy.class, forVariable(variable), "public", "harvest_permit_application_rhy");
        addMetadata();
    }

    public SQHarvestPermitApplicationRhy(String variable, String schema, String table) {
        super(SQHarvestPermitApplicationRhy.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitApplicationRhy(String variable, String schema) {
        super(SQHarvestPermitApplicationRhy.class, forVariable(variable), schema, "harvest_permit_application_rhy");
        addMetadata();
    }

    public SQHarvestPermitApplicationRhy(Path<? extends SQHarvestPermitApplicationRhy> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_application_rhy");
        addMetadata();
    }

    public SQHarvestPermitApplicationRhy(PathMetadata metadata) {
        super(SQHarvestPermitApplicationRhy.class, metadata, "public", "harvest_permit_application_rhy");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(harvestPermitApplicationId, ColumnMetadata.named("harvest_permit_application_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organisationId, ColumnMetadata.named("organisation_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

