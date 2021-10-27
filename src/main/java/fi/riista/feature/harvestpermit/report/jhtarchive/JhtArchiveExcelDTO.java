package fi.riista.feature.harvestpermit.report.jhtarchive;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.util.LocalisedString;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;

public class JhtArchiveExcelDTO {

    private String permitNumber; // Immaterial
    private LocalisedString permitType; // Immaterial
    private HarvestReportState harvestReportState;

    private String permitDates; // Immaterial
    private LocalisedString species;

    private LocalisedString rka; // Immaterial
    private LocalisedString rhy;

    private Float permitSpecimenAmount;
    private Integer permitNestAmount;
    private Integer permitEggAmount;
    private Integer permitConstructionAmount;
    private Boolean mooselikeHuntingFinished;

    private Float applicationSpecimenAmount;
    private Integer applicationNestAmount;
    private Integer applicationEggAmount;
    private Integer applicationConstructionAmount;

    private Float harvestSpecimenAmount;
    private Integer harvestNestAmount;
    private Integer harvestEggAmount;
    private Integer harvestConstructionAmount;

    // Constructor
    // Private, use builder

    private JhtArchiveExcelDTO() {
    }

    // Accessors

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public LocalisedString getPermitType() {
        return permitType;
    }

    public void setPermitType(final LocalisedString permitType) {
        this.permitType = permitType;
    }

    public String getPermitDates() {
        return permitDates;
    }

    public void setPermitDates(final String permitDates) {
        this.permitDates = permitDates;
    }

    public HarvestReportState getHarvestReportState() {
        return harvestReportState;
    }

    public void setHarvestReportState(final HarvestReportState harvestReportState) {
        this.harvestReportState = harvestReportState;
    }

    public LocalisedString getRka() {
        return rka;
    }

    public void setRka(final LocalisedString rka) {
        this.rka = rka;
    }

    public LocalisedString getRhy() {
        return rhy;
    }

    public void setRhy(final LocalisedString rhy) {
        this.rhy = rhy;
    }

    public LocalisedString getSpecies() {
        return species;
    }

    public void setSpecies(final LocalisedString species) {
        this.species = species;
    }

    public Float getPermitSpecimenAmount() {
        return permitSpecimenAmount;
    }

    public void setPermitSpecimenAmount(final Float permitSpecimenAmount) {
        this.permitSpecimenAmount = permitSpecimenAmount;
    }

    public Integer getPermitNestAmount() {
        return permitNestAmount;
    }

    public void setPermitNestAmount(final Integer permitNestAmount) {
        this.permitNestAmount = permitNestAmount;
    }

    public Integer getPermitEggAmount() {
        return permitEggAmount;
    }

    public void setPermitEggAmount(final Integer permitEggAmount) {
        this.permitEggAmount = permitEggAmount;
    }

    public Integer getPermitConstructionAmount() {
        return permitConstructionAmount;
    }

    public void setPermitConstructionAmount(final Integer permitConstructionAmount) {
        this.permitConstructionAmount = permitConstructionAmount;
    }

    public Boolean getMooselikeHuntingFinished() {
        return mooselikeHuntingFinished;
    }

    public void setMooselikeHuntingFinished(final Boolean mooselikeHuntingFinished) {
        this.mooselikeHuntingFinished = mooselikeHuntingFinished;
    }

    public Float getApplicationSpecimenAmount() {
        return applicationSpecimenAmount;
    }

    public void setApplicationSpecimenAmount(final Float applicationSpecimenAmount) {
        this.applicationSpecimenAmount = applicationSpecimenAmount;
    }

    public Integer getApplicationNestAmount() {
        return applicationNestAmount;
    }

    public void setApplicationNestAmount(final Integer applicationNestAmount) {
        this.applicationNestAmount = applicationNestAmount;
    }

    public Integer getApplicationEggAmount() {
        return applicationEggAmount;
    }

    public void setApplicationEggAmount(final Integer applicationEggAmount) {
        this.applicationEggAmount = applicationEggAmount;
    }

    public Integer getApplicationConstructionAmount() {
        return applicationConstructionAmount;
    }

    public void setApplicationConstructionAmount(final Integer applicationConstructionAmount) {
        this.applicationConstructionAmount = applicationConstructionAmount;
    }

    public Float getHarvestSpecimenAmount() {
        return harvestSpecimenAmount;
    }

    public void setHarvestSpecimenAmount(final Float harvestSpecimenAmount) {
        this.harvestSpecimenAmount = harvestSpecimenAmount;
    }

    public Integer getHarvestNestAmount() {
        return harvestNestAmount;
    }

    public void setHarvestNestAmount(final Integer harvestNestAmount) {
        this.harvestNestAmount = harvestNestAmount;
    }

    public Integer getHarvestEggAmount() {
        return harvestEggAmount;
    }

    public void setHarvestEggAmount(final Integer harvestEggAmount) {
        this.harvestEggAmount = harvestEggAmount;
    }

    public Integer getHarvestConstructionAmount() {
        return harvestConstructionAmount;
    }

    public void setHarvestConstructionAmount(final Integer harvestConstructionAmount) {
        this.harvestConstructionAmount = harvestConstructionAmount;
    }

    // Methods

    public static Builder builder() {
        return new Builder();
    }

    // Builder

    public static class Builder {

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);

        private HarvestPermit harvestPermit;
        private HarvestPermitSpeciesAmount permitAmount;
        private GameSpecies gameSpecies;
        private HarvestPermitApplicationSpeciesAmount applicationAmount;
        private Organisation rka;
        private Organisation rhy;
        private Float harvestedSpecimenAmount;
        private Integer harvestedEggAmount;
        private Integer harvestedNestAmount;
        private Integer harvestedConstructionAmount;

        public JhtArchiveExcelDTO build() {
            final JhtArchiveExcelDTO dto = new JhtArchiveExcelDTO();

            if (harvestPermit != null) {
                dto.setPermitNumber(harvestPermit.getPermitNumber());
                dto.setPermitType(PermitTypeCode.getDecisionName(harvestPermit.getPermitTypeCode()));
                dto.setHarvestReportState(harvestPermit.getHarvestReportState());
            }

            if (permitAmount != null) {
                dto.setPermitDates(permitAmount.toString(DATE_FORMATTER));
                dto.setPermitSpecimenAmount(permitAmount.getSpecimenAmount());
                dto.setPermitNestAmount(permitAmount.getNestAmount());
                dto.setPermitEggAmount(permitAmount.getEggAmount());
                dto.setPermitConstructionAmount(permitAmount.getConstructionAmount());
                dto.setMooselikeHuntingFinished(permitAmount.isMooselikeHuntingFinished());
            }

            if (gameSpecies != null) {
                dto.setSpecies(gameSpecies.getNameLocalisation());
            }

            if (applicationAmount != null) {
                dto.setApplicationSpecimenAmount(applicationAmount.getSpecimenAmount());
                dto.setApplicationNestAmount(applicationAmount.getNestAmount());
                dto.setApplicationEggAmount(applicationAmount.getEggAmount());
                dto.setApplicationConstructionAmount(applicationAmount.getConstructionAmount());
            }

            if (rka != null) {
                dto.setRka(rka.getNameLocalisation());
            }

            if (rhy != null) {
                dto.setRhy(rhy.getNameLocalisation());
            }

            dto.setHarvestSpecimenAmount(harvestedSpecimenAmount);
            dto.setHarvestEggAmount(harvestedEggAmount);
            dto.setHarvestNestAmount(harvestedNestAmount);
            dto.setHarvestConstructionAmount(harvestedConstructionAmount);

            return dto;
        }

        public Builder withHarvestPermit(final HarvestPermit harvestPermit) {
            this.harvestPermit = harvestPermit;
            return this;
        }

        public Builder withHarvestPermitSpeciesAmount(final HarvestPermitSpeciesAmount harvestPermitSpeciesAmount) {
            this.permitAmount = harvestPermitSpeciesAmount;
            return this;
        }

        public Builder withGameSpecies(final GameSpecies gameSpecies) {
            this.gameSpecies = gameSpecies;
            return this;
        }

        public Builder withApplicationSpeciesAmount(final HarvestPermitApplicationSpeciesAmount amount) {
            this.applicationAmount = amount;
            return this;
        }

        public Builder withRka(final Organisation rka) {
            this.rka = rka;
            return this;
        }

        public Builder withRhy(final Organisation rhy) {
            this.rhy = rhy;
            return this;
        }

        public Builder withHarvestedSpecimens(final Float harvestedSpecimens) {
            this.harvestedSpecimenAmount = harvestedSpecimens;
            return this;
        }

        public Builder withHarvestedEggs(final Integer harvestedEggs) {
            this.harvestedEggAmount = harvestedEggs;
            return this;
        }

        public Builder withHarvestedNests(final Integer harvestedNests) {
            this.harvestedNestAmount = harvestedNests;
            return this;
        }

        public Builder withHarvestedConstructions(final Integer harvestedConstructions) {
            this.harvestedConstructionAmount = harvestedConstructions;
            return this;
        }
    }
}
