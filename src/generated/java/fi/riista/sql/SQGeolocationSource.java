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
 * SQGeolocationSource is a Querydsl query type for SQGeolocationSource
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQGeolocationSource extends RelationalPathSpatial<SQGeolocationSource> {

    private static final long serialVersionUID = -2027380914;

    public static final SQGeolocationSource geolocationSource = new SQGeolocationSource("geolocation_source");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQGeolocationSource> geolocationSourcePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQSrvaEvent> _srvaEventGeolocationSourceFk = createInvForeignKey(name, "geolocation_source");

    public final com.querydsl.sql.ForeignKey<SQHarvest> _harvestGeolocationSourceFk = createInvForeignKey(name, "geolocation_source");

    public SQGeolocationSource(String variable) {
        super(SQGeolocationSource.class, forVariable(variable), "public", "geolocation_source");
        addMetadata();
    }

    public SQGeolocationSource(String variable, String schema, String table) {
        super(SQGeolocationSource.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQGeolocationSource(Path<? extends SQGeolocationSource> path) {
        super(path.getType(), path.getMetadata(), "public", "geolocation_source");
        addMetadata();
    }

    public SQGeolocationSource(PathMetadata metadata) {
        super(SQGeolocationSource.class, metadata, "public", "geolocation_source");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

