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
 * SQShootingTestAttemptResult is a Querydsl query type for SQShootingTestAttemptResult
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQShootingTestAttemptResult extends RelationalPathSpatial<SQShootingTestAttemptResult> {

    private static final long serialVersionUID = -1461877438;

    public static final SQShootingTestAttemptResult shootingTestAttemptResult = new SQShootingTestAttemptResult("shooting_test_attempt_result");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQShootingTestAttemptResult> shootingTestAttemptResultPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQShootingTestAttempt> _shootingTestAttemptResultFk = createInvForeignKey(name, "result");

    public SQShootingTestAttemptResult(String variable) {
        super(SQShootingTestAttemptResult.class, forVariable(variable), "public", "shooting_test_attempt_result");
        addMetadata();
    }

    public SQShootingTestAttemptResult(String variable, String schema, String table) {
        super(SQShootingTestAttemptResult.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQShootingTestAttemptResult(String variable, String schema) {
        super(SQShootingTestAttemptResult.class, forVariable(variable), schema, "shooting_test_attempt_result");
        addMetadata();
    }

    public SQShootingTestAttemptResult(Path<? extends SQShootingTestAttemptResult> path) {
        super(path.getType(), path.getMetadata(), "public", "shooting_test_attempt_result");
        addMetadata();
    }

    public SQShootingTestAttemptResult(PathMetadata metadata) {
        super(SQShootingTestAttemptResult.class, metadata, "public", "shooting_test_attempt_result");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

