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
 * SQHarvestAreaType is a Querydsl query type for SQHarvestAreaType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestAreaType extends RelationalPathSpatial<SQHarvestAreaType> {

    private static final long serialVersionUID = -807955267;

    public static final SQHarvestAreaType harvestAreaType = new SQHarvestAreaType("harvest_area_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQHarvestAreaType> harvestAreaTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQHarvestArea> _harvestAreaTypeFk = createInvForeignKey(name, "type");

    public SQHarvestAreaType(String variable) {
        super(SQHarvestAreaType.class, forVariable(variable), "public", "harvest_area_type");
        addMetadata();
    }

    public SQHarvestAreaType(String variable, String schema, String table) {
        super(SQHarvestAreaType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestAreaType(Path<? extends SQHarvestAreaType> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_area_type");
        addMetadata();
    }

    public SQHarvestAreaType(PathMetadata metadata) {
        super(SQHarvestAreaType.class, metadata, "public", "harvest_area_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

