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
 * SQBirdPermitApplicationProtectedAreaType is a Querydsl query type for SQBirdPermitApplicationProtectedAreaType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQBirdPermitApplicationProtectedAreaType extends RelationalPathSpatial<SQBirdPermitApplicationProtectedAreaType> {

    private static final long serialVersionUID = -816136020;

    public static final SQBirdPermitApplicationProtectedAreaType birdPermitApplicationProtectedAreaType = new SQBirdPermitApplicationProtectedAreaType("bird_permit_application_protected_area_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQBirdPermitApplicationProtectedAreaType> birdPermitApplicationProtectedAreaTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQBirdPermitApplication> _birdPermitApplicationProtectedAreaTypeFk = createInvForeignKey(name, "protected_area_type");

    public final com.querydsl.sql.ForeignKey<SQPermitDecisionProtectedAreaType> _permitDecisionProtectedAreaTypeFk = createInvForeignKey(name, "protected_area_type");

    public SQBirdPermitApplicationProtectedAreaType(String variable) {
        super(SQBirdPermitApplicationProtectedAreaType.class, forVariable(variable), "public", "bird_permit_application_protected_area_type");
        addMetadata();
    }

    public SQBirdPermitApplicationProtectedAreaType(String variable, String schema, String table) {
        super(SQBirdPermitApplicationProtectedAreaType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQBirdPermitApplicationProtectedAreaType(String variable, String schema) {
        super(SQBirdPermitApplicationProtectedAreaType.class, forVariable(variable), schema, "bird_permit_application_protected_area_type");
        addMetadata();
    }

    public SQBirdPermitApplicationProtectedAreaType(Path<? extends SQBirdPermitApplicationProtectedAreaType> path) {
        super(path.getType(), path.getMetadata(), "public", "bird_permit_application_protected_area_type");
        addMetadata();
    }

    public SQBirdPermitApplicationProtectedAreaType(PathMetadata metadata) {
        super(SQBirdPermitApplicationProtectedAreaType.class, metadata, "public", "bird_permit_application_protected_area_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

