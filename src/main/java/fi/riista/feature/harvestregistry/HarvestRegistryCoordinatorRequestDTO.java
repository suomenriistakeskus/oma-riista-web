package fi.riista.feature.harvestregistry;

import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Collections;

public class HarvestRegistryCoordinatorRequestDTO {

    @NotNull
    @Min(0)
    private Integer page;

    @NotNull
    @Min(1)
    private Integer pageSize;

    private long rhyId;

    @NotNull
    private LocalDate beginDate;

    @NotNull
    private LocalDate endDate;

    private int species;

    @NotNull
    private HarvestRegistryCoordinatorSearchReason searchReason;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String shooterHunterNumber;

    @AssertTrue
    public boolean isHunterNumberAndSearchReasonCompatible() {
        // Searching by hunter number is allowed only for hunting control purposes
        return searchReason == HarvestRegistryCoordinatorSearchReason.HUNTING_CONTROL ||
                shooterHunterNumber == null;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPage(final int page) {
        this.page = page;
    }

    public void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getSpecies() {
        return species;
    }

    public void setSpecies(final int species) {
        this.species = species;
    }

    public long getRhyId() {
        return rhyId;
    }

    public void setRhyId(final long rhyId) {
        this.rhyId = rhyId;
    }

    public String getShooterHunterNumber() {
        return shooterHunterNumber;
    }

    public void setShooterHunterNumber(final String shooterHunterNumber) {
        this.shooterHunterNumber = shooterHunterNumber;
    }

    public HarvestRegistryCoordinatorSearchReason getSearchReason() {
        return searchReason;
    }

    public void setSearchReason(final HarvestRegistryCoordinatorSearchReason searchReason) {
        this.searchReason = searchReason;
    }

    public HarvestRegistryRequestDTO toHarvestReqistryRequestDTO(@Nonnull final Riistanhoitoyhdistys rhy) {
        return HarvestRegistryRequestDTO.Builder.builder()
                .withPage(page)
                .withPageSize(pageSize)
                .withRhyId(rhy.getOfficialCode())
                .withAllSpecies(false)
                .withBeginDate(beginDate)
                .withEndDate(endDate)
                .withShooterHunterNumber(shooterHunterNumber)
                .withSpecies(Collections.singleton(species))
                .build();
    }

    public static final class Builder {
        private Integer page;
        private Integer pageSize;
        private long rhyId;
        private LocalDate beginDate;
        private LocalDate endDate;
        private int species;
        private String shooterHunterNumber;
        private HarvestRegistryCoordinatorSearchReason searchReason;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withPage(final Integer page) {
            this.page = page;
            return this;
        }

        public Builder withPageSize(final Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder withBeginDate(final LocalDate beginDate) {
            this.beginDate = beginDate;
            return this;
        }

        public Builder withEndDate(final LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder withSpecies(final int species) {
            this.species = species;
            return this;
        }

        public Builder withRhyId(final long rhyId) {
            this.rhyId = rhyId;
            return this;
        }

        public Builder withShooterHunterNumber(final String shooterHunterNumber) {
            this.shooterHunterNumber = shooterHunterNumber;
            return this;
        }

        public Builder withSearchReason(final HarvestRegistryCoordinatorSearchReason searchReason) {
            this.searchReason = searchReason;
            return this;
        }

        public HarvestRegistryCoordinatorRequestDTO build() {
            HarvestRegistryCoordinatorRequestDTO harvestRegistryRequestDTO = new HarvestRegistryCoordinatorRequestDTO();
            harvestRegistryRequestDTO.setPage(page);
            harvestRegistryRequestDTO.setPageSize(pageSize);
            harvestRegistryRequestDTO.setRhyId(rhyId);
            harvestRegistryRequestDTO.setBeginDate(beginDate);
            harvestRegistryRequestDTO.setEndDate(endDate);
            harvestRegistryRequestDTO.setSpecies(species);
            harvestRegistryRequestDTO.setShooterHunterNumber(shooterHunterNumber);
            harvestRegistryRequestDTO.setSearchReason(searchReason);

            return harvestRegistryRequestDTO;
        }
    }
}
