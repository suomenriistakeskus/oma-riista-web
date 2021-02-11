package fi.riista.feature.common.decision.authority;

import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class DecisionRkaAuthorityDetails {

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotBlank
    @Column(nullable = false)
    private String firstName;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotBlank
    @Column(nullable = false)
    private String lastName;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotBlank
    @Column(nullable = false)
    private String title;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column
    private String phoneNumber;

    @Email
    @Size(max = 255)
    @NotBlank
    @Column(nullable = false)
    private String email;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DecisionRkaAuthorityDetails details = (DecisionRkaAuthorityDetails) o;
        return firstName.equals(details.firstName) &&
                lastName.equals(details.lastName) &&
                title.equals(details.title) &&
                phoneNumber.equals(details.phoneNumber) &&
                email.equals(details.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, title, phoneNumber, email);
    }
}
