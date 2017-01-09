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
 * SQHarvestPermitGroups is a Querydsl query type for SQHarvestPermitGroups
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitGroups extends RelationalPathSpatial<SQHarvestPermitGroups> {

    private static final long serialVersionUID = -1291928027;

    public static final SQHarvestPermitGroups harvestPermitGroups = new SQHarvestPermitGroups("harvest_permit_groups");

    public final NumberPath<Long> harvestPermitId = createNumber("harvestPermitId", Long.class);

    public final NumberPath<Long> organisationId = createNumber("organisationId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitGroups> harvestPermitGroupsPkey = createPrimaryKey(harvestPermitId, organisationId);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> harvestPermitGroupsHarvestPermitFk = createForeignKey(harvestPermitId, "harvest_permit_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> harvestPermitGroupsOrganisationFk = createForeignKey(organisationId, "organisation_id");

    public SQHarvestPermitGroups(String variable) {
        super(SQHarvestPermitGroups.class, forVariable(variable), "public", "harvest_permit_groups");
        addMetadata();
    }

    public SQHarvestPermitGroups(String variable, String schema, String table) {
        super(SQHarvestPermitGroups.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitGroups(Path<? extends SQHarvestPermitGroups> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_groups");
        addMetadata();
    }

    public SQHarvestPermitGroups(PathMetadata metadata) {
        super(SQHarvestPermitGroups.class, metadata, "public", "harvest_permit_groups");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(harvestPermitId, ColumnMetadata.named("harvest_permit_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organisationId, ColumnMetadata.named("organisation_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

