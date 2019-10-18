package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQHarvestPermitCategory is a Querydsl query type for SQHarvestPermitCategory
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitCategory extends RelationalPathSpatial<SQHarvestPermitCategory> {

    private static final long serialVersionUID = -753151729;

    public static final SQHarvestPermitCategory harvestPermitCategory = new SQHarvestPermitCategory("harvest_permit_category");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitCategory> harvestPermitCategoryPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> _harvestPermitApplicationCategoryFk = createInvForeignKey(name, "harvest_permit_category");

    public SQHarvestPermitCategory(String variable) {
        super(SQHarvestPermitCategory.class, forVariable(variable), "public", "harvest_permit_category");
        addMetadata();
    }

    public SQHarvestPermitCategory(String variable, String schema, String table) {
        super(SQHarvestPermitCategory.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitCategory(String variable, String schema) {
        super(SQHarvestPermitCategory.class, forVariable(variable), schema, "harvest_permit_category");
        addMetadata();
    }

    public SQHarvestPermitCategory(Path<? extends SQHarvestPermitCategory> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_category");
        addMetadata();
    }

    public SQHarvestPermitCategory(PathMetadata metadata) {
        super(SQHarvestPermitCategory.class, metadata, "public", "harvest_permit_category");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

