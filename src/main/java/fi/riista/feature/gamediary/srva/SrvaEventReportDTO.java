package fi.riista.feature.gamediary.srva;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.srva.method.SrvaMethodDTO;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenDTO;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class SrvaEventReportDTO {

    public static SrvaEventReportDTO create(@Nonnull final SrvaEvent event,
                                            final Set<SrvaMethodDTO> methods,
                                            final PersonWithNameDTO author,
                                            final GameSpeciesDTO species,
                                            final Set<SrvaSpecimenDTO> specimens,
                                            final String rhy,
                                            final PersonWithNameDTO approver,
                                            final String activeUser,
                                            final boolean isModerator,
                                            final Set<String> imageURLs,
                                            final String map64Encoded,
                                            final String mapFinland64Encoded,
                                            final String lang) {
        Objects.requireNonNull(event);

        final SrvaEventReportDTO dto = new SrvaEventReportDTO(
                event.getEventName(),
                event.getEventType(),
                event.getEventTypeDetail(),
                event.getTotalSpecimenAmount(),
                event.getDeportationOrderNumber(),
                event.getOtherMethodDescription(),
                event.getOtherTypeDescription(),
                event.getOtherEventTypeDetailDescription(),
                methods,
                event.getPersonCount(),
                event.getTimeSpent(),
                event.getEventResult(),
                event.getEventResultDetail(),
                event.getGeoLocation(),
                event.getPointOfTime(),
                author,
                species,
                event.getDescription(),
                specimens,
                rhy,
                event.getState(),
                event.getOtherSpeciesDescription(),
                approver,
                activeUser,
                isModerator,
                DateUtil.today(),
                imageURLs,
                map64Encoded,
                mapFinland64Encoded,
                lang
        );

        return dto;
    }

    private final SrvaEventNameEnum eventName;
    private final SrvaEventTypeEnum eventType;
    private final SrvaEventTypeDetailsEnum eventTypeDetail;
    private final Integer amount;
    private final String deportationOrderNumber;
    private final String otherMethodDescription;
    private final String otherTypeDescription;
    private final String otherTypeDetailDescription;
    private final Set<SrvaMethodDTO> methods;
    private final Integer personCount;
    private final Integer timeSpent;
    private final SrvaResultEnum eventResult;
    private final SrvaEventResultDetailsEnum eventResultDetail;
    private final GeoLocation geoLocation;
    private final DateTime pointOfTime;
    private final PersonWithNameDTO author;
    private final GameSpeciesDTO species;
    private final String description;
    private final Set<SrvaSpecimenDTO> specimens;
    private final String rhy;
    private final SrvaEventStateEnum state;
    private final String otherSpeciesDescription;
    private final PersonWithNameDTO approver;
    private final String activeUser;
    private final boolean isModerator;
    private final LocalDate reportDate;
    private final Set<String> imageURLs;
    private final String map64Encoded;
    private final String mapFinland64Encoded;
    private final String lang;

    public SrvaEventReportDTO(final SrvaEventNameEnum eventName, final SrvaEventTypeEnum eventType,
                              final SrvaEventTypeDetailsEnum eventTypeDetail, final Integer amount,
                              final String deportationOrderNumber, final String otherMethodDescription,
                              final String otherTypeDescription, final String otherTypeDetailDescription,
                              final Set<SrvaMethodDTO> methods, final Integer personCount, final Integer timeSpent,
                              final SrvaResultEnum eventResult, final SrvaEventResultDetailsEnum eventResultDetail,
                              final GeoLocation geoLocation, final DateTime pointOfTime, final PersonWithNameDTO author,
                              final GameSpeciesDTO species, final String description, final Set<SrvaSpecimenDTO> specimens,
                              final String rhy, final SrvaEventStateEnum state, final String otherSpeciesDescription,
                              final PersonWithNameDTO approver, final String activeUser,
                              final boolean isModerator, final LocalDate reportDate, final Set<String> imageURLs,
                              final String map64Encoded, final String mapFinland64Encoded, final String lang) {
        this.eventName = eventName;
        this.eventType = eventType;
        this.eventTypeDetail = eventTypeDetail;
        this.amount = amount;
        this.deportationOrderNumber = deportationOrderNumber;
        this.otherMethodDescription = otherMethodDescription;
        this.otherTypeDescription = otherTypeDescription;
        this.otherTypeDetailDescription = otherTypeDetailDescription;
        this.methods = methods;
        this.personCount = personCount;
        this.timeSpent = timeSpent;
        this.eventResult = eventResult;
        this.eventResultDetail = eventResultDetail;
        this.geoLocation = geoLocation;
        this.pointOfTime = pointOfTime;
        this.author = author;
        this.species = species;
        this.description = description;
        this.specimens = specimens;
        this.rhy = rhy;
        this.state = state;
        this.otherSpeciesDescription = otherSpeciesDescription;
        this.approver = approver;
        this.activeUser = activeUser;
        this.isModerator = isModerator;
        this.reportDate = reportDate;
        this.imageURLs = imageURLs;
        this.map64Encoded = map64Encoded;
        this.mapFinland64Encoded = mapFinland64Encoded;
        this.lang = lang;
    }

    public SrvaEventNameEnum getEventName() {
        return eventName;
    }

    public SrvaEventTypeEnum getEventType() {
        return eventType;
    }

    public SrvaEventTypeDetailsEnum getEventTypeDetail() {
        return eventTypeDetail;
    }

    public Integer getAmount() {
        return amount;
    }

    public String getDeportationOrderNumber() {
        return deportationOrderNumber;
    }

    public String getOtherMethodDescription() {
        return otherMethodDescription;
    }

    public String getOtherTypeDescription() {
        return otherTypeDescription;
    }

    public String getOtherTypeDetailDescription() {
        return otherTypeDetailDescription;
    }

    public Set<SrvaMethodDTO> getMethods() {
        return methods;
    }

    public Integer getPersonCount() {
        return personCount;
    }

    public Integer getTimeSpent() {
        return timeSpent;
    }

    public SrvaResultEnum getEventResult() {
        return eventResult;
    }

    public SrvaEventResultDetailsEnum getEventResultDetail() {
        return eventResultDetail;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public DateTime getPointOfTime() {
        return pointOfTime;
    }

    public PersonWithNameDTO getAuthor() {
        return author;
    }

    public GameSpeciesDTO getSpecies() {
        return species;
    }

    public String getDescription() {
        return description;
    }

    public Set<SrvaSpecimenDTO> getSpecimens() {
        return specimens;
    }

    public String getRhy() {
        return rhy;
    }

    public SrvaEventStateEnum getState() {
        return state;
    }

    public String getOtherSpeciesDescription() {
        return otherSpeciesDescription;
    }

    public PersonWithNameDTO getApprover() {
        return approver;
    }

    public String getActiveUser() {
        return activeUser;
    }

    public boolean getIsModerator() {
        return isModerator;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public Set<String> getImageURLs() {
        return imageURLs;
    }

    public String getMap64Encoded() {
        return map64Encoded;
    }

    public String getMapFinland64Encoded() {
        return mapFinland64Encoded;
    }

    public String getLang() {
        return lang;
    }
}
