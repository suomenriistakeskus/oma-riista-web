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
 * SQSystemUserPrivilege is a Querydsl query type for SQSystemUserPrivilege
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQSystemUserPrivilege extends RelationalPathSpatial<SQSystemUserPrivilege> {

    private static final long serialVersionUID = 1236675460;

    public static final SQSystemUserPrivilege systemUserPrivilege = new SQSystemUserPrivilege("system_user_privilege");

    public final StringPath name = createString("name");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQSystemUserPrivilege> systemUserPrivilegePkey = createPrimaryKey(userId, name);

    public final com.querydsl.sql.ForeignKey<SQSystemUser> systemUserPrivilegeSystemUserFk = createForeignKey(userId, "user_id");

    public SQSystemUserPrivilege(String variable) {
        super(SQSystemUserPrivilege.class, forVariable(variable), "public", "system_user_privilege");
        addMetadata();
    }

    public SQSystemUserPrivilege(String variable, String schema, String table) {
        super(SQSystemUserPrivilege.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQSystemUserPrivilege(String variable, String schema) {
        super(SQSystemUserPrivilege.class, forVariable(variable), schema, "system_user_privilege");
        addMetadata();
    }

    public SQSystemUserPrivilege(Path<? extends SQSystemUserPrivilege> path) {
        super(path.getType(), path.getMetadata(), "public", "system_user_privilege");
        addMetadata();
    }

    public SQSystemUserPrivilege(PathMetadata metadata) {
        super(SQSystemUserPrivilege.class, metadata, "public", "system_user_privilege");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

