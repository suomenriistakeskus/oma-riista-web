package fi.riista.feature.gamediary.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTOBase;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersionSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import static java.util.Objects.requireNonNull;

public class MobileHarvestDTO extends HarvestDTOBase {

    /**
     * Version of DTO.
     *
     * Used in mobile to compare mobiles data with server data. Mobile should
     * send version it supports, not the one it received from server.
     *
     * Used in server to detect which functionalities client supports.
     *
     * DEPRECATED, {@link #getHarvestSpecVersion()} IS DE FACTO SOURCE FOR
     * RECOGNIZING WHAT MOBILE CLIENT IS CAPABLE TO DO WITH HARVEST DATA.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Deprecated
    private Integer apiVersion = 2;

    @HarvestSpecVersionSupport(lowest = HarvestSpecVersion._3)
    private Long mobileClientRefId;

    @HarvestSpecVersionSupport(lowest = HarvestSpecVersion._3)
    @Min(Harvest.MIN_AMOUNT)
    @Max(Harvest.MAX_AMOUNT)
    private int amount;

    @HarvestSpecVersionSupport(lowest = HarvestSpecVersion._3)
    private boolean harvestReportDone;

    // Accessors -->

    @Deprecated
    public Integer getApiVersion() {
        return apiVersion;
    }

    @Deprecated
    public void setApiVersion(final Integer apiVersion) {
        this.apiVersion = apiVersion;
    }

    public Long getMobileClientRefId() {
        return mobileClientRefId;
    }

    public void setMobileClientRefId(final Long mobileClientRefId) {
        this.mobileClientRefId = mobileClientRefId;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    public void setAmount(final int amount) {
        this.amount = amount;
    }

    public boolean isHarvestReportDone() {
        return harvestReportDone;
    }

    public void setHarvestReportDone(final boolean harvestReportDone) {
        this.harvestReportDone = harvestReportDone;
    }

    // Builder -->

    public static Builder<?, ?> builder(@Nonnull final HarvestSpecVersion specVersion) {
        return new ConcreteBuilder(specVersion);
    }

    // Allows sub-classing for tests and adding new fluent interface style methods.
    public static abstract class Builder<DTO extends MobileHarvestDTO, SELF extends Builder<DTO, SELF>>
            extends HarvestDTOBase.Builder<DTO, SELF> {

        protected Builder(@Nonnull final HarvestSpecVersion specVersion) {
            super();
            withSpecVersion(requireNonNull(specVersion));
        }

        public SELF withMobileClientRefId(@Nullable final Long mobileClientRefId) {
            dto.setMobileClientRefId(mobileClientRefId);
            return self();
        }

        public SELF withAmount(final int amount) {
            dto.setAmount(amount);
            return self();
        }

        public SELF withHarvestReportDone(final boolean harvestReportDone) {
            dto.setHarvestReportDone(harvestReportDone);
            return self();
        }

        // ASSOCIATIONS MUST NOT BE TRAVERSED IN THIS METHOD (except for identifiers that are
        // part of Harvest itself).
        @Override
        public SELF populateWith(@Nonnull final Harvest harvest) {
            return super.populateWith(harvest)
                    .withMobileClientRefId(harvest.getMobileClientRefId())
                    .withHarvestReportDone(harvest.isHarvestReportDone())
                    .withAmount(harvest.getAmount());
        }
    }

    private static final class ConcreteBuilder extends Builder<MobileHarvestDTO, ConcreteBuilder> {

        public ConcreteBuilder(@Nonnull final HarvestSpecVersion specVersion) {
            super(specVersion);
        }

        @Override
        protected ConcreteBuilder self() {
            return this;
        }

        @Override
        protected MobileHarvestDTO createDTO() {
            return new MobileHarvestDTO();
        }

    }
}
