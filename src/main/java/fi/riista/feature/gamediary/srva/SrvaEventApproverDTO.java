package fi.riista.feature.gamediary.srva;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.person.Person;
import org.hibernate.validator.constraints.SafeHtml;

public class SrvaEventApproverDTO {

    public static SrvaEventApproverDTO create(final SystemUser user) {
        final SrvaEventApproverDTO dto = new SrvaEventApproverDTO();

        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());

        return dto;
    }

    public static SrvaEventApproverDTO create(final Person person) {
        final SrvaEventApproverDTO dto = new SrvaEventApproverDTO();

        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());

        return dto;
    }

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String firstName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lastName;

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
