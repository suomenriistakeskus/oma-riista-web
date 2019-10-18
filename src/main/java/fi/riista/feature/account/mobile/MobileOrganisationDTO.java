package fi.riista.feature.account.mobile;

import fi.riista.feature.organization.Organisation;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.SafeHtml;

import java.util.Map;

public class MobileOrganisationDTO {

    public static MobileOrganisationDTO create(final Organisation org) {
        return new MobileOrganisationDTO(org.getId(), org.getNameLocalisation().asMap(), org.getOfficialCode());
    }

    private final Long id;

    private final Map<String, String> name;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private final String officialCode;

    public MobileOrganisationDTO(Long id, Map<String, String> name, String officialCode) {
        this.id = id;
        this.name = name;
        this.officialCode = officialCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final MobileOrganisationDTO dto = (MobileOrganisationDTO) o;

        return new EqualsBuilder()
                .append(id, dto.id)
                .append(name, dto.name)
                .append(officialCode, dto.officialCode)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(name)
                .append(officialCode)
                .toHashCode();
    }

    public Long getId() {
        return id;
    }

    public Map<String, String> getName() {
        return name;
    }

    public String getOfficialCode() {
        return officialCode;
    }
}
