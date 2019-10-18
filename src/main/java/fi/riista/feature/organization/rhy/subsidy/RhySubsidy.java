package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Riistanhoitoyhdistykselle myönnetty valtionavustus, joka maksetaan vuosittain
 * kahdessa erässä.
 */
@Entity
@Access(value = AccessType.FIELD)
public class RhySubsidy extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Riistanhoitoyhdistys rhy;

    @Column(nullable = false, insertable = true, updatable = false)
    private int year;

    // Monetary amount granted for the first batch
    @NotNull
    @Column(nullable = false)
    private BigDecimal amountOfBatch1;

    // Monetary amount granted for the second batch
    @Column
    private BigDecimal amountOfBatch2;

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "rhy_subsidy_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Riistanhoitoyhdistys getRhy() {
        return rhy;
    }

    public void setRhy(final Riistanhoitoyhdistys rhy) {
        this.rhy = rhy;
    }

    public int getYear() {
        return year;
    }

    public void setYear(final int year) {
        this.year = year;
    }

    public BigDecimal getAmountOfBatch1() {
        return amountOfBatch1;
    }

    public void setAmountOfBatch1(final BigDecimal amount) {
        this.amountOfBatch1 = amount;
    }

    public BigDecimal getAmountOfBatch2() {
        return amountOfBatch2;
    }

    public void setAmountOfBatch2(final BigDecimal amount2) {
        this.amountOfBatch2 = amount2;
    }
}
