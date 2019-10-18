package fi.riista.feature.account.area.union;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PersonalAreaUnionAddPartnerDTO {

    @NotNull
    private Long areaUnionId;

    @NotBlank
    @Size(min = 5)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String externalId;

    public PersonalAreaUnionAddPartnerDTO() {
    }

    public PersonalAreaUnionAddPartnerDTO(final Long areaUnionId, final String externalId) {
        this.areaUnionId = areaUnionId;
        this.externalId = externalId;
    }

    public Long getAreaUnionId() {
        return areaUnionId;
    }

    public String getExternalId() {
        return externalId;
    }
}
