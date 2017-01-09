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
 * SQHarvestPermitRhys is a Querydsl query type for SQHarvestPermitRhys
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitRhys extends RelationalPathSpatial<SQHarvestPermitRhys> {

    private static final long serialVersionUID = -439014303;

    public static final SQHarvestPermitRhys harvestPermitRhys = new SQHarvestPermitRhys("harvest_permit_rhys");

    public final NumberPath<Long> harvestPermitId = createNumber("harvestPermitId", Long.class);

    public final NumberPath<Long> organisationId = createNumber("organisationId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitRhys> harvestPermitRhysPkey = createPrimaryKey(harvestPermitId, organisationId);

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestPermitRhysOrganisationFk = createForeignKey(organisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> harvestPermitRhysHarvestPermitFk = createForeignKey(harvestPermitId, "harvest_permit_id");

    public SQHarvestPermitRhys(String variable) {
        super(SQHarvestPermitRhys.class, forVariable(variable), "public", "harvest_permit_rhys");
        addMetadata();
    }

    public SQHarvestPermitRhys(String variable, String schema, String table) {
        super(SQHarvestPermitRhys.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitRhys(Path<? extends SQHarvestPermitRhys> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_rhys");
        addMetadata();
    }

    public SQHarvestPermitRhys(PathMetadata metadata) {
        super(SQHarvestPermitRhys.class, metadata, "public", "harvest_permit_rhys");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(harvestPermitId, ColumnMetadata.named("harvest_permit_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organisationId, ColumnMetadata.named("organisation_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

