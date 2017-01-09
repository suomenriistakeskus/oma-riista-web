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
 * SQMooselikePrice is a Querydsl query type for SQMooselikePrice
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQMooselikePrice extends RelationalPathSpatial<SQMooselikePrice> {

    private static final long serialVersionUID = -941646266;

    public static final SQMooselikePrice mooselikePrice = new SQMooselikePrice("mooselike_price");

    public final NumberPath<java.math.BigDecimal> adultPrice = createNumber("adultPrice", java.math.BigDecimal.class);

    public final StringPath bic = createString("bic");

    public final NumberPath<Integer> consistencyVersion = createNumber("consistencyVersion", Integer.class);

    public final NumberPath<Long> gameSpeciesId = createNumber("gameSpeciesId", Long.class);

    public final NumberPath<Integer> huntingYear = createNumber("huntingYear", Integer.class);

    public final StringPath iban = createString("iban");

    public final NumberPath<Long> mooselikePriceId = createNumber("mooselikePriceId", Long.class);

    public final StringPath recipientName = createString("recipientName");

    public final NumberPath<java.math.BigDecimal> youngPrice = createNumber("youngPrice", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<SQMooselikePrice> mooselikePricePkey = createPrimaryKey(mooselikePriceId);

    public final com.querydsl.sql.ForeignKey<SQGameSpecies> mooselikePriceGameSpeciesFk = createForeignKey(gameSpeciesId, "game_species_id");

    public SQMooselikePrice(String variable) {
        super(SQMooselikePrice.class, forVariable(variable), "public", "mooselike_price");
        addMetadata();
    }

    public SQMooselikePrice(String variable, String schema, String table) {
        super(SQMooselikePrice.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQMooselikePrice(Path<? extends SQMooselikePrice> path) {
        super(path.getType(), path.getMetadata(), "public", "mooselike_price");
        addMetadata();
    }

    public SQMooselikePrice(PathMetadata metadata) {
        super(SQMooselikePrice.class, metadata, "public", "mooselike_price");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(adultPrice, ColumnMetadata.named("adult_price").withIndex(5).ofType(Types.NUMERIC).withSize(6).withDigits(2).notNull());
        addMetadata(bic, ColumnMetadata.named("bic").withIndex(8).ofType(Types.VARCHAR).withSize(11).notNull());
        addMetadata(consistencyVersion, ColumnMetadata.named("consistency_version").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(gameSpeciesId, ColumnMetadata.named("game_species_id").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(huntingYear, ColumnMetadata.named("hunting_year").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(iban, ColumnMetadata.named("iban").withIndex(7).ofType(Types.CHAR).withSize(18).notNull());
        addMetadata(mooselikePriceId, ColumnMetadata.named("mooselike_price_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(recipientName, ColumnMetadata.named("recipient_name").withIndex(9).ofType(Types.VARCHAR).withSize(70).notNull());
        addMetadata(youngPrice, ColumnMetadata.named("young_price").withIndex(6).ofType(Types.NUMERIC).withSize(6).withDigits(2).notNull());
    }

}

