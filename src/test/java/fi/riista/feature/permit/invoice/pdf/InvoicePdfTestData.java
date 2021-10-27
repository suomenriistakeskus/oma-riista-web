package fi.riista.feature.permit.invoice.pdf;

import fi.riista.feature.common.money.FinnishBankAccount;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceType;

import java.math.BigDecimal;
import java.util.Locale;

import static fi.riista.feature.permit.invoice.CreditorReferenceCalculator.computeReferenceForPermitDecisionProcessingInvoice;
import static fi.riista.feature.permit.invoice.CreditorReferenceCalculator.computeReferenceForPermitHarvestInvoice;
import static fi.riista.feature.permit.invoice.harvest.PermitHarvestInvoiceAccounts.PRIMARY_HARVEST_FEE_ACCOUNT;
import static fi.riista.util.DateUtil.now;
import static fi.riista.util.DateUtil.today;
import static java.util.Objects.requireNonNull;

public final class InvoicePdfTestData {

    public static GameSpecies createMoose() {
        final GameSpecies gameSpecies = new GameSpecies();
        gameSpecies.setOfficialCode(GameSpecies.OFFICIAL_CODE_MOOSE);
        gameSpecies.setNameFinnish("Hirvi");
        gameSpecies.setNameSwedish("Älg");
        return gameSpecies;
    }

    public static Riistanhoitoyhdistys createRhy() {
        final Riistanhoitoyhdistys rhy = new Riistanhoitoyhdistys();
        rhy.setOfficialCode("368");
        rhy.setNameFinnish("Nokian seudun riistanhoitoyhdistys");
        rhy.setNameSwedish("Nokianejdens jaktvårdsförening");
        return rhy;
    }

    public static HuntingClub createClub() {
        return createClub(createRhy());
    }

    public static HuntingClub createClub(final Riistanhoitoyhdistys rhy) {
        final String clubName = "Hirvenmetsästäjät ry";
        return new HuntingClub(rhy, clubName, clubName, "12345678");
    }

    public static Person createContactPerson() {
        final Address address = new Address();
        address.setStreetAddress("Katu 123");
        address.setPostalCode("33700");
        address.setCity("Tampere");

        final Person contactPerson = new Person();
        contactPerson.setId(123456L);
        contactPerson.setFirstName("Erkki");
        contactPerson.setLastName("Esimerkki");
        contactPerson.setMrAddress(address);

        return contactPerson;
    }

    public static HarvestPermitApplication createApplication(final Locale locale) {
        final Riistanhoitoyhdistys rhy = createRhy();
        final HuntingClub club = createClub(rhy);
        final Person contactPerson = createContactPerson();
        final HarvestPermitApplication application = new HarvestPermitApplication();
        application.setApplicationYear(2018);
        application.setApplicationNumber(20_000_001);
        application.setRhy(rhy);
        application.setHuntingClub(club);
        application.setHarvestPermitCategory(HarvestPermitCategory.MOOSELIKE);
        application.setPermitHolder(PermitHolder.createHolderForClub(club));
        application.setContactPerson(contactPerson);
        application.setLocale(locale);
        application.setDecisionLocale(locale);
        application.setDeliveryAddress(DeliveryAddress.createFromPersonNullable(contactPerson));

        return application;
    }

    public static PermitDecision createDecision(final HarvestPermitApplication application) {
        requireNonNull(application);

        final PermitDecision permitDecision = PermitDecision.createForApplication(application);
        permitDecision.setPublishDate(now().minusDays(3));
        return permitDecision;
    }

    public static PermitDecision createDecision(final Locale locale) {
        return createDecision(createApplication(locale));
    }

    public static Invoice createProcessingInvoice(final PermitDecision decision) {
        requireNonNull(decision);

        final Invoice invoice = new Invoice(InvoiceType.PERMIT_PROCESSING, false);
        invoice.setInvoiceNumber(200_000);
        // Should not use current date for testing purposes.
        invoice.updateInvoiceAndDueDate(decision.getPublishDate().toLocalDate());
        invoice.setIbanAndBic(FinnishBankAccount.PERMIT_DECISION_FEE_NORDEA);
        invoice.setAmount(decision.getPaymentAmount());
        invoice.setCreditorReference(computeReferenceForPermitDecisionProcessingInvoice(
                decision.getDecisionYear(), decision.getDecisionNumber()));

        final DeliveryAddress deliveryAddress = requireNonNull(decision.getDeliveryAddress());
        invoice.setRecipientName(deliveryAddress.getRecipient());
        invoice.setRecipientAddress(deliveryAddress.toAddress());

        return invoice;
    }

    public static HarvestPermitSpeciesAmount createSpeciesAmount(final PermitDecision decision,
                                                                 final GameSpecies species) {
        requireNonNull(decision, "decision is null");
        requireNonNull(species, "species is null");

        final String permitNumber = decision.createPermitNumber();
        final HarvestPermit permit = HarvestPermit.create(permitNumber);
        permit.setPermitDecision(decision);
        permit.setRhy(decision.getRhy());
        permit.setOriginalContactPerson(decision.getContactPerson());
        permit.setPermitHolder(decision.getPermitHolder());
        permit.setHuntingClub(decision.getHuntingClub());
        permit.setPermitType(decision.getPermitTypeCode());
        final HarvestPermitSpeciesAmount speciesAmount = new HarvestPermitSpeciesAmount();
        speciesAmount.setHarvestPermit(permit);
        speciesAmount.setGameSpecies(species);
        return speciesAmount;
    }

    public static Invoice createHarvestInvoice(final HarvestPermitSpeciesAmount speciesAmount) {
        requireNonNull(speciesAmount, "speciesAmount is null");

        final HarvestPermit permit = requireNonNull(speciesAmount.getHarvestPermit(), "permit is null");
        final PermitDecision decision = requireNonNull(permit.getPermitDecision(), "decision is null");

        final Invoice invoice = new Invoice(InvoiceType.PERMIT_HARVEST, true);
        invoice.setInvoiceNumber(200_000);
        // Should not use current date for testing purposes.
        invoice.updateInvoiceAndDueDate(today().minusDays(2));
        invoice.setIbanAndBic(PRIMARY_HARVEST_FEE_ACCOUNT);
        invoice.setAmount(new BigDecimal("120.00"));

        final HarvestPermitApplication application = requireNonNull(decision.getApplication(), "application is null");
        final GameSpecies species = requireNonNull(speciesAmount.getGameSpecies(), "species is null");

        invoice.setCreditorReference(computeReferenceForPermitHarvestInvoice(
                decision.getDecisionYear(), decision.getDecisionNumber(), species.getOfficialCode()));

        final DeliveryAddress deliveryAddress = requireNonNull(decision.getDeliveryAddress());
        invoice.setRecipientName(deliveryAddress.getRecipient());
        invoice.setRecipientAddress(deliveryAddress.toAddress());

        return invoice;
    }

    private InvoicePdfTestData() {
        throw new AssertionError();
    }
}
