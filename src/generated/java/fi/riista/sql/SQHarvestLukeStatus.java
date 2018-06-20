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
 * SQHarvestLukeStatus is a Querydsl query type for SQHarvestLukeStatus
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestLukeStatus extends RelationalPathSpatial<SQHarvestLukeStatus> {

    private static final long serialVersionUID = -1256676277;

    public static final SQHarvestLukeStatus harvestLukeStatus = new SQHarvestLukeStatus("harvest_luke_status");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQHarvestLukeStatus> harvestLukeStatusPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQHarvest> _harvestLukeStatusFk = createInvForeignKey(name, "luke_status");

    public SQHarvestLukeStatus(String variable) {
        super(SQHarvestLukeStatus.class, forVariable(variable), "public", "harvest_luke_status");
        addMetadata();
    }

    public SQHarvestLukeStatus(String variable, String schema, String table) {
        super(SQHarvestLukeStatus.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestLukeStatus(String variable, String schema) {
        super(SQHarvestLukeStatus.class, forVariable(variable), schema, "harvest_luke_status");
        addMetadata();
    }

    public SQHarvestLukeStatus(Path<? extends SQHarvestLukeStatus> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_luke_status");
        addMetadata();
    }

    public SQHarvestLukeStatus(PathMetadata metadata) {
        super(SQHarvestLukeStatus.class, metadata, "public", "harvest_luke_status");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

