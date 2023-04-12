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
 * SQInformationRequestLinkType is a Querydsl query type for SQInformationRequestLinkType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQInformationRequestLinkType extends RelationalPathSpatial<SQInformationRequestLinkType> {

    private static final long serialVersionUID = -11691958;

    public static final SQInformationRequestLinkType informationRequestLinkType = new SQInformationRequestLinkType("information_request_link_type");

    public final StringPath type = createString("type");

    public final com.querydsl.sql.PrimaryKey<SQInformationRequestLinkType> informationRequestLinkTypePkey = createPrimaryKey(type);

    public final com.querydsl.sql.ForeignKey<SQInformationRequestLink> _informationRequestLinkTypeFk = createInvForeignKey(type, "information_request_link_type");

    public SQInformationRequestLinkType(String variable) {
        super(SQInformationRequestLinkType.class, forVariable(variable), "public", "information_request_link_type");
        addMetadata();
    }

    public SQInformationRequestLinkType(String variable, String schema, String table) {
        super(SQInformationRequestLinkType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQInformationRequestLinkType(String variable, String schema) {
        super(SQInformationRequestLinkType.class, forVariable(variable), schema, "information_request_link_type");
        addMetadata();
    }

    public SQInformationRequestLinkType(Path<? extends SQInformationRequestLinkType> path) {
        super(path.getType(), path.getMetadata(), "public", "information_request_link_type");
        addMetadata();
    }

    public SQInformationRequestLinkType(PathMetadata metadata) {
        super(SQInformationRequestLinkType.class, metadata, "public", "information_request_link_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(type, ColumnMetadata.named("type").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

