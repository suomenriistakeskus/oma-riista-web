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
 * SQObservationType is a Querydsl query type for SQObservationType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQObservationType extends RelationalPathSpatial<SQObservationType> {

    private static final long serialVersionUID = -446890413;

    public static final SQObservationType observationType = new SQObservationType("observation_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQObservationType> observationTypePkey = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsObservationTypeFk = createInvForeignKey(name, "observation_type");

    public final com.querydsl.sql.ForeignKey<SQGameObservation> _gameObservationObservationTypeFk = createInvForeignKey(name, "observation_type");

    public SQObservationType(String variable) {
        super(SQObservationType.class, forVariable(variable), "public", "observation_type");
        addMetadata();
    }

    public SQObservationType(String variable, String schema, String table) {
        super(SQObservationType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQObservationType(String variable, String schema) {
        super(SQObservationType.class, forVariable(variable), schema, "observation_type");
        addMetadata();
    }

    public SQObservationType(Path<? extends SQObservationType> path) {
        super(path.getType(), path.getMetadata(), "public", "observation_type");
        addMetadata();
    }

    public SQObservationType(PathMetadata metadata) {
        super(SQObservationType.class, metadata, "public", "observation_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

