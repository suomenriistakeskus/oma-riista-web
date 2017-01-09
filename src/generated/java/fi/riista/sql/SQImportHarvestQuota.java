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
 * SQImportHarvestQuota is a Querydsl query type for SQImportHarvestQuota
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQImportHarvestQuota extends RelationalPathSpatial<SQImportHarvestQuota> {

    private static final long serialVersionUID = -1420047129;

    public static final SQImportHarvestQuota importHarvestQuota = new SQImportHarvestQuota("import_harvest_quota");

    public final NumberPath<Integer> gameSpeciesOfficialCode = createNumber("gameSpeciesOfficialCode", Integer.class);

    public final StringPath harvestAreaCode = createString("harvestAreaCode");

    public final StringPath harvestAreaType = createString("harvestAreaType");

    public final NumberPath<Integer> huntingYear = createNumber("huntingYear", Integer.class);

    public final NumberPath<Integer> quota = createNumber("quota", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQImportHarvestQuota> importHarvestQuotaPkey = createPrimaryKey(huntingYear, gameSpeciesOfficialCode, harvestAreaType, harvestAreaCode);

    public SQImportHarvestQuota(String variable) {
        super(SQImportHarvestQuota.class, forVariable(variable), "public", "import_harvest_quota");
        addMetadata();
    }

    public SQImportHarvestQuota(String variable, String schema, String table) {
        super(SQImportHarvestQuota.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQImportHarvestQuota(Path<? extends SQImportHarvestQuota> path) {
        super(path.getType(), path.getMetadata(), "public", "import_harvest_quota");
        addMetadata();
    }

    public SQImportHarvestQuota(PathMetadata metadata) {
        super(SQImportHarvestQuota.class, metadata, "public", "import_harvest_quota");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(gameSpeciesOfficialCode, ColumnMetadata.named("game_species_official_code").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(harvestAreaCode, ColumnMetadata.named("harvest_area_code").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(harvestAreaType, ColumnMetadata.named("harvest_area_type").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(huntingYear, ColumnMetadata.named("hunting_year").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(quota, ColumnMetadata.named("quota").withIndex(5).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

