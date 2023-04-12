package fi.riista.feature.organization.rhy.huntingcontrolevent.mobile;

import fi.riista.feature.organization.person.Person;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;

public class MobileHuntingControlInspectorDTO {

    @NotNull
    private Long id;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String firstName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lastName;

    // Constructors / factories

    static MobileHuntingControlInspectorDTO create(final Person person) {
        final MobileHuntingControlInspectorDTO dto = new MobileHuntingControlInspectorDTO();
        dto.setId(person.getId());
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        return dto;
    }

     // Accessors -->

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
