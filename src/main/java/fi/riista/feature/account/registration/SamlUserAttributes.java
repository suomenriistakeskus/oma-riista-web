package fi.riista.feature.account.registration;

import com.google.common.base.Preconditions;
import fi.riista.validation.FinnishSocialSecurityNumberValidator;
import org.springframework.util.StringUtils;

public class SamlUserAttributes {
    public static Builder builder() {
        return new Builder();
    }

    private final String ssn;
    private final String lastName;
    private final String firstNames;
    private final String byName;

    SamlUserAttributes(final Builder builder) {
        Preconditions.checkArgument(StringUtils.hasText(builder.ssn), "Missing SSN");
        Preconditions.checkArgument(FinnishSocialSecurityNumberValidator.isValid(builder.ssn), "Invalid SSN");
        Preconditions.checkArgument(StringUtils.hasText(builder.lastName), "Missing lastName");
        Preconditions.checkArgument(StringUtils.hasText(builder.firstNames), "Missing firstNames");

        this.ssn = builder.ssn;
        this.lastName = builder.lastName;
        this.firstNames = builder.firstNames;
        this.byName = builder.byName;
    }

    public String getSsn() {
        return ssn;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public String getByName() {
        return byName;
    }

    public static class Builder {
        private String ssn;
        private String lastName;
        private String firstNames;
        private String byName;

        private Builder() {
        }

        public Builder withSsn(final String ssn) {
            this.ssn = ssn;
            return this;
        }

        public Builder withLastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withFirstNames(final String firstNames) {
            this.firstNames = firstNames;
            return this;
        }

        public Builder withByName(final String byName) {
            this.byName = byName;
            return this;
        }

        public SamlUserAttributes build() {
            return new SamlUserAttributes(this);
        }
    }
}
