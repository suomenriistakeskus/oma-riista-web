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
 * SQAnnouncementSenderType is a Querydsl query type for SQAnnouncementSenderType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQAnnouncementSenderType extends RelationalPathSpatial<SQAnnouncementSenderType> {

    private static final long serialVersionUID = -294826423;

    public static final SQAnnouncementSenderType announcementSenderType = new SQAnnouncementSenderType("announcement_sender_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQAnnouncementSenderType> announcementSenderTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQAnnouncement> _announcementSenderTypeFk = createInvForeignKey(name, "sender_type");

    public SQAnnouncementSenderType(String variable) {
        super(SQAnnouncementSenderType.class, forVariable(variable), "public", "announcement_sender_type");
        addMetadata();
    }

    public SQAnnouncementSenderType(String variable, String schema, String table) {
        super(SQAnnouncementSenderType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQAnnouncementSenderType(String variable, String schema) {
        super(SQAnnouncementSenderType.class, forVariable(variable), schema, "announcement_sender_type");
        addMetadata();
    }

    public SQAnnouncementSenderType(Path<? extends SQAnnouncementSenderType> path) {
        super(path.getType(), path.getMetadata(), "public", "announcement_sender_type");
        addMetadata();
    }

    public SQAnnouncementSenderType(PathMetadata metadata) {
        super(SQAnnouncementSenderType.class, metadata, "public", "announcement_sender_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

