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
 * SQHarvestPermitApplicationSpecies is a Querydsl query type for SQHarvestPermitApplicationSpecies
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitApplicationSpecies extends RelationalPathSpatial<SQHarvestPermitApplicationSpecies> {

    private static final long serialVersionUID = -1331942243;

    public static final SQHarvestPermitApplicationSpecies harvestPermitApplicationSpecies = new SQHarvestPermitApplicationSpecies("harvest_permit_application_species");

    public final NumberPath<Long> gameSpeciesId = createNumber("gameSpeciesId", Long.class);

    public final NumberPath<Long> harvestPermitApplicationId = createNumber("harvestPermitApplicationId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitApplicationSpecies> harvestPermitApplicationSpeciesPkey = createPrimaryKey(harvestPermitApplicationId, gameSpeciesId);

    public final com.querydsl.sql.ForeignKey<SQGameSpecies> harvestPermitApplicationSpeciesSpeciesFk = createForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> harvestPermitApplicationSpeciesApplicationFk = createForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public SQHarvestPermitApplicationSpecies(String variable) {
        super(SQHarvestPermitApplicationSpecies.class, forVariable(variable), "public", "harvest_permit_application_species");
        addMetadata();
    }

    public SQHarvestPermitApplicationSpecies(String variable, String schema, String table) {
        super(SQHarvestPermitApplicationSpecies.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitApplicationSpecies(String variable, String schema) {
        super(SQHarvestPermitApplicationSpecies.class, forVariable(variable), schema, "harvest_permit_application_species");
        addMetadata();
    }

    public SQHarvestPermitApplicationSpecies(Path<? extends SQHarvestPermitApplicationSpecies> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_application_species");
        addMetadata();
    }

    public SQHarvestPermitApplicationSpecies(PathMetadata metadata) {
        super(SQHarvestPermitApplicationSpecies.class, metadata, "public", "harvest_permit_application_species");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(gameSpeciesId, ColumnMetadata.named("game_species_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitApplicationId, ColumnMetadata.named("harvest_permit_application_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

