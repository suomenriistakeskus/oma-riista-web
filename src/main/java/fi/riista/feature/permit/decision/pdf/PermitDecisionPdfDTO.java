package fi.riista.feature.permit.decision.pdf;

import fi.riista.feature.common.entity.Has2BeginEndDatesDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.document.PermitDecisionDocumentHeadingDTO;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.util.F;
import fi.riista.util.Locales;
import org.joda.time.LocalDateTime;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class PermitDecisionPdfDTO {
    public static class SpeciesAmount extends Has2BeginEndDatesDTO {
        public SpeciesAmount(@Nonnull final HarvestPermitApplicationSpeciesAmount spa) {
            Objects.requireNonNull(spa, "speciesAmount must not be null");

            this.id = spa.getId();
            this.speciesName = spa.getGameSpecies().getNameFinnish();
            this.amount = spa.getAmount();
        }

        public SpeciesAmount(@Nonnull final PermitDecisionSpeciesAmount speciesAmount) {
            Objects.requireNonNull(speciesAmount, "speciesAmount must not be null");
            super.copyDatesFrom(speciesAmount);

            this.id = speciesAmount.getId();
            this.speciesName = speciesAmount.getGameSpecies().getNameFinnish();
            this.amount = speciesAmount.getAmount();
            this.restrictionType = speciesAmount.getRestrictionType();
            this.restrictionAmount = speciesAmount.getRestrictionAmount();
        }

        private Long id;
        private String speciesName;
        private float amount;
        private PermitDecisionSpeciesAmount.RestrictionType restrictionType;
        private Float restrictionAmount;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSpeciesName() {
            return speciesName;
        }

        public float getAmount() {
            return amount;
        }

        public PermitDecisionSpeciesAmount.RestrictionType getRestrictionType() {
            return restrictionType;
        }

        public Float getRestrictionAmount() {
            return restrictionAmount;
        }
    }

    private final boolean swedish;
    private final String permitNumber;
    private final PermitDecisionDocument document;
    private final PermitDecisionDocumentHeadingDTO heading;
    private final OrganisationNameDTO permitHolder;
    private final PersonContactInfoDTO contactPerson;
    private final LocalDateTime publishDate;
    private final List<SpeciesAmount> decisionSpeciesAmounts;
    private final List<SpeciesAmount> applicationSpeciesAmounts;
    private PermitDecision.GrantStatus grantStatus;

    public boolean isIncludeNotificationObligation() {
        return !isRejected() && StringUtils.hasText(document.getNotificationObligation());
    }

    public boolean isIncludeRestriction() {
        return !isRejected() && (StringUtils.hasText(document.getRestriction()) || StringUtils.hasText(document.getRestrictionExtra()));
    }

    public boolean isIncludeExecution() {
        return !isRejected();
    }

    public PermitDecisionPdfDTO(final @Nonnull PermitDecision decision,
                                final @Nonnull PermitDecisionDocument document) {
        Objects.requireNonNull(decision);
        Objects.requireNonNull(document);
        Objects.requireNonNull(decision.getContactPerson());
        Objects.requireNonNull(decision.getApplication());

        this.swedish = Locales.isSwedish(decision.getLocale());
        this.permitNumber = decision.getApplication().getPermitNumber();
        this.publishDate = decision.getPublishDate() != null ? decision.getPublishDate().toLocalDateTime() : null;
        this.document = document;
        this.heading = new PermitDecisionDocumentHeadingDTO(decision.getLocale());
        this.contactPerson = PersonContactInfoDTO.create(decision.getContactPerson());
        this.permitHolder = decision.getPermitHolder() != null
                ? OrganisationNameDTO.create(decision.getApplication().getPermitHolder())
                : null;

        this.decisionSpeciesAmounts = F.mapNonNullsToList(decision.getSpeciesAmounts(), SpeciesAmount::new);
        this.applicationSpeciesAmounts = F.mapNonNullsToList(decision.getApplication().getSpeciesAmounts(), SpeciesAmount::new);
        this.grantStatus = decision.getGrantStatus();
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public boolean isSwedish() {
        return swedish;
    }

    public PermitDecisionDocument getDocument() {
        return document;
    }

    public OrganisationNameDTO getPermitHolder() {
        return permitHolder;
    }

    public PermitDecisionDocumentHeadingDTO getHeading() {
        return heading;
    }

    public PersonContactInfoDTO getContactPerson() {
        return contactPerson;
    }

    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public List<SpeciesAmount> getDecisionSpeciesAmounts() {
        return decisionSpeciesAmounts;
    }

    public List<SpeciesAmount> getApplicationSpeciesAmounts() {
        return applicationSpeciesAmounts;
    }

    public boolean isRejected() {
        return PermitDecision.GrantStatus.REJECTED.equals(grantStatus);
    }
}
