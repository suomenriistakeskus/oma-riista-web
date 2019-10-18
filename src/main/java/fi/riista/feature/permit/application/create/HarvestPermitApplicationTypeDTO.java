package fi.riista.feature.permit.application.create;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.decision.PermitDecisionPaymentAmount;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.util.Optional;

import static fi.riista.feature.permit.decision.PermitDecision.DecisionType.HARVEST_PERMIT;
import static java.util.Objects.requireNonNull;

public class HarvestPermitApplicationTypeDTO {
    private final int huntingYear;
    private final LocalDate begin;
    private final LocalDate end;
    private final boolean active;
    private final BigDecimal price;
    private final HarvestPermitCategory category;

    public HarvestPermitApplicationTypeDTO(final Builder builder) {
        this.category = requireNonNull(builder.category);
        this.huntingYear = requireNonNull(builder.huntingYear);
        this.active = Optional.ofNullable(builder.activeOverride)
                .orElseGet(() -> DateUtil.overlapsInclusive(builder.begin, builder.end, requireNonNull(builder.today)));
        this.price = PermitDecisionPaymentAmount.getDefaultPaymentAmount(HARVEST_PERMIT, category);
        this.end = builder.end;
        this.begin = builder.begin;
    }

    public int getHuntingYear() {
        return huntingYear;
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

    public HarvestPermitCategory getCategory() {
        return category;
    }

    public static final class Builder {
        private final HarvestPermitCategory category;
        private Integer huntingYear;
        private Boolean activeOverride;
        private LocalDate begin;
        private LocalDate end;
        private LocalDate today;

        private Builder(final HarvestPermitCategory category) {
            this.category = requireNonNull(category);
        }

        public static Builder builder(final HarvestPermitCategory category) {
            return new Builder(category);
        }

        public Builder withHuntingYear(int huntingYear) {
            this.huntingYear = huntingYear;
            return this;
        }

        public Builder withBegin(LocalDate begin) {
            this.begin = begin;
            return this;
        }

        public Builder withEnd(LocalDate end) {
            this.end = end;
            return this;
        }

        public Builder withToday(LocalDate today) {
            this.today = today;
            return this;
        }

        public Builder withActiveOverride(final boolean active) {
            this.activeOverride = active;
            return this;
        }

        public HarvestPermitApplicationTypeDTO build() {
            return new HarvestPermitApplicationTypeDTO(this);
        }
    }
}
