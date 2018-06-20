package fi.riista.feature.permit.application;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class HarvestPermitApplicationCreateDTO {

    @SafeHtml
    @NotBlank
    private String permitTypeCode;

    @SafeHtml
    @NotBlank
    private String applicationName;

    @NotNull
    private Integer huntingYear;

    /**
     * If moderator is creating, then id of person to whom application is created.
     * If person is creating himself, value should be null and ignored.
     */
    @Nullable
    private Long personId;

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public void setPermitTypeCode(final String permitTypeCode) {
        this.permitTypeCode = permitTypeCode;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
    }

    public Integer getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final Integer huntingYear) {
        this.huntingYear = huntingYear;
    }

    @Nullable
    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(@Nullable final Long personId) {
        this.personId = personId;
    }
}
