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
 * SQBatchStepExecutionContext is a Querydsl query type for SQBatchStepExecutionContext
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQBatchStepExecutionContext extends RelationalPathSpatial<SQBatchStepExecutionContext> {

    private static final long serialVersionUID = 44642634;

    public static final SQBatchStepExecutionContext batchStepExecutionContext = new SQBatchStepExecutionContext("batch_step_execution_context");

    public final StringPath serializedContext = createString("serializedContext");

    public final StringPath shortContext = createString("shortContext");

    public final NumberPath<Long> stepExecutionId = createNumber("stepExecutionId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQBatchStepExecutionContext> batchStepExecutionContextPk = createPrimaryKey(stepExecutionId);

    public final com.querydsl.sql.ForeignKey<SQBatchStepExecution> stepExecCtxFk = createForeignKey(stepExecutionId, "step_execution_id");

    public SQBatchStepExecutionContext(String variable) {
        super(SQBatchStepExecutionContext.class, forVariable(variable), "public", "batch_step_execution_context");
        addMetadata();
    }

    public SQBatchStepExecutionContext(String variable, String schema, String table) {
        super(SQBatchStepExecutionContext.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQBatchStepExecutionContext(Path<? extends SQBatchStepExecutionContext> path) {
        super(path.getType(), path.getMetadata(), "public", "batch_step_execution_context");
        addMetadata();
    }

    public SQBatchStepExecutionContext(PathMetadata metadata) {
        super(SQBatchStepExecutionContext.class, metadata, "public", "batch_step_execution_context");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(serializedContext, ColumnMetadata.named("serialized_context").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(shortContext, ColumnMetadata.named("short_context").withIndex(2).ofType(Types.VARCHAR).withSize(2500).notNull());
        addMetadata(stepExecutionId, ColumnMetadata.named("step_execution_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

