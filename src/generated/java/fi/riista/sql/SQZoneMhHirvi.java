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
 * SQZoneMhHirvi is a Querydsl query type for SQZoneMhHirvi
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQZoneMhHirvi extends RelationalPathSpatial<SQZoneMhHirvi> {

    private static final long serialVersionUID = -1359212758;

    public static final SQZoneMhHirvi zoneMhHirvi = new SQZoneMhHirvi("zone_mh_hirvi");

    public final NumberPath<Long> mhHirviId = createNumber("mhHirviId", Long.class);

    public final NumberPath<Long> zoneId = createNumber("zoneId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQZoneMhHirvi> zoneMhHirviPkey = createPrimaryKey(zoneId, mhHirviId);

    public final com.querydsl.sql.ForeignKey<SQZone> zoneMhHirviZoneFk = createForeignKey(zoneId, "zone_id");

    public SQZoneMhHirvi(String variable) {
        super(SQZoneMhHirvi.class, forVariable(variable), "public", "zone_mh_hirvi");
        addMetadata();
    }

    public SQZoneMhHirvi(String variable, String schema, String table) {
        super(SQZoneMhHirvi.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQZoneMhHirvi(Path<? extends SQZoneMhHirvi> path) {
        super(path.getType(), path.getMetadata(), "public", "zone_mh_hirvi");
        addMetadata();
    }

    public SQZoneMhHirvi(PathMetadata metadata) {
        super(SQZoneMhHirvi.class, metadata, "public", "zone_mh_hirvi");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(mhHirviId, ColumnMetadata.named("mh_hirvi_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(zoneId, ColumnMetadata.named("zone_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

