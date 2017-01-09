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
 * SQOccupationType is a Querydsl query type for SQOccupationType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQOccupationType extends RelationalPathSpatial<SQOccupationType> {

    private static final long serialVersionUID = 1789782200;

    public static final SQOccupationType occupationType = new SQOccupationType("occupation_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQOccupationType> occupationTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQAnnouncementSubscriber> _announcementSubscriberOccupationTypeFk = createInvForeignKey(name, "occupation_type");

    public final com.querydsl.sql.ForeignKey<SQAnnouncement> _announcementFromOccupationTypeFk = createInvForeignKey(name, "from_occupation_type");

    public final com.querydsl.sql.ForeignKey<SQJhtTraining> _jhtTrainingOccupationTypeFk = createInvForeignKey(name, "occupation_type");

    public final com.querydsl.sql.ForeignKey<SQHuntingClubMemberInvitation> _huntingClubMemberInvitationOccupationTypeFk = createInvForeignKey(name, "occupation_type");

    public final com.querydsl.sql.ForeignKey<SQOccupation> _occupationTypeFk = createInvForeignKey(name, "occupation_type");

    public SQOccupationType(String variable) {
        super(SQOccupationType.class, forVariable(variable), "public", "occupation_type");
        addMetadata();
    }

    public SQOccupationType(String variable, String schema, String table) {
        super(SQOccupationType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQOccupationType(Path<? extends SQOccupationType> path) {
        super(path.getType(), path.getMetadata(), "public", "occupation_type");
        addMetadata();
    }

    public SQOccupationType(PathMetadata metadata) {
        super(SQOccupationType.class, metadata, "public", "occupation_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

