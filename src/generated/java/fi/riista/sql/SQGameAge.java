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
 * SQGameAge is a Querydsl query type for SQGameAge
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQGameAge extends RelationalPathSpatial<SQGameAge> {

    private static final long serialVersionUID = -1120334470;

    public static final SQGameAge gameAge = new SQGameAge("game_age");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQGameAge> gameAgePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQHarvestSpecimen> _harvestSpecimenAgeFk = createInvForeignKey(name, "age");

    public final com.querydsl.sql.ForeignKey<SQSrvaSpecimen> _srvaSpecimenAgeFk = createInvForeignKey(name, "age");

    public final com.querydsl.sql.ForeignKey<SQAmendmentApplicationData> _amendmentApplicationDataAgeFk = createInvForeignKey(name, "age");

    public SQGameAge(String variable) {
        super(SQGameAge.class, forVariable(variable), "public", "game_age");
        addMetadata();
    }

    public SQGameAge(String variable, String schema, String table) {
        super(SQGameAge.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQGameAge(String variable, String schema) {
        super(SQGameAge.class, forVariable(variable), schema, "game_age");
        addMetadata();
    }

    public SQGameAge(Path<? extends SQGameAge> path) {
        super(path.getType(), path.getMetadata(), "public", "game_age");
        addMetadata();
    }

    public SQGameAge(PathMetadata metadata) {
        super(SQGameAge.class, metadata, "public", "game_age");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

