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
 * SQDynamicObservationFieldPresence is a Querydsl query type for SQDynamicObservationFieldPresence
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQDynamicObservationFieldPresence extends RelationalPathSpatial<SQDynamicObservationFieldPresence> {

    private static final long serialVersionUID = 1179540853;

    public static final SQDynamicObservationFieldPresence dynamicObservationFieldPresence = new SQDynamicObservationFieldPresence("dynamic_observation_field_presence");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQDynamicObservationFieldPresence> dynamicObservationFieldPresencePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationCtxFieldsLengthOfPawFk = createInvForeignKey(name, "length_of_paw");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationCtxFieldsAmountFk = createInvForeignKey(name, "amount");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationCtxFieldsWidthOfPawFk = createInvForeignKey(name, "width_of_paw");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationCtxFieldsOfficialAdditionalInfoFk = createInvForeignKey(name, "official_additional_info");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationCtxFieldsObserverPhoneNumberFk = createInvForeignKey(name, "observer_phone_number");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationCtxFieldsVerifiedByCarnivoreAuthorityFk = createInvForeignKey(name, "verified_by_carnivore_authority");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationCtxFieldsObserverNameFk = createInvForeignKey(name, "observer_name");

    public SQDynamicObservationFieldPresence(String variable) {
        super(SQDynamicObservationFieldPresence.class, forVariable(variable), "public", "dynamic_observation_field_presence");
        addMetadata();
    }

    public SQDynamicObservationFieldPresence(String variable, String schema, String table) {
        super(SQDynamicObservationFieldPresence.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQDynamicObservationFieldPresence(String variable, String schema) {
        super(SQDynamicObservationFieldPresence.class, forVariable(variable), schema, "dynamic_observation_field_presence");
        addMetadata();
    }

    public SQDynamicObservationFieldPresence(Path<? extends SQDynamicObservationFieldPresence> path) {
        super(path.getType(), path.getMetadata(), "public", "dynamic_observation_field_presence");
        addMetadata();
    }

    public SQDynamicObservationFieldPresence(PathMetadata metadata) {
        super(SQDynamicObservationFieldPresence.class, metadata, "public", "dynamic_observation_field_presence");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

