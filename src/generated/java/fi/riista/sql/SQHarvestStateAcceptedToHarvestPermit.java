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
 * SQHarvestStateAcceptedToHarvestPermit is a Querydsl query type for SQHarvestStateAcceptedToHarvestPermit
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestStateAcceptedToHarvestPermit extends RelationalPathSpatial<SQHarvestStateAcceptedToHarvestPermit> {

    private static final long serialVersionUID = 1463896071;

    public static final SQHarvestStateAcceptedToHarvestPermit harvestStateAcceptedToHarvestPermit = new SQHarvestStateAcceptedToHarvestPermit("harvest_state_accepted_to_harvest_permit");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQHarvestStateAcceptedToHarvestPermit> harvestStateAcceptedToHarvestPermitPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQHarvest> _harvestHarvestStateAcceptedToHarvestPermitFk = createInvForeignKey(name, "state_accepted_to_harvest_permit");

    public SQHarvestStateAcceptedToHarvestPermit(String variable) {
        super(SQHarvestStateAcceptedToHarvestPermit.class, forVariable(variable), "public", "harvest_state_accepted_to_harvest_permit");
        addMetadata();
    }

    public SQHarvestStateAcceptedToHarvestPermit(String variable, String schema, String table) {
        super(SQHarvestStateAcceptedToHarvestPermit.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestStateAcceptedToHarvestPermit(String variable, String schema) {
        super(SQHarvestStateAcceptedToHarvestPermit.class, forVariable(variable), schema, "harvest_state_accepted_to_harvest_permit");
        addMetadata();
    }

    public SQHarvestStateAcceptedToHarvestPermit(Path<? extends SQHarvestStateAcceptedToHarvestPermit> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_state_accepted_to_harvest_permit");
        addMetadata();
    }

    public SQHarvestStateAcceptedToHarvestPermit(PathMetadata metadata) {
        super(SQHarvestStateAcceptedToHarvestPermit.class, metadata, "public", "harvest_state_accepted_to_harvest_permit");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

