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
 * SQGameFitnessClass is a Querydsl query type for SQGameFitnessClass
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQGameFitnessClass extends RelationalPathSpatial<SQGameFitnessClass> {

    private static final long serialVersionUID = 369085013;

    public static final SQGameFitnessClass gameFitnessClass = new SQGameFitnessClass("game_fitness_class");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQGameFitnessClass> gameFitnessClassPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQHarvestSpecimen> _specimenFitnessClassFk = createInvForeignKey(name, "fitness_class");

    public SQGameFitnessClass(String variable) {
        super(SQGameFitnessClass.class, forVariable(variable), "public", "game_fitness_class");
        addMetadata();
    }

    public SQGameFitnessClass(String variable, String schema, String table) {
        super(SQGameFitnessClass.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQGameFitnessClass(Path<? extends SQGameFitnessClass> path) {
        super(path.getType(), path.getMetadata(), "public", "game_fitness_class");
        addMetadata();
    }

    public SQGameFitnessClass(PathMetadata metadata) {
        super(SQGameFitnessClass.class, metadata, "public", "game_fitness_class");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

