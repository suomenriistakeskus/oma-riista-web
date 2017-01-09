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
 * SQBatchJobExecutionContext is a Querydsl query type for SQBatchJobExecutionContext
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQBatchJobExecutionContext extends RelationalPathSpatial<SQBatchJobExecutionContext> {

    private static final long serialVersionUID = -2129658483;

    public static final SQBatchJobExecutionContext batchJobExecutionContext = new SQBatchJobExecutionContext("batch_job_execution_context");

    public final NumberPath<Long> jobExecutionId = createNumber("jobExecutionId", Long.class);

    public final StringPath serializedContext = createString("serializedContext");

    public final StringPath shortContext = createString("shortContext");

    public final com.querydsl.sql.PrimaryKey<SQBatchJobExecutionContext> batchJobExecutionContextPk = createPrimaryKey(jobExecutionId);

    public final com.querydsl.sql.ForeignKey<SQBatchJobExecution> jobExecCtxFk = createForeignKey(jobExecutionId, "job_execution_id");

    public SQBatchJobExecutionContext(String variable) {
        super(SQBatchJobExecutionContext.class, forVariable(variable), "public", "batch_job_execution_context");
        addMetadata();
    }

    public SQBatchJobExecutionContext(String variable, String schema, String table) {
        super(SQBatchJobExecutionContext.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQBatchJobExecutionContext(Path<? extends SQBatchJobExecutionContext> path) {
        super(path.getType(), path.getMetadata(), "public", "batch_job_execution_context");
        addMetadata();
    }

    public SQBatchJobExecutionContext(PathMetadata metadata) {
        super(SQBatchJobExecutionContext.class, metadata, "public", "batch_job_execution_context");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(jobExecutionId, ColumnMetadata.named("job_execution_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(serializedContext, ColumnMetadata.named("serialized_context").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(shortContext, ColumnMetadata.named("short_context").withIndex(2).ofType(Types.VARCHAR).withSize(2500).notNull());
    }

}

