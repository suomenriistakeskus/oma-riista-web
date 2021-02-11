package fi.riista.feature.permit.application.create;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class HarvestPermitApplicationCreateDTO {

    @NotNull
    private HarvestPermitCategory category;

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

    public HarvestPermitCategory getCategory() {
        return category;
    }

    public void setCategory(HarvestPermitCategory category) {
        this.category = category;
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
