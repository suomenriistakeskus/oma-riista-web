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
 * SQObservedGameState is a Querydsl query type for SQObservedGameState
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQObservedGameState extends RelationalPathSpatial<SQObservedGameState> {

    private static final long serialVersionUID = -1749960860;

    public static final SQObservedGameState observedGameState = new SQObservedGameState("observed_game_state");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQObservedGameState> observedGameStatePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQObservationSpecimen> _observationSpecimenStateFk = createInvForeignKey(name, "state");

    public SQObservedGameState(String variable) {
        super(SQObservedGameState.class, forVariable(variable), "public", "observed_game_state");
        addMetadata();
    }

    public SQObservedGameState(String variable, String schema, String table) {
        super(SQObservedGameState.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQObservedGameState(Path<? extends SQObservedGameState> path) {
        super(path.getType(), path.getMetadata(), "public", "observed_game_state");
        addMetadata();
    }

    public SQObservedGameState(PathMetadata metadata) {
        super(SQObservedGameState.class, metadata, "public", "observed_game_state");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

