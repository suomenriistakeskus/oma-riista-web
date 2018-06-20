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
 * SQShootingTestType is a Querydsl query type for SQShootingTestType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQShootingTestType extends RelationalPathSpatial<SQShootingTestType> {

    private static final long serialVersionUID = 86027330;

    public static final SQShootingTestType shootingTestType = new SQShootingTestType("shooting_test_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQShootingTestType> shootingTestTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQShootingTestAttempt> _shootingTestAttemptTypeFk = createInvForeignKey(name, "type");

    public SQShootingTestType(String variable) {
        super(SQShootingTestType.class, forVariable(variable), "public", "shooting_test_type");
        addMetadata();
    }

    public SQShootingTestType(String variable, String schema, String table) {
        super(SQShootingTestType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQShootingTestType(String variable, String schema) {
        super(SQShootingTestType.class, forVariable(variable), schema, "shooting_test_type");
        addMetadata();
    }

    public SQShootingTestType(Path<? extends SQShootingTestType> path) {
        super(path.getType(), path.getMetadata(), "public", "shooting_test_type");
        addMetadata();
    }

    public SQShootingTestType(PathMetadata metadata) {
        super(SQShootingTestType.class, metadata, "public", "shooting_test_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

