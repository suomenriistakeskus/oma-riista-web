package fi.riista.feature.gamediary.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTOBase;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersionSupport;
import org.hibernate.validator.constraints.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;

public class MobileHarvestDTO extends HarvestDTOBase {

    // Required for apiVersion >= 2
    @HarvestSpecVersionSupport(since = HarvestSpecVersion._2)
    private HarvestSpecVersion harvestSpecVersion;

    /**
     * Version of DTO.
     *
     * Used in mobile to compare mobiles data with server data. Mobile should
     * send version it supports, not the one it received from server.
     *
     * Used in server to detect which functionalities client supports.
     *
     * THIS IS DEPRECATED, {@link #harvestSpecVersion} IS NOW THE PREFERRED
     * SOURCE OF INFORMATION FOR RECOGNIZING WHAT MOBILE CLIENT IS CAPABLE TO DO
     * WITH HARVEST DATA.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Deprecated
    private Integer apiVersion;

    @HarvestSpecVersionSupport(since = HarvestSpecVersion._1)
    protected Long mobileClientRefId;

    @HarvestSpecVersionSupport(since = HarvestSpecVersion._1)
    @Range(min = Harvest.MIN_AMOUNT, max = Harvest.MAX_AMOUNT)
    protected Integer amount;

    @HarvestSpecVersionSupport(since = HarvestSpecVersion._1)
    protected boolean harvestReportDone;

    public MobileHarvestDTO() {
        // Specimen list needs to be left null to simulate/support old mobile client versions.
    }

    public MobileHarvestDTO(@Nonnull final HarvestSpecVersion specVersion) {
        setHarvestSpecVersion(Objects.requireNonNull(specVersion));
        // Specimen list needs to be left null to simulate/support old mobile client versions.
    }

    // Accessors -->

    @Override
    public HarvestSpecVersion getHarvestSpecVersion() {
        return harvestSpecVersion;
    }

    public void setHarvestSpecVersion(final HarvestSpecVersion specVersion) {
        this.harvestSpecVersion = specVersion;

        if (specVersion != null) {
            if (specVersion.requiresDeprecatedApiParameter()) {
                setApiVersion(1);
            }

            if (specVersion.requiresSpecimenList() && getSpecimens() == null) {
                setSpecimens(new ArrayList<>());
            }
        }
    }

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

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(@Nullable final Integer amount) {
        this.amount = amount;
    }

    public boolean isHarvestReportDone() {
        return harvestReportDone;
    }

    public void setHarvestReportDone(final boolean harvestReportDone) {
        this.harvestReportDone = harvestReportDone;
    }

    // Builder -->

    public static Builder<?> builder(@Nonnull final HarvestSpecVersion specVersion) {
        return new ConcreteBuilder(specVersion);
    }

    // Allows sub-classing for tests and adding new fluent interface style methods.
    public static abstract class Builder<SELF extends Builder<SELF>>
            extends HarvestDTOBase.Builder<MobileHarvestDTO, SELF> {

        protected Builder(@Nonnull final HarvestSpecVersion specVersion) {
            super();
            withSpecVersion(Objects.requireNonNull(specVersion));
        }

        public SELF withSpecVersion(@Nullable final HarvestSpecVersion specVersion) {
            dto.setHarvestSpecVersion(specVersion);
            return self();
        }

        public SELF withMobileClientRefId(@Nullable final Long mobileClientRefId) {
            dto.setMobileClientRefId(mobileClientRefId);
            return self();
        }

        public SELF withAmount(@Nullable final Integer amount) {
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

        @Override
        protected MobileHarvestDTO createDTO() {
            return new MobileHarvestDTO();
        }
    }

    private static final class ConcreteBuilder extends Builder<ConcreteBuilder> {

        public ConcreteBuilder(@Nonnull final HarvestSpecVersion specVersion) {
            super(specVersion);
        }

        @Override
        protected ConcreteBuilder self() {
            return this;
        }
    }

}
