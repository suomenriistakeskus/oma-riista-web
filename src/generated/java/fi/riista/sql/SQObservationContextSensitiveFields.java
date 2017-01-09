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
 * SQObservationContextSensitiveFields is a Querydsl query type for SQObservationContextSensitiveFields
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQObservationContextSensitiveFields extends RelationalPathSpatial<SQObservationContextSensitiveFields> {

    private static final long serialVersionUID = -425084391;

    public static final SQObservationContextSensitiveFields observationContextSensitiveFields = new SQObservationContextSensitiveFields("observation_context_sensitive_fields");

    public final StringPath age = createString("age");

    public final StringPath amount = createString("amount");

    public final StringPath collarOrRadio = createString("collarOrRadio");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final StringPath dead = createString("dead");

    public final StringPath earmark = createString("earmark");

    public final BooleanPath extendedAgeRange = createBoolean("extendedAgeRange");

    public final NumberPath<Long> gameSpeciesId = createNumber("gameSpeciesId", Long.class);

    public final StringPath gender = createString("gender");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath legringOrWingmark = createString("legringOrWingmark");

    public final NumberPath<Integer> metadataVersion = createNumber("metadataVersion", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final StringPath mooselikeFemale1CalfAmount = createString("mooselikeFemale1CalfAmount");

    public final StringPath mooselikeFemale2CalfsAmount = createString("mooselikeFemale2CalfsAmount");

    public final StringPath mooselikeFemale3CalfsAmount = createString("mooselikeFemale3CalfsAmount");

    public final StringPath mooselikeFemale4CalfsAmount = createString("mooselikeFemale4CalfsAmount");

    public final StringPath mooselikeFemaleAmount = createString("mooselikeFemaleAmount");

    public final StringPath mooselikeMaleAmount = createString("mooselikeMaleAmount");

    public final StringPath mooselikeUnknownSpecimenAmount = createString("mooselikeUnknownSpecimenAmount");

    public final StringPath observationType = createString("observationType");

    public final StringPath onCarcass = createString("onCarcass");

    public final BooleanPath withinMooseHunting = createBoolean("withinMooseHunting");

    public final StringPath wounded = createString("wounded");

    public final com.querydsl.sql.PrimaryKey<SQObservationContextSensitiveFields> observationContextSensitiveFieldsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsLegringOrWingmarkFk = createForeignKey(legringOrWingmark, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsCollarOrRadioFk = createForeignKey(collarOrRadio, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsMooselikeMaleAmountFk = createForeignKey(mooselikeMaleAmount, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsWoundedFk = createForeignKey(wounded, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsMooselikeFemale4CalfFk = createForeignKey(mooselikeFemale4CalfsAmount, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsDeadFk = createForeignKey(dead, "name");

    public final com.querydsl.sql.ForeignKey<SQGameSpecies> observationContextSensitiveFieldsSpeciesFk = createForeignKey(gameSpeciesId, "game_species_id");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsOnCarcassFk = createForeignKey(onCarcass, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsGenderFk = createForeignKey(gender, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsAgeFk = createForeignKey(age, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsMooselikeFemale2CalfFk = createForeignKey(mooselikeFemale2CalfsAmount, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsMooselikeUnknownSpeciFk = createForeignKey(mooselikeUnknownSpecimenAmount, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsMooselikeFemale1CalfFk = createForeignKey(mooselikeFemale1CalfAmount, "name");

    public final com.querydsl.sql.ForeignKey<SQObservationType> observationContextSensitiveFieldsObservationTypeFk = createForeignKey(observationType, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsMooselikeFemaleAmountFk = createForeignKey(mooselikeFemaleAmount, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsAmountFk = createForeignKey(amount, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsEarmarkFk = createForeignKey(earmark, "name");

    public final com.querydsl.sql.ForeignKey<SQRequired> observationContextSensitiveFieldsMooselikeFemale3CalfFk = createForeignKey(mooselikeFemale3CalfsAmount, "name");

    public SQObservationContextSensitiveFields(String variable) {
        super(SQObservationContextSensitiveFields.class, forVariable(variable), "public", "observation_context_sensitive_fields");
        addMetadata();
    }

    public SQObservationContextSensitiveFields(String variable, String schema, String table) {
        super(SQObservationContextSensitiveFields.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQObservationContextSensitiveFields(Path<? extends SQObservationContextSensitiveFields> path) {
        super(path.getType(), path.getMetadata(), "public", "observation_context_sensitive_fields");
        addMetadata();
    }

    public SQObservationContextSensitiveFields(PathMetadata metadata) {
        super(SQObservationContextSensitiveFields.class, metadata, "public", "observation_context_sensitive_fields");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(age, ColumnMetadata.named("age").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(9).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(collarOrRadio, ColumnMetadata.named("collar_or_radio").withIndex(16).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(dead, ColumnMetadata.named("dead").withIndex(14).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(earmark, ColumnMetadata.named("earmark").withIndex(18).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(extendedAgeRange, ColumnMetadata.named("extended_age_range").withIndex(11).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(gameSpeciesId, ColumnMetadata.named("game_species_id").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(gender, ColumnMetadata.named("gender").withIndex(12).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(legringOrWingmark, ColumnMetadata.named("legring_or_wingmark").withIndex(17).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(metadataVersion, ColumnMetadata.named("metadata_version").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(mooselikeFemale1CalfAmount, ColumnMetadata.named("mooselike_female_1_calf_amount").withIndex(21).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(mooselikeFemale2CalfsAmount, ColumnMetadata.named("mooselike_female_2_calfs_amount").withIndex(22).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(mooselikeFemale3CalfsAmount, ColumnMetadata.named("mooselike_female_3_calfs_amount").withIndex(23).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(mooselikeFemale4CalfsAmount, ColumnMetadata.named("mooselike_female_4_calfs_amount").withIndex(24).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(mooselikeFemaleAmount, ColumnMetadata.named("mooselike_female_amount").withIndex(20).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(mooselikeMaleAmount, ColumnMetadata.named("mooselike_male_amount").withIndex(19).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(mooselikeUnknownSpecimenAmount, ColumnMetadata.named("mooselike_unknown_specimen_amount").withIndex(25).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(observationType, ColumnMetadata.named("observation_type").withIndex(6).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(onCarcass, ColumnMetadata.named("on_carcass").withIndex(15).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(withinMooseHunting, ColumnMetadata.named("within_moose_hunting").withIndex(5).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(wounded, ColumnMetadata.named("wounded").withIndex(13).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

