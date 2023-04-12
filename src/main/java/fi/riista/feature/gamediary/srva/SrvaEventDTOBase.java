package fi.riista.feature.gamediary.srva;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.riista.feature.gamediary.GameDiaryEntryDTO;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.srva.method.SrvaMethodDTO;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenDTO;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

public abstract class SrvaEventDTOBase extends GameDiaryEntryDTO {

    protected static <DTO extends SrvaEventDTOBase, SELF extends Builder<DTO, SELF>> DTO createFromNonCollectionFields(
            @Nonnull final SrvaEvent entity, @Nonnull final SELF builder) {

        Objects.requireNonNull(builder, "builder is null");

        final DTO dto = builder.withIdAndRev(entity)
                .withGeoLocation(entity.getGeoLocation())
                .withPointOfTime(entity.getPointOfTime().toLocalDateTime())
                .withDescription(entity.getDescription())
                .build();

        dto.setEventName(entity.getEventName());
        dto.setEventType(entity.getEventType());
        dto.setEventTypeDetail(entity.getEventTypeDetail());
        dto.setOtherEventTypeDetailDescription(entity.getOtherEventTypeDetailDescription());
        dto.setDeportationOrderNumber(entity.getDeportationOrderNumber());
        dto.setTotalSpecimenAmount(entity.getTotalSpecimenAmount());
        dto.setEventResult(entity.getEventResult());
        dto.setEventResultDetail(entity.getEventResultDetail());
        dto.setOtherMethodDescription(entity.getOtherMethodDescription());
        dto.setOtherTypeDescription(entity.getOtherTypeDescription());
        dto.setTimeSpent(entity.getTimeSpent());
        dto.setEventResult(entity.getEventResult());
        dto.setPersonCount(entity.getPersonCount());
        dto.setRhyId(entity.getRhy().getId());
        dto.setState(entity.getState());
        dto.setOtherSpeciesDescription(entity.getOtherSpeciesDescription());

        return dto;
    }

    @NotNull
    private SrvaEventNameEnum eventName;

    @NotNull
    private SrvaEventTypeEnum eventType;

    @NotNull
    private int totalSpecimenAmount;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String otherMethodDescription;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String otherTypeDescription;

    @Valid
    private List<SrvaMethodDTO> methods;

    @Min(0)
    private Integer personCount;

    @Min(0)
    private Integer timeSpent;

    private SrvaResultEnum eventResult;

    @Valid
    private PersonWithHunterNumberDTO authorInfo;

    @Valid
    private List<SrvaSpecimenDTO> specimens;

    private Long rhyId;

    private SrvaEventStateEnum state;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String otherSpeciesDescription;

    // null value as gameSpeciesCode is used for other / unknown species
    private Integer gameSpeciesCode;

    @Valid
    private SrvaEventApproverDTO approverInfo;

    // SRVA V2 fields

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String deportationOrderNumber;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private SrvaEventTypeDetailsEnum eventTypeDetail;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String otherEventTypeDetailDescription;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private SrvaEventResultDetailsEnum eventResultDetail;

    @AssertTrue
    protected boolean isExclusiveGameSpeciesCodeOrOtherSpeciesDescription() {
        return gameSpeciesCode != null && otherSpeciesDescription == null ||
                gameSpeciesCode == null && otherSpeciesDescription != null;
    }

    public SrvaEventDTOBase() {
        super(GameDiaryEntryType.SRVA);
    }

    //Accessors -->

    public SrvaEventNameEnum getEventName() {
        return eventName;
    }

    public void setEventName(final SrvaEventNameEnum eventName) {
        this.eventName = eventName;
    }

    public SrvaEventTypeEnum getEventType() {
        return eventType;
    }

    public void setEventType(SrvaEventTypeEnum eventType) {
        this.eventType = eventType;
    }

    public int getTotalSpecimenAmount() {
        return totalSpecimenAmount;
    }

    public void setTotalSpecimenAmount(int totalSpecimenAmount) {
        this.totalSpecimenAmount = totalSpecimenAmount;
    }

    public String getOtherMethodDescription() {
        return otherMethodDescription;
    }

    public void setOtherMethodDescription(String otherMethodDescription) {
        this.otherMethodDescription = otherMethodDescription;
    }

    public String getOtherTypeDescription() {
        return otherTypeDescription;
    }

    public void setOtherTypeDescription(String otherTypeDescription) {
        this.otherTypeDescription = otherTypeDescription;
    }

    public List<SrvaMethodDTO> getMethods() {
        return methods;
    }

    public void setMethods(List<SrvaMethodDTO> methods) {
        this.methods = methods;
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

    public SrvaResultEnum getEventResult() {
        return eventResult;
    }

    public void setEventResult(SrvaResultEnum eventResult) {
        this.eventResult = eventResult;
    }

    public PersonWithHunterNumberDTO getAuthorInfo() {
        return authorInfo;
    }

    public void setAuthorInfo(PersonWithHunterNumberDTO authorInfo) {
        this.authorInfo = authorInfo;
    }

    public List<SrvaSpecimenDTO> getSpecimens() {
        return specimens;
    }

    public void setSpecimens(List<SrvaSpecimenDTO> specimens) {
        this.specimens = specimens;
    }

    public Long getRhyId() {
        return rhyId;
    }

    public void setRhyId(Long rhyId) {
        this.rhyId = rhyId;
    }

    public SrvaEventStateEnum getState() {
        return state;
    }

    public void setState(SrvaEventStateEnum state) {
        this.state = state;
    }

    public String getOtherSpeciesDescription() {
        return otherSpeciesDescription;
    }

    public void setOtherSpeciesDescription(String otherSpeciesDescription) {
        this.otherSpeciesDescription = otherSpeciesDescription;
    }

    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public SrvaEventApproverDTO getApproverInfo() {
        return approverInfo;
    }

    public void setApproverInfo(SrvaEventApproverDTO approverInfo) {
        this.approverInfo = approverInfo;
    }

    public String getDeportationOrderNumber() {
        return deportationOrderNumber;
    }

    public void setDeportationOrderNumber(final String deportationOrderNumber) {
        this.deportationOrderNumber = deportationOrderNumber;
    }

    public SrvaEventTypeDetailsEnum getEventTypeDetail() {
        return eventTypeDetail;
    }

    public void setEventTypeDetail(final SrvaEventTypeDetailsEnum eventTypeDetail) {
        this.eventTypeDetail = eventTypeDetail;
    }

    public String getOtherEventTypeDetailDescription() {
        return otherEventTypeDetailDescription;
    }

    public void setOtherEventTypeDetailDescription(final String otherEventTypeDetailDescription) {
        this.otherEventTypeDetailDescription = otherEventTypeDetailDescription;
    }

    public SrvaEventResultDetailsEnum getEventResultDetail() {
        return eventResultDetail;
    }

    public void setEventResultDetail(final SrvaEventResultDetailsEnum eventResultDetail) {
        this.eventResultDetail = eventResultDetail;
    }

}
