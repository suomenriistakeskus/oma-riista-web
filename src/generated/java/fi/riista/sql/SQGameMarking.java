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
 * SQGameMarking is a Querydsl query type for SQGameMarking
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQGameMarking extends RelationalPathSpatial<SQGameMarking> {

    private static final long serialVersionUID = -1474764560;

    public static final SQGameMarking gameMarking = new SQGameMarking("game_marking");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQGameMarking> gameMarkingPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQObservationSpecimen> _observationSpecimenMarkingFk = createInvForeignKey(name, "marking");

    public SQGameMarking(String variable) {
        super(SQGameMarking.class, forVariable(variable), "public", "game_marking");
        addMetadata();
    }

    public SQGameMarking(String variable, String schema, String table) {
        super(SQGameMarking.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQGameMarking(Path<? extends SQGameMarking> path) {
        super(path.getType(), path.getMetadata(), "public", "game_marking");
        addMetadata();
    }

    public SQGameMarking(PathMetadata metadata) {
        super(SQGameMarking.class, metadata, "public", "game_marking");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

