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
 * SQDatabasechangeloglock is a Querydsl query type for SQDatabasechangeloglock
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQDatabasechangeloglock extends RelationalPathSpatial<SQDatabasechangeloglock> {

    private static final long serialVersionUID = -1646950863;

    public static final SQDatabasechangeloglock databasechangeloglock = new SQDatabasechangeloglock("databasechangeloglock");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath locked = createBoolean("locked");

    public final StringPath lockedby = createString("lockedby");

    public final DateTimePath<java.sql.Timestamp> lockgranted = createDateTime("lockgranted", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<SQDatabasechangeloglock> databasechangeloglockPk = createPrimaryKey(id);

    public SQDatabasechangeloglock(String variable) {
        super(SQDatabasechangeloglock.class, forVariable(variable), "public", "databasechangeloglock");
        addMetadata();
    }

    public SQDatabasechangeloglock(String variable, String schema, String table) {
        super(SQDatabasechangeloglock.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQDatabasechangeloglock(Path<? extends SQDatabasechangeloglock> path) {
        super(path.getType(), path.getMetadata(), "public", "databasechangeloglock");
        addMetadata();
    }

    public SQDatabasechangeloglock(PathMetadata metadata) {
        super(SQDatabasechangeloglock.class, metadata, "public", "databasechangeloglock");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(locked, ColumnMetadata.named("locked").withIndex(2).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(lockedby, ColumnMetadata.named("lockedby").withIndex(4).ofType(Types.VARCHAR).withSize(255));
        addMetadata(lockgranted, ColumnMetadata.named("lockgranted").withIndex(3).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
    }

}

