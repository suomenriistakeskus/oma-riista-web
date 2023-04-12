package fi.riista.feature.organization.rhy.huntingcontrolevent.mobile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.riista.config.jackson.LocalTimeToStringSerializer;
import fi.riista.config.jackson.StringToLocalTimeDeserializer;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlCooperationType;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEvent;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventStatus;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventType;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.persistence.Column;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

import static fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventStatus.ACCEPTED;
import static fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventStatus.ACCEPTED_SUBSIDIZED;

public class MobileHuntingControlEventDTO extends BaseEntityDTO<Long> {

    @NotNull
    private MobileHuntingControlSpecVersion specVersion;

    private Long id;
    private Integer rev;

    /** Universally unique identifier for the event
     *
     *  This is set by mobile client only when creating a new event. When updating event, eventId can be used for
     *  identifying the event. Value is null if event is NOT created by mobile.
     *
     *  This is used
     *  - by backend for detecting duplicate entries being created
     *  - by mobile for detecting that new event which was not sent successfully, was stored by backend
     */

    @Column
    private Long mobileClientRefId;

    private HuntingControlEventType eventType;

    private HuntingControlEventStatus status;

    @Valid
    private List<MobileHuntingControlInspectorDTO> inspectors;

    @NotEmpty
    private Set<HuntingControlCooperationType> cooperationTypes;

    private boolean wolfTerritory;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String otherParticipants;

    @NotNull
    @Valid
    private GeoLocation geoLocation;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String locationDescription;

    @NotNull
    private LocalDate date;

    @NotNull
    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    @JsonDeserialize(using = StringToLocalTimeDeserializer.class)
    private LocalTime beginTime;

    @NotNull
    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    @JsonDeserialize(using = StringToLocalTimeDeserializer.class)
    private LocalTime endTime;

    private int customers;

    private int proofOrders;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String description;

    @Valid
    private List<MobileHuntingControlAttachmentDTO> attachments;

    @Valid
    private List<MobileChangeHistoryDTO> changeHistory;

    /** Only output for client */

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean canEdit;

    // Constructors / factories

    public MobileHuntingControlEventDTO() {}

    public static MobileHuntingControlEventDTO create(final MobileHuntingControlSpecVersion specVersion,
                                                      final HuntingControlEvent event,
                                                      final Set<Person> inspectors,
                                                      final Set<HuntingControlCooperationType> cooperationTypes,
                                                      final List<MobileChangeHistoryDTO> changes,
                                                      final List<MobileHuntingControlAttachmentDTO> attachments) {
        /**
         * This is a gentle reminder that different specVersions needs own handling. The current implementation is left
         * simple as there is no need for more complex solution (as someone might think that even this is overengineered).
         */
        switch (specVersion) {
            case _1:
                return createSpecVersion1(event, inspectors, cooperationTypes, changes, attachments);
            default:
                throw new UnsupportedOperationException("MobileHuntingControlSpecVersion " + specVersion + " not supported");
        }
    }

    private static MobileHuntingControlEventDTO createSpecVersion1(final HuntingControlEvent event,
                                                                   final Set<Person> inspectors,
                                                                   final Set<HuntingControlCooperationType> cooperationTypes,
                                                                   final List<MobileChangeHistoryDTO> changes,
                                                                   final List<MobileHuntingControlAttachmentDTO> attachments) {

        final MobileHuntingControlEventDTO dto = new MobileHuntingControlEventDTO();
        DtoUtil.copyBaseFields(event, dto);

        dto.setSpecVersion(MobileHuntingControlSpecVersion._1);
        dto.setMobileClientRefId(event.getMobileClientRefId());
        dto.setEventType(event.getEventType());
        dto.setStatus(event.getStatus() != null ? event.getStatus() : ACCEPTED_SUBSIDIZED);
        dto.setInspectors(F.mapNonNullsToList(inspectors, MobileHuntingControlInspectorDTO::create));
        dto.setCooperationTypes(cooperationTypes);
        dto.setWolfTerritory(event.getWolfTerritory());
        dto.setOtherParticipants(event.getOtherParticipants());
        dto.setGeoLocation(event.getGeoLocation());
        dto.setLocationDescription(event.getLocationDescription());
        dto.setDate(event.getDate());
        dto.setBeginTime(event.getBeginTime());
        dto.setEndTime(event.getEndTime());
        dto.setCustomers(event.getCustomers());
        dto.setProofOrders(event.getProofOrders());
        dto.setDescription(event.getDescription());
        dto.setAttachments(attachments);
        dto.setChangeHistory(changes);
        dto.setCanEdit(!event.isLockedAsPastStatistics() && dto.getStatus() != ACCEPTED && dto.getStatus() != ACCEPTED_SUBSIDIZED);

        return dto;
    }

    // Accessors -->

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public MobileHuntingControlSpecVersion getSpecVersion() {
        return specVersion;
    }

    public void setSpecVersion(final MobileHuntingControlSpecVersion specVersion) {
        this.specVersion = specVersion;
    }

    public Long getMobileClientRefId() {
        return mobileClientRefId;
    }

    public void setMobileClientRefId(final Long mobileClientRefId) {
        this.mobileClientRefId = mobileClientRefId;
    }

    public HuntingControlEventType getEventType() {
        return eventType;
    }

    public void setEventType(final HuntingControlEventType eventType) {
        this.eventType = eventType;
    }

    public HuntingControlEventStatus getStatus() {
        return status;
    }

    public void setStatus(final HuntingControlEventStatus status) {
        this.status = status;
    }

    public List<MobileHuntingControlInspectorDTO> getInspectors() {
        return inspectors;
    }

    public void setInspectors(final List<MobileHuntingControlInspectorDTO> inspectors) {
        this.inspectors = inspectors;
    }

    public Set<HuntingControlCooperationType> getCooperationTypes() {
        return cooperationTypes;
    }

    public void setCooperationTypes(final Set<HuntingControlCooperationType> cooperationTypes) {
        this.cooperationTypes = cooperationTypes;
    }

    public boolean isWolfTerritory() {
        return wolfTerritory;
    }

    public void setWolfTerritory(final boolean wolfTerritory) {
        this.wolfTerritory = wolfTerritory;
    }

    public String getOtherParticipants() {
        return otherParticipants;
    }

    public void setOtherParticipants(final String otherParticipants) {
        this.otherParticipants = otherParticipants;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(final String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(final LocalDate date) {
        this.date = date;
    }

    public LocalTime getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(final LocalTime beginTime) {
        this.beginTime = beginTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(final LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getCustomers() {
        return customers;
    }

    public void setCustomers(final int customers) {
        this.customers = customers;
    }

    public int getProofOrders() {
        return proofOrders;
    }

    public void setProofOrders(final int proofOrders) {
        this.proofOrders = proofOrders;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public List<MobileHuntingControlAttachmentDTO> getAttachments() {
        return attachments;
    }

    public void setAttachments(final List<MobileHuntingControlAttachmentDTO> attachments) {
        this.attachments = attachments;
    }

    public List<MobileChangeHistoryDTO> getChangeHistory() {
        return changeHistory;
    }

    public void setChangeHistory(final List<MobileChangeHistoryDTO> changeHistory) {
        this.changeHistory = changeHistory;
    }

    public Boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(final Boolean canEdit) {
        this.canEdit = canEdit;
    }
}
