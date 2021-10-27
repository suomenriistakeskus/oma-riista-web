package fi.riista.feature.harvestregistry;

import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Set;

public class HarvestRegistryRequestDTO {

    @NotNull
    @Min(0)
    private Integer page;

    @NotNull
    @Min(1)
    private Integer pageSize;

    @NotNull
    private LocalDate beginDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private Boolean allSpecies;

    @NotNull
    private Set<Integer> species;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String municipalityCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String rkaCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String rhyCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String shooterHunterNumber;


    public LocalDate getBeginDate() {
        return beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isAllSpecies() {
        return allSpecies;
    }

    public Set<Integer> getSpecies() {
        return species;
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

    public void setAllSpecies(final boolean allSpecies) {
        this.allSpecies = allSpecies;
    }

    public void setSpecies(final Set<Integer> species) {
        this.species = species;
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(final String municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public String getRkaCode() {
        return rkaCode;
    }

    public void setRkaCode(final String rkaCode) {
        this.rkaCode = rkaCode;
    }

    public String getRhyCode() {
        return rhyCode;
    }

    public void setRhyCode(final String rhyCode) {
        this.rhyCode = rhyCode;
    }

    public String getShooterHunterNumber() {
        return shooterHunterNumber;
    }

    public void setShooterHunterNumber(final String shooterHunterNumber) {
        this.shooterHunterNumber = shooterHunterNumber;
    }


    public static final class Builder {
        private Integer page;
        private Integer pageSize;
        private LocalDate beginDate;
        private LocalDate endDate;
        private Boolean allSpecies;
        private Set<Integer> species;
        private String municipalityCode;
        private String rkaCode;
        private String rhyCode;
        private String shooterHunterNumber;

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

        public Builder withAllSpecies(final Boolean allSpecies) {
            this.allSpecies = allSpecies;
            return this;
        }

        public Builder withSpecies(final Set<Integer> species) {
            this.species = species;
            return this;
        }

        public Builder withMunicipalityCode(final String municipalityCode) {
            this.municipalityCode = municipalityCode;
            return this;
        }

        public Builder withRkaCode(final String rkaCode) {
            this.rkaCode = rkaCode;
            return this;
        }

        public Builder withRhyId(final String rhyCode) {
            this.rhyCode = rhyCode;
            return this;
        }

        public Builder withShooterHunterNumber(final String shooterHunterNumber) {
            this.shooterHunterNumber = shooterHunterNumber;
            return this;
        }

        public HarvestRegistryRequestDTO build() {
            HarvestRegistryRequestDTO harvestRegistryRequestDTO = new HarvestRegistryRequestDTO();
            harvestRegistryRequestDTO.setPage(page);
            harvestRegistryRequestDTO.setPageSize(pageSize);
            harvestRegistryRequestDTO.setBeginDate(beginDate);
            harvestRegistryRequestDTO.setEndDate(endDate);
            harvestRegistryRequestDTO.setAllSpecies(allSpecies);
            harvestRegistryRequestDTO.setSpecies(species);
            harvestRegistryRequestDTO.setMunicipalityCode(municipalityCode);
            harvestRegistryRequestDTO.setRkaCode(rkaCode);
            harvestRegistryRequestDTO.setRhyCode(rhyCode);
            harvestRegistryRequestDTO.setShooterHunterNumber(shooterHunterNumber);
            return harvestRegistryRequestDTO;
        }
    }
}
