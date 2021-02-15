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
 * SQDeerPilot is a Querydsl query type for SQDeerPilot
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQDeerPilot extends RelationalPathSpatial<SQDeerPilot> {

    private static final long serialVersionUID = -1514892041;

    public static final SQDeerPilot deerPilot = new SQDeerPilot("deer_pilot");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> deerPilotId = createNumber("deerPilotId", Long.class);

    public final NumberPath<Long> harvestPermitId = createNumber("harvestPermitId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQDeerPilot> deerPilotPkey = createPrimaryKey(deerPilotId);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> deerPilotHarvestPermitIdFk = createForeignKey(harvestPermitId, "harvest_permit_id");

    public SQDeerPilot(String variable) {
        super(SQDeerPilot.class, forVariable(variable), "public", "deer_pilot");
        addMetadata();
    }

    public SQDeerPilot(String variable, String schema, String table) {
        super(SQDeerPilot.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQDeerPilot(String variable, String schema) {
        super(SQDeerPilot.class, forVariable(variable), schema, "deer_pilot");
        addMetadata();
    }

    public SQDeerPilot(Path<? extends SQDeerPilot> path) {
        super(path.getType(), path.getMetadata(), "public", "deer_pilot");
        addMetadata();
    }

    public SQDeerPilot(PathMetadata metadata) {
        super(SQDeerPilot.class, metadata, "public", "deer_pilot");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(deerPilotId, ColumnMetadata.named("deer_pilot_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitId, ColumnMetadata.named("harvest_permit_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

