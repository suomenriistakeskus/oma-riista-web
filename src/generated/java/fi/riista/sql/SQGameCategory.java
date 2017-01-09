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
 * SQGameCategory is a Querydsl query type for SQGameCategory
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQGameCategory extends RelationalPathSpatial<SQGameCategory> {

    private static final long serialVersionUID = 1330363747;

    public static final SQGameCategory gameCategory = new SQGameCategory("game_category");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQGameCategory> gameCategoryPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQGameSpecies> _gameSpeciesCategoryFk = createInvForeignKey(name, "category");

    public SQGameCategory(String variable) {
        super(SQGameCategory.class, forVariable(variable), "public", "game_category");
        addMetadata();
    }

    public SQGameCategory(String variable, String schema, String table) {
        super(SQGameCategory.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQGameCategory(Path<? extends SQGameCategory> path) {
        super(path.getType(), path.getMetadata(), "public", "game_category");
        addMetadata();
    }

    public SQGameCategory(PathMetadata metadata) {
        super(SQGameCategory.class, metadata, "public", "game_category");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

