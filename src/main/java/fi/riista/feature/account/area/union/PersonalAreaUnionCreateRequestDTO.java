package fi.riista.feature.account.area.union;

import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;

public class PersonalAreaUnionCreateRequestDTO {

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    @NotNull
    private Integer huntingYear;

    public PersonalAreaUnionCreateRequestDTO() {

    }

    public PersonalAreaUnionCreateRequestDTO(final String name, final int huntingYear) {
        this.name = name;
        this.huntingYear = huntingYear;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final Integer huntingYear) {
        this.huntingYear = huntingYear;
    }
}
