package fi.riista.feature.permit.invoice.pdf;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.decision.PermitDecision;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.permit.application.PermitHolder.PermitHolderType.PERSON;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.springframework.util.StringUtils.hasText;

public final class InvoicePdfRecipient {

    public static InvoicePdfRecipient create(final @Nonnull PermitDecision decision) {
        return create(decision.getPermitHolder(), decision.getContactPerson(), decision.getDeliveryAddress());
    }

    public static InvoicePdfRecipient create(final @Nonnull HarvestPermit permit) {
        final PermitDecision decision = permit.getPermitDecision();
        return create(decision.getPermitHolder(), permit.getOriginalContactPerson(), decision.getDeliveryAddress());
    }

    public static InvoicePdfRecipient create(final @Nonnull PermitHolder permitHolder,
                                             final @Nonnull Person person,
                                             final @Nonnull DeliveryAddress deliveryAddress) {
        requireNonNull(person);
        requireNonNull(deliveryAddress, "address is null");

        return new InvoicePdfRecipient(permitHolder, person.getId(), deliveryAddress);
    }


    private final PermitHolder permitHolder;
    private final String customerNumber;
    private final String recipient;
    private final String streetAddress;
    private final String postalCode;
    private final String city;
    private final String country;

    private InvoicePdfRecipient(final @Nonnull PermitHolder permitHolder,
                                final @Nonnull Long customerNumber,
                                final @Nonnull DeliveryAddress deliveryAddress) {

        this.permitHolder = requireNonNull(permitHolder);
        this.customerNumber = Long.toString(requireNonNull(customerNumber));
        this.recipient = deliveryAddress.getRecipient();

        requireNonNull(deliveryAddress, "address is null");
        this.streetAddress = deliveryAddress.getStreetAddress();
        this.postalCode = deliveryAddress.getPostalCode();
        this.city = deliveryAddress.getCity();
        this.country = deliveryAddress.getCountry();

        checkArgument(isNotBlank(streetAddress));
        checkArgument(isNotBlank(postalCode));
        checkArgument(isNotBlank(city));
    }

    public List<String> formatAsLines() {
        final ImmutableList.Builder<String> builder = ImmutableList.<String>builder()
                .add(recipient)
                .add(streetAddress)
                .add(String.format("%s %s", postalCode, city));

        if (hasText(country)) {
            builder.add(country);
        }

        return builder.build();
    }

    public List<String> formatAsLinesWithPermitHolder() {
        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        if (permitHolder.getType() != PERSON) {
            builder.add(String.format("%s %s", permitHolder.getCode(), permitHolder.getName()));
        }

        return builder.addAll(formatAsLines()).build();
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }
}
