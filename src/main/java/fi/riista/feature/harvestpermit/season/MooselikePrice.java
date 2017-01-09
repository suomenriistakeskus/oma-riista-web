package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.BicEntity;
import fi.riista.feature.common.entity.IbanEntity;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.util.BigDecimalHelper;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
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

    @NotNull
    @Embedded
    private IbanEntity iban;

    @NotNull
    @Embedded
    private BicEntity bic;

    @NotBlank
    @Size(min = 1, max = 70)
    @Column(length = 70, nullable = false)
    private String recipientName;


    @AssertTrue
    public boolean assertPricesRange() {
        return priceInRange(adultPrice) && priceInRange(youngPrice);
    }

    private static boolean priceInRange(final BigDecimal p) {
        return BigDecimalHelper.of(p).betweenOrEqual(MIN_PRICE, MAX_PRICE);
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

    public IbanEntity getIban() {
        return iban;
    }

    public void setIban(IbanEntity iban) {
        this.iban = iban;
    }

    public BicEntity getBic() {
        return bic;
    }

    public void setBic(BicEntity bic) {
        this.bic = bic;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
}
