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
 * SQHarvestPermitApplicationStatus is a Querydsl query type for SQHarvestPermitApplicationStatus
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitApplicationStatus extends RelationalPathSpatial<SQHarvestPermitApplicationStatus> {

    private static final long serialVersionUID = 514815089;

    public static final SQHarvestPermitApplicationStatus harvestPermitApplicationStatus = new SQHarvestPermitApplicationStatus("harvest_permit_application_status");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitApplicationStatus> harvestPermitApplicationStatusPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> _harvestPermitApplicationStatusFk = createInvForeignKey(name, "status");

    public SQHarvestPermitApplicationStatus(String variable) {
        super(SQHarvestPermitApplicationStatus.class, forVariable(variable), "public", "harvest_permit_application_status");
        addMetadata();
    }

    public SQHarvestPermitApplicationStatus(String variable, String schema, String table) {
        super(SQHarvestPermitApplicationStatus.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitApplicationStatus(String variable, String schema) {
        super(SQHarvestPermitApplicationStatus.class, forVariable(variable), schema, "harvest_permit_application_status");
        addMetadata();
    }

    public SQHarvestPermitApplicationStatus(Path<? extends SQHarvestPermitApplicationStatus> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_application_status");
        addMetadata();
    }

    public SQHarvestPermitApplicationStatus(PathMetadata metadata) {
        super(SQHarvestPermitApplicationStatus.class, metadata, "public", "harvest_permit_application_status");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

