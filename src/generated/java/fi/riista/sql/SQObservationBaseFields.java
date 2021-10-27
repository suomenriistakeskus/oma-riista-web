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
 * SQObservationBaseFields is a Querydsl query type for SQObservationBaseFields
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQObservationBaseFields extends RelationalPathSpatial<SQObservationBaseFields> {

    private static final long serialVersionUID = -1488631485;

    public static final SQObservationBaseFields observationBaseFields = new SQObservationBaseFields("observation_base_fields");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> gameSpeciesId = createNumber("gameSpeciesId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> metadataVersion = createNumber("metadataVersion", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final StringPath withinDeerHunting = createString("withinDeerHunting");

    public final StringPath withinMooseHunting = createString("withinMooseHunting");

    public final com.querydsl.sql.PrimaryKey<SQObservationBaseFields> observationBaseFieldsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SQGameSpecies> observationBaseFieldsSpeciesFk = createForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationBaseFieldsWithinMooseHuntingFk = createForeignKey(withinMooseHunting, "name");

    public SQObservationBaseFields(String variable) {
        super(SQObservationBaseFields.class, forVariable(variable), "public", "observation_base_fields");
        addMetadata();
    }

    public SQObservationBaseFields(String variable, String schema, String table) {
        super(SQObservationBaseFields.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQObservationBaseFields(String variable, String schema) {
        super(SQObservationBaseFields.class, forVariable(variable), schema, "observation_base_fields");
        addMetadata();
    }

    public SQObservationBaseFields(Path<? extends SQObservationBaseFields> path) {
        super(path.getType(), path.getMetadata(), "public", "observation_base_fields");
        addMetadata();
    }

    public SQObservationBaseFields(PathMetadata metadata) {
        super(SQObservationBaseFields.class, metadata, "public", "observation_base_fields");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(5).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(gameSpeciesId, ColumnMetadata.named("game_species_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(metadataVersion, ColumnMetadata.named("metadata_version").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(withinDeerHunting, ColumnMetadata.named("within_deer_hunting").withIndex(8).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(withinMooseHunting, ColumnMetadata.named("within_moose_hunting").withIndex(7).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

