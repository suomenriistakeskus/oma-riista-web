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
 * SQContactInfoShare is a Querydsl query type for SQContactInfoShare
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQContactInfoShare extends RelationalPathSpatial<SQContactInfoShare> {

    private static final long serialVersionUID = 721770596;

    public static final SQContactInfoShare contactInfoShare = new SQContactInfoShare("contact_info_share");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQContactInfoShare> contactInfoSharePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQOccupation> _occupationContactInfoShareFk = createInvForeignKey(name, "contact_info_share");

    public SQContactInfoShare(String variable) {
        super(SQContactInfoShare.class, forVariable(variable), "public", "contact_info_share");
        addMetadata();
    }

    public SQContactInfoShare(String variable, String schema, String table) {
        super(SQContactInfoShare.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQContactInfoShare(String variable, String schema) {
        super(SQContactInfoShare.class, forVariable(variable), schema, "contact_info_share");
        addMetadata();
    }

    public SQContactInfoShare(Path<? extends SQContactInfoShare> path) {
        super(path.getType(), path.getMetadata(), "public", "contact_info_share");
        addMetadata();
    }

    public SQContactInfoShare(PathMetadata metadata) {
        super(SQContactInfoShare.class, metadata, "public", "contact_info_share");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

