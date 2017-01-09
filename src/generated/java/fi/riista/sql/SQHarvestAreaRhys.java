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
 * SQHarvestAreaRhys is a Querydsl query type for SQHarvestAreaRhys
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestAreaRhys extends RelationalPathSpatial<SQHarvestAreaRhys> {

    private static final long serialVersionUID = -808030893;

    public static final SQHarvestAreaRhys harvestAreaRhys = new SQHarvestAreaRhys("harvest_area_rhys");

    public final NumberPath<Long> harvestAreaId = createNumber("harvestAreaId", Long.class);

    public final NumberPath<Long> organisationId = createNumber("organisationId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestAreaRhys> harvestAreaRhysPkey = createPrimaryKey(harvestAreaId, organisationId);

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestAreaRhysOrganisationFk = createForeignKey(organisationId, "organisation_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestArea> harvestAreaRhysHarvestAreaFk = createForeignKey(harvestAreaId, "harvest_area_id");

    public SQHarvestAreaRhys(String variable) {
        super(SQHarvestAreaRhys.class, forVariable(variable), "public", "harvest_area_rhys");
        addMetadata();
    }

    public SQHarvestAreaRhys(String variable, String schema, String table) {
        super(SQHarvestAreaRhys.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestAreaRhys(Path<? extends SQHarvestAreaRhys> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_area_rhys");
        addMetadata();
    }

    public SQHarvestAreaRhys(PathMetadata metadata) {
        super(SQHarvestAreaRhys.class, metadata, "public", "harvest_area_rhys");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(harvestAreaId, ColumnMetadata.named("harvest_area_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organisationId, ColumnMetadata.named("organisation_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

