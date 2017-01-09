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
 * SQGameAntlersType is a Querydsl query type for SQGameAntlersType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQGameAntlersType extends RelationalPathSpatial<SQGameAntlersType> {

    private static final long serialVersionUID = 1867965494;

    public static final SQGameAntlersType gameAntlersType = new SQGameAntlersType("game_antlers_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQGameAntlersType> gameAntlersTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQHarvestSpecimen> _specimenAntlersTypeFk = createInvForeignKey(name, "antlers_type");

    public SQGameAntlersType(String variable) {
        super(SQGameAntlersType.class, forVariable(variable), "public", "game_antlers_type");
        addMetadata();
    }

    public SQGameAntlersType(String variable, String schema, String table) {
        super(SQGameAntlersType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQGameAntlersType(Path<? extends SQGameAntlersType> path) {
        super(path.getType(), path.getMetadata(), "public", "game_antlers_type");
        addMetadata();
    }

    public SQGameAntlersType(PathMetadata metadata) {
        super(SQGameAntlersType.class, metadata, "public", "game_antlers_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

