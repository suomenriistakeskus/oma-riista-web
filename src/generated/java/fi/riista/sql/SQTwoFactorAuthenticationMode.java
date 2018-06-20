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
 * SQTwoFactorAuthenticationMode is a Querydsl query type for SQTwoFactorAuthenticationMode
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQTwoFactorAuthenticationMode extends RelationalPathSpatial<SQTwoFactorAuthenticationMode> {

    private static final long serialVersionUID = 60375971;

    public static final SQTwoFactorAuthenticationMode twoFactorAuthenticationMode = new SQTwoFactorAuthenticationMode("two_factor_authentication_mode");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQTwoFactorAuthenticationMode> twoFactorAuthenticationModePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQSystemUser> _systemUserTwoFactorFk = createInvForeignKey(name, "two_factor_authentication");

    public SQTwoFactorAuthenticationMode(String variable) {
        super(SQTwoFactorAuthenticationMode.class, forVariable(variable), "public", "two_factor_authentication_mode");
        addMetadata();
    }

    public SQTwoFactorAuthenticationMode(String variable, String schema, String table) {
        super(SQTwoFactorAuthenticationMode.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQTwoFactorAuthenticationMode(String variable, String schema) {
        super(SQTwoFactorAuthenticationMode.class, forVariable(variable), schema, "two_factor_authentication_mode");
        addMetadata();
    }

    public SQTwoFactorAuthenticationMode(Path<? extends SQTwoFactorAuthenticationMode> path) {
        super(path.getType(), path.getMetadata(), "public", "two_factor_authentication_mode");
        addMetadata();
    }

    public SQTwoFactorAuthenticationMode(PathMetadata metadata) {
        super(SQTwoFactorAuthenticationMode.class, metadata, "public", "two_factor_authentication_mode");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

