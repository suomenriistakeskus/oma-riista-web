package fi.riista.feature.organization.rhy.gamedamageinspection;

import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class GameDamageInspectionKmExpenseDTO {

    private Long id;

    @NotNull
    private int kilometers;

    @NotNull
    private BigDecimal expenseUnit;

    @NotNull
    private GameDamageInspectionExpenseType expenseType;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String additionalInfo;

    public static GameDamageInspectionKmExpenseDTO create(final GameDamageInspectionKmExpense expenses) {
        final GameDamageInspectionKmExpenseDTO dto = new GameDamageInspectionKmExpenseDTO();

        dto.setId(expenses.getId());
        dto.setKilometers(expenses.getKilometers());
        dto.setExpenseUnit(expenses.getExpenseUnit());
        dto.setExpenseType(expenses.getExpenseType());
        dto.setAdditionalInfo(expenses.getAdditionalInfo());

        return dto;
    }

    public GameDamageInspectionKmExpenseDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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
