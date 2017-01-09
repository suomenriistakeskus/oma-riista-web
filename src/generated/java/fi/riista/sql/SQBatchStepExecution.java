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
 * SQBatchStepExecution is a Querydsl query type for SQBatchStepExecution
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQBatchStepExecution extends RelationalPathSpatial<SQBatchStepExecution> {

    private static final long serialVersionUID = 1415547045;

    public static final SQBatchStepExecution batchStepExecution = new SQBatchStepExecution("batch_step_execution");

    public final NumberPath<Long> commitCount = createNumber("commitCount", Long.class);

    public final DateTimePath<java.sql.Timestamp> endTime = createDateTime("endTime", java.sql.Timestamp.class);

    public final StringPath exitCode = createString("exitCode");

    public final StringPath exitMessage = createString("exitMessage");

    public final NumberPath<Long> filterCount = createNumber("filterCount", Long.class);

    public final NumberPath<Long> jobExecutionId = createNumber("jobExecutionId", Long.class);

    public final DateTimePath<java.sql.Timestamp> lastUpdated = createDateTime("lastUpdated", java.sql.Timestamp.class);

    public final NumberPath<Long> processSkipCount = createNumber("processSkipCount", Long.class);

    public final NumberPath<Long> readCount = createNumber("readCount", Long.class);

    public final NumberPath<Long> readSkipCount = createNumber("readSkipCount", Long.class);

    public final NumberPath<Long> rollbackCount = createNumber("rollbackCount", Long.class);

    public final DateTimePath<java.sql.Timestamp> startTime = createDateTime("startTime", java.sql.Timestamp.class);

    public final StringPath status = createString("status");

    public final NumberPath<Long> stepExecutionId = createNumber("stepExecutionId", Long.class);

    public final StringPath stepName = createString("stepName");

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public final NumberPath<Long> writeCount = createNumber("writeCount", Long.class);

    public final NumberPath<Long> writeSkipCount = createNumber("writeSkipCount", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQBatchStepExecution> batchStepExecutionPk = createPrimaryKey(stepExecutionId);

    public final com.querydsl.sql.ForeignKey<SQBatchJobExecution> jobExecutionStepFk = createForeignKey(jobExecutionId, "job_execution_id");

    public final com.querydsl.sql.ForeignKey<SQBatchStepExecutionContext> _stepExecCtxFk = createInvForeignKey(stepExecutionId, "step_execution_id");

    public SQBatchStepExecution(String variable) {
        super(SQBatchStepExecution.class, forVariable(variable), "public", "batch_step_execution");
        addMetadata();
    }

    public SQBatchStepExecution(String variable, String schema, String table) {
        super(SQBatchStepExecution.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQBatchStepExecution(Path<? extends SQBatchStepExecution> path) {
        super(path.getType(), path.getMetadata(), "public", "batch_step_execution");
        addMetadata();
    }

    public SQBatchStepExecution(PathMetadata metadata) {
        super(SQBatchStepExecution.class, metadata, "public", "batch_step_execution");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(commitCount, ColumnMetadata.named("commit_count").withIndex(8).ofType(Types.BIGINT).withSize(19));
        addMetadata(endTime, ColumnMetadata.named("end_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(exitCode, ColumnMetadata.named("exit_code").withIndex(16).ofType(Types.VARCHAR).withSize(20));
        addMetadata(exitMessage, ColumnMetadata.named("exit_message").withIndex(17).ofType(Types.VARCHAR).withSize(2500));
        addMetadata(filterCount, ColumnMetadata.named("filter_count").withIndex(10).ofType(Types.BIGINT).withSize(19));
        addMetadata(jobExecutionId, ColumnMetadata.named("job_execution_id").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(lastUpdated, ColumnMetadata.named("last_updated").withIndex(18).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(processSkipCount, ColumnMetadata.named("process_skip_count").withIndex(14).ofType(Types.BIGINT).withSize(19));
        addMetadata(readCount, ColumnMetadata.named("read_count").withIndex(9).ofType(Types.BIGINT).withSize(19));
        addMetadata(readSkipCount, ColumnMetadata.named("read_skip_count").withIndex(12).ofType(Types.BIGINT).withSize(19));
        addMetadata(rollbackCount, ColumnMetadata.named("rollback_count").withIndex(15).ofType(Types.BIGINT).withSize(19));
        addMetadata(startTime, ColumnMetadata.named("start_time").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(status, ColumnMetadata.named("status").withIndex(7).ofType(Types.VARCHAR).withSize(10));
        addMetadata(stepExecutionId, ColumnMetadata.named("step_execution_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(stepName, ColumnMetadata.named("step_name").withIndex(3).ofType(Types.VARCHAR).withSize(100).notNull());
        addMetadata(version, ColumnMetadata.named("version").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(writeCount, ColumnMetadata.named("write_count").withIndex(11).ofType(Types.BIGINT).withSize(19));
        addMetadata(writeSkipCount, ColumnMetadata.named("write_skip_count").withIndex(13).ofType(Types.BIGINT).withSize(19));
    }

}

