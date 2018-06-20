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
 * SQRequired is a Querydsl query type for SQRequired
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQRequired extends RelationalPathSpatial<SQRequired> {

    private static final long serialVersionUID = 1005956978;

    public static final SQRequired required = new SQRequired("required");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQRequired> requiredPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsLegringOrWingmarkFk = createInvForeignKey(name, "legring_or_wingmark");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsMooselikeMaleAmountFk = createInvForeignKey(name, "mooselike_male_amount");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsWoundedFk = createInvForeignKey(name, "wounded");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsDeadFk = createInvForeignKey(name, "dead");

    public final com.querydsl.sql.ForeignKey<SQObservationBaseFields> _observationBaseFieldsWithinMooseHuntingFk = createInvForeignKey(name, "within_moose_hunting");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsGenderFk = createInvForeignKey(name, "gender");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsMooselikeFemaleAmountFk = createInvForeignKey(name, "mooselike_female_amount");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsEarmarkFk = createInvForeignKey(name, "earmark");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsMooselikeFemale3CalfFk = createInvForeignKey(name, "mooselike_female_3_calfs_amount");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsCollarOrRadioFk = createInvForeignKey(name, "collar_or_radio");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsMooselikeFemale4CalfFk = createInvForeignKey(name, "mooselike_female_4_calfs_amount");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationCtxFieldsMooselikeCalfAmountFk = createInvForeignKey(name, "mooselike_calf_amount");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsOnCarcassFk = createInvForeignKey(name, "on_carcass");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsAgeFk = createInvForeignKey(name, "age");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsMooselikeFemale2CalfFk = createInvForeignKey(name, "mooselike_female_2_calfs_amount");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsMooselikeUnknownSpeciFk = createInvForeignKey(name, "mooselike_unknown_specimen_amount");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsMooselikeFemale1CalfFk = createInvForeignKey(name, "mooselike_female_1_calf_amount");

    public SQRequired(String variable) {
        super(SQRequired.class, forVariable(variable), "public", "required");
        addMetadata();
    }

    public SQRequired(String variable, String schema, String table) {
        super(SQRequired.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQRequired(String variable, String schema) {
        super(SQRequired.class, forVariable(variable), schema, "required");
        addMetadata();
    }

    public SQRequired(Path<? extends SQRequired> path) {
        super(path.getType(), path.getMetadata(), "public", "required");
        addMetadata();
    }

    public SQRequired(PathMetadata metadata) {
        super(SQRequired.class, metadata, "public", "required");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

