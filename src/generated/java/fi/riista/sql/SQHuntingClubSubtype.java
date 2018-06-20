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
 * SQHuntingClubSubtype is a Querydsl query type for SQHuntingClubSubtype
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHuntingClubSubtype extends RelationalPathSpatial<SQHuntingClubSubtype> {

    private static final long serialVersionUID = 1045568776;

    public static final SQHuntingClubSubtype huntingClubSubtype = new SQHuntingClubSubtype("hunting_club_subtype");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQHuntingClubSubtype> huntingClubSubtypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQOrganisation> _clubTypeFk = createInvForeignKey(name, "subtype");

    public SQHuntingClubSubtype(String variable) {
        super(SQHuntingClubSubtype.class, forVariable(variable), "public", "hunting_club_subtype");
        addMetadata();
    }

    public SQHuntingClubSubtype(String variable, String schema, String table) {
        super(SQHuntingClubSubtype.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHuntingClubSubtype(String variable, String schema) {
        super(SQHuntingClubSubtype.class, forVariable(variable), schema, "hunting_club_subtype");
        addMetadata();
    }

    public SQHuntingClubSubtype(Path<? extends SQHuntingClubSubtype> path) {
        super(path.getType(), path.getMetadata(), "public", "hunting_club_subtype");
        addMetadata();
    }

    public SQHuntingClubSubtype(PathMetadata metadata) {
        super(SQHuntingClubSubtype.class, metadata, "public", "hunting_club_subtype");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

