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
 * SQGameGender is a Querydsl query type for SQGameGender
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQGameGender extends RelationalPathSpatial<SQGameGender> {

    private static final long serialVersionUID = 476956774;

    public static final SQGameGender gameGender = new SQGameGender("game_gender");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQGameGender> gameGenderPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQObservationSpecimen> _observationSpecimenGenderFk = createInvForeignKey(name, "gender");

    public final com.querydsl.sql.ForeignKey<SQSrvaSpecimen> _srvaSpecimenGenderFk = createInvForeignKey(name, "gender");

    public final com.querydsl.sql.ForeignKey<SQHarvestSpecimen> _harvestSpecimenGenderFk = createInvForeignKey(name, "gender");

    public SQGameGender(String variable) {
        super(SQGameGender.class, forVariable(variable), "public", "game_gender");
        addMetadata();
    }

    public SQGameGender(String variable, String schema, String table) {
        super(SQGameGender.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQGameGender(String variable, String schema) {
        super(SQGameGender.class, forVariable(variable), schema, "game_gender");
        addMetadata();
    }

    public SQGameGender(Path<? extends SQGameGender> path) {
        super(path.getType(), path.getMetadata(), "public", "game_gender");
        addMetadata();
    }

    public SQGameGender(PathMetadata metadata) {
        super(SQGameGender.class, metadata, "public", "game_gender");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

