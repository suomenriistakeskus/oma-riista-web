package fi.riista.feature.permit.decision.pdf;

import fi.riista.feature.common.dto.Has2BeginEndDatesDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.DeliveryAddressDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.document.PermitDecisionDocumentHeadingDTO;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.util.F;
import fi.riista.util.Locales;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import static org.springframework.util.StringUtils.hasText;

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
        private final String speciesName;
        private final float amount;
        private PermitDecisionSpeciesAmount.RestrictionType restrictionType;
        private Float restrictionAmount;

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
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
    private final PermitHolderDTO permitHolder;
    private final OrganisationNameDTO huntingClub;
    private final PersonContactInfoDTO contactPerson;
    private final DeliveryAddressDTO deliveryAddress;
    private final LocalDateTime publishDate;
    private final List<SpeciesAmount> decisionSpeciesAmounts;
    private final List<SpeciesAmount> applicationSpeciesAmounts;
    private final PermitDecision.GrantStatus grantStatus;

    public boolean isIncludeApplicationReasoning() {
        return hasText(document.getApplicationReasoning());
    }

    public boolean isIncludeProcessing() {
        return hasText(document.getProcessing());
    }

    public boolean isIncludeNotificationObligation() {
        return !isRejected() && hasText(document.getNotificationObligation());
    }

    public boolean isIncludeRestriction() {
        return !isRejected() && (hasText(document.getRestriction()) || hasText(document.getRestrictionExtra()));
    }

    public boolean isIncludeExecution() {
        return !isRejected() && hasText(document.getExecution());
    }

    public boolean isIncludePayment() {
        return hasText(document.getPayment());
    }

    public boolean isIncludeAttachments() {
        return hasText(document.getAttachments());
    }

    public PermitDecisionPdfDTO(final @Nonnull String permitNumber,
                                final @Nonnull PermitDecision decision,
                                final @Nonnull PermitDecisionDocument document,
                                final @Nonnull List<PermitDecisionSpeciesAmount> decisionSpeciesAmounts,
                                final @Nonnull List<HarvestPermitApplicationSpeciesAmount> applicationSpeciesAmounts) {
        Objects.requireNonNull(decision);
        Objects.requireNonNull(document);
        Objects.requireNonNull(decision.getContactPerson());
        Objects.requireNonNull(decision.getApplication());

        this.swedish = Locales.isSwedish(decision.getLocale());
        this.permitNumber = Objects.requireNonNull(permitNumber);
        this.publishDate = decision.getPublishDate() != null ? decision.getPublishDate().toLocalDateTime() : null;
        this.document = document;
        this.heading = new PermitDecisionDocumentHeadingDTO(decision.getLocale(), decision.getDecisionName());
        this.contactPerson = PersonContactInfoDTO.create(decision.getContactPerson());
        this.deliveryAddress = DeliveryAddressDTO.from(decision.getDeliveryAddress());
        this.permitHolder = PermitHolderDTO.createFrom(decision.getPermitHolder());
        this.huntingClub = decision.getHuntingClub() != null
                ? OrganisationNameDTO.create(decision.getHuntingClub())
                : null;

        this.decisionSpeciesAmounts = F.mapNonNullsToList(decisionSpeciesAmounts, SpeciesAmount::new);
        this.applicationSpeciesAmounts = F.mapNonNullsToList(applicationSpeciesAmounts, SpeciesAmount::new);
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

    public PermitHolderDTO getPermitHolder() {
        return permitHolder;
    }

    public OrganisationNameDTO getHuntingClub() {
        return huntingClub;
    }

    public PermitDecisionDocumentHeadingDTO getHeading() {
        return heading;
    }

    public PersonContactInfoDTO getContactPerson() {
        return contactPerson;
    }

    public DeliveryAddressDTO getDeliveryAddress() {
        return deliveryAddress;
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
