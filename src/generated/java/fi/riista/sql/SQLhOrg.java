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
 * SQLhOrg is a Querydsl query type for SQLhOrg
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQLhOrg extends RelationalPathSpatial<SQLhOrg> {

    private static final long serialVersionUID = 968994005;

    public static final SQLhOrg lhOrg = new SQLhOrg("lh_org");

    public final NumberPath<Integer> areaSize = createNumber("areaSize", Integer.class);

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final StringPath contactPersonAddress1 = createString("contactPersonAddress1");

    public final StringPath contactPersonAddress2 = createString("contactPersonAddress2");

    public final StringPath contactPersonEmail = createString("contactPersonEmail");

    public final StringPath contactPersonLang = createString("contactPersonLang");

    public final StringPath contactPersonName = createString("contactPersonName");

    public final StringPath contactPersonPhone1 = createString("contactPersonPhone1");

    public final StringPath contactPersonPhone2 = createString("contactPersonPhone2");

    public final StringPath contactPersonRhy = createString("contactPersonRhy");

    public final StringPath contactPersonSsn = createString("contactPersonSsn");

    public final NumberPath<Integer> latitude = createNumber("latitude", Integer.class);

    public final NumberPath<Long> lhOrgId = createNumber("lhOrgId", Long.class);

    public final NumberPath<Integer> longitude = createNumber("longitude", Integer.class);

    public final StringPath mooseAreaCode = createString("mooseAreaCode");

    public final StringPath nameFinnish = createString("nameFinnish");

    public final StringPath nameSwedish = createString("nameSwedish");

    public final StringPath officialCode = createString("officialCode");

    public final StringPath rhyOfficialCode = createString("rhyOfficialCode");

    public final com.querydsl.sql.PrimaryKey<SQLhOrg> lhOrgPkey = createPrimaryKey(lhOrgId);

    public SQLhOrg(String variable) {
        super(SQLhOrg.class, forVariable(variable), "public", "lh_org");
        addMetadata();
    }

    public SQLhOrg(String variable, String schema, String table) {
        super(SQLhOrg.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQLhOrg(Path<? extends SQLhOrg> path) {
        super(path.getType(), path.getMetadata(), "public", "lh_org");
        addMetadata();
    }

    public SQLhOrg(PathMetadata metadata) {
        super(SQLhOrg.class, metadata, "public", "lh_org");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(areaSize, ColumnMetadata.named("area_size").withIndex(10).ofType(Types.INTEGER).withSize(10));
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(contactPersonAddress1, ColumnMetadata.named("contact_person_address_1").withIndex(14).ofType(Types.VARCHAR).withSize(255));
        addMetadata(contactPersonAddress2, ColumnMetadata.named("contact_person_address_2").withIndex(15).ofType(Types.VARCHAR).withSize(255));
        addMetadata(contactPersonEmail, ColumnMetadata.named("contact_person_email").withIndex(18).ofType(Types.VARCHAR).withSize(255));
        addMetadata(contactPersonLang, ColumnMetadata.named("contact_person_lang").withIndex(19).ofType(Types.VARCHAR).withSize(255));
        addMetadata(contactPersonName, ColumnMetadata.named("contact_person_name").withIndex(13).ofType(Types.VARCHAR).withSize(255));
        addMetadata(contactPersonPhone1, ColumnMetadata.named("contact_person_phone_1").withIndex(16).ofType(Types.VARCHAR).withSize(255));
        addMetadata(contactPersonPhone2, ColumnMetadata.named("contact_person_phone_2").withIndex(17).ofType(Types.VARCHAR).withSize(255));
        addMetadata(contactPersonRhy, ColumnMetadata.named("contact_person_rhy").withIndex(12).ofType(Types.VARCHAR).withSize(255));
        addMetadata(contactPersonSsn, ColumnMetadata.named("contact_person_ssn").withIndex(11).ofType(Types.VARCHAR).withSize(11));
        addMetadata(latitude, ColumnMetadata.named("latitude").withIndex(8).ofType(Types.INTEGER).withSize(10));
        addMetadata(lhOrgId, ColumnMetadata.named("lh_org_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(longitude, ColumnMetadata.named("longitude").withIndex(9).ofType(Types.INTEGER).withSize(10));
        addMetadata(mooseAreaCode, ColumnMetadata.named("moose_area_code").withIndex(7).ofType(Types.VARCHAR).withSize(255));
        addMetadata(nameFinnish, ColumnMetadata.named("name_finnish").withIndex(4).ofType(Types.VARCHAR).withSize(255));
        addMetadata(nameSwedish, ColumnMetadata.named("name_swedish").withIndex(5).ofType(Types.VARCHAR).withSize(255));
        addMetadata(officialCode, ColumnMetadata.named("official_code").withIndex(3).ofType(Types.VARCHAR).withSize(7).notNull());
        addMetadata(rhyOfficialCode, ColumnMetadata.named("rhy_official_code").withIndex(6).ofType(Types.CHAR).withSize(3));
    }

}

