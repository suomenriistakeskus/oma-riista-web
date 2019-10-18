package fi.riista.feature.organization.occupation.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.riista.feature.organization.OrganisationType;

import java.util.List;

public class RkaListOrganisationDTO {

    private final String name;

    private final OrganisationType organisationType;

    private final String officialCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<RkaListOrganisationDTO> subOrganisations;

    public String getName() {
        return name;
    }

    public OrganisationType getOrganisationType() {
        return organisationType;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public List<RkaListOrganisationDTO> getSubOrganisations() {
        return subOrganisations;
    }

    private RkaListOrganisationDTO(Builder builder) {
        this.name = builder.name;
        this.organisationType = builder.organisationType;
        this.officialCode = builder.officialCode;
        this.subOrganisations = builder.subOrganisations;
    }

    public static final class Builder {
        private String name;
        private OrganisationType organisationType;
        private String officialCode;
        private List<RkaListOrganisationDTO> subOrganisations;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withOrganisationType(OrganisationType organisationType) {
            this.organisationType = organisationType;
            return this;
        }

        public Builder withOfficialCode(String officialCode) {
            this.officialCode = officialCode;
            return this;
        }

        public Builder withSubOrganisations(List<RkaListOrganisationDTO> subOrganisations) {
            this.subOrganisations = subOrganisations;
            return this;
        }

        public RkaListOrganisationDTO build() {
            return new RkaListOrganisationDTO(this);
        }
    }
}
