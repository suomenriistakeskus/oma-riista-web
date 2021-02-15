package fi.riista.integration.lupahallinta;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HuntingAreaType;
import fi.riista.feature.gamediary.harvest.HuntingMethod;
import fi.riista.feature.gamediary.harvest.PermittedMethod;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.Locales;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This DTO is used for transfer file as CSV to Lupahallinta
 */
public class HarvestReportExportCSVDTO {

    public static List<HarvestReportExportCSVDTO> create(final HarvestPermit permit, final MessageSource messageSource) {
        final List<Harvest> harvests = permit.getHarvests().stream()
                .filter(h -> h.getStateAcceptedToHarvestPermit().equals(Harvest.StateAcceptedToHarvestPermit.ACCEPTED))
                .collect(Collectors.toList());

        if (harvests.isEmpty()) {
            HarvestReportExportCSVDTO dto = new HarvestReportExportCSVDTO();
            dto.submissionRowCount = 1;
            readSubmitter(permit.getHarvestReportAuthor(), dto);
            dto.reportingTime = DateUtil.toLocalDateTimeNullSafe(permit.getCreationTime());
            dto.huntingLicenseNumber = permit.getPermitNumber();
            dto.huntingLicenseAsList = getPermitReportsAsList(permit);
            dto.amount = 0;
            return Lists.newArrayList(dto);
        }
        final int submissionRowCount = harvests.size();
        return F.mapNonNullsToList(harvests, harvest -> {
            final HarvestReportExportCSVDTO dto = create(harvest, messageSource);
            dto.submissionRowCount = submissionRowCount;
            return dto;
        });
    }

    public static HarvestReportExportCSVDTO create(final Harvest harvest, final MessageSource messageSource) {
        HarvestReportExportCSVDTO dto = new HarvestReportExportCSVDTO();

        dto.submissionRowCount = 1;
        dto.reportingTime = DateUtil.toLocalDateTimeNullSafe(harvest.getCreationTime());

        final Person hunter = harvest.getActualShooter();
        if (hunter != null) {
            dto.hunterName = hunter.getByName() + " " + hunter.getLastName();
            dto.hunterFirstName = hunter.getByName();
            dto.hunterLastName = hunter.getLastName();

            Address address = hunter.getAddress();
            if (address != null) {
                dto.hunterAddress = address.getStreetAddress();
                dto.hunterPostalCode = address.getPostalCode();
                dto.hunterPostalResidence = address.getCity();
            }

            dto.hunterPhone = hunter.getPhoneNumber();
            dto.hunterEmail = hunter.getEmail();
            dto.hunterHuntingCard = hunter.getHunterNumber();
        }

        dto.huntingLicenseNumber = getPermitNumberOrNull(harvest);
        dto.huntingLicenseAsList = getPermitReportsAsList(harvest.getHarvestPermit());

        readSubmitter(harvest.getHarvestReportAuthor(), dto);

        if (harvest.getRhy() != null) {
            harvest.getRhy().getClosestAncestorOfType(OrganisationType.RKA).ifPresent(rka -> {
                dto.rkkAreaId = rka.getOfficialCode();
                dto.rkkAreaName = rka.getNameFinnish();
            });
            dto.rhyAreaId = harvest.getRhy().getOfficialCode();
            dto.rhyAreaName = harvest.getRhy().getNameFinnish();
        }

        dto.coordinatesCollectionMethod = harvest.getGeoLocation().getSource();
        dto.coordinatesLatitude = harvest.getGeoLocation().getLatitude();
        dto.coordinatesLongitude = harvest.getGeoLocation().getLongitude();
        dto.coordinatesAccuracy =
                harvest.getGeoLocation().getAccuracy() != null ? harvest.getGeoLocation().getAccuracy() : 0;

        dto.huntingArea = harvest.getHuntingAreaType();
        dto.huntingGroup = harvest.getHuntingParty();

        if (harvest.getHuntingAreaSize() != null) {
            dto.area = Math.round(harvest.getHuntingAreaSize());
        }

        PropertyIdentifier propertyIdentifier = harvest.getPropertyIdentifier();
        if (propertyIdentifier != null) {
            dto.municipality = propertyIdentifier.getKuntanumero();
            dto.village = propertyIdentifier.getSijaintialuenumero();
            dto.property = propertyIdentifier.getRyhmanumero();
            dto.registerNumber = propertyIdentifier.getYksikkonumero();
        }

        LocalDateTime huntingTimestamp = harvest.getPointOfTime().toLocalDateTime();
        dto.dateOfCatch = huntingTimestamp.toLocalDate();
        dto.timeOfCatch = huntingTimestamp.toLocalTime();

        dto.animalId = harvest.getSpecies().getOfficialCode();
        dto.animalSpecies = harvest.getSpecies().getNameFinnish();

        if (harvest.getHuntingMethod() == HuntingMethod.SHOT) {
            dto.sealInformation = "Ammuttu";
        } else if (harvest.getHuntingMethod() == HuntingMethod.SHOT_BUT_LOST) {
            dto.sealInformation = "Ammuttu, mutta menetetty";
        } else if (harvest.getHuntingMethod() == HuntingMethod.CAPTURED_ALIVE) {
            dto.sealInformation = "Elävänä pyytävällä loukulla pyydetty";
        }

        dto.amount = harvest.getAmount();
        dto.genderId = transformSpecimens(harvest, genderIdTransformer());
        dto.genderName = transformSpecimens(harvest, genderNameTransformer(messageSource));
        dto.ageId = transformSpecimens(harvest, ageIdTransformer());
        dto.ageName = transformSpecimens(harvest, ageNameTransformer(messageSource));
        dto.weight = transformSpecimens(harvest, weightTransformer());
        dto.harvestAlsoReportedByPhone = harvest.getReportedWithPhoneCall();

        final Tuple2<String, String> p = createPermittedMethodPair(harvest.getPermittedMethod());
        dto.permittedMethod = p._1();
        dto.permittedMethodDescription = p._2();

        return dto;
    }

    private static void readSubmitter(Person author, HarvestReportExportCSVDTO dto) {
        if (author != null) {
            dto.submitterFirstName = author.getByName();
            dto.submitterLastName = author.getLastName();

            Address address = author.getAddress();
            if (address != null) {
                dto.submitterAddress = address.getStreetAddress();
                dto.submitterPostalCode = address.getPostalCode();
                dto.submitterPostalResidence = address.getCity();
            }

            dto.submitterPhone = author.getPhoneNumber();
            dto.submitterEmail = author.getEmail();
        }
    }

    private static String getPermitNumberOrNull(Harvest harvest) {
        return harvest.getHarvestPermit() != null ?
                harvest.getHarvestPermit().getPermitNumber()
                : null;
    }

    private static String getPermitReportsAsList(HarvestPermit permit) {
        if (permit == null) {
            return "0";
        }
        return permit.isHarvestsAsList() ? "1" : "0";
    }

    private static String transformSpecimens(Harvest harvest, Function<HarvestSpecimen, String> mapper) {
        StringBuilder sb = new StringBuilder();
        Joiner.on(',').useForNull("").appendTo(sb, harvest.getSortedSpecimens().stream().map(mapper).iterator());
        return sb.toString();
    }

    private static Function<HarvestSpecimen, String> genderIdTransformer() {
        return input -> {
            if (input == null || input.getGender() == null) {
                return null;
            }
            return Integer.toString(input.getGender().getOfficialCode());
        };
    }

    private static Function<HarvestSpecimen, String> genderNameTransformer(final MessageSource messageSource) {
        return input -> {
            if (input == null || input.getGender() == null) {
                return null;
            }
            return messageSource.getMessage("GameGender." + input.getGender().name(), null,
                    input.getGender().name(), Locales.FI);
        };
    }

    private static Function<HarvestSpecimen, String> ageIdTransformer() {
        return input -> {
            if (input == null || input.getAge() == null) {
                return null;
            }
            return Integer.toString(input.getAge().getOfficialCode());
        };
    }

    private static Function<HarvestSpecimen, String> ageNameTransformer(final MessageSource messageSource) {
        return input -> {
            if (input == null || input.getAge() == null) {
                return null;
            }
            return messageSource.getMessage("GameAge." + input.getAge().name(), null,
                    input.getAge().name(), Locales.FI);
        };
    }

    private static Function<HarvestSpecimen, String> weightTransformer() {
        return input -> input == null || input.getWeight() == null
                ? null
                : Long.toString(Math.round(input.getWeight()));

    }

    private static Tuple2<String, String> createPermittedMethodPair(PermittedMethod permittedMethod) {
        if (permittedMethod == null) {
            return Tuple.of("", "");
        }
        final String methods = Joiner.on(',').useForNull("").join(
                permittedMethod.getTapeRecordersCode(), permittedMethod.getTrapsCode(), permittedMethod.getOtherCode());

        final String descriptions = Joiner.on(PermittedMethod.DESCRIPTION_SEPARATOR).useForNull("").join(
                null, null, permittedMethod.getDescription());
        return Tuple.of(methods, descriptions);
    }

    // (integer): How many rows are with identical submissionId
    private Integer submissionRowCount;

    // (date): date and time of the reporting.
    // Format: yyyy.mm.dd hh:mm
    // Example: 2013.11.12 13:25
    private LocalDateTime reportingTime;

    // (string) Hunters full name (first and last name)
    private String hunterName;

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

    // (integer) Unique ID of the Riistakeskus area.
    private String rkkAreaId;

    // (string) Name of Riistakeskus area
    private String rkkAreaName;

    // (integer) Unique ID of the RHY area.
    private String rhyAreaId;

    // (string) Name of the RHY area
    private String rhyAreaName;

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

    // (integer) Municipality code
    private String municipality;

    // (integer) Village code
    private String village;

    // (integer) Property code
    private String property;

    // (integer) Property registration number
    private String registerNumber;

    // (string) License number to which this report is part of.
    // Format: {YYYY}/{NNNN}
    // Example: 2013/1234
    private String huntingLicenseNumber;

    // (string) 1 if license is reported as list of harvests
    // 0 if harvests are reported one by one
    private String huntingLicenseAsList;

    // (string) Date of catch.
    // Format: yyyy.mm.dd
    private LocalDate dateOfCatch;

    // (string) Time of catch.
    // Format: hh:mm
    private LocalTime timeOfCatch;

    // (integer) Animal species ID.
    private Integer animalId;

    // (string) Animal species name in Finnish
    private String animalSpecies;

    // (string) Information regarding the seal hunt.
    private String sealInformation;

    // (integer) total amount of specimens
    private Integer amount;

    // Comma separated list of Animal gender IDs.
    // At most as many as 'amount'.
    // (integer) Animal gender ID. Possible values:
    //  if no value given, then empty
    //  1: female;
    //  2: male.
    private String genderId;

    // Comma separated list of Animal genders.
    // At most as many as 'amount'.
    // (string) Animal gender in textual format, in Finnish.
    private String genderName;

    // Comma separated list of Animal age ids.
    // At most as many as 'amount'.
    // (integer) Animal age id. Possible values:
    //  if no value given, then empty
    //  1: Adult;
    //  2: Juvenile;
    //  3: Cub;
    //  4: Calf;
    //  5: Young;
    //  6: Seal pup;
    //  7: Cub which has recently left female;
    private String ageId;

    // Comma separated list of Animal ages.
    // At most as many as 'amount'.
    // (string) Animal age in textual format, in Finnish
    // if no value given, then empty.
    private String ageName;

    // Comma separated list of Weights
    // At most as many as 'amount'.
    // (integer) Weight of the animal, in kg
    // if no value given, then empty.
    private String weight;

    // (integer) Flag to mark that the report has also been harvest_also_reported_by_phone done by phone.
    //   Used (at least) in bear hunting in Lapland. Possible values:
    //    0: No
    //    1: Yes
    private Boolean harvestAlsoReportedByPhone;

    // Code of permitted method, for example B014
    private String permittedMethod;

    // Textual description of used permitted method.
    private String permittedMethodDescription;

    public Integer getSubmissionRowCount() {
        return submissionRowCount;
    }

    public LocalDateTime getReportingTime() {
        return reportingTime;
    }

    public String getHunterName() {
        return hunterName;
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

    public String getRkkAreaId() {
        return rkkAreaId;
    }

    public String getRkkAreaName() {
        return rkkAreaName;
    }

    public String getRhyAreaId() {
        return rhyAreaId;
    }

    public String getRhyAreaName() {
        return rhyAreaName;
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

    public String getMunicipality() {
        return municipality;
    }

    public String getVillage() {
        return village;
    }

    public String getProperty() {
        return property;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public String getHuntingLicenseNumber() {
        return huntingLicenseNumber;
    }

    public String getHuntingLicenseAsList() {
        return huntingLicenseAsList;
    }

    public LocalDate getDateOfCatch() {
        return dateOfCatch;
    }

    public LocalTime getTimeOfCatch() {
        return timeOfCatch;
    }

    public Integer getAnimalId() {
        return animalId;
    }

    public String getAnimalSpecies() {
        return animalSpecies;
    }

    public String getSealInformation() {
        return sealInformation;
    }

    public Integer getAmount() {
        return amount;
    }

    public String getGenderId() {
        return genderId;
    }

    public String getGenderName() {
        return genderName;
    }

    public String getAgeId() {
        return ageId;
    }

    public String getAgeName() {
        return ageName;
    }

    public String getWeight() {
        return weight;
    }

    public Boolean getHarvestAlsoReportedByPhone() {
        return harvestAlsoReportedByPhone;
    }

    public String getPermittedMethod() {
        return permittedMethod;
    }

    public String getPermittedMethodDescription() {
        return permittedMethodDescription;
    }
}
