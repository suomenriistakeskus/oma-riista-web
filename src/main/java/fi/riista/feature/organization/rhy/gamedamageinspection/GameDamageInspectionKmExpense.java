package fi.riista.feature.organization.rhy.gamedamageinspection;

import fi.riista.feature.common.entity.LifecycleEntity;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Access(AccessType.FIELD)
public class GameDamageInspectionKmExpense extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private GameDamageInspectionEvent gameDamageInspectionEvent;

    @NotNull
    @Column(nullable = false)
    private Integer kilometers;

    @NotNull
    @Column(nullable = false)
    private BigDecimal expenseUnit;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GameDamageInspectionExpenseType expenseType;

    @Size(max = 255)
    @Column
    private String additionalInfo;

    public GameDamageInspectionKmExpense() {}

    public GameDamageInspectionKmExpense(final GameDamageInspectionEvent event,
                                         final Integer kilometers,
                                         final BigDecimal expenseUnit,
                                         final GameDamageInspectionExpenseType expenseType,
                                         final String additionalInfo) {
        this.gameDamageInspectionEvent = event;
        this.kilometers = kilometers;
        this.expenseUnit = expenseUnit;
        this.expenseType = expenseType;
        this.additionalInfo = additionalInfo;
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "game_damage_inspection_km_expense_id", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public GameDamageInspectionEvent getGameDamageInspectionEvent() {
        return gameDamageInspectionEvent;
    }

    public void setGameDamageInspectionEvent(final GameDamageInspectionEvent gameDamageInspectionEvent) {
        this.gameDamageInspectionEvent = gameDamageInspectionEvent;
    }

    public Integer getKilometers() {
        return kilometers;
    }

    public void setKilometers(final Integer kilometers) {
        this.kilometers = kilometers;
    }

    public BigDecimal getExpenseUnit() {
        return expenseUnit;
    }

    public void setExpenseUnit(final BigDecimal expenseUnit) {
        this.expenseUnit = expenseUnit;
    }

    public GameDamageInspectionExpenseType getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(final GameDamageInspectionExpenseType expenseType) {
        this.expenseType = expenseType;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
