package fi.riista.feature.permit.invoice;

import fi.riista.feature.organization.address.Address;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.io.Serializable;

import static java.util.Objects.requireNonNull;

public class InvoiceContactDetailsDTO implements Serializable {

    @Nonnull
    public static InvoiceContactDetailsDTO create(@Nonnull final Invoice invoice,
                                                  @Nonnull final Address recipientAddress) {
        requireNonNull(invoice, "invoice is null");
        requireNonNull(recipientAddress, "recipientAddress is null");

        return create(
                invoice.getRecipientName(),
                recipientAddress.getStreetAddress(),
                String.format("%s %s",
                        StringUtils.trimToEmpty(recipientAddress.getPostalCode()),
                        StringUtils.trimToEmpty(recipientAddress.getCity())), "");
    }

    @Nonnull
    public static InvoiceContactDetailsDTO create(final String name,
                                                  final String addressLine1,
                                                  final String addressLine2,
                                                  final String phoneNumber) {

        return new InvoiceContactDetailsDTO(name, addressLine1, addressLine2, phoneNumber);
    }

    private String name;
    private String addressLine1;
    private String addressLine2;
    private String phoneNumber;

    public InvoiceContactDetailsDTO(final String name,
                                    final String addressLine1,
                                    final String addressLine2,
                                    final String phoneNumber) {
        this.name = name;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(final String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(final String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
