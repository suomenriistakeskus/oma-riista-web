package fi.riista.feature.gamediary.harvest.fields;

import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
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
import java.util.Objects;
import java.util.Optional;

public class RequiredHarvestFieldsQueryResponse {
    public static Builder builder(final RequiredHarvestFieldsQuery query) {
        return new Builder(query);
    }

    @Nonnull
    private final HarvestReportingType reportingType;

    @Nonnull
    private final RequiredHarvestFieldsDTO fields;

    private final HarvestSeasonDTO season;
    private final HarvestAreaDTO harvestArea;
    private final OrganisationNameDTO rhy;
    private final String propertyIdentifier;
    private final Map<String, String> municipalityName;

    private RequiredHarvestFieldsQueryResponse(final Builder builder) {
        this.reportingType = Objects.requireNonNull(builder.reportingType);
        this.fields = Objects.requireNonNull(builder.getFields());
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
        private final RequiredHarvestFieldsQuery query;
        private HarvestReportingType reportingType;
        private HarvestSeasonDTO season;
        private HarvestAreaDTO harvestArea;
        private OrganisationNameDTO rhy;
        private String propertyIdentifier;
        private Map<String, String> municipalityName;

        public Builder(final RequiredHarvestFieldsQuery query) {
            this.query = Objects.requireNonNull(query);
        }

        public Builder withReportingType(final HarvestReportingType reportingType) {
            this.reportingType = reportingType;
            return this;
        }

        public Builder withSeason(HarvestSeason harvestSeason) {
            this.season = harvestSeason != null ? HarvestSeasonDTO.create(harvestSeason) : null;
            return this;
        }

        public Builder withQuota(HarvestQuota quota) {
            this.harvestArea = quota != null ? HarvestAreaDTO.create(quota.getHarvestArea()) : null;
            return this;
        }

        public Builder withRhy(Riistanhoitoyhdistys rhy) {
            this.rhy = rhy != null ? OrganisationNameDTO.createWithOfficialCode(rhy) : null;
            return this;
        }

        public Builder withPropertyIdentifier(Optional<MMLRekisteriyksikonTietoja> propertyIdentifier) {
            this.propertyIdentifier = propertyIdentifier
                    .map(MMLRekisteriyksikonTietoja::getPropertyIdentifier)
                    .orElse(null);
            return this;
        }

        public Builder withPropertyIdentifier(PropertyIdentifier propertyIdentifier) {
            this.propertyIdentifier = Optional.ofNullable(propertyIdentifier)
                    .map(PropertyIdentifier::getDelimitedValue)
                    .orElse(null);
            return this;
        }

        public Builder withMunicipalityName(Municipality municipality) {
            this.municipalityName = municipality != null ? municipality.getNameLocalisation().asMap() : null;
            return this;
        }

        private RequiredHarvestFieldsDTO getFields() {
            Objects.requireNonNull(reportingType, "reportingType not set");
            final int huntingYear = DateUtil.huntingYearContaining(query.getHarvestDate());
            return RequiredHarvestFieldsDTO.create(query.getGameSpeciesCode(), huntingYear, reportingType);
        }

        public RequiredHarvestFieldsQueryResponse build() {
            Objects.requireNonNull(reportingType, "reportingType not set");

            if (reportingType == HarvestReportingType.SEASON && season == null) {
                throw new IllegalStateException("season missing");
            }

            return new RequiredHarvestFieldsQueryResponse(this);
        }
    }
}
