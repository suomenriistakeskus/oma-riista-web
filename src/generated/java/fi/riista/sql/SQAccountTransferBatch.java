package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQAccountTransferBatch is a Querydsl query type for SQAccountTransferBatch
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQAccountTransferBatch extends RelationalPathSpatial<SQAccountTransferBatch> {

    private static final long serialVersionUID = 224600117;

    public static final SQAccountTransferBatch accountTransferBatch = new SQAccountTransferBatch("account_transfer_batch");

    public final NumberPath<Long> accountTransferBatchId = createNumber("accountTransferBatchId", Long.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> createdByUserId = createNumber("createdByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> deletedByUserId = createNumber("deletedByUserId", Long.class);

    public final DateTimePath<java.sql.Timestamp> deletionTime = createDateTime("deletionTime", java.sql.Timestamp.class);

    public final StringPath filename = createString("filename");

    public final DatePath<java.sql.Date> filenameDate = createDate("filenameDate", java.sql.Date.class);

    public final DateTimePath<java.sql.Timestamp> modificationTime = createDateTime("modificationTime", java.sql.Timestamp.class);

    public final NumberPath<Long> modifiedByUserId = createNumber("modifiedByUserId", Long.class);

    public final DatePath<java.sql.Date> statementDate = createDate("statementDate", java.sql.Date.class);

    public final com.querydsl.sql.PrimaryKey<SQAccountTransferBatch> accountTransferBatchPkey = createPrimaryKey(accountTransferBatchId);

    public final com.querydsl.sql.ForeignKey<SQAccountTransfer> _accountTransferBatchFk = createInvForeignKey(accountTransferBatchId, "batch_id");

    public SQAccountTransferBatch(String variable) {
        super(SQAccountTransferBatch.class, forVariable(variable), "public", "account_transfer_batch");
        addMetadata();
    }

    public SQAccountTransferBatch(String variable, String schema, String table) {
        super(SQAccountTransferBatch.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQAccountTransferBatch(String variable, String schema) {
        super(SQAccountTransferBatch.class, forVariable(variable), schema, "account_transfer_batch");
        addMetadata();
    }

    public SQAccountTransferBatch(Path<? extends SQAccountTransferBatch> path) {
        super(path.getType(), path.getMetadata(), "public", "account_transfer_batch");
        addMetadata();
    }

    public SQAccountTransferBatch(PathMetadata metadata) {
        super(SQAccountTransferBatch.class, metadata, "public", "account_transfer_batch");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(accountTransferBatchId, ColumnMetadata.named("account_transfer_batch_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createdByUserId, ColumnMetadata.named("created_by_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(creationTime, ColumnMetadata.named("creation_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(deletedByUserId, ColumnMetadata.named("deleted_by_user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(deletionTime, ColumnMetadata.named("deletion_time").withIndex(8).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(filename, ColumnMetadata.named("filename").withIndex(10).ofType(Types.VARCHAR).withSize(255));
        addMetadata(filenameDate, ColumnMetadata.named("filename_date").withIndex(11).ofType(Types.DATE).withSize(13));
        addMetadata(modificationTime, ColumnMetadata.named("modification_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(modifiedByUserId, ColumnMetadata.named("modified_by_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(statementDate, ColumnMetadata.named("statement_date").withIndex(9).ofType(Types.DATE).withSize(13).notNull());
    }

}

