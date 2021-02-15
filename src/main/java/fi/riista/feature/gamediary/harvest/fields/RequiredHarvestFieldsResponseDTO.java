package fi.riista.feature.gamediary.harvest.fields;

import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.harvestpermit.season.HarvestAreaDTO;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.harvestpermit.season.HarvestSeasonDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;
import fi.riista.util.DateUtil;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class RequiredHarvestFieldsResponseDTO {

    public static Builder builder(@Nonnull final RequiredHarvestFieldsRequestDTO request,
                                  @Nonnull final HarvestSpecVersion specVersion,
                                  final boolean isDeerPilotEnabled) {

        return new Builder(request, specVersion, isDeerPilotEnabled);
    }

    private final HarvestReportingType reportingType;

    private final RequiredHarvestFieldsDTO fields;

    private final HarvestSeasonDTO season;
    private final HarvestAreaDTO harvestArea;
    private final OrganisationNameDTO rhy;
    private final String propertyIdentifier;
    private final Map<String, String> municipalityName;

    private RequiredHarvestFieldsResponseDTO(final Builder builder) {
        this.reportingType = requireNonNull(builder.reportingType);

        this.fields = new RequiredHarvestFieldsDTO(builder.getReportFields(), builder.getSpecimenFields());

        this.season = builder.season;
        this.harvestArea = builder.harvestArea;
        this.rhy = builder.rhy;
        this.propertyIdentifier = builder.propertyIdentifier;
        this.municipalityName = builder.municipalityName;
    }

    public HarvestReportingType getReportingType() {
        return reportingType;
    }

    public RequiredHarvestFieldsDTO getFields() {
        return fields;
    }

    public HarvestSeasonDTO getSeason() {
        return season;
    }

    public HarvestAreaDTO getHarvestArea() {
        return harvestArea;
    }

    public OrganisationNameDTO getRhy() {
        return rhy;
    }

    public String getPropertyIdentifier() {
        return propertyIdentifier;
    }

    public Map<String, String> getMunicipalityName() {
        return municipalityName;
    }

    public static final class Builder {

        private final RequiredHarvestFieldsRequestDTO request;
        private final HarvestSpecVersion specVersion;
        private final boolean isDeerPilotEnabled;

        private HarvestReportingType reportingType;
        private HarvestSeasonDTO season;
        private HarvestAreaDTO harvestArea;
        private OrganisationNameDTO rhy;
        private String propertyIdentifier;
        private Map<String, String> municipalityName;

        public Builder(@Nonnull final RequiredHarvestFieldsRequestDTO request,
                       @Nonnull final HarvestSpecVersion specVersion,
                       final boolean isDeerPilotEnabled) {

            this.request = requireNonNull(request);
            this.specVersion = requireNonNull(specVersion);
            this.isDeerPilotEnabled = isDeerPilotEnabled;
        }

        public Builder withReportingType(final HarvestReportingType reportingType) {
            this.reportingType = reportingType;
            return this;
        }

        public Builder withSeason(final HarvestSeason harvestSeason) {
            this.season = harvestSeason != null ? HarvestSeasonDTO.create(harvestSeason) : null;
            return this;
        }

        public Builder withQuota(final HarvestQuota quota) {
            this.harvestArea = quota != null ? HarvestAreaDTO.create(quota.getHarvestArea()) : null;
            return this;
        }

        public Builder withRhy(final Riistanhoitoyhdistys rhy) {
            this.rhy = rhy != null ? OrganisationNameDTO.createWithOfficialCode(rhy) : null;
            return this;
        }

        public Builder withPropertyIdentifier(final Optional<MMLRekisteriyksikonTietoja> propertyIdentifier) {
            this.propertyIdentifier = propertyIdentifier
                    .map(MMLRekisteriyksikonTietoja::getPropertyIdentifier)
                    .orElse(null);
            return this;
        }

        public Builder withPropertyIdentifier(final PropertyIdentifier propertyIdentifier) {
            this.propertyIdentifier = Optional
                    .ofNullable(propertyIdentifier)
                    .map(PropertyIdentifier::getDelimitedValue)
                    .orElse(null);
            return this;
        }

        public Builder withMunicipalityName(final Municipality municipality) {
            this.municipalityName = municipality != null ? municipality.getNameLocalisation().asMap() : null;
            return this;
        }

        private RequiredHarvestReportFieldsDTO getReportFields() {
            requireNonNull(reportingType, "reportingType not set");

            final int huntingYear = DateUtil.huntingYearContaining(request.getHarvestDate());
            return RequiredHarvestReportFieldsDTO
                    .create(request.getGameSpeciesCode(), huntingYear, reportingType, isDeerPilotEnabled);
        }

        private RequiredHarvestSpecimenFieldsDTO getSpecimenFields() {
            requireNonNull(reportingType, "reportingType not set");

            final int huntingYear = DateUtil.huntingYearContaining(request.getHarvestDate());

            return RequiredHarvestSpecimenFieldsDTO.create(
                    request.getGameSpeciesCode(), huntingYear, reportingType, specVersion);
        }

        public RequiredHarvestFieldsResponseDTO build() {
            requireNonNull(reportingType, "reportingType not set");

            if (reportingType == HarvestReportingType.SEASON && season == null) {
                throw new IllegalStateException("season missing");
            }

            return new RequiredHarvestFieldsResponseDTO(this);
        }
    }
}
