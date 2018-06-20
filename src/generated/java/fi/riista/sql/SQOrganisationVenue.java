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
 * SQOrganisationVenue is a Querydsl query type for SQOrganisationVenue
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQOrganisationVenue extends RelationalPathSpatial<SQOrganisationVenue> {

    private static final long serialVersionUID = -452981950;

    public static final SQOrganisationVenue organisationVenue = new SQOrganisationVenue("organisation_venue");

    public final NumberPath<Long> organisationId = createNumber("organisationId", Long.class);

    public final NumberPath<Long> venueId = createNumber("venueId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQOrganisationVenue> organisationVenuePkey = createPrimaryKey(organisationId, venueId);

    public final com.querydsl.sql.ForeignKey<SQVenue> organisationVenueVenueFk = createForeignKey(venueId, "venue_id");

    public final com.querydsl.sql.ForeignKey<SQOrganisation> organisationVenueOrganisationFk = createForeignKey(organisationId, "organisation_id");

    public SQOrganisationVenue(String variable) {
        super(SQOrganisationVenue.class, forVariable(variable), "public", "organisation_venue");
        addMetadata();
    }

    public SQOrganisationVenue(String variable, String schema, String table) {
        super(SQOrganisationVenue.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQOrganisationVenue(String variable, String schema) {
        super(SQOrganisationVenue.class, forVariable(variable), schema, "organisation_venue");
        addMetadata();
    }

    public SQOrganisationVenue(Path<? extends SQOrganisationVenue> path) {
        super(path.getType(), path.getMetadata(), "public", "organisation_venue");
        addMetadata();
    }

    public SQOrganisationVenue(PathMetadata metadata) {
        super(SQOrganisationVenue.class, metadata, "public", "organisation_venue");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(organisationId, ColumnMetadata.named("organisation_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(venueId, ColumnMetadata.named("venue_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

