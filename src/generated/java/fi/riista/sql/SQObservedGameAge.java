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
 * SQObservedGameAge is a Querydsl query type for SQObservedGameAge
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQObservedGameAge extends RelationalPathSpatial<SQObservedGameAge> {

    private static final long serialVersionUID = -1713568622;

    public static final SQObservedGameAge observedGameAge = new SQObservedGameAge("observed_game_age");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQObservedGameAge> observedGameAgePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQObservationSpecimen> _observationSpecimenAgeFk = createInvForeignKey(name, "age");

    public SQObservedGameAge(String variable) {
        super(SQObservedGameAge.class, forVariable(variable), "public", "observed_game_age");
        addMetadata();
    }

    public SQObservedGameAge(String variable, String schema, String table) {
        super(SQObservedGameAge.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQObservedGameAge(Path<? extends SQObservedGameAge> path) {
        super(path.getType(), path.getMetadata(), "public", "observed_game_age");
        addMetadata();
    }

    public SQObservedGameAge(PathMetadata metadata) {
        super(SQObservedGameAge.class, metadata, "public", "observed_game_age");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

