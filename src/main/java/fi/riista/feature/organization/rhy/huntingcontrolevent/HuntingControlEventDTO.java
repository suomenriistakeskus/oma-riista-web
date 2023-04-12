package fi.riista.feature.organization.rhy.huntingcontrolevent;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.riista.config.jackson.LocalTimeToStringSerializer;
import fi.riista.config.jackson.StringToLocalTimeDeserializer;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.SafeHtml.WhiteListType;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.hasText;

public class HuntingControlEventDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    @Valid
    private RiistanhoitoyhdistysDTO rhy;

    @Size(min = 2, max = 255)
    @SafeHtml(whitelistType = WhiteListType.NONE)
    private String title;

    private HuntingControlEventType eventType;

    private HuntingControlEventStatus status;

    // Cannot be NotEmpty due old events' inspectors are in otherParticipants field.
    @Valid
    private List<HuntingControlInspectorDTO> inspectors;

    private int inspectorCount;

    @NotEmpty
    private Set<HuntingControlCooperationType> cooperationTypes;

    private boolean wolfTerritory;

    @SafeHtml(whitelistType = WhiteListType.NONE)
    private String otherParticipants;

    @NotNull
    @Valid
    private GeoLocation geoLocation;

    @SafeHtml(whitelistType = WhiteListType.NONE)
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

    @SafeHtml(whitelistType = WhiteListType.NONE)
    private String description;

    private List<MultipartFile> newAttachments;

    @Valid
    private List<HuntingControlAttachmentDTO> attachments;

    @Valid
    private List<ChangeHistoryDTO> changeHistory;

    /** Fill in only when updating */

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String reasonForChange;

    /** Only output for client */

    private Boolean lockedAsPastStatistics;

    // Constructors / factories

    public HuntingControlEventDTO() {}

    public static HuntingControlEventDTO create(final HuntingControlEvent event,
                                                final Riistanhoitoyhdistys rhy,
                                                final Set<Person> inspectors,
                                                final Set<HuntingControlCooperationType> cooperationTypes,
                                                final List<HuntingControlAttachmentDTO> attachments,
                                                final List<ChangeHistoryDTO> changes) {
        final HuntingControlEventDTO dto = new HuntingControlEventDTO();
        DtoUtil.copyBaseFields(event, dto);

        dto.setRhy(RiistanhoitoyhdistysDTO.create(rhy));

        dto.setEventType(event.getEventType());
        dto.setStatus(event.getStatus() != null ? event.getStatus() : HuntingControlEventStatus.ACCEPTED_SUBSIDIZED);
        dto.setInspectors(inspectors.stream().map(HuntingControlInspectorDTO::create).collect(toList()));
        dto.setTitle(event.getTitle());
        dto.setInspectorCount(event.getInspectorCount());
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
        dto.setLockedAsPastStatistics(event.isLockedAsPastStatistics());

        return dto;
    }

    // Accessors -->

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public RiistanhoitoyhdistysDTO getRhy() {
        return rhy;
    }

    public void setRhy(final RiistanhoitoyhdistysDTO rhy) {
        this.rhy = rhy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
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

    public List<HuntingControlInspectorDTO> getInspectors() {
        return inspectors;
    }

    public void setInspectors(final List<HuntingControlInspectorDTO> inspectors) {
        this.inspectors = inspectors;
    }

    public int getInspectorCount() {
        return inspectorCount;
    }

    public void setInspectorCount(final int inspectorCount) {
        this.inspectorCount = inspectorCount;
    }

    public Set<HuntingControlCooperationType> getCooperationTypes() {
        return cooperationTypes;
    }

    public void setCooperationTypes(final Set<HuntingControlCooperationType> cooperationTypes) {
        this.cooperationTypes = cooperationTypes;
    }

    public boolean getWolfTerritory() {
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

    public List<MultipartFile> getNewAttachments() {
        return newAttachments;
    }

    public void setNewAttachments(final List<MultipartFile> newAttachments) {
        this.newAttachments = newAttachments;
    }

    public List<HuntingControlAttachmentDTO> getAttachments() {
        return attachments;
    }

    public void setAttachments(final List<HuntingControlAttachmentDTO> attachments) {
        this.attachments = attachments;
    }

    public List<ChangeHistoryDTO> getChangeHistory() {
        return changeHistory;
    }

    public void setChangeHistory(final List<ChangeHistoryDTO> changeHistory) {
        this.changeHistory = changeHistory;
    }

    public String getReasonForChange() {
        return reasonForChange;
    }

    public void setReasonForChange(final String reasonForChange) {
        this.reasonForChange = reasonForChange;
    }

    public Boolean getLockedAsPastStatistics() {
        return lockedAsPastStatistics;
    }

    public void setLockedAsPastStatistics(final Boolean lockedAsPastStatistics) {
        this.lockedAsPastStatistics = lockedAsPastStatistics;
    }

    @AssertTrue
    public boolean isDescriptionPresentWhenTypeOther() {
        // Description is mandatory when type is OTHER
        return eventType != HuntingControlEventType.OTHER
                || hasText(description);
    }
}
