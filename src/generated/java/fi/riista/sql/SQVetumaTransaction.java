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
 * SQVetumaTransaction is a Querydsl query type for SQVetumaTransaction
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQVetumaTransaction extends RelationalPathSpatial<SQVetumaTransaction> {

    private static final long serialVersionUID = -1538446649;

    public static final SQVetumaTransaction vetumaTransaction = new SQVetumaTransaction("vetuma_transaction");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final StringPath email = createString("email");

    public final StringPath emailToken = createString("emailToken");

    public final DateTimePath<java.sql.Timestamp> endTime = createDateTime("endTime", java.sql.Timestamp.class);

    public final StringPath remoteAddress = createString("remoteAddress");

    public final StringPath responseSo = createString("responseSo");

    public final DateTimePath<java.sql.Timestamp> startTime = createDateTime("startTime", java.sql.Timestamp.class);

    public final StringPath status = createString("status");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final StringPath vetumaTransactionId = createString("vetumaTransactionId");

    public final com.querydsl.sql.PrimaryKey<SQVetumaTransaction> vetumaTransactionPkey = createPrimaryKey(vetumaTransactionId);

    public final com.querydsl.sql.ForeignKey<SQSystemUser> vetumaTransactionSystemUserFk = createForeignKey(userId, "user_id");

    public SQVetumaTransaction(String variable) {
        super(SQVetumaTransaction.class, forVariable(variable), "public", "vetuma_transaction");
        addMetadata();
    }

    public SQVetumaTransaction(String variable, String schema, String table) {
        super(SQVetumaTransaction.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQVetumaTransaction(String variable, String schema) {
        super(SQVetumaTransaction.class, forVariable(variable), schema, "vetuma_transaction");
        addMetadata();
    }

    public SQVetumaTransaction(Path<? extends SQVetumaTransaction> path) {
        super(path.getType(), path.getMetadata(), "public", "vetuma_transaction");
        addMetadata();
    }

    public SQVetumaTransaction(PathMetadata metadata) {
        super(SQVetumaTransaction.class, metadata, "public", "vetuma_transaction");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(email, ColumnMetadata.named("email").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(emailToken, ColumnMetadata.named("email_token").withIndex(5).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(endTime, ColumnMetadata.named("end_time").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(remoteAddress, ColumnMetadata.named("remote_address").withIndex(10).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(responseSo, ColumnMetadata.named("response_so").withIndex(9).ofType(Types.VARCHAR).withSize(255));
        addMetadata(startTime, ColumnMetadata.named("start_time").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(status, ColumnMetadata.named("status").withIndex(3).ofType(Types.CHAR).withSize(1).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(8).ofType(Types.BIGINT).withSize(19));
        addMetadata(vetumaTransactionId, ColumnMetadata.named("vetuma_transaction_id").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

