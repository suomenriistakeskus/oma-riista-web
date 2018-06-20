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
 * SQHarvestPermitApplicationAttachment is a Querydsl query type for SQHarvestPermitApplicationAttachment
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQHarvestPermitApplicationAttachment extends RelationalPathSpatial<SQHarvestPermitApplicationAttachment> {

    private static final long serialVersionUID = 342140450;

    public static final SQHarvestPermitApplicationAttachment harvestPermitApplicationAttachment = new SQHarvestPermitApplicationAttachment("harvest_permit_application_attachment");

    public final StringPath attachmentType = createString("attachmentType");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> harvestPermitApplicationAttachmentId = createNumber("harvestPermitApplicationAttachmentId", Long.class);

    public final NumberPath<Long> harvestPermitApplicationId = createNumber("harvestPermitApplicationId", Long.class);

    public final StringPath name = createString("name");

    public final StringPath url = createString("url");

    public final com.querydsl.sql.PrimaryKey<SQHarvestPermitApplicationAttachment> harvestPermitApplicationAttachmentPkey = createPrimaryKey(harvestPermitApplicationAttachmentId);

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplicationAttachmentType> harvestPermitApplicationAttachmentTypeFk = createForeignKey(attachmentType, "name");

    public final com.querydsl.sql.ForeignKey<SQHarvestPermitApplication> harvestPermitApplicationAttachmentApplicationFk = createForeignKey(harvestPermitApplicationId, "harvest_permit_application_id");

    public SQHarvestPermitApplicationAttachment(String variable) {
        super(SQHarvestPermitApplicationAttachment.class, forVariable(variable), "public", "harvest_permit_application_attachment");
        addMetadata();
    }

    public SQHarvestPermitApplicationAttachment(String variable, String schema, String table) {
        super(SQHarvestPermitApplicationAttachment.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQHarvestPermitApplicationAttachment(String variable, String schema) {
        super(SQHarvestPermitApplicationAttachment.class, forVariable(variable), schema, "harvest_permit_application_attachment");
        addMetadata();
    }

    public SQHarvestPermitApplicationAttachment(Path<? extends SQHarvestPermitApplicationAttachment> path) {
        super(path.getType(), path.getMetadata(), "public", "harvest_permit_application_attachment");
        addMetadata();
    }

    public SQHarvestPermitApplicationAttachment(PathMetadata metadata) {
        super(SQHarvestPermitApplicationAttachment.class, metadata, "public", "harvest_permit_application_attachment");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(attachmentType, ColumnMetadata.named("attachment_type").withIndex(6).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(harvestPermitApplicationAttachmentId, ColumnMetadata.named("harvest_permit_application_attachment_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(harvestPermitApplicationId, ColumnMetadata.named("harvest_permit_application_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(5).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(url, ColumnMetadata.named("url").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647).notNull());
    }

}

