package fi.riista.feature.permit.invoice.pdf;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.decision.PermitDecision;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public final class InvoicePdfRecipient {

    public static InvoicePdfRecipient create(final @Nonnull PermitDecision decision) {
        return create(decision.getContactPerson(), decision.getDeliveryAddress());
    }

    public static InvoicePdfRecipient create(final @Nonnull HarvestPermit permit) {
        return create(permit.getOriginalContactPerson(), permit.getPermitDecision().getDeliveryAddress());
    }

    public static InvoicePdfRecipient create(final @Nonnull Person person, final @Nonnull DeliveryAddress deliveryAddress) {
        requireNonNull(person);
        requireNonNull(deliveryAddress, "address is null");

        return new InvoicePdfRecipient(person.getId(), deliveryAddress);
    }


    private final String customerNumber;
    private final String recipient;
    private final String streetAddress;
    private final String postalCode;
    private final String city;
    private final String country;

    private InvoicePdfRecipient(final @Nonnull Long customerNumber, final @Nonnull DeliveryAddress deliveryAddress) {

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

        if (StringUtils.hasText(country)) {
            builder.add(country);
        }

        return builder.build();
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
