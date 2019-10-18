package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * SQPermitHolderType is a Querydsl query type for SQPermitHolderType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPermitHolderType extends RelationalPathSpatial<SQPermitHolderType> {

    private static final long serialVersionUID = -66038188;

    public static final SQPermitHolderType permitHolderType = new SQPermitHolderType("permit_holder_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQPermitHolderType> permitHolderTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> _harvestPermitApplicationHolderTypeFk = createInvForeignKey(name, "permit_holder_type");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermit> _harvestPermitHolderTypeFk = createInvForeignKey(name, "permit_holder_type");

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> _permitDecisionHolderTypeFk = createInvForeignKey(name, "permit_holder_type");

    public SQPermitHolderType(String variable) {
        super(SQPermitHolderType.class, forVariable(variable), "public", "permit_holder_type");
        addMetadata();
    }

    public SQPermitHolderType(String variable, String schema, String table) {
        super(SQPermitHolderType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPermitHolderType(String variable, String schema) {
        super(SQPermitHolderType.class, forVariable(variable), schema, "permit_holder_type");
        addMetadata();
    }

    public SQPermitHolderType(Path<? extends SQPermitHolderType> path) {
        super(path.getType(), path.getMetadata(), "public", "permit_holder_type");
        addMetadata();
    }

    public SQPermitHolderType(PathMetadata metadata) {
        super(SQPermitHolderType.class, metadata, "public", "permit_holder_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

