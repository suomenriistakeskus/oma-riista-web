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
 * SQMooseDataCardImportMessage is a Querydsl query type for SQMooseDataCardImportMessage
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMooseDataCardImportMessage extends RelationalPathSpatial<SQMooseDataCardImportMessage> {

    private static final long serialVersionUID = -417984228;

    public static final SQMooseDataCardImportMessage mooseDataCardImportMessage = new SQMooseDataCardImportMessage("moose_data_card_import_message");

    public final NumberPath<Long> importId = createNumber("importId", Long.class);

    public final StringPath message = createString("message");

    public final NumberPath<Integer> ordinal = createNumber("ordinal", Integer.class);

    public final com.querydsl.sql.PrimaryKey<SQMooseDataCardImportMessage> mooseDataCardImportMessagePkey = createPrimaryKey(importId, ordinal);

    public final com.querydsl.sql.ForeignKey<SQMooseDataCardImport> mooseDataCardImportMessageImportFk = createForeignKey(importId, "moose_data_card_import_id");

    public SQMooseDataCardImportMessage(String variable) {
        super(SQMooseDataCardImportMessage.class, forVariable(variable), "public", "moose_data_card_import_message");
        addMetadata();
    }

    public SQMooseDataCardImportMessage(String variable, String schema, String table) {
        super(SQMooseDataCardImportMessage.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQMooseDataCardImportMessage(String variable, String schema) {
        super(SQMooseDataCardImportMessage.class, forVariable(variable), schema, "moose_data_card_import_message");
        addMetadata();
    }

    public SQMooseDataCardImportMessage(Path<? extends SQMooseDataCardImportMessage> path) {
        super(path.getType(), path.getMetadata(), "public", "moose_data_card_import_message");
        addMetadata();
    }

    public SQMooseDataCardImportMessage(PathMetadata metadata) {
        super(SQMooseDataCardImportMessage.class, metadata, "public", "moose_data_card_import_message");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(importId, ColumnMetadata.named("import_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(message, ColumnMetadata.named("message").withIndex(3).ofType(Types.VARCHAR).withSize(1023).notNull());
        addMetadata(ordinal, ColumnMetadata.named("ordinal").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

