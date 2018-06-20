package fi.riista.feature.permit.invoice;

import com.google.common.base.Preconditions;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.decision.PermitDecision;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class InvoiceRecipient {

    public static InvoiceRecipient create(final @Nonnull PermitDecision permitDecision) {
        Objects.requireNonNull(permitDecision);
        final Person contactPerson = permitDecision.getContactPerson();
        final String customerNumber = Long.toString(contactPerson.getId());
        final String personFullName = contactPerson.getFullName();
        final Address address = contactPerson.getAddress();

        return new InvoiceRecipient(customerNumber, personFullName, address,
                createPermitHolder(permitDecision.getPermitHolder()));
    }

    private static PermitHolder createPermitHolder(final @Nullable HuntingClub permitHolder) {
        return permitHolder != null
                ? new PermitHolder(permitHolder.getOfficialCode(), permitHolder.getNameFinnish())
                : null;
    }

    public static class PermitHolder {

        private final String customerNumber;
        private final String name;

        public PermitHolder(final @Nonnull String customerNumber, final @Nonnull String name) {
            this.customerNumber = requireNonNull(customerNumber);
            this.name = requireNonNull(name);
            Preconditions.checkArgument(StringUtils.isNotBlank(customerNumber));
            Preconditions.checkArgument(StringUtils.isNotBlank(name));
        }

        public String formatText() {
            return String.format("%s %s", customerNumber, name);
        }
    }

    private final PermitHolder permitHolder;
    private final String customerNumber;
    private final String personFullName;
    private final String streetAddress;
    private final String postalCode;
    private final String city;

    public InvoiceRecipient(final @Nonnull String customerNumber,
                            final @Nonnull String personFullName,
                            final @Nonnull Address address,
                            final @Nullable PermitHolder permitHolder) {
        requireNonNull(address, "address is null");
        this.customerNumber = requireNonNull(customerNumber);
        this.personFullName = requireNonNull(personFullName);
        this.streetAddress = requireNonNull(address.getStreetAddress());
        this.postalCode = requireNonNull(address.getPostalCode());
        this.city = requireNonNull(address.getCity());
        this.permitHolder = permitHolder;
        Preconditions.checkArgument(StringUtils.isNotBlank(customerNumber));
        Preconditions.checkArgument(StringUtils.isNotBlank(personFullName));
        Preconditions.checkArgument(StringUtils.isNotBlank(streetAddress));
        Preconditions.checkArgument(StringUtils.isNotBlank(postalCode));
        Preconditions.checkArgument(StringUtils.isNotBlank(city));
    }

    public List<String> formatAsLines() {
        final ArrayList<String> lines = new ArrayList<>(4);

        if (permitHolder != null) {
            lines.add(permitHolder.formatText());
        }

        lines.add(personFullName);

        if (isNotBlank(streetAddress) && isNotBlank(postalCode) && isNotBlank(city)) {
            lines.add(streetAddress);
            lines.add(String.format("%s %s", postalCode, city));
        }

        return lines;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public String getInvoiceRecipientName() {
        final StringBuilder sb = new StringBuilder();

        if (permitHolder != null) {
            sb.append(permitHolder.formatText());
            sb.append(" / ");
        }

        sb.append(personFullName);

        return sb.toString();
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
}
