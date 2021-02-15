package fi.riista.feature.harvestpermit.report.excel;

import com.google.common.base.Joiner;
import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.LocalisedEnum;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class HarvestReportExcelDTO {

    public static HarvestReportExcelDTO create(final Harvest harvest, final EnumLocaliser i18n,
                                               final boolean includeDetails) {
        final HarvestReportExcelDTO dto = new HarvestReportExcelDTO();

        dto.harvestReportDate = harvest.getHarvestReportDate().withZone(Constants.DEFAULT_TIMEZONE).toLocalDateTime();
        dto.harvestReportState = i18n.getTranslation(harvest.getHarvestReportState());

        if (includeDetails) {
            if (harvest.getHarvestReportAuthor() != null) {
                final Person author = harvest.getHarvestReportAuthor();
                dto.harvestReportAuthorFirstName = author.getByName();
                dto.harvestReportAuthorLastName = author.getLastName();

                final Address address = author.getAddress();
                if (address != null) {
                    dto.harvestReportAuthorAddress = address.getStreetAddress();
                    dto.harvestReportAuthorPostalCode = address.getPostalCode();
                    dto.harvestReportAuthorPostalResidence = address.getCity();
                }

                dto.harvestReportAuthorPhone = author.getPhoneNumber();
                dto.harvestReportAuthorEmail = author.getEmail();
            }

            if (harvest.getActualShooter() != null) {
                final Person hunter = harvest.getActualShooter();
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

        }

        if (harvest.getHarvestPermit() != null) {
            dto.permitNumber = harvest.getHarvestPermit().getPermitNumber();
            dto.permitType = harvest.getHarvestPermit().getPermitType();
        }

        if (harvest.getHarvestSeason() != null) {
            dto.seasonName = i18n.getTranslation(harvest.getHarvestSeason().getNameLocalisation());
        }

        if (harvest.getHarvestQuota() != null) {
            dto.quotaAreaName = i18n.getTranslation(harvest.getHarvestQuota().getHarvestArea().getNameLocalisation());
        }

        if (harvest.getRhy() != null) {
            dto.rhyName = i18n.getTranslation(harvest.getRhy().getNameLocalisation());

            if (harvest.getRhy().getParentOrganisation() != null) {
                final Organisation rka = harvest.getRhy().getParentOrganisation();
                dto.rkaName = i18n.getTranslation(rka.getNameLocalisation());
            }
        }

        if (harvest.getGeoLocation() != null) {
            final GeoLocation location = harvest.getGeoLocation();
            dto.locationSourceName = i18n.getTranslation(location.getSource());
            dto.locationLatitude = location.getLatitude();
            dto.locationLongitude = location.getLongitude();
            dto.locationAccuracy = location.getAccuracy();
        }
        dto.municipalityCode = harvest.getMunicipalityCode();

        if (harvest.getPropertyIdentifier() != null) {
            dto.propertyIdentifier = harvest.getPropertyIdentifier().getDelimitedValue();
        }

        dto.pointOfTime = harvest.getPointOfTime().toDate();
        dto.speciesName = i18n.getTranslation(harvest.getSpecies().getNameLocalisation());

        final List<HarvestSpecimen> sortedSpecimens = harvest.getSortedSpecimens();

        dto.genderName = commaList(sortedSpecimens, HarvestSpecimen::getGender, i18n);
        dto.ageName = commaList(sortedSpecimens, HarvestSpecimen::getAge, i18n);
        dto.weight = commaList(sortedSpecimens, HarvestSpecimen::getWeight);

        dto.huntingAreaType = i18n.getTranslation(harvest.getHuntingAreaType());
        dto.huntingGroupName = harvest.getHuntingParty();

        if (harvest.getHuntingAreaSize() != null) {
            dto.huntingAreaSize = Math.round(harvest.getHuntingAreaSize());
        }

        dto.feedingPlace = i18n.getTranslation(harvest.getFeedingPlace());
        dto.taigaBeanGoose = i18n.getTranslation(harvest.getTaigaBeanGoose());
        dto.huntingMethodName = i18n.getTranslation(harvest.getHuntingMethod());
        dto.reportedWithPhoneCall = i18n.getTranslation(harvest.getReportedWithPhoneCall());

        return dto;
    }

    private static final Joiner COMMA_JOINER = Joiner.on(", ").useForNull("");

    private static <A, B> String commaList(@Nonnull final Collection<A> coll, @Nonnull final Function<A, B> mapper) {
        return COMMA_JOINER.join(coll.stream().map(mapper).collect(toList()));
    }

    private static <T, E extends Enum<E> & LocalisedEnum> String commaList(@Nonnull final Collection<T> coll,
                                                                           @Nonnull final Function<T, E> enumFunction,
                                                                           @Nonnull final EnumLocaliser enumLocaliser) {
        return COMMA_JOINER.join(coll.stream()
                .map(enumFunction)
                .map(enumLocaliser::getTranslation)
                .collect(toList()));
    }

    // (string) harvest report state, localised.
    private String harvestReportState;

    // (date): date and time of the reporting.
    // Format: yyyy.mm.dd hh:mm
    // Example: 2013.11.12 13:25
    private LocalDateTime harvestReportDate;

    // (string) First name of the report submitter In case the submitter is a different person than the hunter.
    private String harvestReportAuthorFirstName;

    // (string) Last name of the report submitter.
    private String harvestReportAuthorLastName;

    // (string) Address of the report submitter.
    private String harvestReportAuthorAddress;

    // (string) Postal code of the hunter
    // Example: 00100
    private String harvestReportAuthorPostalCode;

    // (string) Postal residence
    // Example: Helsinki
    private String harvestReportAuthorPostalResidence;

    // (string) Phone number of the hunter
    private String harvestReportAuthorPhone;

    // (string) E-mail address of the hunter
    private String harvestReportAuthorEmail;

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

    // (integer) Hunting season ID
    private String seasonName;

    // (string) Name of the quota area
    private String quotaAreaName;

    // (string) Name of Riistakeskus area
    private String rkaName;

    // (string) Name of the RHY area
    private String rhyName;

    // (string) Method by which the coordinates were set in
    // the reporting service. Possible values are:
    //  manual: the user inserted the coordinates manually
    //  geolocation: the user used geolocation to set the coordinates;
    //  select-map: the user selected the location by clicking on the map.
    private String locationSourceName;

    // (string) Coordinates latitude value in ETRS-TM35FIN.
    // Example: 6974176.0716951
    private Integer locationLatitude;

    // (string) Coordinates longitude value in ETRS-TM35FIN.
    // Example: 562697.9207312
    private Integer locationLongitude;

    // (integer) Accuracy value in meters
    private Double locationAccuracy;

    // (string) Property identifier
    private String propertyIdentifier;

    private String municipalityCode;

    // (string) License number to which this report is part of.
    // Format: {YYYY}/{NNNN}
    // Example: 2013/1234
    private String permitNumber;

    // (string) License type to which this report is part of.
    private String permitType;

    // (string) Date and time of catch
    // Format: yyyy.mm.dd hh:mm
    private Date pointOfTime;

    // (string) Animal species name in Finnish
    private String speciesName;

    // (string) Animal gender in textual format, localised.
    private String genderName;

    // (string) Animal age in textual format, localised.
    private String ageName;

    // (integer) Weight of the animal, in kg
    private String weight;

    private String huntingMethodName;

    private String feedingPlace;

    private String taigaBeanGoose;

    // (string) Possible values:
    private String huntingAreaType;

    // (string) Name of the hunting group/association. Set if hunting_area value is ‘S’
    private String huntingGroupName;

    // (integer) Area of the hunting area, in hectares (ha)
    private Long huntingAreaSize;

    private String reportedWithPhoneCall;

    public String getHarvestReportState() {
        return harvestReportState;
    }

    public LocalDateTime getHarvestReportDate() {
        return harvestReportDate;
    }

    public String getHarvestReportAuthorFirstName() {
        return harvestReportAuthorFirstName;
    }

    public String getHarvestReportAuthorLastName() {
        return harvestReportAuthorLastName;
    }

    public String getHarvestReportAuthorAddress() {
        return harvestReportAuthorAddress;
    }

    public String getHarvestReportAuthorPostalCode() {
        return harvestReportAuthorPostalCode;
    }

    public String getHarvestReportAuthorPostalResidence() {
        return harvestReportAuthorPostalResidence;
    }

    public String getHarvestReportAuthorPhone() {
        return harvestReportAuthorPhone;
    }

    public String getHarvestReportAuthorEmail() {
        return harvestReportAuthorEmail;
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

    public String getSeasonName() {
        return seasonName;
    }

    public String getQuotaAreaName() {
        return quotaAreaName;
    }

    public String getRkaName() {
        return rkaName;
    }

    public String getRhyName() {
        return rhyName;
    }

    public String getLocationSourceName() {
        return locationSourceName;
    }

    public Integer getLocationLatitude() {
        return locationLatitude;
    }

    public Integer getLocationLongitude() {
        return locationLongitude;
    }

    public Double getLocationAccuracy() {
        return locationAccuracy;
    }

    public String getPropertyIdentifier() {
        return propertyIdentifier;
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public String getPermitType() {
        return permitType;
    }

    public Date getPointOfTime() {
        return pointOfTime;
    }

    public String getSpeciesName() {
        return speciesName;
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

    public String getHuntingMethodName() {
        return huntingMethodName;
    }

    public String getFeedingPlace() {
        return feedingPlace;
    }

    public String getTaigaBeanGoose() {
        return taigaBeanGoose;
    }

    public String getHuntingAreaType() {
        return huntingAreaType;
    }

    public String getHuntingGroupName() {
        return huntingGroupName;
    }

    public Long getHuntingAreaSize() {
        return huntingAreaSize;
    }

    public String getReportedWithPhoneCall() {
        return reportedWithPhoneCall;
    }
}
