package fi.riista.feature.gamediary.srva;

import com.google.common.base.Joiner;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * This DTO is used for Excel export file generation from UI
 */
public class SrvaEventExportExcelDTO {

    private String otherSpeciesDescription;

    public static SrvaEventExportExcelDTO create(SrvaEvent srvaEvent, final EnumLocaliser i18n) {
        final SrvaEventExportExcelDTO dto = new SrvaEventExportExcelDTO();

        dto.setSrvaEventId(srvaEvent.getId());
        dto.setState(i18n.getTranslation(srvaEvent.getState()));
        dto.setEventName(i18n.getTranslation(srvaEvent.getEventName()));
        dto.setEventType(i18n.getTranslation(srvaEvent.getEventType()));
        dto.setOtherTypeDescription(srvaEvent.getOtherTypeDescription());
        dto.setDescription(srvaEvent.getDescription());
        dto.setAnimalSpecies(srvaEvent.getSpecies() != null
                ? i18n.getTranslation(srvaEvent.getSpecies().getNameLocalisation()) : null);
        dto.setOtherSpeciesDescription(srvaEvent.getOtherSpeciesDescription());
        dto.setSpecimenAmount(srvaEvent.getTotalSpecimenAmount());
        dto.setEventResult(i18n.getTranslation(srvaEvent.getEventResult()));
        dto.setOtherMethodDescription(srvaEvent.getOtherMethodDescription());
        dto.setPersonCount(srvaEvent.getPersonCount());
        dto.setTimeSpent(srvaEvent.getTimeSpent());
        dto.setRhyName(i18n.getTranslation(srvaEvent.getRhy().getNameLocalisation()));
        dto.setCoordinatesCollectionMethod(srvaEvent.getGeoLocation().getSource());
        dto.setCoordinatesLatitude(srvaEvent.getGeoLocation().getLatitude());
        dto.setCoordinatesLongitude(srvaEvent.getGeoLocation().getLongitude());
        dto.setCoordinatesAccuracy(srvaEvent.getGeoLocation().getAccuracy());
        dto.setFromMobile(srvaEvent.isFromMobile());

        final LocalDateTime timestamp = DateUtil.toLocalDateTimeNullSafe(srvaEvent.getPointOfTime());
        dto.setDate(timestamp.toLocalDate());
        dto.setTime(timestamp.toLocalTime());

        final List<SrvaSpecimen> sortedSpecimens = srvaEvent.getSortedSpecimens();

        dto.setSpecimenGenders(commaList(sortedSpecimens.stream(), SrvaSpecimen::getGender, i18n::getTranslation));
        dto.setSpecimenAges(commaList(sortedSpecimens.stream(), SrvaSpecimen::getAge, i18n::getTranslation));
        dto.setEventMethods(commaList(
                srvaEvent.getSortedMethods().stream().filter(SrvaMethod::isChecked), SrvaMethod::getName,
                i18n::getTranslation));

        final Person author = srvaEvent.getAuthor();
        dto.setSubmitterFirstName(author.getByName());
        dto.setSubmitterLastName(author.getLastName());
        dto.setSubmitterPhone(author.getPhoneNumber());
        dto.setSubmitterEmail(author.getEmail());

        final Address address = author.getAddress();
        if (address != null) {
            dto.setSubmitterAddress(address.getStreetAddress());
            dto.setSubmitterPostalCode(address.getPostalCode());
            dto.setSubmitterPostalResidence(address.getCity());
        }

        return dto;
    }

    private static <T, E extends Enum<?>> String commaList(@Nonnull final Stream<T> stream,
                                                           @Nonnull final Function<T, E> enumFunction,
                                                           @Nonnull final Function<E, String> localisationFunction) {

        return commaList(stream, localisationFunction.compose(enumFunction));
    }

    private static <S, T> String commaList(final Stream<S> stream, final Function<S, T> f) {
        return Joiner.on(", ").useForNull("").join(stream.map(f).iterator());
    }

    // (integer): Unique ID of the hunting report
    private Long srvaEventId;

    // (date): date of the reporting.
    // Format: yyyy.mm.dd
    // Example: 2013.11.12
    private LocalDate date;

    // (time): time of the reporting.
    // Format: hh:mm
    // Example: 13:25
    private LocalTime time;

    // (string) Name of event, localized
    private String eventName;

    // (string) Type of event, localized
    private String eventType;

    // (String) Description of event type in case of eventType == OTHER
    private String otherTypeDescription;

    // (string) Animal species name in Finnish
    private String animalSpecies;

    // (integer) Amount of animals in event
    private Integer specimenAmount;

    // (String) Genders of animals in event
    private String specimenGenders;

    // (String) Ages of animals in event
    private String specimenAges;

    // (string) Method by which the coordinates were set in
    // the reporting service. Possible values are:
    //  manual: the user inserted the coordinates manually
    //  geolocation: the user used geolocation to set the coordinates;
    //  select-map: the user selected the location by clicking on the map.
    private GeoLocation.Source coordinatesCollectionMethod;

    // (integer) Coordinates latitude value in ETRS-TM35FIN.
    // Example: 6974176.0716951
    private Integer coordinatesLatitude;

    // (integer) Coordinates longitude value in ETRS-TM35FIN.
    // Example: 562697.9207312
    private Integer coordinatesLongitude;

    // (integer) Accuracy value in meters
    private Double coordinatesAccuracy;

    // (string) First name of the event submitter.
    private String submitterFirstName;

    // (string) Last name of the event submitter.
    private String submitterLastName;

    // (string) Address of the event submitter.
    private String submitterAddress;

    // (string) Postal code of the submitter
    // Example: 00100
    private String submitterPostalCode;

    // (string) Postal residence of submitter
    // Example: Helsinki
    private String submitterPostalResidence;

    // (string) Phone number of the submitter
    private String submitterPhone;

    // (string) E-mail address of the submitter
    private String submitterEmail;

    // (string) Name of the RHY area
    private String rhyName;

    // (string) Result of event, localized
    private String eventResult;

    // (string) Methods of event, localized
    private String eventMethods;

    // (String) Description of event method in case of eventMethods contains OTHER
    private String otherMethodDescription;

    // (integer) Number of person involved in the event
    private Integer personCount;

    // (integer) Total time spent by all person in hours.
    private Integer timeSpent;

    // (String) Description of event
    private String description;

    // (boolean) Is event origin mobile device, localized
    private boolean fromMobile;

    // (string) event state, localised.
    private String state;

    public Long getSrvaEventId() {
        return srvaEventId;
    }

    public void setSrvaEventId(Long srvaEventId) {
        this.srvaEventId = srvaEventId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getOtherTypeDescription() {
        return otherTypeDescription;
    }

    public void setOtherTypeDescription(String otherTypeDescription) {
        this.otherTypeDescription = otherTypeDescription;
    }

    public String getAnimalSpecies() {
        return animalSpecies;
    }

    public void setAnimalSpecies(String animalSpecies) {
        this.animalSpecies = animalSpecies;
    }

    public Integer getSpecimenAmount() {
        return specimenAmount;
    }

    public void setSpecimenAmount(Integer specimenAmount) {
        this.specimenAmount = specimenAmount;
    }

    public GeoLocation.Source getCoordinatesCollectionMethod() {
        return coordinatesCollectionMethod;
    }

    public void setCoordinatesCollectionMethod(GeoLocation.Source coordinatesCollectionMethod) {
        this.coordinatesCollectionMethod = coordinatesCollectionMethod;
    }

    public Integer getCoordinatesLatitude() {
        return coordinatesLatitude;
    }

    public void setCoordinatesLatitude(Integer coordinatesLatitude) {
        this.coordinatesLatitude = coordinatesLatitude;
    }

    public Integer getCoordinatesLongitude() {
        return coordinatesLongitude;
    }

    public void setCoordinatesLongitude(Integer coordinatesLongitude) {
        this.coordinatesLongitude = coordinatesLongitude;
    }

    public Double getCoordinatesAccuracy() {
        return coordinatesAccuracy;
    }

    public void setCoordinatesAccuracy(Double coordinatesAccuracy) {
        this.coordinatesAccuracy = coordinatesAccuracy;
    }

    public String getSubmitterFirstName() {
        return submitterFirstName;
    }

    public void setSubmitterFirstName(String submitterFirstName) {
        this.submitterFirstName = submitterFirstName;
    }

    public String getSubmitterLastName() {
        return submitterLastName;
    }

    public void setSubmitterLastName(String submitterLastName) {
        this.submitterLastName = submitterLastName;
    }

    public String getSubmitterAddress() {
        return submitterAddress;
    }

    public void setSubmitterAddress(String submitterAddress) {
        this.submitterAddress = submitterAddress;
    }

    public String getSubmitterPostalCode() {
        return submitterPostalCode;
    }

    public void setSubmitterPostalCode(String submitterPostalCode) {
        this.submitterPostalCode = submitterPostalCode;
    }

    public String getSubmitterPostalResidence() {
        return submitterPostalResidence;
    }

    public void setSubmitterPostalResidence(String submitterPostalResidence) {
        this.submitterPostalResidence = submitterPostalResidence;
    }

    public String getSubmitterPhone() {
        return submitterPhone;
    }

    public void setSubmitterPhone(String submitterPhone) {
        this.submitterPhone = submitterPhone;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public void setSubmitterEmail(String submitterEmail) {
        this.submitterEmail = submitterEmail;
    }

    public String getRhyName() {
        return rhyName;
    }

    public void setRhyName(String rhyName) {
        this.rhyName = rhyName;
    }

    public String getEventResult() {
        return eventResult;
    }

    public void setEventResult(String eventResult) {
        this.eventResult = eventResult;
    }

    public String getEventMethods() {
        return eventMethods;
    }

    public void setEventMethods(String eventMethods) {
        this.eventMethods = eventMethods;
    }

    public String getOtherMethodDescription() {
        return otherMethodDescription;
    }

    public void setOtherMethodDescription(String otherMethodDescription) {
        this.otherMethodDescription = otherMethodDescription;
    }

    public Integer getPersonCount() {
        return personCount;
    }

    public void setPersonCount(Integer personCount) {
        this.personCount = personCount;
    }

    public Integer getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFromMobile() {
        return fromMobile;
    }

    public void setFromMobile(boolean fromMobile) {
        this.fromMobile = fromMobile;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSpecimenGenders() {
        return specimenGenders;
    }

    public void setSpecimenGenders(String specimenGenders) {
        this.specimenGenders = specimenGenders;
    }

    public String getSpecimenAges() {
        return specimenAges;
    }

    public void setSpecimenAges(String specimenAges) {
        this.specimenAges = specimenAges;
    }

    public void setOtherSpeciesDescription(String otherSpeciesDescription) {
        this.otherSpeciesDescription = otherSpeciesDescription;
    }

    public String getOtherSpeciesDescription() {
        return otherSpeciesDescription;
    }
}
