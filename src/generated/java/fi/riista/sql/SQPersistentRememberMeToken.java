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
 * SQPersistentRememberMeToken is a Querydsl query type for SQPersistentRememberMeToken
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPersistentRememberMeToken extends RelationalPathSpatial<SQPersistentRememberMeToken> {

    private static final long serialVersionUID = 1561897194;

    public static final SQPersistentRememberMeToken persistentRememberMeToken = new SQPersistentRememberMeToken("persistent_remember_me_token");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final StringPath ipAddress = createString("ipAddress");

    public final StringPath series = createString("series");

    public final DateTimePath<java.sql.Timestamp> tokenDate = createDateTime("tokenDate", java.sql.Timestamp.class);

    public final StringPath tokenValue = createString("tokenValue");

    public final StringPath userAgent = createString("userAgent");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQPersistentRememberMeToken> persistentRememberMeTokenPkey = createPrimaryKey(series);

    public final com.querydsl.sql.ForeignKey<SQSystemUser> persistentTokenUserFk = createForeignKey(userId, "user_id");

    public SQPersistentRememberMeToken(String variable) {
        super(SQPersistentRememberMeToken.class, forVariable(variable), "public", "persistent_remember_me_token");
        addMetadata();
    }

    public SQPersistentRememberMeToken(String variable, String schema, String table) {
        super(SQPersistentRememberMeToken.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPersistentRememberMeToken(Path<? extends SQPersistentRememberMeToken> path) {
        super(path.getType(), path.getMetadata(), "public", "persistent_remember_me_token");
        addMetadata();
    }

    public SQPersistentRememberMeToken(PathMetadata metadata) {
        super(SQPersistentRememberMeToken.class, metadata, "public", "persistent_remember_me_token");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(ipAddress, ColumnMetadata.named("ip_address").withIndex(5).ofType(Types.VARCHAR).withSize(255));
        addMetadata(series, ColumnMetadata.named("series").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(tokenDate, ColumnMetadata.named("token_date").withIndex(4).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(tokenValue, ColumnMetadata.named("token_value").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(userAgent, ColumnMetadata.named("user_agent").withIndex(6).ofType(Types.VARCHAR).withSize(255));
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(7).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

