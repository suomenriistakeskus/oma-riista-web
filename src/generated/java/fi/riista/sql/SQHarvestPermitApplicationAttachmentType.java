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
 * SQHarvestPermitApplicationAttachmentType is a Querydsl query type for SQHarvestPermitApplicationAttachmentType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitApplicationAttachmentType extends RelationalPathSpatial<SQHarvestPermitApplicationAttachmentType> {

    private static final long serialVersionUID = 1739114620;

    public static final SQHarvestPermitApplicationAttachmentType harvestPermitApplicationAttachmentType = new SQHarvestPermitApplicationAttachmentType("harvest_permit_application_attachment_type");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitApplicationAttachmentType> harvestPermitApplicationAttachmentTypePk = createPrimaryKey(name);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationAttachment> _harvestPermitApplicationAttachmentTypeFk = createInvForeignKey(name, "attachment_type");

    public SQHarvestPermitApplicationAttachmentType(String variable) {
        super(SQHarvestPermitApplicationAttachmentType.class, forVariable(variable), "public", "harvest_permit_application_attachment_type");
        addMetadata();
    }

    public SQHarvestPermitApplicationAttachmentType(String variable, String schema, String table) {
        super(SQHarvestPermitApplicationAttachmentType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitApplicationAttachmentType(String variable, String schema) {
        super(SQHarvestPermitApplicationAttachmentType.class, forVariable(variable), schema, "harvest_permit_application_attachment_type");
        addMetadata();
    }

    public SQHarvestPermitApplicationAttachmentType(Path<? extends SQHarvestPermitApplicationAttachmentType> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_application_attachment_type");
        addMetadata();
    }

    public SQHarvestPermitApplicationAttachmentType(PathMetadata metadata) {
        super(SQHarvestPermitApplicationAttachmentType.class, metadata, "public", "harvest_permit_application_attachment_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

