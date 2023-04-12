package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.organization.person.Person;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;

public class HuntingControlInspectorDTO {

    public static HuntingControlInspectorDTO create(final Person person) {
        final HuntingControlInspectorDTO dto = new HuntingControlInspectorDTO();
        dto.setId(person.getId());
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        return dto;
    }

    @NotNull
    private Long id;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String firstName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lastName;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }
}
