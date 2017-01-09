package fi.riista.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import com.querydsl.sql.spatial.RelationalPathSpatial;


/**
 * SQGameSpecies is a Querydsl query type for SQGameSpecies
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQGameSpecies extends RelationalPathSpatial<SQGameSpecies> {

    private static final long serialVersionUID = -27516873;

    public static final SQGameSpecies gameSpecies = new SQGameSpecies("game_species");

    public final StringPath category = createString("category");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final NumberPath<Long> gameSpeciesId = createNumber("gameSpeciesId", Long.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final BooleanPath multipleSpecimenAllowedOnHarvest = createBoolean("multipleSpecimenAllowedOnHarvest");

    public final StringPath nameEnglish = createString("nameEnglish");

    public final StringPath nameFinnish = createString("nameFinnish");

    public final StringPath nameSwedish = createString("nameSwedish");

    public final NumberPath<Integer> officialCode = createNumber("officialCode", Integer.class);

    public final StringPath scientificName = createString("scientificName");

    public final NumberPath<Integer> srvaOrdinal = createNumber("srvaOrdinal", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQGameSpecies> gameSpeciesPkey = createPrimaryKey(gameSpeciesId);

    public final com.querydsl.sql.ForeignKey<SQGameCategory> gameSpeciesCategoryFk = createForeignKey(category, "name");

    public final com.querydsl.sql.ForeignKey<SQHarvest> _harvestSpeciesFk = createInvForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitAllocation> _harvestPermitAllocationGameSpeciesFk = createInvForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> _organisationGameSpeciesFk = createInvForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQSrvaEvent> _srvaEventGameSpeciesFk = createInvForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitSpeciesAmount> _harvestPermitSpeciesAmountGameSpeciesFk = createInvForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQObservation> _gameObservationSpeciesFk = createInvForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQMooselikePrice> _mooselikePriceGameSpeciesFk = createInvForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQObservationBaseFields> _observationBaseFieldsSpeciesFk = createInvForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsSpeciesFk = createInvForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsGameSpeciesFk = createInvForeignKey(gameSpeciesId, "game_species_id");

    public SQGameSpecies(String variable) {
        super(SQGameSpecies.class, forVariable(variable), "public", "game_species");
        addMetadata();
    }

    public SQGameSpecies(String variable, String schema, String table) {
        super(SQGameSpecies.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQGameSpecies(Path<? extends SQGameSpecies> path) {
        super(path.getType(), path.getMetadata(), "public", "game_species");
        addMetadata();
    }

    public SQGameSpecies(PathMetadata metadata) {
        super(SQGameSpecies.class, metadata, "public", "game_species");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(category, ColumnMetadata.named("category").withIndex(13).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(gameSpeciesId, ColumnMetadata.named("game_species_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(multipleSpecimenAllowedOnHarvest, ColumnMetadata.named("multiple_specimen_allowed_on_harvest").withIndex(14).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(nameEnglish, ColumnMetadata.named("name_english").withIndex(15).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(nameFinnish, ColumnMetadata.named("name_finnish").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(nameSwedish, ColumnMetadata.named("name_swedish").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(officialCode, ColumnMetadata.named("official_code").withIndex(9).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(scientificName, ColumnMetadata.named("scientific_name").withIndex(12).ofType(Types.VARCHAR).withSize(100));
        addMetadata(srvaOrdinal, ColumnMetadata.named("srva_ordinal").withIndex(16).ofType(Types.INTEGER).withSize(10));
    }

}

