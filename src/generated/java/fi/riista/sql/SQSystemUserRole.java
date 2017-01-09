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
 * SQSystemUserRole is a Querydsl query type for SQSystemUserRole
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQSystemUserRole extends RelationalPathSpatial<SQSystemUserRole> {

    private static final long serialVersionUID = 931877827;

    public static final SQSystemUserRole systemUserRole = new SQSystemUserRole("system_user_role");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQSystemUserRole> systemUserRolePkey = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQSystemUser> _systemUserRoleFk = createInvForeignKey(name, "role");

    public SQSystemUserRole(String variable) {
        super(SQSystemUserRole.class, forVariable(variable), "public", "system_user_role");
        addMetadata();
    }

    public SQSystemUserRole(String variable, String schema, String table) {
        super(SQSystemUserRole.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQSystemUserRole(Path<? extends SQSystemUserRole> path) {
        super(path.getType(), path.getMetadata(), "public", "system_user_role");
        addMetadata();
    }

    public SQSystemUserRole(PathMetadata metadata) {
        super(SQSystemUserRole.class, metadata, "public", "system_user_role");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

