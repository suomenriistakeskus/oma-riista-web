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
 * SQDeerHuntingType is a Querydsl query type for SQDeerHuntingType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQDeerHuntingType extends RelationalPathSpatial<SQDeerHuntingType> {

    private static final long serialVersionUID = 720844360;

    public static final SQDeerHuntingType deerHuntingType = new SQDeerHuntingType("deer_hunting_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQDeerHuntingType> deerHuntingTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQGameObservation> _gameObservationDeerHuntingTypeFk = createInvForeignKey(name, "deer_hunting_type");

    public final com.querydsl.sql.ForeignKey<SQHarvest> _harvestDeerHuntingTypeFk = createInvForeignKey(name, "deer_hunting_type");

    public SQDeerHuntingType(String variable) {
        super(SQDeerHuntingType.class, forVariable(variable), "public", "deer_hunting_type");
        addMetadata();
    }

    public SQDeerHuntingType(String variable, String schema, String table) {
        super(SQDeerHuntingType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQDeerHuntingType(String variable, String schema) {
        super(SQDeerHuntingType.class, forVariable(variable), schema, "deer_hunting_type");
        addMetadata();
    }

    public SQDeerHuntingType(Path<? extends SQDeerHuntingType> path) {
        super(path.getType(), path.getMetadata(), "public", "deer_hunting_type");
        addMetadata();
    }

    public SQDeerHuntingType(PathMetadata metadata) {
        super(SQDeerHuntingType.class, metadata, "public", "deer_hunting_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

