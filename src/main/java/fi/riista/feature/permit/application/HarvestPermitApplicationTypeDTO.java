package fi.riista.feature.permit.application;

import org.joda.time.LocalDate;

import java.math.BigDecimal;

public class HarvestPermitApplicationTypeDTO {
    private final int huntingYear;
    private final String code;
    private final LocalDate begin;
    private final LocalDate end;
    private final boolean active;
    private final BigDecimal price;

    public HarvestPermitApplicationTypeDTO(final int huntingYear, final String code,
                                           final LocalDate begin, final LocalDate end,
                                           final boolean active, final BigDecimal price) {
        this.huntingYear = huntingYear;
        this.code = code;
        this.begin = begin;
        this.end = end;
        this.active = active;
        this.price = price;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public String getCode() {
        return code;
    }

    public LocalDate getBegin() {
        return begin;
    }

    public LocalDate getEnd() {
        return end;
    }

    public boolean isActive() {
        return active;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
