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
 * SQOrganisationType is a Querydsl query type for SQOrganisationType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQOrganisationType extends RelationalPathSpatial<SQOrganisationType> {

    private static final long serialVersionUID = -845936633;

    public static final SQOrganisationType organisationType = new SQOrganisationType("organisation_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQOrganisationType> organisationTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQOrganisation> _organisationTypeFk = createInvForeignKey(name, "organisation_type");

    public SQOrganisationType(String variable) {
        super(SQOrganisationType.class, forVariable(variable), "public", "organisation_type");
        addMetadata();
    }

    public SQOrganisationType(String variable, String schema, String table) {
        super(SQOrganisationType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQOrganisationType(Path<? extends SQOrganisationType> path) {
        super(path.getType(), path.getMetadata(), "public", "organisation_type");
        addMetadata();
    }

    public SQOrganisationType(PathMetadata metadata) {
        super(SQOrganisationType.class, metadata, "public", "organisation_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

