package fi.riista.feature.harvestpermit.report.paper;

import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.util.F;
import fi.riista.util.RiistakeskusConstants;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class PermitHarvestReportModel {
    public static PermitHarvestReportModel create(final PermitDecision permitDecision,
                                                  final List<PermitDecisionSpeciesAmount> speciesAmountList) {
        final List<String> recipientAddress = buildRecipientAddressLines(permitDecision);
        final List<SpeciesAndPermitNumber> speciesList = buildSpeciesList(permitDecision, speciesAmountList);

        return new PermitHarvestReportModel(speciesList, recipientAddress,
                permitDecision.getLocale(), permitDecision.getPermitTypeCode());
    }

    private static List<SpeciesAndPermitNumber> buildSpeciesList(
            final PermitDecision permitDecision,
            final List<PermitDecisionSpeciesAmount> speciesAmountList) {
        final LinkedList<SpeciesAndPermitNumber> speciesList = new LinkedList<>();

        speciesAmountList.stream()
                .filter(spa -> spa.getAmount() > 0)
                .collect(groupingBy(PermitDecisionSpeciesAmount::getPermitYear, toList()))
                .forEach((year, amountList) -> speciesList.addAll(buildSpeciesListForYear(permitDecision, year,
                        amountList)));

        speciesList.sort(comparing(SpeciesAndPermitNumber::getYear)
                .thenComparing(SpeciesAndPermitNumber::getGameSpeciesCode));

        return speciesList;
    }

    private static List<SpeciesAndPermitNumber> buildSpeciesListForYear(final PermitDecision permitDecision,
                                                                        final Integer year,
                                                                        final List<PermitDecisionSpeciesAmount> source) {
        return F.mapNonNullsToList(source, spa -> {
            final int speciesCode = spa.getGameSpecies().getOfficialCode();
            final String permitNumber = permitDecision.createPermitNumber(year);

            return new SpeciesAndPermitNumber(speciesCode, permitNumber, year);
        });
    }

    private static List<String> buildRecipientAddressLines(final PermitDecision decision) {
        final LinkedList<String> lineList = new LinkedList<>();

        final DeliveryAddress deliveryAddress = decision.getDeliveryAddress();
        lineList.add(deliveryAddress.getRecipient());
        lineList.add(deliveryAddress.getStreetAddress());
        lineList.add(deliveryAddress.getPostalCode() + " " + deliveryAddress.getCity());

        final Person contactPerson = decision.getContactPerson();

        if (StringUtils.hasText(contactPerson.getPhoneNumber())) {
            lineList.add(contactPerson.getPhoneNumber());
        }

        if (StringUtils.hasText(contactPerson.getEmail())) {
            lineList.add(contactPerson.getEmail());
        }

        return lineList;
    }

    public static class SpeciesAndPermitNumber {
        private final int gameSpeciesCode;
        private final String permitNumber;
        private final int year;

        public SpeciesAndPermitNumber(final int gameSpeciesCode, final String permitNumber, final int year) {
            this.gameSpeciesCode = gameSpeciesCode;
            this.permitNumber = permitNumber;
            this.year = year;
        }

        public int getGameSpeciesCode() {
            return gameSpeciesCode;
        }

        public String getPermitNumber() {
            return permitNumber;
        }

        public int getYear() {
            return year;
        }
    }

    private final List<SpeciesAndPermitNumber> speciesList;
    private final List<String> recipientAddress;
    private final Locale locale;
    private final String permitTypeCode;

    public PermitHarvestReportModel(final List<SpeciesAndPermitNumber> speciesList,
                                    final List<String> recipientAddress,
                                    final Locale locale,
                                    final String permitTypeCode) {
        this.speciesList = requireNonNull(speciesList);
        this.recipientAddress = requireNonNull(recipientAddress);
        this.locale = requireNonNull(locale);
        this.permitTypeCode = requireNonNull(permitTypeCode);
    }

    public List<SpeciesAndPermitNumber> getSpeciesList() {
        return speciesList;
    }

    public List<String> getRecipientAddress() {
        return recipientAddress;
    }

    public List<String> getSenderAddress() {
        return Arrays.asList(
                RiistakeskusConstants.NAME.getTranslation(locale),
                RiistakeskusConstants.STREET_ADDRESS.getTranslation(locale),
                RiistakeskusConstants.POST_OFFICE.getTranslation(locale));
    }

    public String getSenderPhoneNumber() {
        return RiistakeskusConstants.PHONE_NUMBER;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }
}
