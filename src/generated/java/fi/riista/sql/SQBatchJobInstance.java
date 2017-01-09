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
 * SQBatchJobInstance is a Querydsl query type for SQBatchJobInstance
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQBatchJobInstance extends RelationalPathSpatial<SQBatchJobInstance> {

    private static final long serialVersionUID = -520064853;

    public static final SQBatchJobInstance batchJobInstance = new SQBatchJobInstance("batch_job_instance");

    public final NumberPath<Long> jobInstanceId = createNumber("jobInstanceId", Long.class);

    public final StringPath jobKey = createString("jobKey");

    public final StringPath jobName = createString("jobName");

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQBatchJobInstance> batchJobInstancePk = createPrimaryKey(jobInstanceId);

    public final com.querydsl.sql.ForeignKey<SQBatchJobExecution> _jobInstanceExecutionFk = createInvForeignKey(jobInstanceId, "job_instance_id");

    public SQBatchJobInstance(String variable) {
        super(SQBatchJobInstance.class, forVariable(variable), "public", "batch_job_instance");
        addMetadata();
    }

    public SQBatchJobInstance(String variable, String schema, String table) {
        super(SQBatchJobInstance.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQBatchJobInstance(Path<? extends SQBatchJobInstance> path) {
        super(path.getType(), path.getMetadata(), "public", "batch_job_instance");
        addMetadata();
    }

    public SQBatchJobInstance(PathMetadata metadata) {
        super(SQBatchJobInstance.class, metadata, "public", "batch_job_instance");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(jobInstanceId, ColumnMetadata.named("job_instance_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(jobKey, ColumnMetadata.named("job_key").withIndex(4).ofType(Types.VARCHAR).withSize(2500));
        addMetadata(jobName, ColumnMetadata.named("job_name").withIndex(3).ofType(Types.VARCHAR).withSize(100).notNull());
        addMetadata(version, ColumnMetadata.named("version").withIndex(2).ofType(Types.BIGINT).withSize(19));
    }

}

