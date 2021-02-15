package fi.riista.feature.common.decision.nomination.pdf;

import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.NominationDecisionDocumentHeadingDTO;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionDocument;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.DeliveryAddressDTO;
import fi.riista.util.F;
import fi.riista.util.Locales;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.StringUtils.hasText;

public class NominationDecisionPdfDTO {

    private final boolean swedish;
    private final String documentNumber;
    private final NominationDecisionDocument document;
    private final NominationDecisionDocumentHeadingDTO heading;
    private final PersonContactInfoDTO contactPerson;
    private final DeliveryAddressDTO deliveryAddress;
    private final LocalDateTime publishDate;

    public NominationDecisionPdfDTO(final @Nonnull String documentNumber,
                                    final @Nonnull NominationDecision decision,
                                    final @Nonnull NominationDecisionDocument document,
                                    final @Nonnull Person contactPerson) {
        requireNonNull(decision);
        requireNonNull(document);
        requireNonNull(contactPerson);

        this.swedish = Locales.isSwedish(decision.getLocale());
        this.documentNumber = requireNonNull(documentNumber);
        this.publishDate = F.mapNullable(decision.getPublishDate(), DateTime::toLocalDateTime);
        this.document = document;
        this.heading = new NominationDecisionDocumentHeadingDTO(decision.getLocale(), decision.getDecisionName());
        this.contactPerson = PersonContactInfoDTO.create(contactPerson);
        this.deliveryAddress = DeliveryAddressDTO.from(decision.getDeliveryAddress());

    }

    public boolean isIncludeProcessing() {
        return hasText(document.getProcessing());
    }

    public boolean isIncludePayment() {
        return hasText(document.getPayment());
    }

    public boolean isIncludeAttachments() {
        return hasText(document.getAttachments());
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public boolean isSwedish() {
        return swedish;
    }

    public NominationDecisionDocument getDocument() {
        return document;
    }

    public NominationDecisionDocumentHeadingDTO getHeading() {
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

}
