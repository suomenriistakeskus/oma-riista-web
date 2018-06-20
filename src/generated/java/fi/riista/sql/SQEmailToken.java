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
 * SQEmailToken is a Querydsl query type for SQEmailToken
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQEmailToken extends RelationalPathSpatial<SQEmailToken> {

    private static final long serialVersionUID = 1515141840;

    public static final SQEmailToken emailToken = new SQEmailToken("email_token");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final StringPath createRemoteAddress = createString("createRemoteAddress");

    public final StringPath email = createString("email");

    public final DateTimePath<java.sql.Timestamp> revokedAt = createDateTime("revokedAt", java.sql.Timestamp.class);

    public final StringPath revokeRemoteAddress = createString("revokeRemoteAddress");

    public final StringPath tokenData = createString("tokenData");

    public final StringPath tokenType = createString("tokenType");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final DateTimePath<java.sql.Timestamp> validFrom = createDateTime("validFrom", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> validUntil = createDateTime("validUntil", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<SQEmailToken> emailTokenPkey = createPrimaryKey(tokenData);

    public final com.querydsl.sql.ForeignKey<SQSystemUser> emailTokenSystemUserFk = createForeignKey(userId, "user_id");

    public SQEmailToken(String variable) {
        super(SQEmailToken.class, forVariable(variable), "public", "email_token");
        addMetadata();
    }

    public SQEmailToken(String variable, String schema, String table) {
        super(SQEmailToken.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQEmailToken(String variable, String schema) {
        super(SQEmailToken.class, forVariable(variable), schema, "email_token");
        addMetadata();
    }

    public SQEmailToken(Path<? extends SQEmailToken> path) {
        super(path.getType(), path.getMetadata(), "public", "email_token");
        addMetadata();
    }

    public SQEmailToken(PathMetadata metadata) {
        super(SQEmailToken.class, metadata, "public", "email_token");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(createRemoteAddress, ColumnMetadata.named("create_remote_address").withIndex(8).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(email, ColumnMetadata.named("email").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(revokedAt, ColumnMetadata.named("revoked_at").withIndex(9).ofType(Types.TIMESTAMP).withSize(35).withDigits(6));
        addMetadata(revokeRemoteAddress, ColumnMetadata.named("revoke_remote_address").withIndex(10).ofType(Types.VARCHAR).withSize(255));
        addMetadata(tokenData, ColumnMetadata.named("token_data").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(tokenType, ColumnMetadata.named("token_type").withIndex(3).ofType(Types.CHAR).withSize(1).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(validFrom, ColumnMetadata.named("valid_from").withIndex(6).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(validUntil, ColumnMetadata.named("valid_until").withIndex(7).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
    }

}

