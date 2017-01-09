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
 * SQRequired is a Querydsl query type for SQRequired
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQRequired extends RelationalPathSpatial<SQRequired> {

    private static final long serialVersionUID = 1005956978;

    public static final SQRequired required = new SQRequired("required");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQRequired> requiredPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsFitnessClassFk = createInvForeignKey(name, "fitness_class");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsHuntingAreaTypeFk = createInvForeignKey(name, "hunting_area_type");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsLegringOrWingmarkFk = createInvForeignKey(name, "legring_or_wingmark");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsAdditionalInfoFk = createInvForeignKey(name, "additional_info");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsMooselikeMaleAmountFk = createInvForeignKey(name, "mooselike_male_amount");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsWoundedFk = createInvForeignKey(name, "wounded");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsPermitNumberFk = createInvForeignKey(name, "permit_number");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsHuntingAreaSizeFk = createInvForeignKey(name, "hunting_area_size");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsDeadFk = createInvForeignKey(name, "dead");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsReportedWithPhoneCallFk = createInvForeignKey(name, "reported_with_phone_call");

    public final com.querydsl.sql.ForeignKey<SQObservationBaseFields> _observationBaseFieldsWithinMooseHuntingFk = createInvForeignKey(name, "within_moose_hunting");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsAntlerPointsRightFk = createInvForeignKey(name, "antler_points_right");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsAntlersTypeFk = createInvForeignKey(name, "antlers_type");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsGenderFk = createInvForeignKey(name, "gender");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsHuntingMethodFk = createInvForeignKey(name, "hunting_method");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsMooselikeFemaleAmountFk = createInvForeignKey(name, "mooselike_female_amount");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsAmountFk = createInvForeignKey(name, "amount");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsGenderFk = createInvForeignKey(name, "gender");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsWeightFk = createInvForeignKey(name, "weight");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsEarmarkFk = createInvForeignKey(name, "earmark");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsMooselikeFemale3CalfFk = createInvForeignKey(name, "mooselike_female_3_calfs_amount");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsWeightEstimatedFk = createInvForeignKey(name, "weight_estimated");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsCollarOrRadioFk = createInvForeignKey(name, "collar_or_radio");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsHuntingPartyFk = createInvForeignKey(name, "hunting_party");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsMooselikeFemale4CalfFk = createInvForeignKey(name, "mooselike_female_4_calfs_amount");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsOnCarcassFk = createInvForeignKey(name, "on_carcass");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsAntlerPointsLeftFk = createInvForeignKey(name, "antler_points_left");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsAgeFk = createInvForeignKey(name, "age");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsAgeFk = createInvForeignKey(name, "age");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsMooselikeFemale2CalfFk = createInvForeignKey(name, "mooselike_female_2_calfs_amount");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsMooselikeUnknownSpeciFk = createInvForeignKey(name, "mooselike_unknown_specimen_amount");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsMooselikeFemale1CalfFk = createInvForeignKey(name, "mooselike_female_1_calf_amount");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsAntlersWidthFk = createInvForeignKey(name, "antlers_width");

    public final com.querydsl.sql.ForeignKey<SQHarvestReportFields> _harvestReportFieldsWeightMeasuredFk = createInvForeignKey(name, "weight_estimated");

    public SQRequired(String variable) {
        super(SQRequired.class, forVariable(variable), "public", "required");
        addMetadata();
    }

    public SQRequired(String variable, String schema, String table) {
        super(SQRequired.class, forVariable(variable), schema, table);
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

