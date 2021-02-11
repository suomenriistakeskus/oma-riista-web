package fi.riista.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;



/**
 * SQPublicPdfDownload is a Querydsl query type for SQPublicPdfDownload
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQPublicPdfDownload extends RelationalPathSpatial<SQPublicPdfDownload> {

    private static final long serialVersionUID = 1776578302;

    public static final SQPublicPdfDownload publicPdfDownload = new SQPublicPdfDownload("public_pdf_download");

    public final DateTimePath<java.sql.Timestamp> downloadTime = createDateTime("downloadTime", java.sql.Timestamp.class);

    public final NumberPath<Integer> gid = createNumber("gid", Integer.class);

    public final NumberPath<Long> permitDecisionId = createNumber("permitDecisionId", Long.class);

    public final com.querydsl.sql.PrimaryKey<SQPublicPdfDownload> publicPdfDownloadPkey = createPrimaryKey(gid);

    public final com.querydsl.sql.ForeignKey<SQPermitDecision> publicPdfDownloadDecisionFk = createForeignKey(permitDecisionId, "permit_decision_id");

    public SQPublicPdfDownload(String variable) {
        super(SQPublicPdfDownload.class, forVariable(variable), "public", "public_pdf_download");
        addMetadata();
    }

    public SQPublicPdfDownload(String variable, String schema, String table) {
        super(SQPublicPdfDownload.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQPublicPdfDownload(String variable, String schema) {
        super(SQPublicPdfDownload.class, forVariable(variable), schema, "public_pdf_download");
        addMetadata();
    }

    public SQPublicPdfDownload(Path<? extends SQPublicPdfDownload> path) {
        super(path.getType(), path.getMetadata(), "public", "public_pdf_download");
        addMetadata();
    }

    public SQPublicPdfDownload(PathMetadata metadata) {
        super(SQPublicPdfDownload.class, metadata, "public", "public_pdf_download");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(downloadTime, ColumnMetadata.named("download_time").withIndex(2).ofType(Types.TIMESTAMP).withSize(35).withDigits(6).notNull());
        addMetadata(gid, ColumnMetadata.named("gid").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(permitDecisionId, ColumnMetadata.named("permit_decision_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

