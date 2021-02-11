package fi.riista.feature.organization.rhy.huntingcontrolevent;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.riista.config.jackson.LocalTimeToStringSerializer;
import fi.riista.config.jackson.StringToLocalTimeDeserializer;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.SafeHtml.WhiteListType;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class HuntingControlEventDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    @Valid
    private RiistanhoitoyhdistysDTO rhy;

    @NotNull
    @Size(min = 2, max = 255)
    @SafeHtml(whitelistType = WhiteListType.NONE)
    private String title;

    private int inspectorCount;

    @NotNull
    private HuntingControlCooperationType cooperationType;

    private boolean wolfTerritory;

    @NotNull
    @SafeHtml(whitelistType = WhiteListType.NONE)
    private String inspectors;

    @NotNull
    @Valid
    private GeoLocation geoLocation;

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

    @NotNull
    @SafeHtml(whitelistType = WhiteListType.NONE)
    private String description;

    private List<MultipartFile> newAttachments;

    @Valid
    private List<HuntingControlAttachmentDTO> attachments;

    // Only output for client
    private Boolean lockedAsPastStatistics;

    public HuntingControlEventDTO() {}

    public static HuntingControlEventDTO create(final HuntingControlEvent event,
                                                final Riistanhoitoyhdistys rhy,
                                                final List<HuntingControlAttachmentDTO> attachments) {
        final HuntingControlEventDTO dto = new HuntingControlEventDTO();
        DtoUtil.copyBaseFields(event, dto);

        dto.setRhy(RiistanhoitoyhdistysDTO.create(rhy));

        dto.setTitle(event.getTitle());
        dto.setInspectorCount(event.getInspectorCount());
        dto.setCooperationType(event.getCooperationType());
        dto.setWolfTerritory(event.getWolfTerritory());
        dto.setInspectors(event.getInspectors());
        dto.setGeoLocation(event.getGeoLocation());
        dto.setDate(event.getDate());
        dto.setBeginTime(event.getBeginTime());
        dto.setEndTime(event.getEndTime());
        dto.setCustomers(event.getCustomers());
        dto.setProofOrders(event.getProofOrders());
        dto.setDescription(event.getDescription());
        dto.setAttachments(attachments);
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

    public int getInspectorCount() {
        return inspectorCount;
    }

    public void setInspectorCount(final int inspectorCount) {
        this.inspectorCount = inspectorCount;
    }

    public HuntingControlCooperationType getCooperationType() {
        return cooperationType;
    }

    public void setCooperationType(final HuntingControlCooperationType cooperationType) {
        this.cooperationType = cooperationType;
    }

    public boolean getWolfTerritory() {
        return wolfTerritory;
    }

    public void setWolfTerritory(final boolean wolfTerritory) {
        this.wolfTerritory = wolfTerritory;
    }

    public String getInspectors() {
        return inspectors;
    }

    public void setInspectors(final String inspectors) {
        this.inspectors = inspectors;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
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

    public Boolean getLockedAsPastStatistics() {
        return lockedAsPastStatistics;
    }

    public void setLockedAsPastStatistics(final Boolean lockedAsPastStatistics) {
        this.lockedAsPastStatistics = lockedAsPastStatistics;
    }
}
