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
 * SQHarvestPermitAreaStatus is a Querydsl query type for SQHarvestPermitAreaStatus
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitAreaStatus extends RelationalPathSpatial<SQHarvestPermitAreaStatus> {

    private static final long serialVersionUID = 1605862064;

    public static final SQHarvestPermitAreaStatus harvestPermitAreaStatus = new SQHarvestPermitAreaStatus("harvest_permit_area_status");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitAreaStatus> harvestPermitAreaStatusPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitAreaEvent> _harvestPermitAreaEventStatusFk = createInvForeignKey(name, "status");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitArea> _harvestPermitAreaStatusFk = createInvForeignKey(name, "status");

    public SQHarvestPermitAreaStatus(String variable) {
        super(SQHarvestPermitAreaStatus.class, forVariable(variable), "public", "harvest_permit_area_status");
        addMetadata();
    }

    public SQHarvestPermitAreaStatus(String variable, String schema, String table) {
        super(SQHarvestPermitAreaStatus.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitAreaStatus(String variable, String schema) {
        super(SQHarvestPermitAreaStatus.class, forVariable(variable), schema, "harvest_permit_area_status");
        addMetadata();
    }

    public SQHarvestPermitAreaStatus(Path<? extends SQHarvestPermitAreaStatus> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_area_status");
        addMetadata();
    }

    public SQHarvestPermitAreaStatus(PathMetadata metadata) {
        super(SQHarvestPermitAreaStatus.class, metadata, "public", "harvest_permit_area_status");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

