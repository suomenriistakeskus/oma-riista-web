package fi.riista.feature.permit.application.create;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.decision.PermitDecisionPaymentAmount;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDateTime;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static fi.riista.feature.permit.decision.PermitDecision.DecisionType.HARVEST_PERMIT;
import static java.util.Objects.requireNonNull;

public class HarvestPermitApplicationTypeDTO {
    private final int huntingYear;
    private final LocalDateTime begin;
    private final LocalDateTime end;
    private final boolean active;
    private final BigDecimal price;
    private final HarvestPermitCategory category;
    private final Map<String, String> instructions;

    public HarvestPermitApplicationTypeDTO(final Builder builder) {
        this.category = requireNonNull(builder.category);
        this.huntingYear = requireNonNull(builder.huntingYear);
        this.active = Optional.ofNullable(builder.activeOverride)
                .orElseGet(() -> DateUtil.rangeFrom(builder.begin, builder.end).contains(requireNonNull(builder.now)));

        // Use validity year 1 since it does not affect the price
        final String permitTypeCode = PermitTypeCode.getPermitTypeCode(category, 1);
        this.price = PermitDecisionPaymentAmount.getDefaultPaymentAmount(HARVEST_PERMIT, permitTypeCode);
        this.end = builder.end;
        this.begin = builder.begin;
        this.instructions = builder.instructions.asMap();
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public LocalDateTime getBegin() {
        return begin;
    }

    public LocalDateTime getEnd() {
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

    public Map<String, String> getInstructions() {
        return instructions;
    }

    public static final class Builder {
        private final HarvestPermitCategory category;
        private Integer huntingYear;
        private Boolean activeOverride;
        private LocalDateTime begin;
        private LocalDateTime end;
        private LocalDateTime now;
        private LocalisedString instructions;

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

        public Builder withBegin(LocalDateTime begin) {
            this.begin = begin;
            return this;
        }

        public Builder withEnd(LocalDateTime end) {
            this.end = end;
            return this;
        }

        public Builder withNow(LocalDateTime now) {
            this.now = now;
            return this;
        }

        public Builder withActiveOverride(final Boolean active) {
            this.activeOverride = active;
            return this;
        }

        public Builder withInstructions(final LocalisedString instructions) {
            this.instructions = instructions;
            return this;
        }

        public HarvestPermitApplicationTypeDTO build() {
            return new HarvestPermitApplicationTypeDTO(this);
        }
    }
}
