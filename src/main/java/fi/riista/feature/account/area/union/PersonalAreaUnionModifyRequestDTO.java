package fi.riista.feature.account.area.union;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

public class PersonalAreaUnionModifyRequestDTO {

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    public PersonalAreaUnionModifyRequestDTO() {

    }

    public PersonalAreaUnionModifyRequestDTO(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
