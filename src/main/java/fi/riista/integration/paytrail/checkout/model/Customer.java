package fi.riista.integration.paytrail.checkout.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

public class Customer {

    @Email
    private String email;

    @Length(max = 64)
    @Pattern(regexp = "[\\pL-0-9- \"\\',()\\[\\]{}*\\/+\\-_,.:&!?@#$£=*;~]*")
    private String firstName;

    @Length(max = 64)
    @Pattern(regexp = "[\\pL-0-9- \"\\',()\\[\\]{}*\\/+\\-_,.:&!?@#$£=*;~]*")
    private String lastName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Pattern(regexp = "[0-9+-]{0,64}")
    private String phone;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Length(max = 128)
    @Pattern(regexp = "[\\pL-0-9- \"\\',()\\[\\]{}*\\/+\\-_,.:&!?@#$£=*;~]*")
    private String companyName;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(final String companyName) {
        this.companyName = companyName;
    }
}
