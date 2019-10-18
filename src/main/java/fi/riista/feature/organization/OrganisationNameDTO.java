package fi.riista.feature.organization;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.util.LocalisedString;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
