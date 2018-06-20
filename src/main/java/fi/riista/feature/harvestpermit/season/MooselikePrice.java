package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.BicConverter;
import fi.riista.feature.common.entity.IbanConverter;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.util.BigDecimalComparison;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.iban4j.Bic;
import org.iban4j.Iban;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Access(AccessType.FIELD)
public class MooselikePrice extends BaseEntity<Long> {

    private static final BigDecimal MIN_PRICE = BigDecimal.ZERO;
    private static final BigDecimal MAX_PRICE = new BigDecimal(9999);

    private Long id;

    @Range(min = 2000, max = 2100)
    @Column(nullable = false)
    private int huntingYear;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private GameSpecies gameSpecies;

    @NotNull
    @Column(nullable = false)
    private BigDecimal adultPrice;

    @NotNull
    @Column(nullable = false)
    private BigDecimal youngPrice;

    @Column(length = 18)
    @Convert(converter = IbanConverter.class)
    private Iban iban;

    @Convert(converter = BicConverter.class)
    @Column(length = 11)
    private Bic bic;

    @NotBlank
    @Size(min = 1, max = 70)
    @Column(length = 70, nullable = false)
    private String recipientName;

    @AssertTrue
    public boolean isPriceRangeValid() {
        return priceInRange(adultPrice) && priceInRange(youngPrice);
    }

    private static boolean priceInRange(final BigDecimal p) {
        return BigDecimalComparison.of(p).betweenOrEqual(MIN_PRICE, MAX_PRICE);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mooselike_price_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(int huntingYear) {
        this.huntingYear = huntingYear;
    }

    public GameSpecies getGameSpecies() {
        return gameSpecies;
    }

    public void setGameSpecies(GameSpecies gameSpecies) {
        this.gameSpecies = gameSpecies;
    }

    public BigDecimal getAdultPrice() {
        return adultPrice;
    }

    public void setAdultPrice(BigDecimal adultPrice) {
        this.adultPrice = adultPrice;
    }

    public BigDecimal getYoungPrice() {
        return youngPrice;
    }

    public void setYoungPrice(BigDecimal youngPrice) {
        this.youngPrice = youngPrice;
    }

    public Iban getIban() {
        return iban;
    }

    public void setIban(Iban iban) {
        this.iban = iban;
    }

    public Bic getBic() {
        return bic;
    }

    public void setBic(Bic bic) {
        this.bic = bic;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
}
