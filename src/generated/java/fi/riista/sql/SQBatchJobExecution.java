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
 * SQBatchJobExecution is a Querydsl query type for SQBatchJobExecution
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQBatchJobExecution extends RelationalPathSpatial<SQBatchJobExecution> {

    private static final long serialVersionUID = -62213694;

    public static final SQBatchJobExecution batchJobExecution = new SQBatchJobExecution("batch_job_execution");

    public final DateTimePath<java.sql.Timestamp> createTime = createDateTime("createTime", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> endTime = createDateTime("endTime", java.sql.Timestamp.class);

    public final StringPath exitCode = createString("exitCode");

    public final StringPath exitMessage = createString("exitMessage");

    public final StringPath jobConfigurationLocation = createString("jobConfigurationLocation");

    public final NumberPath<Long> jobExecutionId = createNumber("jobExecutionId", Long.class);

    public final NumberPath<Long> jobInstanceId = createNumber("jobInstanceId", Long.class);

    public final DateTimePath<java.sql.Timestamp> lastUpdated = createDateTime("lastUpdated", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> startTime = createDateTime("startTime", java.sql.Timestamp.class);

    public final StringPath status = createString("status");

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQBatchJobExecution> batchJobExecutionPk = createPrimaryKey(jobExecutionId);

    public final com.querydsl.sql.ForeignKey<SQBatchJobInstance> jobInstanceExecutionFk = createForeignKey(jobInstanceId, "job_instance_id");

    public final com.querydsl.sql.ForeignKey<SQBatchJobExecutionContext> _jobExecCtxFk = createInvForeignKey(jobExecutionId, "job_execution_id");

    public final com.querydsl.sql.ForeignKey<SQBatchStepExecution> _jobExecutionStepFk = createInvForeignKey(jobExecutionId, "job_execution_id");

    public final com.querydsl.sql.ForeignKey<SQBatchJobExecutionParams> _jobExecParamsFk = createInvForeignKey(jobExecutionId, "job_execution_id");

    public SQBatchJobExecution(String variable) {
        super(SQBatchJobExecution.class, forVariable(variable), "public", "batch_job_execution");
        addMetadata();
    }

    public SQBatchJobExecution(String variable, String schema, String table) {
        super(SQBatchJobExecution.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQBatchJobExecution(String variable, String schema) {
        super(SQBatchJobExecution.class, forVariable(variable), schema, "batch_job_execution");
        addMetadata();
    }

    public SQBatchJobExecution(Path<? extends SQBatchJobExecution> path) {
        super(path.getType(), path.getMetadata(), "public", "batch_job_execution");
        addMetadata();
    }

    public SQBatchJobExecution(PathMetadata metadata) {
        super(SQBatchJobExecution.class, metadata, "public", "batch_job_execution");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createTime, ColumnMetadata.named("create_time").withIndex(4).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(endTime, ColumnMetadata.named("end_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(exitCode, ColumnMetadata.named("exit_code").withIndex(8).ofType(Types.VARCHAR).withSize(20));
        addMetadata(exitMessage, ColumnMetadata.named("exit_message").withIndex(9).ofType(Types.VARCHAR).withSize(2500));
        addMetadata(jobConfigurationLocation, ColumnMetadata.named("job_configuration_location").withIndex(11).ofType(Types.VARCHAR).withSize(2500));
        addMetadata(jobExecutionId, ColumnMetadata.named("job_execution_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(jobInstanceId, ColumnMetadata.named("job_instance_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(lastUpdated, ColumnMetadata.named("last_updated").withIndex(10).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(startTime, ColumnMetadata.named("start_time").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(status, ColumnMetadata.named("status").withIndex(7).ofType(Types.VARCHAR).withSize(10));
        addMetadata(version, ColumnMetadata.named("version").withIndex(2).ofType(Types.BIGINT).withSize(19));
    }

}

