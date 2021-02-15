package fi.riista.feature.organization;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.util.LocalisedString;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static java.lang.String.format;

public class OrganisationNameDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String officialCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameFI;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameSV;

    public static OrganisationNameDTO create(@Nonnull final Organisation organisation) {
        return new OrganisationNameDTO(organisation);
    }

    public static OrganisationNameDTO createWithOfficialCode(@Nullable final Organisation organisation) {
        if (organisation == null) {
            return null;
        }

        final OrganisationNameDTO dto = new OrganisationNameDTO(organisation);
        dto.setOfficialCode(organisation.getOfficialCode());
        return dto;
    }

    public OrganisationNameDTO() {
    }

    public OrganisationNameDTO(@Nonnull final Organisation organisation) {
        setId(organisation.getId());
        setRev(organisation.getConsistencyVersion());
        setNameFI(organisation.getNameFinnish());
        setNameSV(organisation.getNameSwedish());
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof OrganisationNameDTO)) {
            return false;
        } else {
            final OrganisationNameDTO that = (OrganisationNameDTO) o;

            return Objects.equals(this.id, that.id)
                    && Objects.equals(this.rev, that.rev)
                    && Objects.equals(this.officialCode, that.officialCode)
                    && Objects.equals(this.nameFI, that.nameFI)
                    && Objects.equals(this.nameSV, that.nameSV);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rev, officialCode, nameFI, nameSV);
    }

    @Override
    public String toString() {
        return format("{ id: %s, rev: %s, officialCode: %s, name: %s }", id, rev, officialCode, getNameLocalisation());
    }

    public LocalisedString getNameLocalisation() {
        return LocalisedString.of(nameFI, nameSV);
    }

    // Accessors -->

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public void setOfficialCode(final String officialCode) {
        this.officialCode = officialCode;
    }

    public String getNameFI() {
        return nameFI;
    }

    public void setNameFI(final String nameFI) {
        this.nameFI = nameFI;
    }

    public String getNameSV() {
        return nameSV;
    }

    public void setNameSV(final String nameSV) {
        this.nameSV = nameSV;
    }

}
