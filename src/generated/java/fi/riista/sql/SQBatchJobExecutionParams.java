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
 * SQBatchJobExecutionParams is a Querydsl query type for SQBatchJobExecutionParams
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQBatchJobExecutionParams extends RelationalPathSpatial<SQBatchJobExecutionParams> {

    private static final long serialVersionUID = -263537176;

    public static final SQBatchJobExecutionParams batchJobExecutionParams = new SQBatchJobExecutionParams("batch_job_execution_params");

    public final DateTimePath<java.sql.Timestamp> dateVal = createDateTime("dateVal", java.sql.Timestamp.class);

    public final NumberPath<Double> doubleVal = createNumber("doubleVal", Double.class);

    public final StringPath identifying = createString("identifying");

    public final NumberPath<Long> jobExecutionId = createNumber("jobExecutionId", Long.class);

    public final StringPath keyName = createString("keyName");

    public final NumberPath<Long> longVal = createNumber("longVal", Long.class);

    public final StringPath stringVal = createString("stringVal");

    public final StringPath typeCd = createString("typeCd");

    public final com.querydsl.sql.ForeignKey<SQBatchJobExecution> jobExecParamsFk = createForeignKey(jobExecutionId, "job_execution_id");

    public SQBatchJobExecutionParams(String variable) {
        super(SQBatchJobExecutionParams.class, forVariable(variable), "public", "batch_job_execution_params");
        addMetadata();
    }

    public SQBatchJobExecutionParams(String variable, String schema, String table) {
        super(SQBatchJobExecutionParams.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQBatchJobExecutionParams(String variable, String schema) {
        super(SQBatchJobExecutionParams.class, forVariable(variable), schema, "batch_job_execution_params");
        addMetadata();
    }

    public SQBatchJobExecutionParams(Path<? extends SQBatchJobExecutionParams> path) {
        super(path.getType(), path.getMetadata(), "public", "batch_job_execution_params");
        addMetadata();
    }

    public SQBatchJobExecutionParams(PathMetadata metadata) {
        super(SQBatchJobExecutionParams.class, metadata, "public", "batch_job_execution_params");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dateVal, ColumnMetadata.named("date_val").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(doubleVal, ColumnMetadata.named("double_val").withIndex(7).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(identifying, ColumnMetadata.named("identifying").withIndex(8).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(jobExecutionId, ColumnMetadata.named("job_execution_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(keyName, ColumnMetadata.named("key_name").withIndex(3).ofType(Types.VARCHAR).withSize(100).notNull());
        addMetadata(longVal, ColumnMetadata.named("long_val").withIndex(6).ofType(Types.BIGINT).withSize(19));
        addMetadata(stringVal, ColumnMetadata.named("string_val").withIndex(4).ofType(Types.VARCHAR).withSize(250));
        addMetadata(typeCd, ColumnMetadata.named("type_cd").withIndex(2).ofType(Types.VARCHAR).withSize(6).notNull());
    }

}

