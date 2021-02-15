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
 * SQRequiredWithinDeerPilot is a Querydsl query type for SQRequiredWithinDeerPilot
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQRequiredWithinDeerPilot extends RelationalPathSpatial<SQRequiredWithinDeerPilot> {

    private static final long serialVersionUID = 1400166509;

    public static final SQRequiredWithinDeerPilot requiredWithinDeerPilot = new SQRequiredWithinDeerPilot("required_within_deer_pilot");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQRequiredWithinDeerPilot> requiredWithinDeerPilotPk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQObservationBaseFields> _observationBaseFieldsWithinDeerHuntingFk = createInvForeignKey(name, "within_deer_hunting");

    public SQRequiredWithinDeerPilot(String variable) {
        super(SQRequiredWithinDeerPilot.class, forVariable(variable), "public", "required_within_deer_pilot");
        addMetadata();
    }

    public SQRequiredWithinDeerPilot(String variable, String schema, String table) {
        super(SQRequiredWithinDeerPilot.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQRequiredWithinDeerPilot(String variable, String schema) {
        super(SQRequiredWithinDeerPilot.class, forVariable(variable), schema, "required_within_deer_pilot");
        addMetadata();
    }

    public SQRequiredWithinDeerPilot(Path<? extends SQRequiredWithinDeerPilot> path) {
        super(path.getType(), path.getMetadata(), "public", "required_within_deer_pilot");
        addMetadata();
    }

    public SQRequiredWithinDeerPilot(PathMetadata metadata) {
        super(SQRequiredWithinDeerPilot.class, metadata, "public", "required_within_deer_pilot");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

