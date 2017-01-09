package fi.riista.feature.harvestpermit.report.excel;

import com.google.common.base.Joiner;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.HuntingAreaType;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.Localiser;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * This DTO is used for Excel export file generation from UI
 */
public class HarvestReportExportExcelDTO {

    public static List<HarvestReportExportExcelDTO> create(final Collection<HarvestReport> harvestReports,
                                                           final EnumLocaliser enumLocaliser) {
        return Objects.requireNonNull(harvestReports, "harvestReports is null").stream()
                .filter(Objects::nonNull)
                .map(HarvestReport::getHarvests)
                .flatMap(Set::stream)
                .map(input -> HarvestReportExportExcelDTO.create(input.getHarvestReport(), input, enumLocaliser))
                .collect(toList());
    }

    public static HarvestReportExportExcelDTO create(HarvestReport report,
                                                     Harvest harvest,
                                                     final EnumLocaliser enumLocaliser) {
        final HarvestReportExportExcelDTO dto = new HarvestReportExportExcelDTO();

        dto.submissionId = report.getId();
        dto.reportingTime = DateUtil.toLocalDateTimeNullSafe(report.getCreationTime());

        if (harvest.getHarvestSeason() != null) {
            dto.season = Localiser.select(harvest.getHarvestSeason().getNameLocalisation());
        }

        final Person hunter = harvest.getActualShooter();

        if (hunter != null) {
            dto.hunterFirstName = hunter.getByName();
            dto.hunterLastName = hunter.getLastName();

            final Address address = hunter.getAddress();
            if (address != null) {
                dto.hunterAddress = address.getStreetAddress();
                dto.hunterPostalCode = address.getPostalCode();
                dto.hunterPostalResidence = address.getCity();
            }

            dto.hunterPhone = hunter.getPhoneNumber();
            dto.hunterEmail = hunter.getEmail();
            dto.hunterHuntingCard = hunter.getHunterNumber();
        }

        if (report.getHarvestPermit() != null) {
            dto.huntingLicenseNumber = report.getHarvestPermit().getPermitNumber();
            dto.huntingLicenseType = report.getHarvestPermit().getPermitType();
        }

        final Person author = report.getAuthor();
        if (author != null) {
            dto.submitterFirstName = author.getByName();
            dto.submitterLastName = author.getLastName();

            final Address address = author.getAddress();
            if (address != null) {
                dto.submitterAddress = address.getStreetAddress();
                dto.submitterPostalCode = address.getPostalCode();
                dto.submitterPostalResidence = address.getCity();
            }

            dto.submitterPhone = author.getPhoneNumber();
            dto.submitterEmail = author.getEmail();
        }

        if (harvest.getHarvestQuota() != null) {
            dto.quotaAreaName = Localiser.select(harvest.getHarvestQuota().getHarvestArea().getNameLocalisation());
        }

        final Optional<Organisation> rkaOption = harvest.getRhy().getClosestAncestorOfType(OrganisationType.RKA);

        if (rkaOption.isPresent()) {
            final Organisation rka = rkaOption.get();
            dto.rkkAreaName = Localiser.select(rka.getNameLocalisation());
        }

        dto.rhyName = Localiser.select(harvest.getRhy().getNameLocalisation());

        dto.coordinatesCollectionMethod = harvest.getGeoLocation().getSource();
        dto.coordinatesLatitude = harvest.getGeoLocation().getLatitude();
        dto.coordinatesLongitude = harvest.getGeoLocation().getLongitude();
        dto.coordinatesAccuracy = harvest.getGeoLocation().getAccuracy();

        dto.huntingArea = harvest.getHuntingAreaType();
        dto.huntingGroup = harvest.getHuntingParty();

        if (harvest.getHuntingAreaSize() != null) {
            dto.area = Math.round(harvest.getHuntingAreaSize());
        }

        final PropertyIdentifier propertyIdentifier = harvest.getPropertyIdentifier();

        if (propertyIdentifier != null) {
            dto.propertyIdentifier = propertyIdentifier.getDelimitedValue();
        }

        final LocalDateTime huntingTimestamp = DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTime());
        dto.dateOfCatch = huntingTimestamp.toLocalDate();
        dto.timeOfCatch = huntingTimestamp.toLocalTime();

        dto.animalSpecies = Localiser.select(harvest.getSpecies().getNameLocalisation());

        dto.state = enumLocaliser.getTranslation(report.getState());

        final List<HarvestSpecimen> sortedSpecimens = harvest.getSortedSpecimens();
        final Function<Enum<?>, String> lf = enumLocaliser::getTranslation;

        dto.genderName = commaList(sortedSpecimens, HarvestSpecimen::getGender, lf);
        dto.ageName = commaList(sortedSpecimens, HarvestSpecimen::getAge, lf);
        dto.weight = commaList(sortedSpecimens, HarvestSpecimen::getWeight);

        return dto;
    }

    private static <T, E extends Enum<?>> String commaList(@Nonnull final Collection<T> coll,
                                                           @Nonnull final Function<T, E> enumFunction,
                                                           @Nonnull final Function<E, String> localisationFunction) {

        return commaList(coll, localisationFunction.compose(enumFunction));
    }

    private static <S, T> String commaList(final Collection<S> coll, final Function<S, T> f) {
        return Joiner.on(", ").useForNull("").join(coll.stream().map(f).iterator());
    }

    // (integer): Unique ID of the hunting report
    private Long submissionId;

    // (date): date and time of the reporting.
    // Format: yyyy.mm.dd hh:mm
    // Example: 2013.11.12 13:25
    private LocalDateTime reportingTime;

    // (integer) Hunting season ID
    private String season;

    // (string) First name of the hunter
    private String hunterFirstName;

    // (string) Last name of the hunter
    private String hunterLastName;

    // (string) Address of the hunter
    private String hunterAddress;

    // (string) Postal code of the hunter
    // Example: 00100
    private String hunterPostalCode;

    // (string) Postal residence
    // Example: Helsinki
    private String hunterPostalResidence;

    // (string) Phone number of the hunter
    private String hunterPhone;

    // (string) E-mail address of the hunter
    private String hunterEmail;

    // (string) Hunting card number of the hunter
    // Format: 7-digit number
    private String hunterHuntingCard;

    // (string) First name of the report submitter In case the submitter is a different person than the hunter.
    private String submitterFirstName;

    // (string) Last name of the report submitter.
    private String submitterLastName;

    // (string) Address of the report submitter.
    private String submitterAddress;

    // (string) Postal code of the hunter
    // Example: 00100
    private String submitterPostalCode;

    // (string) Postal residence
    // Example: Helsinki
    private String submitterPostalResidence;

    // (string) Phone number of the hunter
    private String submitterPhone;

    // (string) E-mail address of the hunter
    private String submitterEmail;

    // (string) Name of the quota area
    private String quotaAreaName;

    // (string) Name of Riistakeskus area
    private String rkkAreaName;

    // (string) Name of the RHY area
    private String rhyName;

    // (string) Method by which the coordinates were set in
    // the reporting service. Possible values are:
    //  manual: the user inserted the coordinates manually
    //  geolocation: the user used geolocation to set the coordinates;
    //  select-map: the user selected the location by clicking on the map.
    private GeoLocation.Source coordinatesCollectionMethod;

    // (string) Coordinates latitude value in ETRS-TM35FIN.
    // Example: 6974176.0716951
    private Integer coordinatesLatitude;

    // (string) Coordinates longitude value in ETRS-TM35FIN.
    // Example: 562697.9207312
    private Integer coordinatesLongitude;

    // (integer) Accuracy value in meters
    private Double coordinatesAccuracy;

    // (string) Possible values:
    //  S: Group/Association area;
    //  T: Separate area.
    private HuntingAreaType huntingArea;

    // (string) Name of the hunting group/association. Set if hunting_area value is ‘S’
    private String huntingGroup;

    // (integer) Area of the hunting area, in hectares (ha)
    private Long area;

    // (string) Property identifier
    private String propertyIdentifier;

    // (string) License number to which this report is part of.
    // Format: {YYYY}/{NNNN}
    // Example: 2013/1234
    private String huntingLicenseNumber;

    // (string) License type to which this report is part of.
    private String huntingLicenseType;

    // (string) Date of catch.
    // Format: yyyy.mm.dd
    private LocalDate dateOfCatch;

    // (string) Time of catch.
    // Format: hh:mm
    private LocalTime timeOfCatch;

    // (string) Animal species name in Finnish
    private String animalSpecies;

    // (string) Animal gender in textual format, localised.
    private String genderName;

    // (string) Animal age in textual format, localised.
    private String ageName;

    // (integer) Weight of the animal, in kg
    private String weight;

    // (string) harvest report state, localised.
    private String state;

    public Long getSubmissionId() {
        return submissionId;
    }

    public LocalDateTime getReportingTime() {
        return reportingTime;
    }

    public String getSeason() {
        return season;
    }

    public String getHunterFirstName() {
        return hunterFirstName;
    }

    public String getHunterLastName() {
        return hunterLastName;
    }

    public String getHunterAddress() {
        return hunterAddress;
    }

    public String getHunterPostalCode() {
        return hunterPostalCode;
    }

    public String getHunterPostalResidence() {
        return hunterPostalResidence;
    }

    public String getHunterPhone() {
        return hunterPhone;
    }

    public String getHunterEmail() {
        return hunterEmail;
    }

    public String getHunterHuntingCard() {
        return hunterHuntingCard;
    }

    public String getSubmitterFirstName() {
        return submitterFirstName;
    }

    public String getSubmitterLastName() {
        return submitterLastName;
    }

    public String getSubmitterAddress() {
        return submitterAddress;
    }

    public String getSubmitterPostalCode() {
        return submitterPostalCode;
    }

    public String getSubmitterPostalResidence() {
        return submitterPostalResidence;
    }

    public String getSubmitterPhone() {
        return submitterPhone;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public String getQuotaAreaName() {
        return quotaAreaName;
    }

    public String getRkkAreaName() {
        return rkkAreaName;
    }

    public String getRhyName() {
        return rhyName;
    }

    public GeoLocation.Source getCoordinatesCollectionMethod() {
        return coordinatesCollectionMethod;
    }

    public Integer getCoordinatesLatitude() {
        return coordinatesLatitude;
    }

    public Integer getCoordinatesLongitude() {
        return coordinatesLongitude;
    }

    public Double getCoordinatesAccuracy() {
        return coordinatesAccuracy;
    }

    public HuntingAreaType getHuntingArea() {
        return huntingArea;
    }

    public String getHuntingGroup() {
        return huntingGroup;
    }

    public Long getArea() {
        return area;
    }

    public String getPropertyIdentifier() {
        return propertyIdentifier;
    }

    public String getHuntingLicenseNumber() {
        return huntingLicenseNumber;
    }

    public String getHuntingLicenseType() {
        return huntingLicenseType;
    }

    public LocalDate getDateOfCatch() {
        return dateOfCatch;
    }

    public LocalTime getTimeOfCatch() {
        return timeOfCatch;
    }

    public String getAnimalSpecies() {
        return animalSpecies;
    }

    public String getGenderName() {
        return genderName;
    }

    public String getAgeName() {
        return ageName;
    }

    public String getWeight() {
        return weight;
    }

    public String getState() {
        return state;
    }
}
