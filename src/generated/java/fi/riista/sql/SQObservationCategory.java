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
 * SQObservationCategory is a Querydsl query type for SQObservationCategory
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQObservationCategory extends RelationalPathSpatial<SQObservationCategory> {

    private static final long serialVersionUID = 45741975;

    public static final SQObservationCategory observationCategory = new SQObservationCategory("observation_category");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQObservationCategory> observationCategoryPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQGameObservation> _gameObservationObservationCategoryFk = createInvForeignKey(name, "observation_category");

    public final com.querydsl.sql.ForeignKey<SQObservationContextSensitiveFields> _observationContextSensitiveFieldsObservationCategoryFk = createInvForeignKey(name, "observation_category");

    public SQObservationCategory(String variable) {
        super(SQObservationCategory.class, forVariable(variable), "public", "observation_category");
        addMetadata();
    }

    public SQObservationCategory(String variable, String schema, String table) {
        super(SQObservationCategory.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQObservationCategory(String variable, String schema) {
        super(SQObservationCategory.class, forVariable(variable), schema, "observation_category");
        addMetadata();
    }

    public SQObservationCategory(Path<? extends SQObservationCategory> path) {
        super(path.getType(), path.getMetadata(), "public", "observation_category");
        addMetadata();
    }

    public SQObservationCategory(PathMetadata metadata) {
        super(SQObservationCategory.class, metadata, "public", "observation_category");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

