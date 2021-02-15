package fi.riista.feature.common.decision.nomination.document;

import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;

public class NominationDecisionDocumentSectionDTO {
    public interface ContentValidation {
    }

    public interface CompleteValidation {
    }

    @NotNull(groups = {ContentValidation.class, CompleteValidation.class})
    private NominationDecisionSectionIdentifier sectionId;

    @NotNull(groups = {ContentValidation.class})
    @SafeHtml(groups = {ContentValidation.class}, whitelistType = SafeHtml.WhiteListType.BASIC)
    private String content;

    @NotNull(groups = {CompleteValidation.class})
    private Boolean complete;

    public NominationDecisionSectionIdentifier getSectionId() {
        return sectionId;
    }

    public void setSectionId(final NominationDecisionSectionIdentifier sectionId) {
        this.sectionId = sectionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public Boolean getComplete() {
        return complete;
    }

    public void setComplete(final Boolean complete) {
        this.complete = complete;
    }
}
