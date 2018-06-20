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
 * SQFileContent is a Querydsl query type for SQFileContent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQFileContent extends RelationalPathSpatial<SQFileContent> {

    private static final long serialVersionUID = 669809322;

    public static final SQFileContent fileContent1 = new SQFileContent("file_content");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final SimplePath<byte[]> fileContent = createSimple("fileContent", byte[].class);

    public final StringPath fileMetadataUuid = createString("fileMetadataUuid");

    public final com.querydsl.sql.PrimaryKey<SQFileContent> fileContentPkey = createPrimaryKey(fileMetadataUuid);

    public final com.querydsl.sql.ForeignKey<SQFileMetadata> fileContentMetadataUuidFk = createForeignKey(fileMetadataUuid, "file_metadata_uuid");

    public SQFileContent(String variable) {
        super(SQFileContent.class, forVariable(variable), "public", "file_content");
        addMetadata();
    }

    public SQFileContent(String variable, String schema, String table) {
        super(SQFileContent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQFileContent(String variable, String schema) {
        super(SQFileContent.class, forVariable(variable), schema, "file_content");
        addMetadata();
    }

    public SQFileContent(Path<? extends SQFileContent> path) {
        super(path.getType(), path.getMetadata(), "public", "file_content");
        addMetadata();
    }

    public SQFileContent(PathMetadata metadata) {
        super(SQFileContent.class, metadata, "public", "file_content");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(fileContent, ColumnMetadata.named("file_content").withIndex(3).ofType(Types.BINARY).withSize(2147483647).notNull());
        addMetadata(fileMetadataUuid, ColumnMetadata.named("file_metadata_uuid").withIndex(1).ofType(Types.CHAR).withSize(36).notNull());
    }

}

